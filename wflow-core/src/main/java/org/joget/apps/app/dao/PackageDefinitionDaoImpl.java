package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.hibernate.Cache;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * DAO to load/store PackageDefinition and mapping objects
 */
public class PackageDefinitionDaoImpl extends AbstractVersionedObjectDao<PackageDefinition> implements PackageDefinitionDao {

    public static final String ENTITY_NAME = "PackageDefinition";
    private static final String APP_DEFINITION_ENTITY_NAME = "AppDefinition";
    private static final String PACKAGE_ACTIVITY_FORM_ENTITY_NAME = "PackageActivityForm";
    private static final String PACKAGE_ACTIVITY_PLUGIN_ENTITY_NAME = "PackageActivityPlugin";
    private static final String PACKAGE_PARTICIPANT_ENTITY_NAME = "PackageParticipant";
    // Run near the end of transaction completion, but strictly before the migration-start
    // synchronization (Ordered.LOWEST_PRECEDENCE) even when this callback was registered later.
    static final int PACKAGE_CACHE_EVICTION_SYNCHRONIZATION_ORDER = Ordered.LOWEST_PRECEDENCE - 100;
    private AppDefinitionDao appDefinitionDao;
    
    @Autowired
    AppService appService;
    
    private AppDefCache cache;
    
    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    public static String getCacheKey(AppDefinition appDef){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appDef.getAppId()+"_"+Long.toString(appDef.getVersion())+"_PACKAGE";
    }

    public AppDefinitionDao getAppDefinitionDao() {
        return appDefinitionDao;
    }

    public void setAppDefinitionDao(AppDefinitionDao appDefinitionDao) {
        this.appDefinitionDao = appDefinitionDao;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public void saveOrUpdate(PackageDefinition packageDef) {   
        // save date in db
        Date date = new Date();
        if (packageDef.getDateCreated() == null) {
            packageDef.setDateCreated(date);
        }
        packageDef.setDateModified(date);
        
        super.saveOrUpdate(packageDef);
        AppDefinition appDef = packageDef.getAppDefinition();
        // refresh appDef to prevent detached Hibernate entity
        appDef = appDefinitionDao.loadVersion(appDef.getId(), appDef.getVersion());
        appDefinitionDao.updateDateModified(appDef, date);
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update package " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            try {
                filename = "appDefinition.yaml";
                XmlMapper xmlMapper = new XmlMapper();
                Map<String, Object> map = xmlMapper.readValue(xml, Map.class);
                AppDevUtil.processPluginProperties(map);
                String yaml = AppDevUtil.mapToYamlString(map);
                AppDevUtil.fileSave(appDef, filename, yaml, "");
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        // remove from cache
        clearPackageDefinitionCaches(appDef);
        
        WorkflowHelper appWorkflowHelper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        appWorkflowHelper.cleanDeadlineAppDefinitionCache(packageDef.getId(), packageDef.getVersion().toString());
    }    
    
    @Override
    public void delete(PackageDefinition obj) {
        AppDefinition appDef = obj.getAppDefinition();
        String packageId = obj.getId();
        String packageVersion = obj.getVersion().toString();
        if (appDef != null) {
            // disassociate from app
            Collection<PackageDefinition> list = appDef.getPackageDefinitionList();
            for (Iterator<PackageDefinition> i = list.iterator(); i.hasNext();) {
                PackageDefinition def = i.next();
                if (def.getId() != null && def.getId().equals(obj.getId())) {
                    i.remove();
                }
            }
            appDefinitionDao.saveOrUpdate(appDef);
        }
        // delete package definition
        super.delete(getEntityName(), obj);
        
        // remove from cache
        clearPackageDefinitionCaches(appDef);

        if (!AppDevUtil.isGitDisabled()) {
            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        WorkflowHelper appWorkflowHelper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        appWorkflowHelper.cleanDeadlineAppDefinitionCache(packageId, packageVersion);
    }

    /**
     * Loads the package definition for a specific app version
     * @param appId
     * @param appVersion
     * @return
     */
    @Override
    public PackageDefinition loadAppPackageDefinition(String appId, Long appVersion) {
        // load the package definition, getting from appService so that current app def is set correctly
        AppDefinition appDef = appService.getAppDefinition(appId, Long.toString(appVersion));
        return appDef.getCachedPackageDefinition();
    }

    @Override
    public Object[] getPackageMetadata(String appId, Long appVersion) {
        if (appId == null || appVersion == null) {
            return null;
        }
        // Scalar projection, deliberately NOT marked cacheable (no setCacheable call): the values
        // must come straight from the current committed row. A scalar query is not served from the
        // persistence context the way a managed entity would be, so this reflects a concurrent
        // package save that has committed rather than the stale in-session snapshot.
        Query q = findSession().createQuery("SELECT e.version, e.dateModified FROM " + getEntityName()
                + " e WHERE e.appDefinition.appId = ?1 AND e.appDefinition.version = ?2");
        q.setParameter(1, appId);
        q.setParameter(2, appVersion);
        q.setMaxResults(1);
        List<?> results = q.list();
        if (results != null && !results.isEmpty()) {
            return (Object[]) results.get(0);
        }
        return null;
    }

    /**
     * Loads the package definition
     * @param packageId
     * @param packageVersion
     * @return
     */
    @Override
    public PackageDefinition loadPackageDefinition(String packageId, Long packageVersion) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            // load the package definition
            String condition = " WHERE e.id=? AND e.version=?";
            Object[] params = {packageId, packageVersion};
            Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }

    /**
     * Loads the package definition based on a process definition ID
     * @param packageVersion
     * @param processDefId
     * @return
     */
    @Override
    public PackageDefinition loadPackageDefinitionByProcess(String packageId, Long packageVersion, String processDefId) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

            // load the package definition
            String condition = " INNER JOIN e.packageActivityFormMap paf WHERE e.id=? AND e.version=? AND paf.processDefId=?";
            Object[] params = {packageId, packageVersion, processDefId};
            Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }
    
    @Override
    public PackageDefinition createPackageDefinition(AppDefinition appDef, Long packageVersion) {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId(appDef.getId());
        packageDef.setVersion(packageVersion);
        packageDef.setName(appDef.getName());
        packageDef.setAppDefinition(appDef);
        packageDef.setPackageActivityFormMap(new HashMap<>());
        packageDef.setPackageActivityPluginMap(new HashMap<>());
        packageDef.setPackageParticipantMap(new HashMap<>());
        
        Collection<PackageDefinition> list = appDef.getPackageDefinitionList();
        if (list == null) {
            list = new ArrayList<>();
            appDef.setPackageDefinitionList(list);
        }
        list.add(packageDef);
        getAppDefinitionDao().saveOrUpdate(appDef);
        clearPackageDefinitionCaches(appDef);
        
        return packageDef;
    }

    @Override
    public PackageDefinition updatePackageDefinitionVersion(PackageDefinition packageDef, Long packageVersion) {
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        AppDefinition oldAppDef = packageDef.getAppDefinition();
        String appId = oldAppDef.getAppId();
        Long appVersion = oldAppDef.getVersion();
        Long oldPackageVersion = packageDef.getVersion();
        String packageName = packageDef.getName();
        Date dateCreated = packageDef.getDateCreated();
        Date dateModified = packageDef.getDateModified();
        Collection<WorkflowProcess> previousProcessList = workflowManager.getProcessList(appId, oldPackageVersion.toString());
        HashSet<String> previousProcessIds = new HashSet();  
        String packageId = packageDef.getId();
        
        for (WorkflowProcess wp : previousProcessList) {
            previousProcessIds.add(WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId()));
        }
        
        // Retain only mappings that still exist in the newly deployed workflow package.
        Collection<String> activityIds = new ArrayList<String>();
        Collection<String> toolIds = new ArrayList<String>();
        Collection<String> participantIds = new ArrayList<String>();
        Collection<String> newProcessIds = new ArrayList<String>();
        Map<String, PackageActivityForm> packageActivityFormMap = new HashMap<String, PackageActivityForm>();
        Map<String, PackageActivityPlugin> packageActivityPluginMap = new HashMap<String, PackageActivityPlugin>();
        Map<String, PackageParticipant> packageParticipantMap = new HashMap<String, PackageParticipant>();
        try {
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());
            for (WorkflowProcess wp : processList) {
                String processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId());
                Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(wp.getId());
                activityIds.add(processDefId+"::"+WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
                participantIds.add(processDefId+"::"+"processStartWhiteList");
                toolIds.add(processDefId+"::"+WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
                for (WorkflowActivity a : activityList) {
                    if (a.getType().equalsIgnoreCase("normal")) {
                        activityIds.add(processDefId+"::"+a.getId());
                        toolIds.add(processDefId+"::"+a.getId());
                    } else if (a.getType().equalsIgnoreCase("tool") || a.getType().equalsIgnoreCase("route")) {
                        toolIds.add(processDefId+"::"+a.getId());
                    }
                }

                Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(wp.getId());
                for (WorkflowParticipant p : participantList) {
                    participantIds.add(processDefId+"::"+p.getId());
                }
                
                if (!previousProcessIds.contains(WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId()))) {
                    newProcessIds.add(wp.getId());
                }
            }

            Map<String, PackageActivityForm> activityForms = packageDef.getPackageActivityFormMap();
            if (activityForms != null) {
                for (String key : activityForms.keySet()) {
                    if (activityIds.contains(key)) {
                        packageActivityFormMap.put(key, activityForms.get(key));
                    }
                }
            }
            Map<String, PackageActivityPlugin> activityPluginMap = packageDef.getPackageActivityPluginMap();
            if (activityPluginMap != null) {
                for (String key : activityPluginMap.keySet()) {
                    if (toolIds.contains(key)) {
                        packageActivityPluginMap.put(key, activityPluginMap.get(key));
                    }
                }
            }
            Map<String, PackageParticipant> participantMap = packageDef.getPackageParticipantMap();
            if (participantMap != null) {
                for (String key : participantMap.keySet()) {
                    if (participantIds.contains(key)) {
                        packageParticipantMap.put(key, participantMap.get(key));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(PackageDefinitionDaoImpl.class.getName(), e, "");
        }

        deletePackageDefinitionRows(packageId, oldPackageVersion);
        clearPackageDefinitionCaches(oldAppDef, true);

        // The rows behind these instances were just bulk-deleted outside the session; evict them
        // individually so retained child instances can be safely re-keyed to the new package version.
        evictFromSession(packageDef);
        evictMapValuesFromSession(packageActivityFormMap);
        evictMapValuesFromSession(packageActivityPluginMap);
        evictMapValuesFromSession(packageParticipantMap);

        packageDef = new PackageDefinition();
        packageDef.setId(packageId);
        packageDef.setVersion(packageVersion);
        packageDef.setName(packageName);
        packageDef.setDateCreated(dateCreated);
        packageDef.setDateModified(dateModified);
                
        packageDef.setPackageActivityFormMap(packageActivityFormMap);
        packageDef.setPackageActivityPluginMap(packageActivityPluginMap);
        packageDef.setPackageParticipantMap(packageParticipantMap);

        // save app and package definition
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, appVersion);
        packageDef.setAppDefinition(appDef);
        reattachPackageDefinitionMappings(packageDef);
        replaceAppPackageDefinition(appDef, packageDef);
        appDefinitionDao.merge(appDef);

        // remove from cache
        clearPackageDefinitionCaches(appDef);
        replaceAppPackageDefinition(oldAppDef, packageDef);

        // only run these once the version switch has fully succeeded
        if (!AppDevUtil.isGitDisabled()) {
            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        WorkflowHelper appWorkflowHelper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        appWorkflowHelper.cleanDeadlineAppDefinitionCache(packageId, oldPackageVersion.toString());

        return packageDef;
    }

    /**
     * Deletes the package definition and child mapping rows for a specific package version.
     * <p>
     * The delete is performed with bulk HQL so Hibernate does not leave old rows behind when a
     * workflow package version is replaced. Callers must evict affected session instances before
     * reusing retained mapping objects.
     * </p>
     * @param packageId Package id to delete
     * @param packageVersion Package version to delete
     */
    protected void deletePackageDefinitionRows(String packageId, Long packageVersion) {
        Object[] params = new Object[]{packageId, packageVersion};
        delete(PACKAGE_ACTIVITY_FORM_ENTITY_NAME, "WHERE e.packageId = ?1 AND e.packageVersion = ?2", params);
        delete(PACKAGE_ACTIVITY_PLUGIN_ENTITY_NAME, "WHERE e.packageId = ?1 AND e.packageVersion = ?2", params);
        delete(PACKAGE_PARTICIPANT_ENTITY_NAME, "WHERE e.packageId = ?1 AND e.packageVersion = ?2", params);
        delete(ENTITY_NAME, "WHERE e.id = ?1 AND e.version = ?2", params);
        findSession().flush();
    }

    /**
     * Reattaches retained package mapping objects to the newly created package definition.
     * <p>
     * Mapping objects store the package id and version as part of their identifier fields, so
     * calling {@code setPackageDefinition} also refreshes those values for the new package version.
     * </p>
     * @param packageDef Package definition that owns the retained mappings
     */
    protected void reattachPackageDefinitionMappings(PackageDefinition packageDef) {
        if (packageDef.getPackageActivityFormMap() != null) {
            for (PackageActivityForm form : packageDef.getPackageActivityFormMap().values()) {
                form.setPackageDefinition(packageDef);
            }
        }
        if (packageDef.getPackageActivityPluginMap() != null) {
            for (PackageActivityPlugin plugin : packageDef.getPackageActivityPluginMap().values()) {
                plugin.setPackageDefinition(packageDef);
            }
        }
        if (packageDef.getPackageParticipantMap() != null) {
            for (PackageParticipant participant : packageDef.getPackageParticipantMap().values()) {
                participant.setPackageDefinition(packageDef);
            }
        }
    }

    /**
     * Replaces the app's in-memory package reference with the current package definition.
     * <p>
     * Cache invalidation alone is not sufficient when the same request still holds an
     * {@link AppDefinition}; a later {@code getCachedPackageDefinition()} call can repopulate
     * AppDefCache from that stale package collection.
     * </p>
     * @param appDef App definition whose package reference should be updated
     * @param packageDef Current package definition
     */
    protected void replaceAppPackageDefinition(AppDefinition appDef, PackageDefinition packageDef) {
        if (appDef == null || packageDef == null) {
            return;
        }
        Collection<PackageDefinition> packageDefinitionList = appDef.getPackageDefinitionList();
        if (packageDefinitionList == null) {
            packageDefinitionList = new ArrayList<>();
            appDef.setPackageDefinitionList(packageDefinitionList);
        } else {
            for (Iterator<PackageDefinition> i = packageDefinitionList.iterator(); i.hasNext();) {
                PackageDefinition def = i.next();
                if (def.getId() != null && def.getId().equals(packageDef.getId())) {
                    i.remove();
                }
            }
        }
        packageDefinitionList.add(packageDef);
    }

    @Override
    public void addAppActivityForm(String appId, Long appVersion, PackageActivityForm activityForm) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        addAppActivityForm(packageDef, appId, appVersion, activityForm);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppActivityForm(PackageDefinition packageDef, String appId, Long appVersion, PackageActivityForm activityForm) {
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityForm.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityForm.setProcessDefId(processDefId);
        String activityDefId = activityForm.getActivityDefId();
        boolean isUpdated = false;
        if (processDefId != null && activityDefId != null) {
            PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
            if (paf != null) {
                paf.setAutoContinue(activityForm.isAutoContinue());
                paf.setDisableSaveAsDraft(activityForm.getDisableSaveAsDraft());
                paf.setForm(activityForm.getForm());
                paf.setFormIFrameStyle(activityForm.getFormIFrameStyle());
                paf.setFormId(activityForm.getFormId());
                paf.setFormUrl(activityForm.getFormUrl());
                paf.setType(activityForm.getType());
                isUpdated = true;
            }
        }

        if (!isUpdated) {
            packageDef.addPackageActivityForm(activityForm);
        }
    }

    @Override
    public void removeAppActivityForm(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityForm(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppActivityPlugin(String appId, Long appVersion, PackageActivityPlugin activityPlugin) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        addAppActivityPlugin(packageDef, appId, appVersion, activityPlugin);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppActivityPlugin(PackageDefinition packageDef, String appId, Long appVersion, PackageActivityPlugin activityPlugin) {
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityPlugin.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityPlugin.setProcessDefId(processDefId);
        String activityDefId = activityPlugin.getActivityDefId();
        if (processDefId != null && activityDefId != null) {
            packageDef.removePackageActivityPlugin(processDefId, activityDefId);
        }
        packageDef.addPackageActivityPlugin(activityPlugin);
    }

    @Override
    public void removeAppActivityPlugin(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityPlugin(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppParticipant(String appId, Long appVersion, PackageParticipant participant) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        addAppParticipant(packageDef, appId, appVersion, participant);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppParticipant(PackageDefinition packageDef, String appId, Long appVersion, PackageParticipant participant) {
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = participant.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        participant.setProcessDefId(processDefId);
        String participantId = participant.getParticipantId();
        if (processDefId != null && participantId != null) {
            packageDef.removePackageParticipant(processDefId, participantId);
        }
        packageDef.addPackageParticipant(participant);
    }

    @Override
    public void removeAppParticipant(String appId, Long appVersion, String processDefId, String participantId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageParticipant(processDefId, participantId);
        saveOrUpdate(packageDef);
    }
    
    public Collection<Long> getPackageVersions(String packageId) {
        Session session = findSession();
        String query = "SELECT e.version FROM " + ENTITY_NAME + " e WHERE e.id = ?1";

        Query q = session.createQuery(query);
        setCacheable(q, null);
        q.setParameter(1, packageId);

        return (Collection<Long>) q.list();
    }
    
    public AppDefinition getAppDefinitionByPackage(String packageId, Long packageVersion) {
        Session session = findSession();
        String query = "SELECT e.appDefinition FROM " + getEntityName() + " e  WHERE e.id=?1 AND e.version=?2";

        Query q = session.createQuery(query);
        setCacheable(q, null);
        q.setParameter(1, packageId);
        q.setParameter(2, packageVersion);

        Collection list = q.list();
        if (list != null && !list.isEmpty()) {
            return (AppDefinition) list.iterator().next();
        }
        return null;
    }
    
    /**
     * Merge an existing package definition
     * @param packageDef 
     */
    @Override
    public void merge(PackageDefinition packageDef) {
        Session session = findSession();
        session.merge(getEntityName(), packageDef);
        session.flush();
        clearPackageDefinitionCaches(packageDef.getAppDefinition());
    }

    @Override
    public void clearPackageDefinitionCaches(AppDefinition appDef) {
        clearPackageDefinitionCaches(appDef, false);
    }

    /**
     * Clears app-level and Hibernate package metadata caches after package mappings change.
     * <p>
     * Eviction happens immediately, not only after the transaction completes: several callers
     * (e.g. {@code AppServiceImpl.deployWorkflowPackage}'s stale-package-cache recovery and
     * {@code AppUtil.reloadAppDefinitionAfterStalePackageCache}) clear the cache and reload the
     * app definition again within the very same transaction, and must see a fresh cache miss
     * rather than the same invalid reference they just tried to evict.
     * </p>
     * <p>
     * A second eviction is also scheduled for after the transaction completes (see
     * {@link #scheduleCacheEviction}), since a concurrent reader could otherwise repopulate the
     * caches with pre-change data before this transaction commits; that repopulated stale entry
     * would then survive indefinitely, as nothing would evict it again afterwards.
     * </p>
     * @param appDef App definition whose package metadata changed
     * @param clearCurrentSession true to evict the current session's app definition instance
     */
    @Override
    public void clearPackageDefinitionCaches(AppDefinition appDef, boolean clearCurrentSession) {
        if (clearCurrentSession) {
            evictFromSession(appDef);
        }
        evictPackageDefinitionCaches(appDef);
        scheduleCacheEviction(appDef);
    }

    /**
     * Schedules a second eviction of the shared {@link AppDefCache} and the Hibernate 2nd-level
     * package caches for once the enclosing transaction completes, in addition to the immediate
     * eviction {@link #clearPackageDefinitionCaches} already performed.
     * <p>
     * This closes the window where a concurrent reader repopulates those caches with pre-change
     * data before this transaction commits: without a second, later eviction that repopulated
     * stale entry would survive after commit, since the immediate eviction already ran before it
     * happened. Firing on {@link TransactionSynchronization#afterCompletion(int)} covers both
     * commit and rollback.
     * </p>
     * <p>
     * On rollback specifically, in-memory mutations already made to the AppDefinition/
     * PackageDefinition object graph during this transaction (see
     * {@link #replaceAppPackageDefinition}) are not undone by the database rollback, so
     * {@link AppUtil#resetAppDefinition()} is also called here to force the next read on this
     * request/thread to reload from the database rather than reuse them.
     * </p>
     * @param appDef App definition whose package metadata changed
     */
    protected void scheduleCacheEviction(final AppDefinition appDef) {
        if (appDef == null || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new PackageCacheEvictionSynchronization(appDef));
    }

    /**
     * Orders the final package cache eviction ahead of process migration startup. Process Builder
     * can register another cache eviction after {@code updateRunningProcesses()} (for example when
     * the same save also changes mappings), so registration order alone is not a safe fence.
     */
    private class PackageCacheEvictionSynchronization implements TransactionSynchronization, Ordered {
        private final AppDefinition appDef;

        private PackageCacheEvictionSynchronization(AppDefinition appDef) {
            this.appDef = appDef;
        }

        @Override
        public int getOrder() {
            return PACKAGE_CACHE_EVICTION_SYNCHRONIZATION_ORDER;
        }

        @Override
        public void afterCompletion(int status) {
            evictPackageDefinitionCaches(appDef);
            if (status != TransactionSynchronization.STATUS_COMMITTED) {
                AppUtil.resetAppDefinition();
            }
        }
    }

    /**
     * Evicts the shared {@link AppDefCache} and Hibernate 2nd-level package caches immediately.
     * @param appDef App definition whose package metadata changed
     */
    protected void evictPackageDefinitionCaches(AppDefinition appDef) {
        if (cache != null && appDef != null) {
            cache.removeAll(appDef);
        }
        clearHibernatePackageDefinitionCaches();
    }

    /**
     * Evicts a single entity from the current Hibernate session, without disturbing any other
     * managed entity in the same (possibly shared, transactional) persistence context.
     * @param entity
     */
    protected void evictFromSession(Object entity) {
        if (entity == null) {
            return;
        }
        try {
            findSession().evict(entity);
        } catch (Exception e) {
            LogUtil.warn(getClass().getName(), "Failed to evict entity from Hibernate session: " + e.getMessage());
        }
    }

    /**
     * Evicts all values in a retained mapping collection from the current Hibernate session.
     * @param map Mapping collection whose values should be detached
     */
    protected void evictMapValuesFromSession(Map<String, ?> map) {
        if (map == null) {
            return;
        }
        for (Object value : map.values()) {
            evictFromSession(value);
        }
    }

    /**
     * Evicts the Hibernate second-level cache regions that can hold package metadata.
     * <p>
     * This intentionally targets the package-related entity and collection regions instead of
     * evicting every Hibernate cache region.
     * </p>
     */
    protected void clearHibernatePackageDefinitionCaches() {
        try {
            Cache hibernateCache = getSessionFactory().getCache();
            if (hibernateCache == null) {
                return;
            }

            // Evict package metadata touched by process package replacement, not the whole 2nd-level cache.
            hibernateCache.evictEntityData(APP_DEFINITION_ENTITY_NAME);
            hibernateCache.evictEntityData(ENTITY_NAME);
            hibernateCache.evictEntityData(PACKAGE_ACTIVITY_FORM_ENTITY_NAME);
            hibernateCache.evictEntityData(PACKAGE_ACTIVITY_PLUGIN_ENTITY_NAME);
            hibernateCache.evictEntityData(PACKAGE_PARTICIPANT_ENTITY_NAME);
            hibernateCache.evictCollectionData(APP_DEFINITION_ENTITY_NAME + ".packageDefinitionList");
            hibernateCache.evictCollectionData(ENTITY_NAME + ".packageActivityFormMap");
            hibernateCache.evictCollectionData(ENTITY_NAME + ".packageActivityPluginMap");
            hibernateCache.evictCollectionData(ENTITY_NAME + ".packageParticipantMap");
        } catch (Exception e) {
            LogUtil.warn(getClass().getName(), "Failed to clear package definition Hibernate cache: " + e.getMessage());
        }
    }
    
}
