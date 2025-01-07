package org.joget.commons.cache;

/**
 * Interface to indicate that JDBC queries are cacheable.
 */
public interface InMemoryJdbcCacheable {
    
    boolean isJdbcCacheable(String cacheKey);
    
}
