package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class UserviewDefinitionDaoImpl extends AbstractAppVersionedObjectDao<UserviewDefinition> implements UserviewDefinitionDao {

    public static final String ENTITY_NAME = "UserviewDefinition";

    private Cache cache;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String id, String appId, Long version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_VIEW_"+id;
    }
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<UserviewDefinition> getUserviewDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
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

    public Long getUserviewDefinitionListCount(String filterString, AppDefinition appDefinition) {
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
    public UserviewDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        Element element = cache.get(cacheKey);

        if (element == null) {
            UserviewDefinition uvDef = super.loadById(id, appDefinition);
            
            if (uvDef != null) {
                element = new Element(cacheKey, (Serializable) uvDef);
                cache.put(element);
            }
            return uvDef;
        }else{
            return (UserviewDefinition) element.getValue();
        }
    }

    @Override
    public boolean add(UserviewDefinition object) {
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "userviews/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Add userview " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // save in db
        object.setDateCreated(new Date());
        object.setDateModified(new Date());
        return result;
    }

    @Override
    public boolean update(UserviewDefinition object) {
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "userviews/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Update userview " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // remove from cache and save in db
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()));
        
        object.setDateModified(new Date());
        return result;
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            UserviewDefinition obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<UserviewDefinition> list = appDef.getUserviewDefinitionList();
                for (UserviewDefinition object : list) {
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
                
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()));
                
                if (!AppDevUtil.isGitDisabled()) {
                    // delete json
                    String filename = "userviews/" + id + ".json";
                    String commitMessage = "Delete userview " + id;
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