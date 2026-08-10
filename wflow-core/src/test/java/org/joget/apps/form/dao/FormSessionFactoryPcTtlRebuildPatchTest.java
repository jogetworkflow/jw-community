package org.joget.apps.form.dao;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.Cache;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Benchmarks the fix proposed in anis.yusra's 2026-09-09 review comment on MR !1248
 * (<a href="https://gitlab.joget.org/joget/jw-community/-/issues/2085#note_48496">note_48496</a>):
 * changing {@code findSessionFactory}'s disk-reload entry condition from {@code if (pc == null)}
 * to {@code if (pc == null && sf == null)}.
 *
 * <p>That comment's root-cause read checks out independently (formPersistentClassCache has a fixed
 * 120s TTL while formSessionFactoryCache is eternal, per {@code wflow-commons/.../ehcache.xml}'s
 * {@code defaultCache} - a node's {@code sf} can go stale while its {@code pc} cache separately
 * expires and reloads a mapping file another node already updated, so a "no change" verdict against
 * the fresh {@code pc} tells you nothing about whether {@code sf} itself was ever rebuilt). But the
 * comment also claims the fix "only forces a rebuild in the one case actually proven unsafe", unlike
 * the "cruder alternative" of unconditionally rebuilding on every {@code PersistentClass} cache
 * expiry - which it says would reintroduce #7709.
 *
 * <p>This drives the REAL, current {@code findSessionFactory} and a byte-for-byte copy of it with
 * ONLY that one condition changed ({@link PatchedDao}), against the identical scenario: {@code sf}
 * warm and still correct, {@code pc} cache missed (standing in for a TTL expiry), and the on-disk
 * mapping matches the current, authoritative column set exactly - i.e. nothing actually changed.
 */
public class FormSessionFactoryPcTtlRebuildPatchTest {

    private static final String TABLE = "app_fd_T2085RACE";
    private static final int CALLS = 10;

    @Test
    public void currentCode_repeatedPcCacheMissWithWarmUnchangedSf_neverRebuilds() {
        AtomicInteger rebuildCount = new AtomicInteger();
        SessionFactory warmSf = warmSessionFactory();
        FormDataDaoImpl dao = wire(new UnpatchedDao(), warmSf, rebuildCount);
        // warmSf's own provenance: it really was built from field1/field2, matching the disk
        // fixture - i.e. the "nothing has actually changed" premise this test is exercising.
        // Without this, the current (fixed) code's own new decoupling check fails closed on
        // unknown provenance, which would defeat the point of this specific test.
        recordSessionFactoryProvenance(dao, "ttl-sf-key:" + TABLE, warmSf, mapping("field1", "field2"));

        for (int i = 0; i < CALLS; i++) {
            SessionFactory result = dao.findSessionFactory(TABLE, TABLE, null, FormDataDaoImpl.ACTION_TYPE_LOAD);
            assertSame("an sf that is still correct must be returned unrebuilt", warmSf, result);
        }

        assertEquals("current (as-merged) code must not rebuild when nothing actually changed, "
                + "even across repeated PersistentClass-cache misses", 0, rebuildCount.get());
    }

    @Test
    public void patchedCode_repeatedPcCacheMissWithWarmUnchangedSf_rebuildsEveryCall() {
        AtomicInteger rebuildCount = new AtomicInteger();
        SessionFactory warmSf = warmSessionFactory();
        FormDataDaoImpl dao = wire(new PatchedDao(), warmSf, rebuildCount);

        for (int i = 0; i < CALLS; i++) {
            dao.findSessionFactory(TABLE, TABLE, null, FormDataDaoImpl.ACTION_TYPE_LOAD);
        }

        assertEquals("the proposed `pc == null && sf == null` patch forces a full SessionFactory "
                + "rebuild on EVERY PersistentClass-cache miss, even when nothing changed - i.e. for "
                + "any table whose sf is warm when its pc entry hits its 120s TTL (the routine case "
                + "for an actively-used table), this IS the 'rebuild every ~120s' cost the comment "
                + "says it avoids, not just 'the one case actually proven unsafe'", CALLS, rebuildCount.get());
    }

    // ------------------------------------------------------------------ fixture

    private SessionFactory warmSessionFactory() {
        SessionFactory sf = mock(SessionFactory.class);
        when(sf.isClosed()).thenReturn(false);
        return sf;
    }

    /** Wires the common scaffolding: warm sf cache, always-cold pc cache, and a rebuild counter. */
    private FormDataDaoImpl wire(FormDataDaoImpl dao, SessionFactory warmSf, AtomicInteger rebuildCount) {
        Cache sfCache = mock(Cache.class);
        when(sfCache.get(anyString())).thenReturn(warmSf);
        inject(dao, "formSessionFactoryCache", sfCache);

        // Always a miss: models formPersistentClassCache's 120s TTL having expired again on every
        // call, independently of whatever formSessionFactoryCache is doing.
        Cache pcCache = mock(Cache.class);
        when(pcCache.get(anyString())).thenReturn(null);
        inject(dao, "formPersistentClassCache", pcCache);

        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(java.util.Collections.emptyIterator());
        inject(dao, "joinFormSessionFactoryCache", joinCache);

        FormDataDaoImpl spyDao = spy(dao);
        doAnswer(inv -> {
            rebuildCount.incrementAndGet();
            return mock(SessionFactory.class);
        }).when(spyDao).createSessionFactoryWithSingleFlight(
                anyString(), anyString(), any(), anyInt(), anyString());
        return spyDao;
    }

    /** A minimal mock PersistentClass exposing only the customProperties column names used here. */
    private PersistentClass mapping(String... columns) {
        PersistentClass pc = mock(PersistentClass.class);
        java.util.List<Property> props = new java.util.ArrayList<Property>();
        for (String c : columns) {
            Property p = mock(Property.class);
            when(p.getName()).thenReturn(c);
            props.add(p);
        }
        Component component = mock(Component.class);
        when(component.getPropertySpan()).thenReturn(props.size());
        when(component.getProperties()).thenReturn(props);
        Property custom = mock(Property.class);
        when(custom.getValue()).thenReturn(component);
        when(pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES)).thenReturn(custom);
        when(pc.getProperty(FormUtil.PROPERTY_CREATED_BY)).thenReturn(mock(Property.class));
        return pc;
    }

    @SuppressWarnings("unchecked")
    private static void recordSessionFactoryProvenance(FormDataDaoImpl dao, String cacheKey, SessionFactory sf, PersistentClass builtFromPc) {
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

    private static void inject(FormDataDaoImpl dao, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = FormDataDaoImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dao, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject " + fieldName, e);
        }
    }

    /** Shared scaffolding for both variants: a real, parseable on-disk mapping that matches the
     *  authoritative column set exactly, so neither variant has a genuine change to detect. */
    private static class BaseDao extends FormDataDaoImpl {
        @Override
        protected String getFormMappingPath() {
            return "src/test/resources/issue2085race/";
        }

        @Override
        protected MetadataSources createMetadataSources() {
            // The production override reaches Spring for a live Hibernate Configuration; none of
            // that exists in this unit test, and none of it is needed just to parse the on-disk
            // .hbm.xml back into a PersistentClass. A dialect still has to be nameable.
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
            builder.applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            return new MetadataSources(builder.build());
        }

        @Override
        protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
            return "ttl-sf-key:" + entityName;
        }

        @Override
        protected String getPersistentClassCacheKey(String entityName) {
            return "ttl-pc-key:" + entityName;
        }

        @Override
        public Collection<String> getFormDefinitionColumnNames(String tableName) {
            // Matches src/test/resources/issue2085race/app_fd_T2085RACE.hbm.xml exactly (field1,
            // field2) - the routine "authoritative truth agrees with the disk mapping" case, as
            // opposed to the already-fixed missing/unparseable-mapping case.
            return Arrays.asList("field1", "field2");
        }
    }

    /** The current, unmodified findSessionFactory - inherited as-is. */
    private static class UnpatchedDao extends BaseDao {
    }

    /**
     * A byte-for-byte copy of {@code FormDataDaoImpl.findSessionFactory} (as of MR !1248 commit
     * {@code bf031dbb}) with ONLY the one-line change from note_48496 applied: the disk-reload entry
     * condition on line ~994 is {@code if (pc == null && sf == null)} instead of {@code if (pc == null)}.
     * Every other line is unchanged.
     */
    private static class PatchedDao extends BaseDao {
        @Override
        protected SessionFactory findSessionFactory(String entityName, String tableName, FormRowSet rowSet, int actionType) throws HibernateException {
            PersistentClass pc = null;

            String cacheKey = getSessionFactoryCacheKey(entityName, tableName, rowSet);
            SessionFactory sf = (SessionFactory) formSessionFactoryCacheField().get(cacheKey);

            if (actionType == ACTION_TYPE_LOAD) {
                if (sf != null) {
                    pc = (PersistentClass) formPersistentClassCacheField().get(getPersistentClassCacheKey(entityName));
                }

                Metadata metadata = null;
                if (pc == null && sf == null) { // <<< the proposed patch, note_48496
                    try {
                        String path = getFormMappingPath();
                        String filename = entityName + ".hbm.xml";
                        File mappingFile = new File(path, filename);
                        if (mappingFile.exists()) {
                            MetadataSources metadataSources = createMetadataSources();
                            metadataSources.addFile(mappingFile);
                            metadata = metadataSources.buildMetadata();

                            pc = metadata.getEntityBinding(entityName);
                            if (pc != null) {
                                formPersistentClassCacheField().put(getPersistentClassCacheKey(entityName), pc);
                            }
                        }
                    } catch (MappingException me) {
                        // pc stays null; regenerated below via the guarded single-flight rebuild path.
                    }
                }
                if (pc != null) {
                    boolean changes = false;

                    if (!tableName.equals(pc.getTable().getName())) {
                        changes = true;
                    }

                    String cacheAccessStrategy = pc.getCacheConcurrencyStrategy();
                    if (!"transactional".equals(cacheAccessStrategy)) {
                        changes = true;
                    }

                    if (!changes) {
                        Collection<String> formFields = getFormDefinitionColumnNames(tableName);
                        changes = hasMappingChanged(pc, formFields);
                    }

                    if (changes) {
                        if (sf != null && !sf.isClosed()) {
                            sf.close();
                        }
                        sf = null;
                        formPersistentClassCacheField().remove(getPersistentClassCacheKey(entityName));
                        clearJoinCache(entityName);
                    } else if (metadata != null && sf == null) {
                        sf = getSessionFactory(entityName, cacheKey, actionType, metadata, false);
                    }
                } else if (sf != null) {
                    if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " PersistentClass could not be resolved; treating cached session factory as unverifiable");
                    }

                    if (!sf.isClosed()) {
                        sf.close();
                    }
                    sf = null;
                    clearJoinCache(entityName);
                }
            }

            if (sf == null || sf.isClosed()) {
                sf = createSessionFactoryWithSingleFlight(entityName, tableName, rowSet, actionType, cacheKey);
            }
            return sf;
        }

        private Cache formSessionFactoryCacheField() {
            return (Cache) readField("formSessionFactoryCache");
        }

        private Cache formPersistentClassCacheField() {
            return (Cache) readField("formPersistentClassCache");
        }

        private Object readField(String name) {
            try {
                java.lang.reflect.Field f = FormDataDaoImpl.class.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
