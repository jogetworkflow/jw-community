package org.joget.commons.spring.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        String cacheKey = getCacheKey(key,locale);
        Element element = cache.get(cacheKey);

        if (element == null) {
            Collection<ResourceBundleMessage> results = new ArrayList<ResourceBundleMessage>();

            results = super.find(ENTITY_NAME, "WHERE e.key = ? AND e.locale = ?", new String[]{key, locale}, null, null, null, null);
            ResourceBundleMessage result = null;
            if (results != null && results.size() != 0) {
                result = results.iterator().next();
            }
            element = new Element(cacheKey, (Serializable) result);
            cache.put(element);
            return result;
        }else{
            return (ResourceBundleMessage) element.getValue();
        }
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
        return DynamicDataSourceManager.getCurrentProfile()+key+locale;
    }
}
