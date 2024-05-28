package org.joget.commons.ignite;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Set;
import org.aspectj.lang.annotation.Around;

/**
 * AOP aspect to intercept and cache JDBC queries.
 * To run on Java 17 and above, need to add the following JVM options:
 * --add-opens=java.base/java.nio=ALL-UNNAMED
 * --add-opens=java.base/java.math=ALL-UNNAMED
 * --add-opens=java.sql/java.sql=ALL-UNNAMED
 * --add-opens=java.sql.rowset/javax.sql.rowset=ALL-UNNAMED
 * --add-opens=java.sql.rowset/com.sun.rowset=ALL-UNNAMED
 * --add-opens=java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED
 * --add-opens=java.sql.rowset/com.sun.rowset.providers=ALL-UNNAMED
 */
@Aspect
public class IgniteJdbcCacheAspect {

    private final static ThreadLocal cacheableObject = new InheritableThreadLocal();
    
    /**
     * Intercept JDBC plugin method calls.
     */
//    @Pointcut("execution(* org.joget.plugin.enterprise.Jdbc*.*(..))")
    @Pointcut("execution(* org.joget.commons.ignite.IgniteJdbcCacheable+.*(..))")
    public void jdbcPluginMethods() {
    }

    @Around("jdbcPluginMethods()")
    public Object cacheJdbcPluginSql(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object target = pjp.getTarget();
            if (target instanceof IgniteJdbcCacheable) {
                cacheableObject.set(target);
            }
            Object result = pjp.proceed();
            return result;
        } finally {
            cacheableObject.set(null);
        }
    }
    
    /**
     * Intercept PreparedStatement.execute* method calls.
     */
    @Pointcut("execution(* java.sql.PreparedStatement.executeUpdate(..)) || execution(* java.sql.PreparedStatement.executeQuery(..))")
    public void sqlMethods() {
    }

    /**
     * Cache JDBC SQL SELECT queries.
     * Similar algorithm to the standard implementation of TimestampsCache in org.hibernate.cache.internal.TimestampsCacheEnabledImpl.
     * @param pjp
     * @return
     * @throws Throwable 
     */
    @Around("sqlMethods()")
    public Object cacheSql(ProceedingJoinPoint pjp) throws Throwable {
        Object target = pjp.getTarget();
        String method = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();
            
        try {
            // get cache key for query
            String cacheKey = IgniteJdbcCacheManager.generateCacheKey(target, args);
            if (cacheKey == null) {
                // not cacheable, just proceed
                return pjp.proceed();
            }

            boolean isCacheable = false;
            Object activeObject = cacheableObject.get();
            if (activeObject != null && activeObject instanceof IgniteJdbcCacheable) {
                isCacheable = ((IgniteJdbcCacheable)activeObject).isJdbcCacheable(cacheKey);
            }
            if (!isCacheable) {
                // not within JDBC plugin, just proceed
                return pjp.proceed();
            }            
            
            // get tables used in query
            Set<String> tableNames = IgniteJdbcCacheManager.extractTableNames(cacheKey);

            // get cached query if available
            ResultSet cachedResultSet = IgniteJdbcCacheManager.readQueryCache(method, cacheKey, tableNames);
            if (cachedResultSet != null) {
                return cachedResultSet;
            }

            // no cached result available, proceed with original query
            Object result = pjp.proceed();

            // update cache with result
            cachedResultSet = IgniteJdbcCacheManager.updateQueryCache(method, cacheKey, result, tableNames);
            if (cachedResultSet != null) {
                result = cachedResultSet;
            }
            
            return result;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Throwable t) {
            throw t;
        } finally {

        }
    } 

}
