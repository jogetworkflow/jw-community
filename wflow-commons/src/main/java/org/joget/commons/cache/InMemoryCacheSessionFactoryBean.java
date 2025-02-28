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
            // cache is enabled, set hibernate cache properties
            Properties cacheManagerProperties = cacheManager.getHibernateProperties();
            hibernateProperties.putAll(cacheManagerProperties);
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
