package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Element;
import org.hibernate.query.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DAO to load/store FormDefinition objects
 */
public class FormDefinitionDaoImpl extends AbstractAppVersionedObjectDao<FormDefinition> implements FormDefinitionDao {

    public static final String ENTITY_NAME = "FormDefinition";
    
    private FormColumnCache formColumnCache;
    private AppDefCache cache;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String id, String appId, Long version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_FORM_"+id;
    }
    
    public FormColumnCache getFormColumnCache() {
        return formColumnCache;
    }

    public void setFormColumnCache(FormColumnCache formColumnCache) {
        this.formColumnCache = formColumnCache;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * Retrieves FormDefinitions mapped to a table name.
     * @param tableName
     * @return
     */
    @Override
    public Collection<FormDefinition> loadFormDefinitionByTableName(String tableName) {
        // load the form definitions
        String condition = " WHERE e.tableName=?";
        Object[] params = {tableName};
        Collection<FormDefinition> results = find(getEntityName(), condition, params, null, null, 0, -1);
        return results;
    }

    public Collection<FormDefinition> getFormDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getFormDefinitionListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }
    
    protected FormDefinition load(String id, AppDefinition appDefinition) {
        FormDefinition formDef = super.loadById(id, appDefinition);
        return formDef;
    }
    
    protected boolean shouldEvict(AppDefinition appDefinition) {
        return true;
    }

    @Override
    public FormDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        Element element = cache.get(cacheKey, appDefinition);

        if (element == null) {
            FormDefinition formDef = load(id, appDefinition);

            if (formDef != null) {
                if (shouldEvict(appDefinition)) {
                    findSession().evict(formDef);
                }
                element = new Element(cacheKey, (Serializable) formDef);
                cache.put(element, appDefinition);
            }
            return formDef;
        } else {
            return (FormDefinition) element.getValue();
        }
    }
    
    @Override
    public boolean add(FormDefinition object) {
        // save in db
        Date date = new Date();
        object.setDateCreated(date);
        object.setDateModified(date);
        
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), date);

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "forms/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Add form " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // clear cache
        formColumnCache.remove(object.getTableName());
        return result;
    }

    @Override
    public boolean update(FormDefinition object) {
        // update object
        object.setDateModified(new Date());
        
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), object.getDateModified());

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "forms/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Update form " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // clear from cache
        formColumnCache.remove(object.getTableName());
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()), object.getAppDefinition());
        
        return result;
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            FormDefinition obj = super.loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<FormDefinition> list = appDef.getFormDefinitionList();
                if (list != null) {
                    for (FormDefinition object : list) {
                        if (obj.getId().equals(object.getId())) {
                            list.remove(obj);
                            break;
                        }
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                appDefinitionDao.updateDateModified(appDef);
                result = true;
                
                // clear from cache
                formColumnCache.remove(obj.getTableName());
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()), appDef);
                
                if (!AppDevUtil.isGitDisabled()) {
                    // delete json
                    String filename = "forms/" + id + ".json";
                    String commitMessage = "Delete form " + id;
                    AppDevUtil.fileDelete(appDef, filename, commitMessage);

                    // sync app plugins
                    AppDevUtil.dirSyncAppPlugins(appDef);  
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }

    public Collection<String> getTableNameList(AppDefinition appDefinition) {
        final AppDefinition appDef = appDefinition;
        
        String query = "SELECT DISTINCT e.tableName FROM " + getEntityName() + " e where e.appId = ?1 and e.appVersion = ?2";

        Query q = findSession().createQuery(query);
        q.setParameter(1, appDef.getAppId());
        q.setParameter(2, appDef.getVersion());

        return q.list();
    }
}