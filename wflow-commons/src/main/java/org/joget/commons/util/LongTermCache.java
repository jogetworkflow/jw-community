package org.joget.commons.util;

import java.util.Date;
import java.util.Iterator;
import javax.cache.Cache;
import javax.cache.CacheManager;
import net.sf.ehcache.Element;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.RBM_CACHE");
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Object getObject(String key) {
        Object value = null;
        Element element = get(key);
        if (element != null) {
            value = element.getObjectValue();
        }
        return value;
    }

    /**
     * Like {@link #getObject(String)}, but does not trust a CACHE_LAST_CLEAR_ value that was read
     * through the debounced setup cache when that value reports the entry as still fresh.
     *
     * <p>Why this exists: {@link #get(String)} resolves CACHE_LAST_CLEAR_ via
     * {@code SetupManager.getSettingValue}, which is served from a cache that refreshes on a ~20
     * second debounce. So for ~20-25 seconds after another node invalidates an entry, this cache
     * reports its OLD value as current. That is the widest window of #2085: a stale Hibernate
     * mapping matched a stale column set and was served.</p>
     *
     * @param key
     * @return the cached value, or null if absent or verified stale
     */
    public Object getObjectFresh(String key) {
        Element element = getFresh(key);
        return (element != null) ? element.getObjectValue() : null;
    }

    /**
     * Like {@link #get(String)}, but verifies a surviving entry against the database.
     *
     * <p>The cheap path runs first and usually wins. A cached CACHE_LAST_CLEAR_ can only ever be
     * BEHIND the database, never ahead of it — the value only moves forward, and this node either
     * has seen a given advance or has not. So a cached value that ALREADY condemns the entry is
     * conclusive and needs no round trip; only an entry that still looks fresh can be wrong, and
     * only that case pays for a read.</p>
     *
     * <p>Eviction is identical to {@link #get(String)}, so callers need no new invalidation
     * handling: a caller that rebuilds on null keeps working unchanged.</p>
     */
    public Element getFresh(String key) {
        Element element = get(key);
        if (element == null) {
            // Absent, or the cached CACHE_LAST_CLEAR_ already condemned it. Either way, nothing
            // a database read could tell us would change the answer.
            return null;
        }

        Long lastClear = getLastClearTimeFresh(key);
        if (lastClear != null && element.getCreationTime() <= lastClear) {
            cache.remove(key);
            LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh (verified against database).");
            return null;
        }
        return element;
    }

    /**
     * Backward compatible method for getting cache
     *
     * @param key
     */
    public Element get(String key) {
        Element element = (Element)cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null) {
            if (lastClear != null && element.getCreationTime() <= lastClear) {
                cache.remove(key);
                LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
                return null;
            }
        }
        return element;
    }
    
    public void remove(String key) {
        cache.remove(key);
        setupManager.updateSetting("CACHE_LAST_CLEAR_" + key, Long.toString((new Date()).getTime()));
        LogUtil.debug(LongTermCache.class.getName(), key + " is removed.");
    }
    
    public void removeAll(String prefix) {
        for (Iterator i=cache.iterator(); i.hasNext();) {
            Cache.Entry entry = (Cache.Entry)i.next();
            if (entry.getKey().toString().startsWith(prefix)) {
                i.remove();
                setupManager.updateSetting("CACHE_LAST_CLEAR_" + entry.getKey().toString(), Long.toString((new Date()).getTime()));
            }
        }
        LogUtil.debug(LongTermCache.class.getName(), "All caches with `"+prefix+"` prefix are removed.");
    }

    public void putObject(String key, Object value) {
        putObject(key, value, null);
    }
    
    /**
     * This is used for long processing time cache data. 
     * Keep its requesting time instead of cache creation time to prevent stale cache
     * 
     * @param key
     * @param value
     * @param requestTime 
     */
    public void putObject(String key, Object value, Date requestTime) {
        if (requestTime == null) {
            requestTime = new Date();
        }
        
        Element element = new Element(key, value, requestTime.getTime());
        put(element);
    }
    
    /**
     * Backward compatible method for adding cache 
     * 
     * @param element 
     */
    public void put(Element element) {
        cache.put(element.getKey(), element);
        LogUtil.debug(LongTermCache.class.getName(), element.getKey() + " is refreshed.");
    }
    
    public Long getLastClearTime(String key) {
        return parseLastClearTime(setupManager.getSettingValue("CACHE_LAST_CLEAR_" + key));
    }

    /**
     * The same value as {@link #getLastClearTime(String)}, read straight from the database instead
     * of through the debounced setup cache. One indexed read — see
     * {@code SetupManager.getSettingValuesFresh}.
     *
     * <p><b>Takes the MAXIMUM across duplicate rows.</b> {@code wf_setup.property} is not unique and
     * {@code updateSetting} resolves the existing row through the debounced cache, so two nodes
     * invalidating a given key for the first time can each insert their own row. Thereafter each
     * node keeps updating "its" row and an arbitrary pick can be permanently behind — the node would
     * never see the other's invalidations, which is #2085 with extra steps. The maximum is the
     * correct resolution rather than a heuristic: this value only ever moves forward, so the newest
     * duplicate is the only one that reflects every invalidation seen so far.</p>
     */
    public Long getLastClearTimeFresh(String key) {
        Long latest = null;
        for (String value : setupManager.getSettingValuesFresh("CACHE_LAST_CLEAR_" + key)) {
            Long parsed = parseLastClearTime(value);
            if (parsed != null && (latest == null || parsed.longValue() > latest.longValue())) {
                latest = parsed;
            }
        }
        return latest;
    }

    private Long parseLastClearTime(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                LogUtil.error(LongTermCache.class.getName(), e, value);
            }
        }
        return null;
    }
}
