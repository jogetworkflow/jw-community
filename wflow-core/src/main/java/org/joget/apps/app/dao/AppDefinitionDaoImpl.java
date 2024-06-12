package org.joget.apps.app.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO to load/store AppDefinition objects
 */
public class AppDefinitionDaoImpl extends AbstractVersionedObjectDao<AppDefinition> implements AppDefinitionDao {

    @Autowired
    FormDefinitionDao formDefinitionDao;
    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    BuilderDefinitionDao builderDefinitionDao;
    @Autowired
    PluginManager pluginManager;    
     
    public static final String ENTITY_NAME = "AppDefinition";
    private AppDefCache cache;

    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    public void clearCache(AppDefinition obj) {
        cache.removeAll(obj);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public void delete(AppDefinition obj) {
        // disassociate
        if (obj != null) {
            if (obj.getDatalistDefinitionList() != null) {
                obj.getDatalistDefinitionList().clear();
            }
            if (obj.getFormDefinitionList() != null) {
                obj.getFormDefinitionList().clear();
            }
            if (obj.getUserviewDefinitionList() != null) {
                obj.getUserviewDefinitionList().clear();
            }
            if (obj.getBuilderDefinitionList() != null) {
                obj.getBuilderDefinitionList().clear();
            }
            if (obj.getPackageDefinitionList() != null) {
                obj.getPackageDefinitionList().clear();
            }
            if (obj.getPluginDefaultPropertiesList() != null) {
                obj.getPluginDefaultPropertiesList().clear();
            }
            if (obj.getEnvironmentVariableList() != null) {
                obj.getEnvironmentVariableList().clear();
            }
            if (obj.getMessageList() != null) {
                obj.getMessageList().clear();
            }
            super.saveOrUpdate(obj);
            
            clearCache(obj);

            // delete
            super.delete(obj);

            // delete directory
            if (!AppDevUtil.isGitDisabled()) {
                String commitMessage = "Delete app version " + obj.getId() + " " + obj.getVersion();
                AppDevUtil.dirDelete(obj, commitMessage);
            }
        }
    }

    @Override
    public Long getPublishedVersion(final String appId) {
        // execute query and return result
        String query = "SELECT version FROM " + getEntityName() + " e  where 1=1 AND e.published = true and appId=?1";
        Query q = findSession().createQuery(query);

        q.setParameter(1, appId);

        Iterator it = q.iterate();
        return (it.hasNext()) ? ((Long)it.next()).longValue() : null;
    }
    
    @Override
    public AppDefinition getPublishedAppDefinition(final String appId) {
        Collection list = super.find(getEntityName(), " WHERE e.published = true and e.id = ?", new String[]{appId}, null, null, null, 1);
        
        if (list != null && !list.isEmpty()) {
            return (AppDefinition) list.iterator().next();
        }
        return null;
    }

    @Override
    public Collection<AppDefinition> findPublishedApps(final String sort, final Boolean desc, final Integer start, final Integer rows) {
        String query = "SELECT e FROM " + getEntityName() + " e WHERE 1=1 AND e.published = true";

        if (sort != null && !sort.equals("")) {
            query += " ORDER BY " + sort;

            if (desc) {
                query += " DESC";
            }
        }
        Query q = findSession().createQuery(query);

        int s = (start == null) ? 0 : start;
        q.setFirstResult(s);

        if (rows != null && rows > 0) {
            q.setMaxResults(rows);
        }

        return q.list();
    }

    @Override
    public void saveOrUpdate(AppDefinition appDef) {
        super.saveOrUpdate(appDef);
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            // save and commit app definition
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            // save or delete app config
            filename = "appConfig.xml";
            Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
            boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            if (commitConfig) {
                xml = AppDevUtil.getAppConfigXml(appDef);
                commitMessage =  "Update app config " + appDef.getId();
                AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
            } else {
                AppDevUtil.fileDelete(appDef, filename, null);
            }

            // sync app resources
            AppDevUtil.dirSyncAppResources(appDef);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }    
        
        // update date modified
        appDef.setDateModified(new Date());
        saveOrUpdate(getEntityName(), appDef); 
    }    

    @Override
    public void merge(AppDefinition appDef) {
        // merge object
        Session session = findSession();
        session.merge(getEntityName(), appDef);
        session.flush();
        
        if (!AppDevUtil.isGitDisabled()) {
            // save and commit app definition
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            // save or delete app config
            filename = "appConfig.xml";
            Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
            boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            if (commitConfig) {
                xml = AppDevUtil.getAppConfigXml(appDef);
                commitMessage =  "Update app config " + appDef.getId();
                AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
            } else {
                AppDevUtil.fileDelete(appDef, filename, null);
            }

            // sync app resources
            AppDevUtil.dirSyncAppResources(appDef);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);  
        }
        
        // update date modified
        appDef.setDateModified(new Date());
        session.merge(getEntityName(), appDef);
        session.flush();
    }
    
    @Override
    public void saveOrUpdate(String appId, Long version, boolean includeDependencies) {
        AppDefinition appDef = loadVersion(appId, version);
        if (appDef != null) {
            if (includeDependencies) {
                Collection<FormDefinition> formList = appDef.getFormDefinitionList();
                for (FormDefinition form: formList) {
                    formDefinitionDao.update(form);
                }
                Collection<DatalistDefinition> datalistList = appDef.getDatalistDefinitionList();
                for (DatalistDefinition list: datalistList) {
                    datalistDefinitionDao.update(list);
                }
                Collection<UserviewDefinition> userviewList = appDef.getUserviewDefinitionList();
                for (UserviewDefinition userview: userviewList) {
                    userviewDefinitionDao.update(userview);
                }
                Collection<BuilderDefinition> builderList = appDef.getBuilderDefinitionList();
                for (BuilderDefinition builder: builderList) {
                    builderDefinitionDao.update(builder);
                }
            }
            saveOrUpdate(appDef);
            
            if (!AppDevUtil.isGitDisabled()) {
                // save xpdl
                String packageXpdl = AppDevUtil.getPackageXpdl(appDef);      
                if (packageXpdl != null && !packageXpdl.isEmpty()) {
                    String filename = "package.xpdl";
                    String commitMessage = "Update xpdl " + appDef.getId();
                    AppDevUtil.fileSave(appDef, filename, packageXpdl, commitMessage);  
                }
            }
        }
    }

    @Override
    public void updateDateModified(AppDefinition appDef) {
        updateDateModified(appDef, new Date());
    }
    
    @Override
    public void updateDateModified(AppDefinition appDef, Date date) {
        Session session = findSession();
        Query query = session.createQuery("UPDATE "+ENTITY_NAME+" e SET e.dateModified = :dateModified WHERE e.id = :appID and e.version = :appVersion");
        query.setParameter("dateModified", date);
        query.setParameter("appID", appDef.getAppId());
        query.setParameter("appVersion", appDef.getVersion());
        query.executeUpdate();
    }
    
    @Override
    @Transactional
    public AppDefinition syncAppDefinition(String appId, Long version) {
        // load app def
        AppDefinition appDef = super.loadVersion(appId, version);
        
        // create app def from xml
        boolean newVersion = false;
        if (appDef == null) {
            // app version not in db, create dummy object
            appDef = AppDevUtil.createDummyAppDefinition(appId, version);
            newVersion = true;
        }
        LogUtil.debug(getClass().getName(), "--- Start sync app " + appDef + "---");

        String filename = "appDefinition.xml";
        String appDefXml = AppDevUtil.fileReadToString(appDef, filename, false);
        if (appDefXml == null) {
            return appDef;
        }
        
        byte[] appDefData;
        try {
            appDefData = appDefXml.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            appDefData = appDefXml.getBytes();
        }
        AppDefinition newAppDef = AppDevUtil.createAppDefinitionFromXml(appDefData);
        if (newVersion) {
            appDef = newAppDef;
        }
        appDef.setVersion(version);
        LogUtil.debug(getClass().getName(), "Sync app " + appDef);

        // update plugins
        syncPlugins(appDef, newAppDef);

        // update messages
        syncMessages(appDef, newAppDef);
        
        // update resources
        syncResources(appDef, newAppDef);

        // update forms
        syncForms(appDef, newAppDef);
        
        // update lists
        syncLists(appDef, newAppDef);
        
        // update userviews
        syncUserviews(appDef, newAppDef);
        
        // update builders
        syncBuilders(appDef, newAppDef);
        
        // update package def
        syncPackageDefinition(appDef, newAppDef);
        
        // update app config
        syncAppConfig(appDef);
                       
        // update app def date modified
        getHibernateTemplate().clear();
        appDef.setName(newAppDef.getName());
        appDef.setLicense(newAppDef.getLicense());
        appDef.setDescription(newAppDef.getDescription());
        saveOrUpdate(appDef);        

        // update processes
        syncPackageXpdl(appDef, newAppDef);
        
        LogUtil.debug(getClass().getName(), "--- End sync app " + appDef + "---");
        
        return appDef;
        
    }    

    protected void syncPackageDefinition(AppDefinition appDef, AppDefinition newAppDef) {        
        // get new mappings from xml
        refreshPackageDefinition(appDef);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        PackageDefinition newPackageDef = newAppDef.getPackageDefinition();        
        if (newPackageDef != null) {
            LogUtil.debug(getClass().getName(), "Sync package for " + appDef);
            if (packageDef == null) {
                packageDef = new PackageDefinition();
                packageDef.setAppId(newPackageDef.getAppId());
                packageDef.setDateCreated(newPackageDef.getDateCreated());
                packageDef.setDateModified(newPackageDef.getDateModified());
                packageDef.setName(newPackageDef.getName());
                packageDef.setId(newPackageDef.getId());
                packageDef.setVersion(newPackageDef.getVersion());
                packageDef.setPackageActivityFormMap(new HashMap<String, PackageActivityForm>());
                packageDef.setPackageActivityPluginMap(new HashMap<String, PackageActivityPlugin>());
                packageDef.setPackageParticipantMap(new HashMap<String, PackageParticipant>());
                appDef.getPackageDefinitionList().add(packageDef);
            }
            Map<String, PackageActivityForm> activityFormMap = new LinkedHashMap<>();
            if (newPackageDef.getPackageActivityFormMap() != null) {
                activityFormMap.putAll(newPackageDef.getPackageActivityFormMap());
            }
            Map<String, PackageActivityPlugin> activityPluginMap = new LinkedHashMap<>();
            if (newPackageDef.getPackageActivityPluginMap() != null) {
                activityPluginMap.putAll(newPackageDef.getPackageActivityPluginMap());
            }
            Map<String, PackageParticipant> participantMap = new LinkedHashMap<>();
            if (newPackageDef.getPackageParticipantMap() != null) {
                participantMap.putAll(newPackageDef.getPackageParticipantMap());
            }

            // clear previous mappings
            packageDef.getPackageActivityFormMap().clear();
            packageDef.getPackageActivityPluginMap().clear();
            packageDef.getPackageParticipantMap().clear();

            // add new mappings
            for (Map.Entry e: activityFormMap.entrySet()) {
                PackageActivityForm entry = (PackageActivityForm) e.getValue();
                entry.setPackageDefinition(packageDef);
            }
            for (Map.Entry e: activityPluginMap.entrySet()) {
                PackageActivityPlugin entry = (PackageActivityPlugin) e.getValue();
                entry.setPackageDefinition(packageDef);
            }
            for (Map.Entry e: participantMap.entrySet()) {
                PackageParticipant entry = (PackageParticipant) e.getValue();
                entry.setPackageDefinition(packageDef);
            }
            packageDef.getPackageActivityFormMap().putAll(activityFormMap);
            packageDef.getPackageActivityPluginMap().putAll(activityPluginMap);
            packageDef.getPackageParticipantMap().putAll(participantMap);            

            // save new mappings
            if (packageDef.getAppDefinition() == null) {
                packageDef.setAppDefinition(appDef);
            }
        }        
    }

    protected void syncMessages(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync messages for " + appDef);
        if (!appDef.equals(newAppDef)) {
            Collection<Message> messageList = appDef.getMessageList();
            messageList.clear();

            Collection<Message> newMessageList = newAppDef.getMessageList();
            if (newMessageList != null) {
                messageList.addAll(newMessageList);
            }
        }
        Collection<Message> messageList = appDef.getMessageList();
        for (Message message: messageList) {
            message.setAppDefinition(appDef);
        }
    }      
    
    protected void syncResources(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync resources for " + appDef);
        if (!appDef.equals(newAppDef)) {
            Collection<AppResource> resourceList = appDef.getResourceList();
            resourceList.clear();

            Collection<AppResource> newResourceList = newAppDef.getResourceList();
            if (newResourceList != null) {
                resourceList.addAll(newResourceList);
            }
        }
        Collection<AppResource> resourceList = appDef.getResourceList();
        for (AppResource resource: resourceList) {
            resource.setAppDefinition(appDef);
        }
    }      

    protected void syncForms(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync forms for " + appDef);
        Collection<FormDefinition> currentList = appDef.getFormDefinitionList();
        currentList.clear();

        Collection newList = AppDevUtil.fileFindAll(FormDefinition.class, appDef, true);
        currentList.addAll(newList);
    }      
    
    protected void syncLists(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync lists for " + appDef);
        Collection<DatalistDefinition> currentList = appDef.getDatalistDefinitionList();
        currentList.clear();

        Collection newList = AppDevUtil.fileFindAll(DatalistDefinition.class, appDef, true);
        currentList.addAll(newList);
    }      
    
    protected void syncUserviews(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync userviews for " + appDef);
        Collection<UserviewDefinition> currentList = appDef.getUserviewDefinitionList();
        currentList.clear();

        Collection newList = AppDevUtil.fileFindAll(UserviewDefinition.class, appDef, true);
        currentList.addAll(newList);
    }   
    
    protected void syncBuilders(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync builder for " + appDef);
        Collection<BuilderDefinition> currentList = appDef.getBuilderDefinitionList();
        currentList.clear();

        Collection newList = AppDevUtil.fileFindAll(BuilderDefinition.class, appDef, true);
        currentList.addAll(newList);
    }
    
    protected void syncPackageXpdl(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync package XPDL for " + appDef);
        String filename = "package.xpdl";
        String newXpdl = AppDevUtil.fileReadToString(appDef, filename, false);
        if (newXpdl != null) {
            String currentXpdl = AppDevUtil.getPackageXpdl(appDef);
            if (currentXpdl == null || !AppDevUtil.cleanForCompare(newXpdl).equals(AppDevUtil.cleanForCompare(currentXpdl))) {
                try {
                    LogUtil.debug(getClass().getName(), "Deploy package XPDL for " + appDef);
                    AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
                    appService.deployWorkflowPackage(appDef.getAppId(), appDef.getVersion().toString(), newXpdl.getBytes("UTF-8"), false, true);
                } catch(Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else {
                LogUtil.debug(getClass().getName(), "No change in package XPDL for " + appDef);
            }
        }
    }  

    protected void syncAppConfig(AppDefinition appDef) {
        LogUtil.debug(getClass().getName(), "Sync app config for " + appDef);
        
        String filename = "appConfig.xml";
        String appDefXml = AppDevUtil.fileReadToString(appDef, filename, false);
        if (appDefXml != null) {
            // create app def from xml
            byte[] appDefData;
            try {
                appDefData = appDefXml.getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                appDefData = appDefXml.getBytes();
            }
            AppDefinition newAppDef = AppDevUtil.createAppDefinitionFromXml(appDefData);

            // update env variables
            LogUtil.debug(getClass().getName(), "Sync env variables for " + appDef);
            Collection<EnvironmentVariable> envVarList = appDef.getEnvironmentVariableList();
            envVarList.clear();

            Collection<EnvironmentVariable> newEnvVarList = newAppDef.getEnvironmentVariableList();
            if (newEnvVarList != null) {
                for (EnvironmentVariable ev: newEnvVarList) {
                    ev.setAppDefinition(appDef);
                }
                envVarList.addAll(newEnvVarList);
            }
            
            // update plugin default properties
            LogUtil.debug(getClass().getName(), "Sync plugin default properties for " + appDef);
            Collection<PluginDefaultProperties> pluginDefList = appDef.getPluginDefaultPropertiesList();
            pluginDefList.clear();

            Collection<PluginDefaultProperties> newPluginDefList = newAppDef.getPluginDefaultPropertiesList();
            if (newPluginDefList != null) {
                for (PluginDefaultProperties pd: newPluginDefList) {
                    pd.setAppDefinition(appDef);
                }
                pluginDefList.addAll(newPluginDefList);
            }
        }
    }      

    protected void refreshPackageDefinition(AppDefinition appDef) {
        // reset package def
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null && packageDef.getAppDefinition() == null) {
            packageDef.setAppDefinition(appDef);
            Map<String, PackageActivityForm> activityFormMap = packageDef.getPackageActivityFormMap();
            Map<String, PackageActivityPlugin> activityPluginMap = packageDef.getPackageActivityPluginMap();
            Map<String, PackageParticipant> participantMap = packageDef.getPackageParticipantMap();
            if (activityFormMap != null) {
                for (Map.Entry e: activityFormMap.entrySet()) {
                    PackageActivityForm entry = (PackageActivityForm) e.getValue();
                    entry.setPackageDefinition(packageDef);
                }
            }
            if (activityPluginMap != null) {
                for (Map.Entry e: activityPluginMap.entrySet()) {
                    PackageActivityPlugin entry = (PackageActivityPlugin) e.getValue();
                    entry.setPackageDefinition(packageDef);
                }
            }
            if (participantMap != null) {
                for (Map.Entry e: participantMap.entrySet()) {
                    PackageParticipant entry = (PackageParticipant) e.getValue();
                    entry.setPackageDefinition(packageDef);
                }
            }
        }
    }

    protected void syncPlugins(AppDefinition appDef, AppDefinition newAppDef) {
        LogUtil.debug(getClass().getName(), "Sync plugins for " + appDef);
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = AppDevUtil.getAppGitDirectory(appDef);
        try {
            File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
            String pluginDirName = "plugins";
            File pluginDir = new File(projectDir, pluginDirName);
            if (pluginDir.exists()) {
                // find all plugin files
                final String[] extensions = new String[] { "jar" };
                Iterator<File> fileIterator = FileUtils.iterateFiles(pluginDir, new FileFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String path = file.getName();
                        int dotIndex = path.lastIndexOf(".");
                        String ext = (dotIndex >= 0) ? path.substring(dotIndex + 1) : path;
                        return ArrayUtils.contains(extensions, ext);
                    }
                }, new DirectoryFileFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return true;
                    }
                });
                Collection<File> pluginsToUpload = new ArrayList<>();
                while(fileIterator.hasNext()) {
                    File pluginFile = fileIterator.next();
                    String pluginFileName = pluginFile.getName();
                    File existingPluginFile = new File(SetupManager.getBaseDirectory() + File.separator + "app_plugins" +  File.separator, pluginFileName);
                    boolean modified = !existingPluginFile.exists() || !FileUtils.contentEquals(pluginFile, existingPluginFile);
                    if (modified) {
                        pluginsToUpload.add(pluginFile);
                    }
                }    
                if (!pluginsToUpload.isEmpty()) {
                    for (File pluginFile: pluginsToUpload) {
                        LogUtil.debug(getClass().getName(), "Uploading plugin " + pluginFile.getName());
                        FileInputStream in = null;
                        try {
                            in = new FileInputStream(pluginFile);
                            pluginManager.upload(pluginFile.getName(), in);
                        } catch(Exception e) {
                            LogUtil.error(getClass().getName(), e, e.getMessage());
                        } finally {
                            try {
                                if (in != null) {
                                    in.close();
                                }
                            } catch (IOException ex) {
                                LogUtil.error(AppDefinitionDaoImpl.class.getName(), ex, "");
                            }
                        }
                    }
                    pluginManager.clearCache();
                }
            }
        } catch (IOException ex) {
            LogUtil.error(getClass().getName(), ex, ex.getMessage());
        }
    }
    
}