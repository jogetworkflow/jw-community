package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joget.apps.app.model.AbstractVersionedObject;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO to load/store VersionedObjects objects
 */
public abstract class AbstractVersionedObjectDao<T extends AbstractVersionedObject> extends AbstractSpringDao implements VersionedObjectDao<T> {

    /**
     * Loads an object by unique ID (primary key)
     * @param uid
     * @return
     */
    public T loadByUid(T uid) {
        Session session = findSession();
        T result = (T) session.get(getEntityName(), uid);
        if (result != null) {
            session.refresh(result);
        }
        return result;
    }

    /**
     * Find the latest version of an object by ID
     * @param id
     * @return
     */
    public T loadById(String id) {
        T result = null;
        Collection<T> versions = findLatestVersions(id, null, null, null, null, 0, 1);
        if (versions != null && !versions.isEmpty()) {
            result = versions.iterator().next();
        }
        if (result != null) {
            findSession().refresh(result);
        }
        return result;
    }

    /**
     * Find a specific version of an object by ID
     * @param id
     * @param version if null, the latest version is returned.
     * @return
     */
    public T loadVersion(String id, Long version) {
        T result = null;

        if (id != null && !id.trim().isEmpty()) {
            // formulate query and parameters
            ArrayList<Object> paramList = new ArrayList<Object>();
            String query = " WHERE id=?";
            paramList.add(id);
            if (version != null) {
                query += " AND version=?";
                paramList.add(version);
            }
            query += " ORDER BY version DESC";
            Object[] params = (Object[]) paramList.toArray();

            // execute query and return result
            Collection<T> resultList = find(getEntityName(), query, params, null, null, 0, 1);
            if (resultList != null && !resultList.isEmpty()) {
                result = resultList.iterator().next();
            }
        }
        if (result != null) {
            findSession().refresh(result);
        }
        return result;
    }

    /**
     * Find the latest versions of matching objects
     * @param id Optional ID to filter
     * @param appId Optional package ID to filter
     * @param name Optional name to filter
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<T> findLatestVersions(String id, String appId, String name, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        final String condition = generateQueryCondition(id, appId, null, name);
        final Object[] params = generateQueryParams(id, appId, null, name);

        // execute query and return result
        String query = "SELECT e FROM " + getEntityName() + " e " + condition + " AND e.version >= ALL";
        query += "(SELECT version FROM " + getEntityName() + " e2 WHERE e.id=e2.id)";

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

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }

    /**
     * Count the latest versions of matching objects
     * @param id Optional ID to filter
     * @param appId Optional package ID to filter
     * @param name Optional name to filter
     * @return
     */
    public Long countLatestVersions(String id, String appId, String name) {
        final String condition = generateQueryCondition(id, appId, null, name);
        final Object[] params = generateQueryParams(id, appId, null, name);

        // execute query and return result
        String query = "SELECT COUNT(*) FROM " + getEntityName() + " e " + condition + " AND e.version >= ALL";
        query += "(SELECT version FROM " + getEntityName() + " e2 WHERE e.id=e2.id)";
        Query q = findSession().createQuery(query);

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return ((Long) q.iterate().next()).longValue();
    }

    /**gigi
     * Gets the latest version for an object
     * @param id
     * @return
     */
    public Long getLatestVersion(final String id) {
        String query = "SELECT MAX(version) FROM " + getEntityName() + " e WHERE id=?1";
        Query q = findSession().createQuery(query);
        q.setParameter(1, id);
        Long value = (Long) q.iterate().next();
        return (value != null) ? value.longValue() : new Long(0);
    }

    /**
     * Find unique forms by specific version
     * @param id
     * @param appId
     * @param version
     * @param name
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<T> findByVersion(String id, String appId, Long version, String name, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        String condition = generateQueryCondition(id, appId, version, name);
        Object[] params = generateQueryParams(id, appId, version, name);
        Collection<T> resultList = find(getEntityName(), condition, params, sort, desc, start, rows);
        return resultList;
    }

    /**
     * Total row count by specific version
     * @param id
     * @param appId
     * @param version
     * @param name
     * @return
     */
    public Long countByVersion(String id, String appId, Long version, String name) {
        String condition = generateQueryCondition(id, appId, version, name);
        Object[] params = generateQueryParams(id, appId, version, name);
        Long result = count(getEntityName(), condition, params);
        return result;
    }

    /**
     * Generates a HQL query string for the optional conditions
     * @param id
     * @param appId
     * @param version
     * @param name
     * @return
     */
    protected String generateQueryCondition(String id, String appId, Long version, String name) {
        // formulate query and parameters
        String query = " where 1=1";
        int ordinalParameter = 1;
        if (id != null && !id.trim().isEmpty()) {
            query += " and id=?" + ordinalParameter++;
        }
        if (appId != null && !appId.trim().isEmpty()) {
            query += " and appId=?" + ordinalParameter++;
        }
        if (version != null) {
            query += " and version=?" + ordinalParameter++;
        }
        if (name != null && !name.trim().isEmpty()) {
            query += " and name like ?" + ordinalParameter++;
        }
        return query;
    }

    /**
     * Generates an array of parameters for the optional conditions
     * @param id
     * @param appId
     * @param version
     * @param name
     * @return
     */
    protected Object[] generateQueryParams(String id, String appId, Long version, String name) {
        // formulate query and parameters
        ArrayList<Object> paramList = new ArrayList<Object>();
        if (id != null && !id.trim().isEmpty()) {
            paramList.add(id);
        }
        if (appId != null && !appId.trim().isEmpty()) {
            paramList.add(appId);
        }
        if (version != null) {
            paramList.add(version);
        }
        if (name != null && !name.trim().isEmpty()) {
            paramList.add("%" + name + "%");
        }
        Object[] params = (Object[]) paramList.toArray();
        return params;
    }

    /**
     * Find the versions for a specific ID.
     * @param id
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<T> findVersions(String id, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        if (id == null || id.trim().isEmpty()) {
            return new ArrayList<T>();
        }
        String condition = " where id=?";
        Object[] params = {id};
        Collection<T> resultList = find(getEntityName(), condition, params, sort, desc, start, rows);
        return resultList;
    }

    /**
     * Total number of versions for a specific ID.
     * @param id
     * @return
     */
    public Long countVersions(String id) {
        if (id == null || id.trim().isEmpty()) {
            return new Long(0);
        }
        String condition = " where id=?";
        Object[] params = {id};
        Long result = count(getEntityName(), condition, params);
        return result;
    }

    /**
     * Save/update an object
     * @param object
     */
    public void saveOrUpdate(T object) {
        if (object.getDateCreated() == null) {
            object.setDateCreated(new Date());
        }
        object.setDateModified(new Date());
        saveOrUpdate(getEntityName(), object);
    }

    /**
     * Delete an object by primary key
     * @param uid
     */
    public void delete(T obj) {
        Session session = findSession();
        session.refresh(obj);
        delete(getEntityName(), obj);
    }

    /**
     * Delete a specific object version
     * @param id
     * @param version
     */
    public void deleteVersion(String id, Long version) {
        T object = loadVersion(id, version);
        delete(object);
    }

    /**
     * Delete all versions of an object
     * @param appId
     */
    public void deleteAllVersions(String id) {
        Collection<T> objects = findVersions(id, null, null, null, null);
        for (T object : objects) {
            delete(object);
        }
    }
}
