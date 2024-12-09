package org.joget.commons.ignite;

import java.util.Properties;

/**
 * Custom Hibernate LocalSessionFactoryBean for Apache Ignite integration.
 */
public class IgniteSessionFactoryBean extends org.springframework.orm.hibernate5.LocalSessionFactoryBean {
    
    @Override
    public void setHibernateProperties(Properties hibernateProperties) {
        if (IgniteCacheManager.isIgniteCacheEnabled()) {
            // ignite is enabled, set hibernate cache properties
            hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
            hibernateProperties.setProperty("hibernate.cache.use_query_cache", "true");           
            hibernateProperties.setProperty("hibernate.cache.region.factory_class", "org.joget.commons.ignite.IgniteHibernateRegionFactory");            
            hibernateProperties.setProperty("org.apache.ignite.hibernate.ignite_instance_name", "ignite-grid");            
        }
        super.setHibernateProperties(hibernateProperties);
    }
        
}
