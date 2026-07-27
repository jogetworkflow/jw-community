package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.service.AppUtil;
import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PackageDefinitionDaoImplTest {

    @Test
    public void testClearHibernatePackageDefinitionCachesEvictsPackageMetadataRegions() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Cache hibernateCache = mock(Cache.class);
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);

        when(sessionFactory.getCache()).thenReturn(hibernateCache);

        dao.clearHibernatePackageDefinitionCaches();

        verify(hibernateCache).evictEntityData("AppDefinition");
        verify(hibernateCache).evictEntityData("PackageDefinition");
        verify(hibernateCache).evictEntityData("PackageActivityForm");
        verify(hibernateCache).evictEntityData("PackageActivityPlugin");
        verify(hibernateCache).evictEntityData("PackageParticipant");
        verify(hibernateCache).evictCollectionData("AppDefinition.packageDefinitionList");
        verify(hibernateCache).evictCollectionData("PackageDefinition.packageActivityFormMap");
        verify(hibernateCache).evictCollectionData("PackageDefinition.packageActivityPluginMap");
        verify(hibernateCache).evictCollectionData("PackageDefinition.packageParticipantMap");
        verify(hibernateCache, never()).evictAllRegions();
    }

    @Test
    public void testClearPackageDefinitionCachesCanEvictOnlyCurrentAppDefinitionFromSession() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Cache hibernateCache = mock(Cache.class);
        AppDefCache appDefCache = mock(AppDefCache.class);
        AppDefinition appDef = new AppDefinition();
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);
        dao.setCache(appDefCache);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(sessionFactory.getCache()).thenReturn(hibernateCache);

        dao.clearPackageDefinitionCaches(appDef, true);

        verify(appDefCache).removeAll(appDef);
        verify(session).evict(appDef);
        verify(session, never()).clear();
    }

    /**
     * Eviction must happen immediately (not only deferred to post-commit): callers such as
     * AppServiceImpl.deployWorkflowPackage's stale-package-cache recovery and
     * AppUtil.reloadAppDefinitionAfterStalePackageCache clear the cache and reload the app
     * definition again within the same transaction, and must see a fresh cache miss rather than
     * the same invalid reference they just tried to evict.
     */
    @Test
    public void testClearPackageDefinitionCachesEvictsImmediately() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Cache hibernateCache = mock(Cache.class);
        AppDefCache appDefCache = mock(AppDefCache.class);
        AppDefinition appDef = new AppDefinition();
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);
        dao.setCache(appDefCache);
        when(sessionFactory.getCache()).thenReturn(hibernateCache);

        TransactionSynchronizationManager.initSynchronization();
        try {
            dao.clearPackageDefinitionCaches(appDef);

            verify(appDefCache, times(1)).removeAll(appDef);

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        // Evicted a second time after commit, closing the window where a concurrent reader
        // repopulated the cache with pre-change data before this transaction committed.
        verify(appDefCache, times(2)).removeAll(appDef);
    }

    /**
     * A Process Builder save can register a mapping-cache eviction after it has already registered
     * the migration-start callback. Explicit ordering must still put that later cache eviction
     * first during transaction completion, so the migration thread cannot release the save lock
     * before the cache fence is complete.
     */
    @Test
    public void testCacheEvictionSynchronizationRunsBeforeMigrationStartRegardlessOfRegistrationOrder() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Cache hibernateCache = mock(Cache.class);
        AppDefCache appDefCache = mock(AppDefCache.class);
        AppDefinition appDef = new AppDefinition();
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);
        dao.setCache(appDefCache);
        when(sessionFactory.getCache()).thenReturn(hibernateCache);

        TransactionSynchronizationManager.initSynchronization();
        try {
            // Model the migration synchronization being registered before a later mapping save.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public int getOrder() {
                    return Ordered.LOWEST_PRECEDENCE;
                }
            });
            dao.clearPackageDefinitionCaches(appDef);

            java.util.List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(2, synchronizations.size());
            assertEquals("The later-registered cache eviction must be ordered before migration startup",
                    PackageDefinitionDaoImpl.PACKAGE_CACHE_EVICTION_SYNCHRONIZATION_ORDER,
                    synchronizations.get(0).getOrder());
            assertEquals(Ordered.LOWEST_PRECEDENCE, synchronizations.get(1).getOrder());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /**
     * In-memory mutations made to the AppDefinition/PackageDefinition object graph during a
     * transaction (e.g. replaceAppPackageDefinition) are not undone by a database rollback. On
     * rollback the current thread's AppDefinition must be reset so the next read reloads from the
     * database instead of reusing those now-inconsistent in-memory objects.
     */
    @Test
    public void testClearPackageDefinitionCachesResetsAppDefinitionOnRollback() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Cache hibernateCache = mock(Cache.class);
        AppDefCache appDefCache = mock(AppDefCache.class);
        AppDefinition appDef = new AppDefinition();
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);
        dao.setCache(appDefCache);
        when(sessionFactory.getCache()).thenReturn(hibernateCache);

        AppUtil.setCurrentAppDefinition(appDef);
        assertFalse(AppUtil.isAppDefinitionReset());

        try {
            TransactionSynchronizationManager.initSynchronization();
            try {
                dao.clearPackageDefinitionCaches(appDef);
                for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                    synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
                }
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }

            verify(appDefCache, times(2)).removeAll(appDef);
            assertTrue("A rolled-back transaction's in-memory changes must not be trusted; the next read must reload from the database", AppUtil.isAppDefinitionReset());
        } finally {
            AppUtil.setCurrentAppDefinition(null);
        }
    }

    @Test
    public void testReplaceAppPackageDefinitionUpdatesRetainedAppDefinitionReference() {
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086");
        appDef.setVersion(1L);

        PackageDefinition oldPackageDef = new PackageDefinition();
        oldPackageDef.setId("t8086");
        oldPackageDef.setVersion(11L);
        oldPackageDef.setAppDefinition(appDef);
        ArrayList<PackageDefinition> packageDefinitions = new ArrayList<>();
        packageDefinitions.add(oldPackageDef);
        appDef.setPackageDefinitionList(packageDefinitions);

        PackageDefinition newPackageDef = new PackageDefinition();
        newPackageDef.setId("t8086");
        newPackageDef.setVersion(12L);

        new PackageDefinitionDaoImpl().replaceAppPackageDefinition(appDef, newPackageDef);

        assertSame(newPackageDef, appDef.getPackageDefinition());
        assertEquals("Stale package entry should be removed before caching can repopulate", 1, appDef.getPackageDefinitionList().size());
        assertEquals(Long.valueOf(12), appDef.getPackageDefinition().getVersion());
    }

    @Test
    public void testReattachPackageDefinitionMappingsRekeysRetainedChildMappings() {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId("t8086");
        packageDef.setVersion(12L);

        PackageActivityForm form = new PackageActivityForm();
        form.setPackageId("t8086");
        form.setPackageVersion(11L);
        PackageActivityPlugin plugin = new PackageActivityPlugin();
        plugin.setPackageId("t8086");
        plugin.setPackageVersion(11L);
        PackageParticipant participant = new PackageParticipant();
        participant.setPackageId("t8086");
        participant.setPackageVersion(11L);

        Map<String, PackageActivityForm> forms = new HashMap<>();
        forms.put("Process 1::activity1", form);
        Map<String, PackageActivityPlugin> plugins = new HashMap<>();
        plugins.put("Process 1::tool1", plugin);
        Map<String, PackageParticipant> participants = new HashMap<>();
        participants.put("Process 1::participant1", participant);

        packageDef.setPackageActivityFormMap(forms);
        packageDef.setPackageActivityPluginMap(plugins);
        packageDef.setPackageParticipantMap(participants);

        new PackageDefinitionDaoImpl().reattachPackageDefinitionMappings(packageDef);

        assertRetainedMapping(packageDef, form);
        assertRetainedMapping(packageDef, plugin);
        assertRetainedMapping(packageDef, participant);
    }

    private void assertRetainedMapping(PackageDefinition packageDef, PackageActivityForm form) {
        assertSame(packageDef, form.getPackageDefinition());
        assertEquals("t8086", form.getPackageId());
        assertEquals(Long.valueOf(12), form.getPackageVersion());
    }

    /**
     * getPackageMetadata must read the package version/modification time with a scalar query that
     * is NOT marked cacheable, so a Process Builder read sees the latest committed values rather
     * than a cached or session-managed (stale) snapshot when detecting a concurrent save.
     */
    @Test
    public void testGetPackageMetadata_returnsScalarRowAndIsNotCacheable() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Query query = mock(Query.class);
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        java.util.Date dateModified = new java.util.Date(1_700_000_000_000L);
        when(query.list()).thenReturn(new ArrayList<Object>(java.util.Collections.singletonList(new Object[]{7L, dateModified})));

        Object[] meta = dao.getPackageMetadata("t8086", 3L);

        assertEquals("Package version should come from the scalar row", 7L, meta[0]);
        assertSame("dateModified should come from the scalar row", dateModified, meta[1]);
        verify(query).setParameter(1, "t8086");
        verify(query).setParameter(2, 3L);
        verify(query).setMaxResults(1);
        // A cacheable query could return a stale snapshot, defeating the freshness check.
        verify(query, never()).setCacheable(anyBoolean());
    }

    @Test
    public void testGetPackageMetadata_returnsNullWhenNoPackageRow() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Query query = mock(Query.class);
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(new ArrayList<Object>());

        assertNull("A missing package definition must yield null, not an empty array", dao.getPackageMetadata("t8086", 3L));
    }

    @Test
    public void testGetPackageMetadata_returnsNullForNullArgumentsWithoutQuerying() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        PackageDefinitionDaoImpl dao = new PackageDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);

        assertNull(dao.getPackageMetadata(null, 3L));
        assertNull(dao.getPackageMetadata("t8086", null));
        verify(sessionFactory, never()).getCurrentSession();
    }

    private void assertRetainedMapping(PackageDefinition packageDef, PackageActivityPlugin plugin) {
        assertSame(packageDef, plugin.getPackageDefinition());
        assertEquals("t8086", plugin.getPackageId());
        assertEquals(Long.valueOf(12), plugin.getPackageVersion());
    }

    private void assertRetainedMapping(PackageDefinition packageDef, PackageParticipant participant) {
        assertSame(packageDef, participant.getPackageDefinition());
        assertEquals("t8086", participant.getPackageId());
        assertEquals(Long.valueOf(12), participant.getPackageVersion());
    }
}
