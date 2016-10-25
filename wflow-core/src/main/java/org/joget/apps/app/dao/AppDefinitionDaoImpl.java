package org.joget.apps.app.dao;

import java.util.Collection;
import java.util.Iterator;
import net.sf.ehcache.Cache;
import org.hibernate.Query;
import org.joget.apps.app.model.AppDefinition;

/**
 * DAO to load/store AppDefinition objects
 */
public class AppDefinitionDaoImpl extends AbstractVersionedObjectDao<AppDefinition> implements AppDefinitionDao {

    public static final String ENTITY_NAME = "AppDefinition";
    private Cache cache;

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public void clearCache(AppDefinition obj) {
        cache.removeAll();
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
        }
        clearCache(obj);

        // delete
        super.delete(obj);
    }

    public Long getPublishedVersion(final String appId) {
        // execute query and return result
        String query = "SELECT version FROM " + getEntityName() + " e  where 1=1 AND e.published = true and appId=?";
        Query q = findSession().createQuery(query);

        q.setParameter(0, appId);

        Iterator it = q.iterate();
        return (it.hasNext()) ? ((Long)it.next()).longValue() : null;
    }

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
}