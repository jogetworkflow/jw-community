package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

/**
 * DAO to load/store PackageDefinition and mapping objects
 */
public class PackageDefinitionDaoImpl extends AbstractVersionedObjectDao<PackageDefinition> implements PackageDefinitionDao {

    public static final String ENTITY_NAME = "PackageDefinition";
    private AppDefinitionDao appDefinitionDao;

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
        appDefinitionDao.updateDateModified(packageDef.getAppDefinition(), date);
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            AppDefinition appDef = packageDef.getAppDefinition();
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update package " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        WorkflowHelper appWorkflowHelper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        appWorkflowHelper.cleanDeadlineAppDefinitionCache(packageDef.getId(), packageDef.getVersion().toString());
    }    
    
    @Override
    public void delete(PackageDefinition obj) {
        AppDefinition appDef = obj.getAppDefinition();
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

        if (!AppDevUtil.isGitDisabled()) {
            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        String packageId = obj.getId();
        String packageVersion = obj.getVersion().toString();
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
        PackageDefinition packageDef = null;

        // load the package definition
        String condition = " INNER JOIN e.appDefinition app WHERE app.id=? AND app.version=?";
        Object[] params = {appId, appVersion};
        Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
        if (results != null && !results.isEmpty()) {
            packageDef = results.iterator().next();
        }

        return packageDef;
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
        
        Collection<PackageDefinition> list = appDef.getPackageDefinitionList();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(packageDef);
        getAppDefinitionDao().saveOrUpdate(appDef);
        
        return packageDef;
    }

    @Override
    public PackageDefinition updatePackageDefinitionVersion(PackageDefinition packageDef, Long packageVersion) {
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        Collection<WorkflowProcess> previousProcessList = workflowManager.getProcessList(packageDef.getAppDefinition().getAppId(), packageDef.getVersion().toString());
        HashSet<String> previousProcessIds = new HashSet();  
        String packageId = packageDef.getId();
        
        for (WorkflowProcess wp : previousProcessList) {
            previousProcessIds.add(WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId()));
        }

        // detach previous package version
        delete(packageDef);

        // update package definition
        packageDef.setId(packageId);
        packageDef.setVersion(packageVersion);
        
        //remove not exist participants, activities and tools in mapping
        Collection<String> activityIds = new ArrayList<String>();
        Collection<String> toolIds = new ArrayList<String>();
        Collection<String> participantIds = new ArrayList<String>();
        Collection<String> newProcessIds = new ArrayList<String>();
        Map<String, PackageActivityForm> packageActivityFormMap = new HashMap<String, PackageActivityForm>();
        Map<String, PackageActivityPlugin> packageActivityPluginMap = new HashMap<String, PackageActivityPlugin>();
        Map<String, PackageParticipant> packageParticipantMap = new HashMap<String, PackageParticipant>();
        try {
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(packageDef.getAppDefinition().getAppId(), packageVersion.toString());
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
                
        packageDef.setPackageActivityFormMap(packageActivityFormMap);
        packageDef.setPackageActivityPluginMap(packageActivityPluginMap);
        packageDef.setPackageParticipantMap(packageParticipantMap);

        // save app and package definition
        AppDefinition appDef = packageDef.getAppDefinition();
        if (appDef.getPackageDefinition() == null) {
            appDef.getPackageDefinitionList().add(packageDef);
        }
//        appDefinitionDao.merge(appDef);
        saveOrUpdate(packageDef);
        
        return packageDef;
    }

    @Override
    public void addAppActivityForm(String appId, Long appVersion, PackageActivityForm activityForm) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
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
        saveOrUpdate(packageDef);
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
            saveOrUpdate(packageDef);
        }
        packageDef.addPackageActivityPlugin(activityPlugin);
        saveOrUpdate(packageDef);
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
            saveOrUpdate(packageDef);
        }
        packageDef.addPackageParticipant(participant);
        saveOrUpdate(packageDef);
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
        q.setParameter(1, packageId);

        return (Collection<Long>) q.list();
    }
    
    public AppDefinition getAppDefinitionByPackage(String packageId, Long packageVersion) {
        Session session = findSession();
        String query = "SELECT e.appDefinition FROM " + getEntityName() + " e  WHERE e.id=?1 AND e.version=?2";

        Query q = session.createQuery(query);
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
    }
    
}
