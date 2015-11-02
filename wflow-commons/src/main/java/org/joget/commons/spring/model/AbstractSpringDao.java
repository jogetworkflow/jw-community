package org.joget.commons.spring.model;

import org.joget.commons.util.DynamicDataSource;
import org.joget.commons.util.LogUtil;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.Collection;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import java.util.List;
import javax.sql.DataSource;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public abstract class AbstractSpringDao extends HibernateDaoSupport {

    private LocalSessionFactoryBean localSessionFactory;
    private String dataSourceUrl;

    public AbstractSpringDao() {
    }

    public LocalSessionFactoryBean getLocalSessionFactory() {
        return localSessionFactory;
    }

    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory) {
        this.localSessionFactory = localSessionFactory;
        if (localSessionFactory != null) {
            DataSource ds = localSessionFactory.getDataSource();
            if (ds instanceof DynamicDataSource) {
                String url = ((DynamicDataSource) ds).getUrl();
                this.dataSourceUrl = url;
            }
        }
    }

    protected HibernateTemplate findHibernateTemplate() {
        if (this.localSessionFactory != null) {
            SessionFactory sf = super.getSessionFactory();
            DataSource ds = SessionFactoryUtils.getDataSource(sf);
            if (ds instanceof DynamicDataSource) {
                String url = ((DynamicDataSource) ds).getConfigDataSourceUrl();
                if (url != null && !url.equals(this.dataSourceUrl)) {
                    // datasource config has changed, create new session factory
                    try {
                        sf.openSession().reconnect(ds.getConnection());
                        super.setSessionFactory(sf);
                        this.dataSourceUrl = url;
                    } catch (Exception ex) {
                        LogUtil.error(getClass().getName(), ex, ex.getMessage());
                    }finally{
                        // close old session factory
                        sf.close();
                    }
                }
            }
        }
        HibernateTemplate ht = super.getHibernateTemplate();
        return ht;
    }

    protected Serializable save(String entityName, Object obj) {
        HibernateTemplate ht = findHibernateTemplate();
        Serializable save = ht.save(entityName, obj);
        ht.flush();
        return save;
    }

    protected void saveOrUpdate(String entityName, Object obj) {
        HibernateTemplate ht = findHibernateTemplate();
        ht.saveOrUpdate(entityName, obj);
        ht.flush();
    }

    protected void merge(String entityName, Object obj) {
        HibernateTemplate ht = findHibernateTemplate();
        ht.merge(entityName, obj);
        ht.flush();
    }

    protected void delete(String entityName, Object obj) {
        HibernateTemplate ht = findHibernateTemplate();
        ht.delete(entityName, obj);
        ht.flush();
    }

    protected Object find(String entityName, String id) {
        return findHibernateTemplate().get(entityName, id);
    }

    protected List findByExample(String entityName, Object object) {
        return findHibernateTemplate().findByExample(entityName, object);
    }

    protected List findAll(String entityName) {
        return findHibernateTemplate().find("FROM " + entityName);
    }

    protected Collection find(final String entityName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {

        List result = (List) this.findHibernateTemplate().execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException {
                        String query = "SELECT e FROM " + entityName + " e " + condition;

                        if (sort != null && !sort.equals("")) {
                            String filteredSort = filterSpace(sort);
                            query += " ORDER BY " + filteredSort;

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
                });

        return result;
    }

    protected Long count(final String entityName, final String condition, final Object[] params) {

        Long count = (Long) this.findHibernateTemplate().execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException {
                        Query q = session.createQuery("SELECT COUNT(*) FROM " + entityName + " e " + condition);

                        if (params != null) {
                            int i = 0;
                            for (Object param : params) {
                                q.setParameter(i, param);
                                i++;
                            }
                        }

                        return ((Long) q.iterate().next()).longValue();
                    }
                });

        return count;
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

