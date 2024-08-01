package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DatalistDefinitionDaoImpl extends AbstractAppVersionedObjectDao<DatalistDefinition> implements DatalistDefinitionDao {

    public static final String ENTITY_NAME = "DatalistDefinition";

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
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_LIST_"+id;
    }
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<DatalistDefinition> getDatalistDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getDatalistDefinitionListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public DatalistDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        Element element = cache.get(cacheKey, appDefinition);

        if (element == null) {
            DatalistDefinition listDef = super.loadById(id, appDefinition);
            
            if (listDef != null) {
                findSession().evict(listDef);
                element = new Element(cacheKey, (Serializable) listDef);
                cache.put(element, appDefinition);
            }
            return listDef;
        }else{
            return (DatalistDefinition) element.getValue();
        }
    }

    @Override
    public boolean add(DatalistDefinition object) {// save in db
        Date date = new Date();
        object.setDateCreated(date);
        object.setDateModified(date);
        
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), date);
        
        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "lists/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Add list " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        return result;
    }

    @Override
    public boolean update(DatalistDefinition object) {
        // save in db
        object.setDateModified(new Date());
        
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), object.getDateModified());

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "lists/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Update list " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // remove from cache
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()), object.getAppDefinition());
        
        return result;
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            DatalistDefinition obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<DatalistDefinition> list = appDef.getDatalistDefinitionList();
                for (DatalistDefinition object : list) {
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
                
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()), appDef);
                
                if (!AppDevUtil.isGitDisabled()) {
                    // remove json
                    String filename = "lists/" + id + ".json";
                    String commitMessage = "Delete list " + id;
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
}