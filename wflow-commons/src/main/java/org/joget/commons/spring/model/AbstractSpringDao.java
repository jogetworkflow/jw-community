package org.joget.commons.spring.model;

import java.io.Serializable;
import java.util.Collection;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

public abstract class AbstractSpringDao extends HibernateDaoSupport {

    public AbstractSpringDao() {
    }

    public Session findSession() {
        Session session;
        SessionFactory sf = super.getSessionFactory();
        session = sf.getCurrentSession();
        return session;
    }
    
    protected Serializable save(String entityName, Object obj) {
        Session session = findSession();
        Serializable save = session.save(entityName, obj);
        session.flush();
        return save;
    }

    protected void saveOrUpdate(String entityName, Object obj) {
        Session session = findSession();
        session.saveOrUpdate(entityName, obj);
        session.flush();
    }

    protected void merge(String entityName, Object obj) {
        Session session = findSession();
        session.merge(entityName, obj);
        session.flush();
    }

    protected void delete(String entityName, Object obj) {
        Session session = findSession();
        session.delete(entityName, obj);
        session.flush();
    }

    protected Object find(String entityName, String id) {
        Session session = findSession();
        return session.get(entityName, id);
    }

    protected List findByExample(String entityName, Object object) {
        Session session = findSession();
        Criteria crit = session.createCriteria(object.getClass());
        Example example = Example.create(object);
        crit.add(example);
        return crit.list();        
    }

    protected Collection find(final String entityName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        Session session = findSession();
        String query = "SELECT e FROM " + entityName + " e " + condition;

        if (sort != null && !sort.equals("")) {
            query += " ORDER BY " + sort;

            if (desc) {
                query += " DESC";
            }
        }
        Query q = session.createQuery(query);

        int s = (start == null) ? 0 : start;
        q.setFirstResult(s);

        if (rows != null && rows > 0) {
            q.setMaxResults(rows);
        }

        if (params != null) {
            int i = 0;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }

    protected Long count(final String entityName, final String condition, final Object[] params) {
        Session session = findSession();
        Query q = session.createQuery("SELECT COUNT(*) FROM " + entityName + " e " + condition);

        if (params != null) {
            int i = 0;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return ((Long) q.iterate().next());
    }
}

