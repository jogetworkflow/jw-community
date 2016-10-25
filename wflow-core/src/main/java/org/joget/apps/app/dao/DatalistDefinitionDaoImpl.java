package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

public class DatalistDefinitionDaoImpl extends AbstractAppVersionedObjectDao<DatalistDefinition> implements DatalistDefinitionDao {

    public static final String ENTITY_NAME = "DatalistDefinition";

    private Cache cache;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
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
        Element element = cache.get(cacheKey);

        if (element == null) {
            DatalistDefinition listDef = super.loadById(id, appDefinition);
            
            if (listDef != null) {
                element = new Element(cacheKey, (Serializable) listDef);
                cache.put(element);
            }
            return listDef;
        }else{
            return (DatalistDefinition) element.getValue();
        }
    }

    @Override
    public boolean add(DatalistDefinition object) {
        object.setDateCreated(new Date());
        object.setDateModified(new Date());
        return super.add(object);
    }

    @Override
    public boolean update(DatalistDefinition object) {
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()));
        
        object.setDateModified(new Date());
        return super.update(object);
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
                result = true;
                
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()));
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
}