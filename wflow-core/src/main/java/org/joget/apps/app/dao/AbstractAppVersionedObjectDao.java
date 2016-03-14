package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * DAO to load/store AppVersionedObjects objects
 */
public abstract class AbstractAppVersionedObjectDao<T extends AbstractAppVersionedObject> extends AbstractSpringDao implements AppVersionedObjectDao<T> {

    public T loadById(String id, AppDefinition appDefinition) {
        T result = null;
        Collection<T> results = find("and id=?", new Object[]{id}, appDefinition, null, null, 0, 1);
        if (results != null && !results.isEmpty()) {
            result = results.iterator().next();
        }
        if (result != null) {
            findHibernateTemplate().refresh(result);
        }
        return result;
    }
    
    public T loadByIdForUpdate(String id, AppDefinition appDefinition) {
        T result = null;
        
        final String conds = generateQueryCondition(appDefinition) + "and id=?";
        final List<Object> paramsList = generateQueryParams(appDefinition);
        paramsList.add(id);
        
        Collection<T> results = (List) this.findHibernateTemplate().execute(
            new HibernateCallback() {

                public Object doInHibernate(Session session) throws HibernateException {
                    String query = "SELECT " + getEntityName() + " FROM " + getEntityName() + " " + getEntityName() + " " + conds;

                    Query q = session.createQuery(query);
                    q.setLockMode(getEntityName(), LockMode.UPGRADE);

                    q.setFirstResult(0);
                    q.setMaxResults(1);

                    int i = 0;
                    for (Object param : paramsList) {
                        q.setParameter(i, param);
                        i++;
                    }

                    return q.list();
                }
            }
        );
        
        if (results != null && !results.isEmpty()) {
            result = results.iterator().next();
        }
        if (result != null) {
            findHibernateTemplate().refresh(result);
        }
        return result;
    }

    public boolean add(T object) {
        try {
            save(getEntityName(), object);
            return true;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return false;
    }

    public boolean update(T object) {
        try {
            merge(getEntityName(), object);
            return true;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return false;
    }

    public boolean delete(String id, AppDefinition appDefinition) {
        try {
            T object = loadById(id, appDefinition);
            if (object != null) {
                delete(getEntityName(), object);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return false;
    }

    public Collection<T> getList(AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        return find(null, null, appDefinition, sort, desc, start, rows);
    }

    public Long getCount(AppDefinition appDefinition) {
        return count(null, null, appDefinition);
    }

    public Collection<T> find(String condition, Object[] params, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conds = generateQueryCondition(appDefinition);
        List<Object> paramsList = generateQueryParams(appDefinition);

        if (condition != null && !condition.trim().isEmpty()) {
            conds += condition;
        }
        if (params != null && params.length > 0) {
            paramsList.addAll(Arrays.asList(params));
        }

        try {
            return find(getEntityName(), conds, paramsList.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }

        return null;
    }

    public Long count(String condition, Object[] params, AppDefinition appDefinition) {
        String conds = generateQueryCondition(appDefinition);
        List<Object> paramsList = generateQueryParams(appDefinition);

        if (condition != null && !condition.trim().isEmpty()) {
            conds += condition;
        }
        if (params != null && params.length > 0) {
            paramsList.addAll(Arrays.asList(params));
        }

        try {
            return count(getEntityName(), conds, paramsList.toArray());
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }

        return 0L;
    }

    protected String generateQueryCondition(AppDefinition appDefinition) {
        // formulate query and parameters
        String query = " where 1=1";
        if (appDefinition != null) {
            query += " and appId=? and appVersion=? ";
        }
        return query;
    }

    protected List<Object> generateQueryParams(AppDefinition appDefinition) {
        // formulate query and parameters
        List<Object> paramList = new ArrayList<Object>();
        if (appDefinition != null) {
            paramList.add(appDefinition.getId());
            paramList.add(appDefinition.getVersion());
        }
        return paramList;
    }
}
