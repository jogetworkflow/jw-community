package org.joget.commons.spring.model;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.joget.commons.util.StringUtil;

public abstract class AbstractSpringDao {

    transient SessionFactory sessionFactory;
    
    public AbstractSpringDao() {
    }
    
    public void setSessionFactory(SessionFactory sf) {
        this.sessionFactory = sf;
    }
    
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session findSession() {
        Session session;
        SessionFactory sf = this.sessionFactory;
        session = sf.getCurrentSession();
        return session;
    }
    
    protected Serializable save(String entityName, Object obj) {
        Session session = findSession();
        Serializable save = (Serializable)session.merge(entityName, obj);
        session.flush();
        return save;
    }

    protected void saveOrUpdate(String entityName, Object obj) {
        saveOrUpdateAndReturn(entityName, obj);
    }

    protected Object saveOrUpdateAndReturn(String entityName, Object obj) {
        Session session = findSession();
        Object o = session.merge(entityName, obj);
        session.flush();
        return o;
    }

    protected void merge(String entityName, Object obj) {
        saveOrUpdateAndReturn(entityName, obj);
    }

    protected void delete(String entityName, Object obj) {
        Session session = findSession();
        session.remove(obj);
        session.flush();
    }

    protected Object find(String entityName, String id) {
        Object result = null;
        String query = "WHERE id = ?1";
        Collection list = find(entityName, query, new String[] {id}, null, null, 0, 1);
        if (!list.isEmpty()) {
            result = list.iterator().next();
        }
        return result;
    }

    protected Collection find(final String entityName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        Session session = findSession();
        String query = "SELECT e FROM " + entityName + " e " + newCondition;

        if (sort != null && !sort.equals("")) {
            String filteredSort = filterSpace(sort);
            query += " ORDER BY " + filteredSort;

            if (desc) {
                query += " DESC";
            }
        }
        Query q = session.createQuery(query);
        q.setCacheable(true);

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

    protected Long count(final String entityName, final String condition, final Object[] params) {
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        Session session = findSession();
        Query q = session.createQuery("SELECT COUNT(*) FROM " + entityName + " e " + newCondition);
        q.setCacheable(true);

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }
        
        List result = q.list();
        if (!newCondition.contains(" group by ")) {
            return (Long) result.get(0);
        } else {
            return Long.valueOf(result.size());
        }
    }
    
    /**
     * Normalizes and truncates a String if there is a space.
     * @param str
     * @return 
     */
    protected String filterSpace(String str) {
        if (str != null) {
            str = Normalizer.normalize(str, Normalizer.Form.NFKC);
            if (str.contains(" ")) {
                str = str.substring(0, str.indexOf(" "));
            }
        }
        return str;
    }    
}

