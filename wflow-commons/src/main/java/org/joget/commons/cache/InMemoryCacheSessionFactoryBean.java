package org.joget.commons.cache;

import java.util.Properties;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Custom Hibernate LocalSessionFactoryBean for in-memory cache integration.
 */
public class InMemoryCacheSessionFactoryBean extends org.springframework.orm.hibernate5.LocalSessionFactoryBean {
    
    @Override
    public void setHibernateProperties(Properties hibernateProperties) {
        InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
        if (cacheManager.isEnabled()) {
            // Cache is enabled: fill in the hibernate cache properties the context did NOT set.
            //
            // Only fill gaps. An explicit <prop> in a Spring context is a deliberate choice and has
            // to win: this used to be a blanket putAll, which meant any key the cache manager
            // happened to return silently replaced the configured value. Nothing currently returns
            // a conflicting key, so nothing was actually broken -- but it put every DDL
            // SessionFactory one line of cache-manager code away from quietly reverting
            // hibernate.hbm2ddl.jdbc_metadata_extraction_strategy to Hibernate's default and
            // reading the whole database catalog again (#2253), with no test able to notice.
            //
            // Same gap-filling idiom as org.springframework.orm.hibernate4.LocalSessionFactoryBean.
            Properties cacheManagerProperties = cacheManager.getHibernateProperties();
            for (String key : cacheManagerProperties.stringPropertyNames()) {
                if (!hibernateProperties.containsKey(key)) {
                    hibernateProperties.setProperty(key, cacheManagerProperties.getProperty(key));
                }
            }
        }
        super.setHibernateProperties(hibernateProperties);
        
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        if (Runtime.version().feature() >= 21) {
            // use virtual threads for Java 21 and above
            executor.setVirtualThreads(true);
        }
        setBootstrapExecutor(executor);
    }
        
}
