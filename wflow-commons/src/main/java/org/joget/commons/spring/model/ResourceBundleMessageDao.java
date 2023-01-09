package org.joget.commons.spring.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.commons.util.DynamicDataSourceManager;

public class ResourceBundleMessageDao extends AbstractSpringDao {

    public static final String ENTITY_NAME = "ResourceBundleMessage";

    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void saveOrUpdate(ResourceBundleMessage message) {
        String cacheKey = getCacheKey(message.getKey(),message.getLocale());
        cache.remove(cacheKey);
        super.saveOrUpdate(ENTITY_NAME, message);
    }

    public void delete(ResourceBundleMessage message) {
        String cacheKey = getCacheKey(message.getKey(),message.getLocale());
        cache.remove(cacheKey);
        super.delete(ENTITY_NAME, message);
    }

    public ResourceBundleMessage getMessageById(String id) {
        return (ResourceBundleMessage) super.find(ENTITY_NAME, id);
    }

    public ResourceBundleMessage getMessage(String key, String locale) {
        Map<String, ResourceBundleMessage> messageMap;
        String cacheKey = getCacheKey(key,locale);
        Element element = cache.get(cacheKey);
        if (element == null) {
            messageMap = new HashMap<String, ResourceBundleMessage>();
            Collection<ResourceBundleMessage> results = super.find(ENTITY_NAME, "WHERE e.locale = ?", new String[]{locale}, null, null, null, null);
            for (ResourceBundleMessage message : results) {
                messageMap.put(message.getKey(), message);
            }
            element = new Element(cacheKey, messageMap);
            cache.put(element);
        } else {
            messageMap = (HashMap<String, ResourceBundleMessage>) element.getObjectValue();
        }
        ResourceBundleMessage result = messageMap.get(key);
        return result;
    }

    public List<ResourceBundleMessage> getMessages(String condition, String[] param, String sort, Boolean desc, Integer start, Integer rows) {
        if (condition != null && condition.trim().length() != 0) {
            return (List<ResourceBundleMessage>) super.find(ENTITY_NAME, condition, param, sort, desc, start, rows);
        } else {
            return (List<ResourceBundleMessage>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
        }
    }

    public List<ResourceBundleMessage> getMessages(String locale, String sort, Boolean desc, Integer start, Integer rows) {
        if (locale != null && locale.trim().length() != 0) {
            return (List<ResourceBundleMessage>) super.find(ENTITY_NAME, "WHERE e.locale = ?", new String[]{locale}, sort, desc, start, rows);
        } else {
            return (List<ResourceBundleMessage>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
        }
    }

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    private String getCacheKey(String key, String locale){
        return DynamicDataSourceManager.getCurrentProfile()+locale;
    }
}
