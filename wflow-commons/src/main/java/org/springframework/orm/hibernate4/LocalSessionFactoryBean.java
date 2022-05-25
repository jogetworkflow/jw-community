package org.springframework.orm.hibernate4;

import java.util.Properties;

/**
 * Adapter for Hibernate 4 LocalSessionFactoryBean to provide backward compatibility for plugins using Hibernate 4
 */
public class LocalSessionFactoryBean extends org.springframework.orm.hibernate5.LocalSessionFactoryBean {
    
    @Override
    public void setHibernateProperties(Properties hibernateProperties) {
        
        // set properties required for hibernate 5
        if (!hibernateProperties.containsKey("hibernate.enable_lazy_load_no_trans")) {
            hibernateProperties.setProperty("hibernate.enable_lazy_load_no_trans", "true");
        }
        if (!hibernateProperties.containsKey("hibernate.allow_update_outside_transaction")) {
            hibernateProperties.setProperty("hibernate.allow_update_outside_transaction", "true");
        }
        if (!hibernateProperties.containsKey("hibernate.transaction.coordinator_class")) {
            hibernateProperties.setProperty("hibernate.transaction.coordinator_class", "jta");
        }
        
        super.setHibernateProperties(hibernateProperties);
    }
}