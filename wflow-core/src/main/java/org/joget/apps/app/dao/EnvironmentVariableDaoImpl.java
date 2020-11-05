package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
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
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
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
        }

        return result;
    }
    
    @Override
    public boolean update(EnvironmentVariable object) {
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled()) {
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
                appDefinitionDao.updateDateModified(appDef);
                result = true;

                if (!AppDevUtil.isGitDisabled()) {
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
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    @Override
    public Integer getIncreasedCounter(final String id, final String remark, final AppDefinition appDef) {
        Integer count = 0;
        SessionFactory sf = super.getSessionFactory();
        Session session = null;
        Transaction transaction = null;
        try {
            session = sf.openSession();
            EnvironmentVariable key = new EnvironmentVariable();
            key.setId(id);
            key.setAppId(appDef.getId());
            key.setAppVersion(appDef.getVersion());
            key.setId(id);
            
            transaction = session.beginTransaction();
            EnvironmentVariable env = (EnvironmentVariable) session.get(getEntityName(), key, new LockOptions(LockMode.PESSIMISTIC_WRITE));
            
            if (env != null) {
                session.refresh(env, new LockOptions(LockMode.PESSIMISTIC_WRITE));
                if (env.getValue() != null && env.getValue().trim().length() > 0) {
                    count = Integer.parseInt(env.getValue());
                }
                count += 1;
                env.setValue(Integer.toString(count));
                
                session.merge(getEntityName(), env);
            } else {
                count += 1;
                    
                env = key;        
                env.setAppDefinition(appDef);
                env.setRemarks(remark);
                env.setValue(Integer.toString(count));
                session.save(getEntityName(), env);     
            }
            session.flush(); 
            transaction.commit();
            clearDeadlineCache(env);
        } catch (Exception e) {
            LogUtil.error(EnvironmentVariableDaoImpl.class.getName(), e, id);
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            if (session != null) {
                session.clear();
                session.close();
            }
        }
        return count;
    }
}
