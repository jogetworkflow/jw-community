package org.joget.commons.ignite;

/**
 * Interface to indicate that JDBC queries are cacheable.
 */
public interface IgniteJdbcCacheable {
    
    boolean isJdbcCacheable(String cacheKey);
    
}
