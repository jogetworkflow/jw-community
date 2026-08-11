package org.joget.apps.app.service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import org.joget.commons.cache.InMemoryCacheManager;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JdbcUtil#clearQueryCache(String)}.
 *
 * <p>JdbcUtil has a static initializer that resolves the {@code cacheManager} bean via
 * {@link AppUtil#getApplicationContext()}. Every test therefore stubs AppUtil before JdbcUtil
 * is first referenced, so the class can initialize without a real Spring context.</p>
 */
public class JdbcUtilTest {

    /**
     * Stubs AppUtil.getApplicationContext() so JdbcUtil's static initializer can resolve the
     * "cacheManager" bean (and its datasource cache) without a real Spring context.
     */
    private void stubAppContext(MockedStatic<AppUtil> appUtil) {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        when(context.getBean("cacheManager")).thenReturn(cacheManager);
        when(cacheManager.getCache(anyString())).thenReturn(Mockito.mock(Cache.class));
        appUtil.when(AppUtil::getApplicationContext).thenReturn(context);
    }

    @Test
    public void clearQueryCache_availableGrid_clearsTableQueryRegion() {
        String tableName = "app_fd_customer";
        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
             MockedStatic<InMemoryCacheManager> cacheStatic = Mockito.mockStatic(InMemoryCacheManager.class)) {
            stubAppContext(appUtil);

            InMemoryCacheManager grid = Mockito.mock(InMemoryCacheManager.class);
            cacheStatic.when(InMemoryCacheManager::isAvailable).thenReturn(true);
            cacheStatic.when(InMemoryCacheManager::getInMemoryCacheManager).thenReturn(grid);

            JdbcUtil.clearQueryCache(tableName);

            // clears exactly the table's query-results region, following the naming convention
            verify(grid, times(1)).clear(InMemoryCacheManager.PREFIX_QUERY_CACHE + tableName);
        }
    }

    @Test
    public void clearQueryCache_gridNotAvailable_doesNothing() {
        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
             MockedStatic<InMemoryCacheManager> cacheStatic = Mockito.mockStatic(InMemoryCacheManager.class)) {
            stubAppContext(appUtil);

            cacheStatic.when(InMemoryCacheManager::isAvailable).thenReturn(false);

            JdbcUtil.clearQueryCache("app_fd_customer");

            // when the grid is inactive there is nothing to clear, so the manager is never resolved
            cacheStatic.verify(InMemoryCacheManager::getInMemoryCacheManager, never());
        }
    }

    @Test
    public void clearQueryCache_blankTableName_doesNothing() {
        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
             MockedStatic<InMemoryCacheManager> cacheStatic = Mockito.mockStatic(InMemoryCacheManager.class)) {
            stubAppContext(appUtil);

            JdbcUtil.clearQueryCache(null);
            JdbcUtil.clearQueryCache("");
            JdbcUtil.clearQueryCache("   ");

            // blank input short-circuits before the grid is even checked
            cacheStatic.verify(InMemoryCacheManager::isAvailable, never());
            cacheStatic.verify(InMemoryCacheManager::getInMemoryCacheManager, never());
        }
    }
}
