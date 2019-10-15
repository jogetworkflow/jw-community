package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class EnvironmentVariableDaoImpl extends AbstractAppVersionedObjectDao<EnvironmentVariable> implements EnvironmentVariableDao {

    public static final String ENTITY_NAME = "EnvironmentVariable";

    @Autowired
    AppService appService;
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<EnvironmentVariable> getEnvironmentVariableList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getEnvironmentVariableListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean add(EnvironmentVariable object) {
        boolean result = super.add(object);
        
        AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
        Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
        String filename = "appConfig.xml";
        boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
        if (commitConfig) {
            String xml = AppDevUtil.getAppConfigXml(appDef);
            String commitMessage =  "Update app config " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
        } else {
            AppDevUtil.fileDelete(appDef, filename, null);
        }

        return result;
    }
    
    @Override
    public boolean update(EnvironmentVariable object) {
        boolean result = super.update(object);
        
        AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
        Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
        String filename = "appConfig.xml";
        boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
        if (commitConfig) {
            String xml = AppDevUtil.getAppConfigXml(appDef);
            String commitMessage =  "Update app config " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
        } else {
            AppDevUtil.fileDelete(appDef, filename, null);
        }

        return result;
    }
    
    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            EnvironmentVariable obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<EnvironmentVariable> list = appDef.getEnvironmentVariableList();
                for (EnvironmentVariable object : list) {
                    if (obj.getId().equals(object.getId())) {
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                result = true;

                appDef = appService.loadAppDefinition(appDef.getAppId(), appDef.getVersion().toString());
                Properties gitProperties = AppDevUtil.getAppDevProperties(appDef);
                String filename = "appConfig.xml";
                boolean commitConfig = !Boolean.parseBoolean(gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
                if (commitConfig) {
                    String xml = AppDevUtil.getAppConfigXml(appDef);
                    String commitMessage =  "Update app config " + appDef.getId();
                    AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
                } else {
                    AppDevUtil.fileDelete(appDef, filename, null);
                }        
                
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    @Override
    public Integer getIncreasedCounter(final String id, final String remark, final AppDefinition appDef) {
        Integer count = 0;
        
        try {
            EnvironmentVariable env = loadByIdForUpdate(id, appDef);
            if (env != null && env.getValue() != null && env.getValue().trim().length() > 0) {
                count = Integer.parseInt(env.getValue());
            }
            count += 1;

            if (env == null) {
                env = new EnvironmentVariable();
                env.setAppDefinition(appDef);
                env.setAppId(appDef.getId());
                env.setAppVersion(appDef.getVersion());
                env.setId(id);
                env.setRemarks(remark);
                env.setValue(Integer.toString(count));
                super.add(env);
            } else {
                env.setValue(Integer.toString(count));
                super.update(env);
            }
        } catch (Exception e) {
            LogUtil.error(EnvironmentVariableDaoImpl.class.getName(), e, id);
        }
        return count;
    }
}
