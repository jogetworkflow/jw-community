package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.model.CustomBuilderCallback;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class BuilderDefinitionDaoImpl extends AbstractAppVersionedObjectDao<BuilderDefinition> implements BuilderDefinitionDao  {
    public static final String ENTITY_NAME = "BuilderDefinition";

    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    private AppDefCache cache;

    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String id, String appId, Long version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_BUILDER_"+id;
    }
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }
    
    @Override
    public Collection<BuilderDefinition> getBuilderDefinitionList(String type, String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();
        
        if (type != null) {
            conditions += "and type = ? ";
            params.add(type);
        }

        if (filterString == null) {
            filterString = "";
        }
        conditions += "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    @Override
    public Long getBuilderDefinitionListCount(String type, String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (type != null) {
            conditions += "and type = ? ";
            params.add(type);
        }
        
        if (filterString == null) {
            filterString = "";
        }
        conditions += "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }
    
    @Override
    public BuilderDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        Element element = cache.get(cacheKey, appDefinition);

        if (element == null) {
            BuilderDefinition def = super.loadById(id, appDefinition);
            
            if (def != null) {
                findSession().evict(def);
                element = new Element(cacheKey, (Serializable) def);
                cache.put(element, appDefinition);
            }
            return def;
        }else{
            return (BuilderDefinition) element.getValue();
        }
    }

    @Override
    public boolean add(BuilderDefinition object) {
        // save in db
        Date date = new Date();
        object.setDateCreated(date);
        object.setDateModified(date);
        
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), date);
        
        CustomBuilder builder = CustomBuilderUtil.getBuilder(object.getType());
        if (builder instanceof CustomBuilderCallback) {
            ((CustomBuilderCallback) builder).addDefinition(object);
        }
        
        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "builder/" + object.getType() + "/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Add " + object.getType() + " " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        return result;
    }

    @Override
    public boolean update(BuilderDefinition object) {
        // save in db
        object.setDateModified(new Date());
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), object.getDateModified());

        CustomBuilder builder = CustomBuilderUtil.getBuilder(object.getType());
        if (builder instanceof CustomBuilderCallback) {
            ((CustomBuilderCallback) builder).updateDefinition(object);
        }
        
        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String type = SecurityUtil.validateStringInput(object.getType());
            String id = SecurityUtil.validateStringInput(object.getId());
            String filename = "builder/" + type + "/" + id + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Update " + type + " " + id;
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
            BuilderDefinition obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<BuilderDefinition> list = appDef.getBuilderDefinitionList();
                for (BuilderDefinition object : list) {
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
                
                CustomBuilder builder = CustomBuilderUtil.getBuilder(obj.getType());
                if (builder instanceof CustomBuilderCallback) {
                    ((CustomBuilderCallback) builder).deleteDefinition(obj);
                }
                
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()), appDef);
                
                if (!AppDevUtil.isGitDisabled()) {
                    // remove json
                    String filename = "builder/" + obj.getType() + "/" + id + ".json";
                    String commitMessage = "Delete " + obj.getType() + " " + id;
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
