package org.joget.apps.form.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.mapping.PersistentClass;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormService;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LongTermCache;
import org.joget.commons.util.SetupManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Targets the 2026-09-07 GitLab #2085 finding (note_48150, daniel.phang's DEBUG-enabled logs from a
 * real 2-node multi-tenant cluster) and its leading hypothesis: that {@code findSessionFactory}'s
 * staleness check (via {@code hasMappingChanged}) reads a STALE column set from
 * {@code getFormDefinitionColumnNames}, even though the SAME thread had just rebuilt that table's
 * column cache to include the new field moments earlier — a same-thread, same-request
 * read-your-own-write inconsistency.
 *
 * <p><b>Result: that specific mechanism is REFUTED here.</b> This test drives the REAL
 * {@link FormColumnResolver}/{@link FormColumnCache}/{@link LongTermCache} stack (as
 * {@code FormColumnMultiTenantTest} does, not a mocked {@code getFormDefinitionColumnNames}, which
 * could not exercise this interaction at all) through the exact sequence the live logs show: an
 * on-disk {@code .hbm.xml} fallback resolves a STALE, 2-column {@code PersistentClass} (this is a
 * DIFFERENT case from the one {@code c14263ef9} already fixed — that one is {@code pc == null}; here
 * {@code pc} resolves successfully, just from stale disk content) in the SAME call where the shared
 * column cache had ALREADY been rebuilt, on the SAME thread, to a fresher 3-column set. Against the
 * current (already-merged) code, {@code hasMappingChanged} correctly sees the size mismatch, closes
 * the stale {@code SessionFactory}, and forces a rebuild — this test is GREEN, not red.
 *
 * <p>Kept as a permanent regression guard (the disk-mapping-fallback branch of
 * {@code findSessionFactory}, lines ~969-993, had NO test coverage with a real, parseable mapping
 * file before this — every existing test pointed {@code getFormMappingPath()} at a directory with
 * nothing in it) and as a documented, evidence-based narrowing of daniel.phang's still-open report:
 * the same-thread {@code FormColumnCache} race is not the mechanism, so the remaining search space is
 * either genuine cross-thread/cross-request concurrency on node1, or something in Hibernate's own
 * caching under a real (non-mocked) {@code SessionFactory} — neither of which a mocked-DAO JUnit test
 * can reach. See the 2026-09-07 {@code PLANS.md} entry for the full writeup.</p>
 */
public class FormSessionFactoryColumnCacheRaceTest {

    private static final String TABLE = "app_fd_T2085RACE";
    private static final String UNPREFIXED = "T2085RACE";
    private static final String PROFILE = "race2085";

    private Map<Object, Object> sharedCache;
    private Map<String, String> settings;
    private FormColumnCache formColumnCache;
    private FormColumnResolver resolver;
    private String builderJson;

    @Before
    public void setUp() {
        // NOTE: FormColumnCache.VERIFY_INTERVAL_MS is a `static final` read once when the class
        // first loads in this JVM/fork, so it cannot be overridden per-test via a system property
        // without leaking into whichever test happens to trigger that class-load first. Not needed
        // here anyway: the fake SetupManager below is not debounced (unlike production's real
        // SetupManager), so both the throttled and unthrottled verification paths observe the same
        // CACHE_LAST_CLEAR_ value regardless of the throttle window.
        sharedCache = new LinkedHashMap<Object, Object>();
        settings = new LinkedHashMap<String, String>();

        LongTermCache longTermCache = new LongTermCache();
        longTermCache.setCacheManager(cacheManager(sharedCache));
        longTermCache.setSetupManager(setupManager());

        formColumnCache = new FormColumnCache();
        formColumnCache.setCache(longTermCache);

        FormDefinitionDao formDefinitionDao = mock(FormDefinitionDao.class);
        BuilderDefinitionDao builderDefinitionDao = mock(BuilderDefinitionDao.class);
        // No FormDefinition rows anywhere: the manifest/incremental machinery is exercised by
        // FormColumnMultiTenantTest already. Here the columns come from a builder definition, which
        // (deliberately, per FormColumnResolver's own javadoc) is re-read in full on every build,
        // incremental or not — the simplest way to get a real, additive column-cache rebuild.
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenReturn(new ArrayList<>());
        when(formDefinitionDao.getFormDefinitionManifestByTableName(anyString())).thenReturn(new ArrayList<>());
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> builderDefs());

        resolver = new FormColumnResolver();
        resolver.setFormColumnCache(formColumnCache);
        resolver.setFormDefinitionDao(formDefinitionDao);
        resolver.setBuilderDefinitionDao(builderDefinitionDao);
        resolver.setFormService(mock(FormService.class));

        HostManager.setCurrentProfile(PROFILE);
    }

    @After
    public void tearDown() {
        HostManager.setCurrentProfile(null);
    }

    /**
     * Mutation-verified in the direction that matters: temporarily short-circuiting
     * {@code hasMappingChanged} to always return {@code false} (the bug this test would need to
     * catch, if the mechanism it targets were real) turns this RED
     * ({@code staleSf.close()} never invoked); restoring the real method turns it GREEN again. That
     * confirms this test can actually detect the mechanism it was built to look for, rather than
     * passing vacuously.
     */
    @Test
    public void findSessionFactory_rebuildsSessionFactory_whenDiskMappingIsStaleAndColumnCacheWasJustRefreshedOnTheSameThread() {
        // --- "node1's last real rebuild, long ago": the column cache AND the on-disk mapping file
        // both reflect the same 2-column state.
        builderJson = "{\"columns\":{\"field1\":{},\"field2\":{}}}";
        Collection<String> warm = resolver.getColumnNames(TABLE);
        assertEquals("sanity: the pre-edit column cache must match the on-disk mapping fixture",
                2, warm.size());

        // --- "node2 edits the form, the invalidation flag becomes visible on node1": a 3rd column
        // appears, and the CACHE_LAST_CLEAR_ flag is now newer than the warm entry.
        builderJson = "{\"columns\":{\"field1\":{},\"field2\":{},\"radio1\":{}}}";
        settings.put("CACHE_LAST_CLEAR_" + PROFILE + "_FORM_COLUMNS_" + UNPREFIXED,
                Long.toString(System.currentTimeMillis() + 1000L));

        // --- Call #1: the SELECT-clause build (AdvancedFormRowDataListBinder.getColumns()/
        // getColumnName(), which run BEFORE findCustomQuery reaches findSessionFactory) asks for
        // this table's columns first. This is what actually rebuilds the cache on this thread.
        Collection<String> fresh = dao().getFormDefinitionColumnNames(TABLE);
        assertEquals("the column cache must have rebuilt to include the new field", 3, fresh.size());
        assertTrue(fresh.contains("radio1"));

        // --- Call #2, same thread, moments later: findSessionFactory's own staleness check
        // (hasMappingChanged) asks the SAME question again while resolving the stale cached
        // SessionFactory against a PersistentClass it just loaded from the (still 2-column) disk
        // mapping file.
        SessionFactory staleSf = mock(SessionFactory.class);
        when(staleSf.isClosed()).thenReturn(false);

        FormDataDaoImpl dao = daoForFindSessionFactory(staleSf);

        SessionFactory result = dao.findSessionFactory(TABLE, TABLE, null, FormDataDaoImpl.ACTION_TYPE_LOAD);

        verify(staleSf, description("hasMappingChanged must detect the size mismatch between the "
                + "2-column disk mapping and the 3-column column cache that was just rebuilt on this "
                + "same thread, and close the now-unverifiable stale SessionFactory")).close();
        assertNotSame("a SessionFactory rebuilt to match the fresh column set must not be the stale "
                + "mock returned unverified", staleSf, result);
    }

    // ------------------------------------------------------------------ helpers

    private java.util.List<BuilderDefinition> builderDefs() {
        java.util.List<BuilderDefinition> defs = new ArrayList<BuilderDefinition>();
        BuilderDefinition def = mock(BuilderDefinition.class);
        when(def.getJson()).thenReturn(builderJson);
        defs.add(def);
        return defs;
    }

    /** A bare DAO wired only for {@code getFormDefinitionColumnNames}, used to run call #1. */
    private FormDataDaoImpl dao() {
        FormDataDaoImpl dao = new FormDataDaoImpl();
        injectField(dao, "columnResolver", resolver);
        return dao;
    }

    /** The DAO that runs {@code findSessionFactory} itself (call #2), sharing the same resolver. */
    private FormDataDaoImpl daoForFindSessionFactory(SessionFactory staleSf) {
        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "src/test/resources/issue2085race/";
            }

            @Override
            protected MetadataSources createMetadataSources() {
                // The production override reaches Spring (InMemoryCacheManager, the setup
                // datasource) to configure a live Hibernate Configuration; none of that exists in
                // this unit test, and none of it is needed just to parse the on-disk .hbm.xml back
                // into a PersistentClass. A dialect still has to be nameable (buildMetadata()
                // resolves one even with no JDBC connection), so pin one explicitly instead of
                // Hibernate trying to auto-detect it from a live connection that does not exist here.
                org.hibernate.boot.registry.StandardServiceRegistryBuilder builder =
                        new org.hibernate.boot.registry.StandardServiceRegistryBuilder();
                builder.applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                return new MetadataSources(builder.build());
            }

            @Override
            protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
                return "race-sf-key:" + entityName + ";" + tableName;
            }

            @Override
            protected String getPersistentClassCacheKey(String entityName) {
                return "race-pc-key:" + entityName;
            }
        };
        injectField(dao, "columnResolver", resolver);

        Cache sfCache = mock(Cache.class);
        when(sfCache.get(anyString())).thenReturn(staleSf);
        injectField(dao, "formSessionFactoryCache", sfCache);

        // Cache miss: models formPersistentClassCache's 120s TTL having expired independently of
        // the column cache's own invalidation, forcing the disk fallback in findSessionFactory.
        Cache pcCache = mock(Cache.class);
        when(pcCache.get(anyString())).thenReturn(null);
        injectField(dao, "formPersistentClassCache", pcCache);

        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(java.util.Collections.emptyIterator());
        injectField(dao, "joinFormSessionFactoryCache", joinCache);

        // staleSf really was built from the fixture's ORIGINAL 2-column state - without this,
        // findSessionFactory's #2085 pc/sf-decoupling check (added 2026-09-09) fails closed on
        // unknown provenance regardless of what hasMappingChanged concludes, which would make this
        // test's own mutation-verification claim (forcing hasMappingChanged to return false turns
        // it red) vacuous: it would stay green for the WRONG reason (Sol review finding, same date).
        seedSessionFactoryProvenance(dao, "race-sf-key:" + TABLE + ";" + TABLE, staleSf, parseFixturePersistentClass());

        // The rebuild path needs a live DB/Spring context; stub it so this stays a pure unit test
        // of findSessionFactory's own detection logic, exactly like FormDataDaoImplTest does for
        // the already-fixed pc==null case.
        FormDataDaoImpl spyDao = spy(dao);
        doReturn(mock(SessionFactory.class)).when(spyDao).createSessionFactoryWithSingleFlight(
                anyString(), anyString(), any(), anyInt(), anyString());
        return spyDao;
    }

    /** Parses the on-disk fixture directly to get a REAL, genuine PersistentClass matching its
     *  current (2-column) state, independent of any dao instance. */
    private PersistentClass parseFixturePersistentClass() {
        org.hibernate.boot.registry.StandardServiceRegistryBuilder builder =
                new org.hibernate.boot.registry.StandardServiceRegistryBuilder();
        builder.applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        MetadataSources metadataSources = new MetadataSources(builder.build());
        metadataSources.addFile(new java.io.File("src/test/resources/issue2085race/", TABLE + ".hbm.xml"));
        return metadataSources.buildMetadata().getEntityBinding(TABLE);
    }

    @SuppressWarnings("unchecked")
    private static void seedSessionFactoryProvenance(FormDataDaoImpl dao, String cacheKey, SessionFactory sf, PersistentClass builtFromPc) {
        try {
            java.lang.reflect.Field field = FormDataDaoImpl.class.getDeclaredField("formSessionFactorySourceCache");
            field.setAccessible(true);
            java.util.Map<String, FormDataDaoImpl.SessionFactoryProvenance> map =
                    (java.util.Map<String, FormDataDaoImpl.SessionFactoryProvenance>) field.get(dao);
            map.put(cacheKey, new FormDataDaoImpl.SessionFactoryProvenance(sf, builtFromPc));
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed formSessionFactorySourceCache", e);
        }
    }

    private static void injectField(FormDataDaoImpl dao, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = FormDataDaoImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dao, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject " + fieldName, e);
        }
    }

    /** Reads and writes a single tenant's wf_setup, like a tenant-routed SetupDao would. */
    private SetupManager setupManager() {
        return new SetupManager() {
            @Override
            public String getSettingValue(String property) {
                return settings.get(property);
            }

            @Override
            public java.util.Collection<String> getSettingValuesFresh(String property) {
                String v = settings.get(property);
                return (v == null) ? java.util.Collections.<String>emptyList()
                        : java.util.Collections.singletonList(v);
            }

            @Override
            public void updateSetting(String property, String value) {
                settings.put(property, value);
            }
        };
    }

    private static Object entry(final Map<Object, Object> store, final Object key) {
        return Proxy.newProxyInstance(FormSessionFactoryColumnCacheRaceTest.class.getClassLoader(),
                new Class[]{Cache.Entry.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                switch (method.getName()) {
                    case "getKey":
                        return key;
                    case "getValue":
                        return store.get(key);
                    default:
                        return null;
                }
            }
        });
    }

    private static CacheManager cacheManager(final Map<Object, Object> store) {
        final Cache jcache = (Cache) Proxy.newProxyInstance(
                FormSessionFactoryColumnCacheRaceTest.class.getClassLoader(),
                new Class[]{Cache.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                switch (method.getName()) {
                    case "get":
                        return store.get(args[0]);
                    case "put":
                        store.put(args[0], args[1]);
                        return null;
                    case "remove":
                        return Boolean.valueOf(store.remove(args[0]) != null);
                    case "iterator":
                        java.util.List<Object> entries = new ArrayList<Object>();
                        for (Object key : new ArrayList<Object>(store.keySet())) {
                            entries.add(entry(store, key));
                        }
                        return new java.util.Iterator<Object>() {
                            private final java.util.Iterator<Object> delegate = entries.iterator();
                            private Object last;

                            @Override
                            public boolean hasNext() {
                                return delegate.hasNext();
                            }

                            @Override
                            public Object next() {
                                last = delegate.next();
                                return last;
                            }

                            @Override
                            public void remove() {
                                store.remove(((Cache.Entry) last).getKey());
                            }
                        };
                    default:
                        return null;
                }
            }
        });
        return (CacheManager) Proxy.newProxyInstance(
                FormSessionFactoryColumnCacheRaceTest.class.getClassLoader(),
                new Class[]{CacheManager.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return "getCache".equals(method.getName()) ? jcache : null;
            }
        });
    }
}
