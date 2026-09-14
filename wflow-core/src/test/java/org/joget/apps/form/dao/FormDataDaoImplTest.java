package org.joget.apps.form.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.cache.Cache;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import static org.junit.Assert.assertNotEquals;
import org.junit.BeforeClass;
import static org.mockito.Mockito.*;


public class FormDataDaoImplTest {

    private FormDataDaoImpl dao;
    @Mock
    private Cache formPersistentClassCache;
    
    private static Method getIndexNameMethod;
    private static int maxIndexNameLength;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dao = spy(new FormDataDaoImpl());
    }

    @Test
    public void testProcessQuery_ReservedKeywordHandlingSmallCaps() {

        String input = "SELECT e.customProperties.class, e.customProperties.null, e.customProperties.true, e.customProperties.false FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertTrue("Should add t__ prefix to reserved keyword 'class'", result.contains("e.customProperties.t__class"));
        assertTrue("Should add t__ prefix to reserved keyword 'null'", result.contains("e.customProperties.t__null"));
        assertTrue("Should add t__ prefix to reserved keyword 'true'", result.contains("e.customProperties.t__true"));
        assertTrue("Should add t__ prefix to reserved keyword 'false'", result.contains("e.customProperties.t__false "));
    }

    @Test
    public void testProcessQuery_ReservedKeywordHandlingBigCaps() {

        String input = "SELECT e.customProperties.CLASS, e.customProperties.NULL, e.customProperties.TRUE, e.customProperties.FALSE FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertTrue("Should add t__ prefix to reserved keyword 'CLASS'", result.contains("e.customProperties.t__CLASS"));
        assertTrue("Should add t__ prefix to reserved keyword 'NULL'", result.contains("e.customProperties.t__NULL"));
        assertTrue("Should add t__ prefix to reserved keyword 'TRUE'", result.contains("e.customProperties.t__TRUE"));
        assertTrue("Should add t__ prefix to reserved keyword 'FALSE'", result.contains("e.customProperties.t__FALSE"));
    }

    @Test
    public void testProcessQuery_DigitPrefixedFields() {

        String input = "SELECT e.customProperties.123field FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertEquals("Should add t__ prefix to digit-prefixed field",
                "SELECT e.customProperties.t__123field FROM app_fd_test e", result);
    }

    @Test
    public void testProcessQuery_LegacyCustomPropertyPlusZeroSortCast() {
        String input = "SELECT e FROM app_fd_test e WHERE 1=1 ORDER BY e.customProperties.sortNo  + 0";
        String result = dao.processQuery(input);

        assertEquals("SELECT e FROM app_fd_test e WHERE 1=1 ORDER BY cast(e.customProperties.sortNo as big_decimal)", result);
    }

    @Test
    public void testProcessQuery_LegacyColumnPrefixPlusZeroSortCast() {
        String condition = dao.replaceColumnNameWithPrefix("app_fd_test", "WHERE 1=1 ORDER BY c_sortNo  + 0");
        String result = dao.processQuery("SELECT e FROM app_fd_test e " + condition);

        assertEquals("SELECT e FROM app_fd_test e WHERE 1=1 ORDER BY cast(e.customProperties.sortNo as big_decimal)", result);
    }

    @Test
    public void testExtractAliasToTableMapping_SingleTables() {
        String query = "FROM app_fd_TEST_TABLE e";
        FormDataDaoImpl dao = new FormDataDaoImpl();
        Map<String, String> result = dao.extractAliasToTableMapping(query);

        assertTrue(result.containsKey("e"));
        assertEquals("app_fd_TEST_TABLE", result.get("e"));
        assertEquals(1, result.size());
    }

    @Test
    public void testExtractAliasToTableMapping_MultipleTables() {
        String query = "FROM app_fd_TABLE1 e, app_fd_TABLE2 AS TABLE2, app_fd_TABLE3 AS TABLE3";
        FormDataDaoImpl dao = new FormDataDaoImpl();
        Map<String, String> result = dao.extractAliasToTableMapping(query);

        assertEquals(3, result.size());
        assertEquals("app_fd_TABLE1", result.get("e"));
        assertEquals("app_fd_TABLE2", result.get("TABLE2"));
        assertEquals("app_fd_TABLE3", result.get("TABLE3"));
    }

    @Test
    public void testNormalizeCustomPropertiesForAlias_BasicCaseNormalization() {
        // Create mock PersistentClass with customProperties
        PersistentClass mockPC = createMockPersistentClass("FIELD2", "field1");

        // Mock cache to return our mock PersistentClass
        when(formPersistentClassCache.get(anyString())).thenReturn(mockPC);

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "src/test/resources/app_forms/";
            }

            @Override
            protected String getPersistentClassCacheKey(String tableName) {
                return "test_key_" + tableName;
            }
        };

        // Inject mocked cache via reflection or create a setter method
        injectCache(dao, formPersistentClassCache);

        String query = "SELECT e.customProperties.field2 FROM app_fd_TEST_TABLE e";
        String result = dao.normalizeCustomPropertiesForAlias(query, "e", "app_fd_TEST_TABLE");

        // Verify - field2 should be corrected to FIELD2 (exact case from hbm.xml)
        assertTrue(result.contains("e.customProperties.FIELD2"));
        assertFalse(result.contains("e.customProperties.field2"));
    }

    @Test
    public void testNormalizeCustomPropertiesForAlias_MultipleFieldNormalization() {
        // Create mock PersistentClass with customProperties
        PersistentClass mockPC = createMockPersistentClass("FIELD2", "field1");

        // Mock cache to return our mock PersistentClass
        when(formPersistentClassCache.get(anyString())).thenReturn(mockPC);

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "src/test/resources/app_forms/";
            }

            @Override
            protected String getPersistentClassCacheKey(String tableName) {
                return "test_key_" + tableName;
            }
        };

        // Inject mocked cache
        injectCache(dao, formPersistentClassCache);

        String query = "SELECT e.customProperties.field2, e.customProperties.FIELD1, e.id FROM app_fd_TEST_TABLE e WHERE e.customProperties.field1 = 'test'";
        String result = dao.normalizeCustomPropertiesForAlias(query, "e", "app_fd_TEST_TABLE");

        // Verify - field2 should be corrected to FIELD2, FIELD1 should remain FIELD1, field1 should be corrected to field1
        assertTrue(result.contains("e.customProperties.FIELD2"));  // corrected
        assertTrue(result.contains("e.customProperties.field1"));  // corrected in SELECT clause but already correct in WHERE clause

        // Ensure incorrect cases are no longer present
        assertFalse(result.contains("e.customProperties.field2"));
        assertFalse(result.contains("e.customProperties.FIELD1"));
    }

    private PersistentClass createMockPersistentClass(String... fieldNames) {
        PersistentClass mockPC = mock(PersistentClass.class);
        Property mockCustomProp = mock(Property.class);
        Component mockComponent = mock(Component.class);

        when(mockPC.getProperty("customProperties")).thenReturn(mockCustomProp);
        when(mockCustomProp.getValue()).thenReturn(mockComponent);

        // Create mock properties for each field
        Map<String, Property> mockProperties = new HashMap<>();
        for (String fieldName : fieldNames) {
            Property mockProp = mock(Property.class);
            when(mockProp.getName()).thenReturn(fieldName);
            mockProperties.put(fieldName, mockProp);
        }

        // Mock iterator for properties
        when(mockComponent.getProperties()).thenReturn(new ArrayList<>(mockProperties.values()));

        return mockPC;
    }

    private void injectCache(FormDataDaoImpl dao, Cache cache) {
        injectCache(dao, "formPersistentClassCache", cache);
    }

    private void injectCache(FormDataDaoImpl dao, String fieldName, Cache cache) {
        try {
            java.lang.reflect.Field field = FormDataDaoImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dao, cache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject cache", e);
        }
    }

    // ---------------------------------------------------------- findSessionFactory (GitLab #2085,
    // note_47650: pc/sf cache desync)
    //
    // formPersistentClassCache ("default" ehcache region) expires entries on a 120s TTL;
    // formSessionFactoryCache ("org.joget.cache.SF_CACHE") is eternal (<expiry><none/></expiry> in
    // ehcache.xml). So on a completely normal, single-node, no-memory-pressure deployment, every
    // ~120s a PersistentClass cache entry can expire while its SessionFactory sibling is still
    // cached and open. findSessionFactory only re-verified staleness inside `if (pc != null)`;
    // when pc could not be resolved AND the on-disk <entity>.hbm.xml fallback also missed
    // (deleted, wrong path, or being rewritten concurrently by another thread's
    // createSessionFactory), there used to be no `else` branch, so the method fell straight
    // through to `if (sf == null || sf.isClosed())`, found the cached sf open, and returned it
    // untouched — no staleness check, no rebuild. Fixed by treating an unresolvable pc the same
    // as a detected mapping change: close the cached sf and let it be rebuilt.
    @Test
    public void findSessionFactory_rebuildsSessionFactory_whenPersistentClassCacheMissAndMappingFileMissing() {
        SessionFactory staleSf = mock(SessionFactory.class);
        when(staleSf.isClosed()).thenReturn(false);
        SessionFactory rebuiltSf = mock(SessionFactory.class);

        Cache sfCache = mock(Cache.class);
        when(sfCache.get(anyString())).thenReturn(staleSf);

        Cache pcCache = mock(Cache.class);
        when(pcCache.get(anyString())).thenReturn(null);

        FormDataDaoImpl dao = spy(new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                // No <entity>.hbm.xml exists here: forces the disk fallback in
                // findSessionFactory to leave pc == null, exactly like a deleted/unreachable
                // mapping file or a read that lost a race with a concurrent rewrite.
                return "target/no-such-mapping-dir-2085/";
            }

            @Override
            protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
                // Avoid DynamicDataSourceManager's static profile cache, which is not
                // initialized outside a running Spring/Joget context.
                return "test-sf-key:" + entityName + ";" + tableName;
            }

            @Override
            protected String getPersistentClassCacheKey(String entityName) {
                return "test-pc-key:" + entityName;
            }
        });
        injectCache(dao, "formSessionFactoryCache", sfCache);
        injectCache(dao, "formPersistentClassCache", pcCache);
        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(java.util.Collections.emptyIterator());
        injectCache(dao, "joinFormSessionFactoryCache", joinCache);
        // Real rebuild goes through Hibernate/schema-update, which needs a live DB; stub it so
        // this test stays a pure unit test of findSessionFactory's own decision, not the rebuild.
        doReturn(rebuiltSf).when(dao).createSessionFactoryWithSingleFlight(
                anyString(), anyString(), any(), anyInt(), anyString());

        SessionFactory result = dao.findSessionFactory("t2085", "app_fd_t2085", null, FormDataDaoImpl.ACTION_TYPE_LOAD);

        verify(staleSf).close();
        assertEquals("an unresolvable PersistentClass must force the cached SessionFactory to be "
                + "rebuilt rather than silently reused unverified", rebuiltSf, result);
    }

    /** The ordinary "brand-new entity" first load: no cached sf yet, no mapping file yet either — must not be treated as the unverifiable-stale case above. */
    @Test
    public void findSessionFactory_createsSessionFactory_whenNoCachedFactoryAndNoMappingFileYet() {
        Cache sfCache = mock(Cache.class);
        when(sfCache.get(anyString())).thenReturn(null);

        Cache pcCache = mock(Cache.class);
        when(pcCache.get(anyString())).thenReturn(null);

        SessionFactory newSf = mock(SessionFactory.class);

        FormDataDaoImpl dao = spy(new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "target/no-such-mapping-dir-2085/";
            }

            @Override
            protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
                return "test-sf-key:" + entityName + ";" + tableName;
            }

            @Override
            protected String getPersistentClassCacheKey(String entityName) {
                return "test-pc-key:" + entityName;
            }
        });
        injectCache(dao, "formSessionFactoryCache", sfCache);
        injectCache(dao, "formPersistentClassCache", pcCache);
        doReturn(newSf).when(dao).createSessionFactoryWithSingleFlight(
                anyString(), anyString(), any(), anyInt(), anyString());

        SessionFactory result = dao.findSessionFactory("t2085new", "app_fd_t2085new", null, FormDataDaoImpl.ACTION_TYPE_LOAD);

        assertEquals(newSf, result);
    }

    // ------------------------------------------------- 2026-09-08 follow-up audit, Finding 2
    //
    // clearFormTableCache()'s mapping-file delete was completely unguarded, racing
    // createSessionFactory's OWN guarded write-then-immediate-reread of the same file
    // (XMLUtil.saveDocument, then metadataSources.addFile a few lines later, both inside the
    // FormBuildGuard "MAP:" lock). Confirmed live, against this project's actual Hibernate
    // 6.6.1.Final dependency (see the scratch probe referenced in PLANS.md), that:
    //  (a) MetadataSources.addFile(File) reads and parses the file EAGERLY and synchronously - a
    //      file deleted *after* addFile() returns cannot affect an already-built Metadata, so the
    //      real race window is only up to and including the addFile() call itself, not the whole
    //      write-then-buildMetadata() span;
    //  (b) a file missing at addFile() time throws org.hibernate.boot.MappingNotFoundException,
    //      which is a SIBLING of org.hibernate.boot.InvalidMappingException (both extend
    //      org.hibernate.boot.MappingException), not a subclass of it - so the pre-fix
    //      `catch (InvalidMappingException ime)` in findSessionFactory's disk-fallback read did
    //      NOT catch it, and it would propagate uncaught out of a request that should have just
    //      fallen back to a normal rebuild.
    //
    // Two independent, additive fixes, each pinned by its own test below:
    //  Finding 2b - findSessionFactory's disk-read fallback now catches org.hibernate.MappingException
    //      (the common superclass), not just InvalidMappingException, so a concurrently-deleted
    //      mapping file degrades to "pc stays null, rebuild" instead of throwing.
    //  Finding 2a - clearFormTableCache's delete now waits on the same FormBuildGuard "MAP:" lock
    //      createSessionFactoryWithSingleFlight holds, so it cannot land between that method's own
    //      write and its own immediate re-read of the same file.

    /**
     * Finding 2b: a mapping file that vanishes between findSessionFactory's {@code exists()} check
     * and its {@code addFile()} call must degrade to "unresolvable pc, fall back to rebuild" - not
     * propagate an uncaught {@code MappingNotFoundException}.
     *
     * <p>Uses a REAL {@link MetadataSources}/{@link StandardServiceRegistryBuilder} (no Hibernate
     * mocking) against a real temp {@code .hbm.xml} file, so the exact exception type thrown is
     * whatever Hibernate 6.6.1 itself actually throws, not an assumption about it. The race is made
     * deterministic (not timing-hopeful) by overriding {@code createMetadataSources()} - the real
     * method already does non-trivial work here (a Spring bean lookup, a fresh ServiceRegistry) - to
     * pause exactly between the {@code exists()} check and {@code addFile()}, so the test can delete
     * the file in that exact window on every run.</p>
     */
    @Test(timeout = 15000)
    public void findSessionFactory_fallsBackToRebuild_whenMappingFileDeletedBetweenExistsCheckAndRead() throws Exception {
        final java.io.File dir = java.nio.file.Files.createTempDirectory("form-dao-race-2085").toFile();
        final String entityName = "app_fd_racetest2085";
        final java.io.File mappingFile = new java.io.File(dir, entityName + ".hbm.xml");
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
                + "\"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">\n"
                + "<hibernate-mapping>\n"
                + "  <class name=\"org.joget.apps.form.model.FormRow\" entity-name=\"" + entityName
                + "\" table=\"" + entityName + "\">\n"
                + "    <id name=\"id\" column=\"id\" type=\"string\" length=\"25\"><generator class=\"assigned\"/></id>\n"
                + "  </class>\n"
                + "</hibernate-mapping>\n";
        java.nio.file.Files.write(mappingFile.toPath(), xml.getBytes(StandardCharsets.UTF_8));

        SessionFactory staleSf = mock(SessionFactory.class);
        when(staleSf.isClosed()).thenReturn(false);
        SessionFactory rebuiltSf = mock(SessionFactory.class);

        Cache sfCache = mock(Cache.class);
        when(sfCache.get(anyString())).thenReturn(staleSf);
        Cache pcCache = mock(Cache.class);
        when(pcCache.get(anyString())).thenReturn(null);

        final java.util.concurrent.CountDownLatch reachedCreateMetadataSources = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch fileDeleted = new java.util.concurrent.CountDownLatch(1);

        FormDataDaoImpl dao = spy(new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return dir.getPath() + java.io.File.separator;
            }

            @Override
            protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
                return "test-sf-key:" + entityName + ";" + tableName;
            }

            @Override
            protected String getPersistentClassCacheKey(String entityName) {
                return "test-pc-key:" + entityName;
            }

            @Override
            protected org.hibernate.boot.MetadataSources createMetadataSources() {
                // Real exists() check already passed by the time we get here. Pause right where
                // the real method spends real time (Spring bean lookup, fresh ServiceRegistry
                // build) so the test can delete the file in the exact window findSessionFactory's
                // own try/catch is meant to cover, deterministically instead of by luck.
                reachedCreateMetadataSources.countDown();
                try {
                    assertTrue("test never deleted the file", fileDeleted.await(10, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                org.hibernate.boot.registry.StandardServiceRegistryBuilder ssrb =
                        new org.hibernate.boot.registry.StandardServiceRegistryBuilder();
                ssrb.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                return new org.hibernate.boot.MetadataSources(ssrb.build());
            }
        });
        injectCache(dao, "formSessionFactoryCache", sfCache);
        injectCache(dao, "formPersistentClassCache", pcCache);
        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(java.util.Collections.emptyIterator());
        injectCache(dao, "joinFormSessionFactoryCache", joinCache);
        doReturn(rebuiltSf).when(dao).createSessionFactoryWithSingleFlight(
                anyString(), anyString(), any(), anyInt(), anyString());

        final java.util.concurrent.atomic.AtomicReference<SessionFactory> result = new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                result.set(dao.findSessionFactory(entityName, entityName, null, FormDataDaoImpl.ACTION_TYPE_LOAD));
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        worker.setDaemon(true);
        worker.start();

        assertTrue("worker never reached createMetadataSources()",
                reachedCreateMetadataSources.await(10, TimeUnit.SECONDS));
        // The exact race: delete the file AFTER exists() returned true but BEFORE addFile() (which
        // createMetadataSources() is currently blocking ahead of) actually opens it.
        assertTrue("setup: file must have existed to delete", mappingFile.delete());
        fileDeleted.countDown();
        worker.join(10000);

        if (failure.get() != null) {
            throw new AssertionError("findSessionFactory must not propagate an uncaught exception "
                    + "when the on-disk mapping file is deleted mid-read - it must fall back to "
                    + "rebuilding, exactly like the already-handled missing-file-from-the-start case",
                    failure.get());
        }
        assertEquals("must fall back to the guarded rebuild once the disk read is unresolvable",
                rebuiltSf, result.get());
        verify(staleSf).close();
    }

    /**
     * Finding 2a: {@code clearFormTableCache}'s mapping-file delete must wait for an in-flight,
     * lock-holding rebuild of the SAME table to finish, rather than deleting underneath it.
     *
     * <p>Simulates the holder side of {@code createSessionFactoryWithSingleFlight}'s own critical
     * section directly against the real {@link FormBuildGuard} - the same static striped-lock
     * utility production uses, not a mock or fake - taking the exact {@code "MAP:" + profile + ":"
     * + entityName} key that method takes. A concurrent {@code clearFormTableCache} call for the
     * same table must be observably still blocked while the holder keeps the lock, and must
     * complete (deleting the file) only once the holder releases it - proven with a latch rather
     * than a raw sleep-and-hope, so the assertion is evidence-based.</p>
     */
    @Test(timeout = 15000)
    public void clearFormTableCache_waitsForInFlightMappingRebuild_beforeDeletingTheMappingFile() throws Exception {
        final java.io.File dir = java.nio.file.Files.createTempDirectory("form-dao-race-2085b").toFile();
        final String unprefixed = "racetest2085b";
        final String entityName = FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + unprefixed;
        final java.io.File mappingFile = new java.io.File(dir, entityName + ".hbm.xml");
        java.nio.file.Files.write(mappingFile.toPath(), "<hibernate-mapping/>".getBytes(StandardCharsets.UTF_8));

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return dir.getPath() + java.io.File.separator;
            }

            @Override
            protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
                return "test-sf-key:" + entityName;
            }

            @Override
            protected String getPersistentClassCacheKey(String entityName) {
                return "test-pc-key:" + entityName;
            }
        };
        injectCache(dao, "formSessionFactoryCache", mock(Cache.class));
        injectCache(dao, "formPersistentClassCache", mock(Cache.class));
        org.joget.apps.form.model.FormColumnCache columnCache = mock(org.joget.apps.form.model.FormColumnCache.class);
        dao.setFormColumnCache(columnCache);
        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(java.util.Collections.emptyIterator());
        injectCache(dao, "joinFormSessionFactoryCache", joinCache);

        String key = "MAP:" + org.joget.commons.util.HostManager.getCurrentProfile() + ":" + entityName;
        final java.util.concurrent.CountDownLatch holderHasLock = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch releaseHolder = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch workerDone = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();

        Thread holder = new Thread(() -> {
            ReentrantLock lock = FormBuildGuard.tryLock(key, 20);
            try {
                holderHasLock.countDown();
                releaseHolder.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        });
        holder.setDaemon(true);
        holder.start();
        assertTrue("holder never acquired the MAP: lock", holderHasLock.await(10, TimeUnit.SECONDS));

        Thread worker = new Thread(() -> {
            try {
                dao.clearFormTableCache(unprefixed);
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                workerDone.countDown();
            }
        });
        worker.setDaemon(true);
        worker.start();

        // While the holder still has the lock, clearFormTableCache must NOT have finished (in
        // particular, must not yet have deleted the file) - a bounded, evidence-based check, not a
        // raw sleep-and-hope.
        assertFalse("clearFormTableCache must wait for the in-flight rebuild's lock, not delete "
                + "underneath it", workerDone.await(300, TimeUnit.MILLISECONDS));
        assertTrue("file must still exist while the rebuild holder has the lock", mappingFile.exists());

        releaseHolder.countDown();
        assertTrue("clearFormTableCache never completed after the lock was released",
                workerDone.await(10, TimeUnit.SECONDS));

        if (failure.get() != null) {
            throw new AssertionError("clearFormTableCache threw", failure.get());
        }
        assertFalse("file must be deleted once clearFormTableCache finally acquires the lock",
                mappingFile.exists());
    }

    /**
     * Finding 3: {@code createJoinSessionFactory} reads each joined entity's {@code .hbm.xml}
     * DIRECTLY off disk with no exception handling at all - not even the narrower
     * {@code InvalidMappingException} catch {@code findSessionFactory}'s own disk-fallback read
     * has. A mapping file missing at that exact moment (e.g. deleted or mid-rewrite by a
     * concurrent request for the SAME entity, racing between when {@code findJoinSessionFactory}'s
     * own per-entity {@code findSessionFactory} loop verified it and when this method later reads
     * it) threw a raw, uncaught {@code org.hibernate.boot.MappingNotFoundException} straight out of
     * the DAO - the same underlying mechanism as Finding 2, but for the join path, and with no
     * handling at all rather than a too-narrow one.
     *
     * <p>Uses a REAL {@link MetadataSources} against real files (no Hibernate mocking) - one entity
     * with a valid mapping file, one entity whose mapping file is simply absent, standing in for
     * "deleted by a concurrent request between verification and this read". Pins that the fix turns
     * that into a clear, explicit, retriable {@link HibernateException} naming the affected entity,
     * not an opaque internal Hibernate type leaking through the DAO's own abstraction boundary.</p>
     */
    @Test
    public void createJoinSessionFactory_throwsAClearRetriableException_whenAJoinedEntitysMappingFileIsMissing() throws Exception {
        final java.io.File dir = java.nio.file.Files.createTempDirectory("form-dao-race-2085c").toFile();
        final String presentEntity = "app_fd_racetest2085c_present";
        final String missingEntity = "app_fd_racetest2085c_missing"; // never written - stands in for "deleted mid-race"
        java.io.File presentFile = new java.io.File(dir, presentEntity + ".hbm.xml");
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
                + "\"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">\n"
                + "<hibernate-mapping>\n"
                + "  <class name=\"org.joget.apps.form.model.FormRow\" entity-name=\"" + presentEntity
                + "\" table=\"" + presentEntity + "\">\n"
                + "    <id name=\"id\" column=\"id\" type=\"string\" length=\"25\"><generator class=\"assigned\"/></id>\n"
                + "  </class>\n"
                + "</hibernate-mapping>\n";
        java.nio.file.Files.write(presentFile.toPath(), xml.getBytes(StandardCharsets.UTF_8));

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return dir.getPath() + java.io.File.separator;
            }

            @Override
            protected org.hibernate.boot.MetadataSources createMetadataSources() {
                org.hibernate.boot.registry.StandardServiceRegistryBuilder ssrb =
                        new org.hibernate.boot.registry.StandardServiceRegistryBuilder();
                ssrb.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                return new org.hibernate.boot.MetadataSources(ssrb.build());
            }
        };

        try {
            dao.createJoinSessionFactory(new String[]{presentEntity, missingEntity});
            fail("must throw when a joined entity's mapping file cannot be read");
        } catch (HibernateException e) {
            assertFalse("must not leak the raw internal Hibernate exception type through the DAO's "
                    + "own abstraction boundary", e instanceof org.hibernate.boot.MappingException);
            assertTrue("message must name the affected entity: " + e.getMessage(),
                    e.getMessage().contains(missingEntity));
            assertTrue("message must say this is retriable, matching this file's own established "
                    + "wording for other known-transient races: " + e.getMessage(),
                    e.getMessage().contains("Retry the request"));
        }
    }

    @Test
    public void testReplaceColumnNameWithPrefix_SpacePrefixedWithMixedCase() {
        FormDataDaoImpl dao = new FormDataDaoImpl();
        String input = "WHERE c_fieldName = 'value' AND C_otherField > 10";
        String expected = "WHERE e.customProperties.fieldName = 'value' AND e.customProperties.otherField > 10";
        String result = dao.replaceColumnNameWithPrefix("app_fd_test", input);
        assertEquals(expected, result);
    }

    @Test
    public void testReplaceColumnNameWithPrefix_NestedParenthesesWithMixedCase() {
        FormDataDaoImpl dao = new FormDataDaoImpl();
        String input = "WHERE ((c_field1 = 'value') AND (C_field2 > 5))";
        String expected = "WHERE ((e.customProperties.field1 = 'value') AND (e.customProperties.field2 > 5))";
        String result = dao.replaceColumnNameWithPrefix("app_fd_test", input);
        assertEquals(expected, result);
    }
    

    // ---------------------------------------------------------- getFormRowColumnNames (STORE side)

    /**
     * The STORE-side column rule must agree with the LOAD side, so both now read the SAME
     * constants off {@link FormColumnResolver}. Previously this method inlined its own copy of the
     * predefined-column list and the name pattern, which is exactly how the two sides drift: a
     * column the store path accepts but the load mapping does not carry is #2085.
     *
     * <p>Untested before this change, so these pin the behaviour the shared constants must keep.</p>
     */
    @Test
    public void getFormRowColumnNames_excludesPredefinedColumns() {
        Collection<String> result = dao.getFormRowColumnNames(rowSet(
                "id", "dateCreated", "dateModified", "createdBy", "createdByName",
                "modifiedBy", "modifiedByName", "title"));

        assertEquals("only the non-predefined column may survive: " + result, 1, result.size());
        assertTrue(result.contains("title"));
    }

    @Test
    public void getFormRowColumnNames_rejectsNamesThatCannotBeColumns() {
        Collection<String> result = dao.getFormRowColumnNames(rowSet("ok_name-1", "bad name", "bad;name", ""));

        assertEquals("only the pattern-valid name may survive: " + result, 1, result.size());
        assertTrue(result.contains("ok_name-1"));
    }

    @Test
    public void getFormRowColumnNames_collapsesCaseOnlyDuplicates() {
        Collection<String> result = dao.getFormRowColumnNames(rowSet("Title", "title"));

        assertEquals("a case-only duplicate must not produce two columns: " + result, 1, result.size());
    }

    /** A {@code c_}-prefixed key is the stored spelling of a column already present unprefixed. */
    @Test
    public void getFormRowColumnNames_skipsThePrefixedShadowOfAPresentColumn() {
        Collection<String> result = dao.getFormRowColumnNames(rowSet("title", "c_title"));

        assertEquals("the c_ shadow must not become a second column: " + result, 1, result.size());
        assertTrue(result.contains("title"));
    }

    private FormRowSet rowSet(String... columnNames) {
        FormRow row = new FormRow();
        for (String name : columnNames) {
            row.put(name, "v");
        }
        FormRowSet rowSet = new FormRowSet();
        rowSet.add(row);
        return rowSet;
    }
    

    @BeforeClass
    public static void setUpClass() throws Exception {
        getIndexNameMethod = FormDataDaoImpl.class.getDeclaredMethod("getIndexName", String.class, String.class);
        getIndexNameMethod.setAccessible(true);

        Field field = FormDataDaoImpl.class.getDeclaredField("MAX_INDEX_NAME_LENGTH");
        field.setAccessible(true);
        maxIndexNameLength = field.getInt(null);
    }

    @Test
    public void returnsCandidateUnchangedWhenWellWithinMaxLength() throws Exception {
        String result = invokeGetIndexName("users", "email");

        assertEquals("idx_users_email", result);
    }

    @Test
    public void returnsCandidateUnchangedWhenExactlyAtMaxLength() throws Exception {
        String tableName = repeat('t', 30);
        String columnName = repeat('c', maxIndexNameLength - 5 - 30);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertEquals(maxIndexNameLength, candidate.length());

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(candidate, result);
    }

    @Test
    public void hashesAndTruncatesWhenOneCharacterOverMaxLength() throws Exception {
        String tableName = repeat('t', 30);
        String columnName = repeat('c', maxIndexNameLength - 5 - 30 + 1);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertEquals(maxIndexNameLength + 1, candidate.length());

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(maxIndexNameLength, result.length());
        assertEquals(expectedHashedName(candidate), result);
    }

    @Test
    public void hashesAndTruncatesWhenCandidateFarExceedsMaxLength() throws Exception {
        String tableName = repeat('t', 40);
        String columnName = repeat('c', 40);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertTrue(candidate.length() > maxIndexNameLength);

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(maxIndexNameLength, result.length());
        assertEquals(expectedHashedName(candidate), result);
    }

    @Test
    public void isDeterministicForTheSameInputs() throws Exception {
        String tableName = repeat('a', 50);
        String columnName = repeat('b', 50);

        String first = invokeGetIndexName(tableName, columnName);
        String second = invokeGetIndexName(tableName, columnName);

        assertEquals(first, second);
    }

    @Test
    public void differentInputsProduceDifferentHashedNames() throws Exception {
        String tableName = repeat('a', 50);

        String result1 = invokeGetIndexName(tableName, repeat('b', 50));
        String result2 = invokeGetIndexName(tableName, repeat('c', 50));

        assertNotEquals(result1, result2);
    }

    private String expectedHashedName(String candidate) {
        String hash = UUID.nameUUIDFromBytes(candidate.getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "").substring(0, 12);
        int prefixLength = maxIndexNameLength - hash.length() - 1;
        return candidate.substring(0, prefixLength) + "_" + hash;
    }

    private String invokeGetIndexName(String tableName, String columnName) throws Exception {
        try {
            return (String) getIndexNameMethod.invoke(null, tableName, columnName);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
