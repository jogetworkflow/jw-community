package org.joget.commons.util;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import static org.joget.commons.util.DynamicDataSourceManager.getProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testCommonsApplicationContext.xml"})
public class TestCache {
    
    @Autowired
    ResourceBundleMessageDao resourceBundleMessageDao;
    
    @Autowired
    SetupManager setupManager;
    
    @Autowired
    CacheManager cacheManager;
    
    @Autowired
    LongTermCache longTermCache;
    
    @Test
    @Transactional
    public void testResourceBundleMessage() throws Exception {
        Cache cache = cacheManager.getCache("org.joget.cache.RBM_CACHE");
        String cacheKey = DynamicDataSourceManager.getCurrentProfile()+ "_zh_CN";
        
        //make sure data is not exist first
        ResourceBundleMessage message = resourceBundleMessageDao.getMessage("test", "zh_CN");
        if (message != null) {
            resourceBundleMessageDao.delete(message);
        }
        
        //clear cache
        longTermCache.remove(cacheKey);
        
        //check cache is not exist
        Assert.isTrue(!cache.containsKey(cacheKey), "cache should not exist after remove");
        
        
        //adding message
        message = new ResourceBundleMessage();
        message.setKey("test");
        message.setLocale("zh_CN");
        message.setMessage("test message");
        resourceBundleMessageDao.saveOrUpdate(message);
        
        //get message
        ResourceBundleMessage returnMessage = resourceBundleMessageDao.getMessage("test", "zh_CN");
        Assert.isTrue("test message".equals(returnMessage.getMessage()), "the message is not correct");
        
        //check cache is exist
        Assert.isTrue(cache.containsKey(cacheKey), "cache should exist retrieve messages");
        
        //check long term cache can return correctly
        Map<String, ResourceBundleMessage> messageMap = (Map<String, ResourceBundleMessage>) longTermCache.get(cacheKey);
        Assert.isTrue(messageMap.size() == 1, "messages size is wrong");
        Assert.isTrue(messageMap.get("test") != null, "return messages is wrong");
        
        //update the last clear data to clear cache
        setupManager.updateSetting("CACHE_LAST_CLEAR_" + cacheKey, Long.toString((new Date()).getTime()) + 5);
        
        //check long term cache return null
        messageMap = (Map<String, ResourceBundleMessage>) longTermCache.get(cacheKey);
        Assert.isNull(messageMap, "long term cache should exist");
        
        // remove the modified last clear date
        setupManager.deleteSetting("CACHE_LAST_CLEAR_" + cacheKey);
        
        //get message again to refresh cache
        returnMessage = resourceBundleMessageDao.getMessage("test", "zh_CN");
        Assert.isTrue("test message".equals(returnMessage.getMessage()), "return message is wrong");
        
        //check cache is exist
        Assert.isTrue(cache.containsKey(cacheKey), "cache should exist after refresh");
        
        //check long term cache can return correctly
        messageMap = (Map<String, ResourceBundleMessage>) longTermCache.get(cacheKey);
        Assert.isTrue(messageMap.size() == 1, "messages size is wrong after refresh");
        Assert.isTrue(messageMap.get("test") != null, "return messages is wrong after refresh");
        
        message = resourceBundleMessageDao.getMessage("test", "zh_CN");
        
        //update message
        message.setMessage("test message 2");
        resourceBundleMessageDao.saveOrUpdate(message);
        
        //check cache is not exist
        Assert.isTrue(!cache.containsKey(cacheKey), "cache should not exist after update");
        
        //get message again to refresh cache
        returnMessage = resourceBundleMessageDao.getMessage("test", "zh_CN");
        Assert.isTrue("test message 2".equals(returnMessage.getMessage()), "the message is not correct after update");
        
        //check long term cache can return correctly
        messageMap = (Map<String, ResourceBundleMessage>) longTermCache.get(cacheKey);
        Assert.isTrue(messageMap.size() == 1, "messages size is wrong after upate");
        Assert.isTrue(messageMap.get("test") != null, "return messages is wrong after update");
        
        message = resourceBundleMessageDao.getMessage("test", "zh_CN");
        
        //delete message
        resourceBundleMessageDao.delete(message);
        
        //check cache is not exist
        Assert.isTrue(!cache.containsKey(cacheKey), "cache should not exist after delete");
    }
    
    @Test
    @Transactional
    public void testSessionFactoryCache() {
        Cache cache = cacheManager.getCache("org.joget.cache.SF_CACHE");
        String cacheKey = "test_session_factory";
                
        // create configuration
        Configuration configuration = new Configuration();
        configuration.setProperty("show_sql", "false");
        configuration.setProperty("cglib.use_reflection_optimizer", "true");
        configuration.setProperty(Environment.USE_QUERY_CACHE, "true");
        configuration.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        configuration.setProperty(Environment.CACHE_REGION_FACTORY, "org.joget.commons.ignite.IgniteHibernateRegionFactory");
        configuration.setProperty("org.apache.ignite.hibernate.ignite_instance_name", "ignite-grid");
        configuration.setProperty(Environment.LOG_SLOW_QUERY, "500");
        
        // set datasource
        DataSource dataSource = (DataSource) SecurityUtil.getApplicationContext().getBean("setupDataSource");
        configuration.getProperties().put(Environment.JAKARTA_JTA_DATASOURCE, dataSource);
        configuration.getProperties().put(Environment.ALLOW_UPDATE_OUTSIDE_TRANSACTION, true);
        Properties properties = getProperties();
        
        // set schema from datasource properties
        String workflowSchema = (String) properties.get("workflowSchema");
        if (workflowSchema != null && !workflowSchema.isEmpty()) {
            configuration.setProperty(Environment.DEFAULT_SCHEMA, (String) properties.get("workflowSchema"));
        }
        
        final ServiceRegistry sr = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        MetadataSources metadataSources = new MetadataSources(sr);
        
        metadataSources.addResource("/org/joget/commons/spring/model/Setting.hbm.xml");
        
        Metadata metadata = metadataSources.buildMetadata();
        
        SessionFactory sf = metadata.buildSessionFactory();
        
        //check session factory is open
        Assert.isTrue(!sf.isClosed(), "session factory should not closed after creation");
        
        cache.put(cacheKey, sf);
        
        //check cache is exist
        Assert.isTrue(cache.containsKey(cacheKey), "cache element should exist after put in cache");
        
        //check cached session factory is open
        SessionFactory cachedSf = (SessionFactory) cache.get(cacheKey);
        Assert.isTrue(!cachedSf.isClosed(), "false");
        
        //remove session factory from cache
        cache.remove(cacheKey);
        
        //check cache is not exist
        Assert.isTrue(!cache.containsKey(cacheKey), "cache element should not exist after remove from cache");
        
        try  {
            Thread.sleep(1000);
        } catch (Exception e) {
            //ignore
        }
        
        //check session factory is closed by cache listener
        Assert.isTrue(sf.isClosed(), "the session factory should be closed after remove from cache");
    }
}
