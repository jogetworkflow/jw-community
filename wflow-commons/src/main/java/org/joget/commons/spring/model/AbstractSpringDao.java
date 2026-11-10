package org.joget.commons.spring.model;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Collection;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.query.Query;
import org.joget.commons.util.StringUtil;

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
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        Session session = findSession();
        String query = "SELECT e FROM " + entityName + " e " + newCondition;

        if (sort != null && !sort.equals("")) {
            String filteredSort = safeOrderByColumn(sort);
            if (filteredSort != null) {
                query += " ORDER BY " + filteredSort;

                if (desc) {
                    query += " DESC";
                }
            }
        }
        Query q = session.createQuery(query);

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
            return new Long(result.size());
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

    // Security (R4): an ORDER BY column cannot be a bound parameter, so a caller-supplied sort must be
    // validated. filterSpace only truncates at the first space, which does not stop space-free injection.
    // This restricts the value to a bare property reference (identifier(.identifier)*); anything else
    // returns null and the caller omits the ORDER BY. Shared by all DAOs.
    private static final java.util.regex.Pattern SAFE_ORDER_BY =
            java.util.regex.Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$");

    public static String safeOrderByColumn(String sort) {
        if (sort == null) {
            return null;
        }
        String col = Normalizer.normalize(sort, Normalizer.Form.NFKC);
        int sp = col.indexOf(' ');
        if (sp >= 0) {
            col = col.substring(0, sp);
        }
        if (!col.isEmpty() && SAFE_ORDER_BY.matcher(col).matches()) {
            return col;
        }
        return null;
    }    
}

