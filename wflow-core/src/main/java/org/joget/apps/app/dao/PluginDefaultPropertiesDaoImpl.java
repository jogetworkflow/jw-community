package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class PluginDefaultPropertiesDaoImpl extends AbstractAppVersionedObjectDao<PluginDefaultProperties> implements PluginDefaultPropertiesDao {

    public static final String ENTITY_NAME = "PluginDefaultProperties";

    @Autowired
    AppService appService;     
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
        
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<PluginDefaultProperties> getPluginDefaultPropertiesList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or pluginName like ? or pluginDescription like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getPluginDefaultPropertiesListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or pluginName like ? or pluginDescription like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean add(PluginDefaultProperties object) {
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
            String filename = "appConfig.xml";
            String xml = AppDevUtil.getAppConfigXml(appDef);
            boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            if (commitConfig) {
                String commitMessage =  "Update app config " + appDef.getId();
                AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
            } else {
                AppDevUtil.fileDelete(appDef, filename, null);
            }

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        return result;
    }
    
    @Override
    public boolean update(PluginDefaultProperties object) {
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled()) {
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
            String filename = "appConfig.xml";
            String xml = AppDevUtil.getAppConfigXml(appDef);
            boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            if (commitConfig) {
                String commitMessage =  "Update app config " + appDef.getId();
                AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
            } else {
                AppDevUtil.fileDelete(appDef, filename, null);
            }

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(appDef);
        }
        
        return result;
    }
    
    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            PluginDefaultProperties obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<PluginDefaultProperties> list = appDef.getPluginDefaultPropertiesList();
                for (PluginDefaultProperties object : list) {
                    if (obj.getId().equals(object.getId())) {
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                appDefinitionDao.updateDateModified(appDef);
                result = true;
                
                if (!AppDevUtil.isGitDisabled()) {
                    appDef = appService.loadAppDefinition(appDef.getAppId(), appDef.getVersion().toString());
                    Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
                    String filename = "appConfig.xml";
                    String xml = AppDevUtil.getAppConfigXml(appDef);
                    boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
                    if (commitConfig) {
                        String commitMessage =  "Update app config " + appDef.getId();
                        AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
                    } else {
                        AppDevUtil.fileDelete(appDef, filename, null);
                    }        

                    // sync app plugins
                    AppDevUtil.dirSyncAppPlugins(appDef);
                }                
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
}
