package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.commons.util.HostManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Guards {@code FormDefinitionDaoImpl.update} against forgetting the table a form was moved AWAY
 * from.
 *
 * <p>It evicted only {@code object.getTableName()}, which by then is the NEW name, so moving a form
 * from table A to table B left A still holding this form's columns.</p>
 *
 * <p>Severity, stated honestly: a SUPERSET cannot cause the #2085 failure — only a MISSING column
 * does that, and A is recomputed correctly at its next invalidation. It is not free either: the load
 * mapping emits a property per union column and {@code SchemaUpdate} physically adds it, so a
 * lingering surplus column becomes a permanent {@code longtext} on the table.</p>
 *
 * <p><b>Custom-table builder definitions are deliberately NOT covered here.</b> An earlier version
 * of this suite asserted that {@code BuilderDefinitionDaoImpl} must invalidate the column cache.
 * That was wrong: {@code CustomFormDataTableUtil} owns that responsibility by design, and it
 * discharges it in {@code createTableIndexes}. The only method there that does not clear —
 * {@code createTable} — has no caller in this tree, and no {@code CustomBuilder} plugin is
 * registered for {@code formdata_table}, so the generic builder endpoints cannot reach these
 * definitions either.</p>
 */
public class FormColumnCacheInvalidationTest {


    static {
        // AppDevUtil caches this in a static final at class-init, and the DAO paths under test call
        // into git persistence otherwise. Set before AppDevUtil is ever loaded.
        System.setProperty("git.disabled", "true");
    }

    private FormColumnCache formColumnCache;
    private AppDefCache appDefCache;
    private AppDefinitionDao appDefinitionDao;
    private SessionFactory sessionFactory;
    private Session session;
    private AppDefinition appDef;

    @Before
    public void setUp() throws Exception {
        // DynamicDataSourceManager.getCurrentProfile() returns immediately when HostManager has a
        // profile; without this it dereferences a static cache that only Spring populates.
        HostManager.setCurrentProfile("test");

        formColumnCache = mock(FormColumnCache.class);
        appDefCache = mock(AppDefCache.class);
        appDefinitionDao = mock(AppDefinitionDao.class);
        sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        when(sessionFactory.getCurrentSession()).thenReturn(session);

        appDef = mock(AppDefinition.class);
        when(appDef.getId()).thenReturn("app1");
        when(appDef.getAppId()).thenReturn("app1");
        when(appDef.getVersion()).thenReturn(1L);
        // clearDeadlineCache only reaches into Spring when a package definition exists.
        when(appDef.getPackageDefinition()).thenReturn(null);
    }

    @After
    public void tearDown() {
        // Static and thread-local: leaking it makes unrelated tests order-dependent.
        HostManager.setCurrentProfile(null);
    }

    // ------------------------------------------------------------------ GAP 2

    /**
     * A DAO whose session models Hibernate's AUTO-FLUSH, which is what makes the ordering testable.
     *
     * <p>A constant stub cannot: it hands back the old table name whenever it is asked, so moving
     * {@code loadPersistedTableName} to AFTER {@code super.update()} — deleting the actual fix —
     * would still pass. The real session does not behave that way. {@code super.update()} reaches
     * {@code session.merge(entityName, obj)} followed by {@code flush()}, after which any query in
     * that session sees the NEW value. So the stub flips once merge has been called, and reading the
     * previous name too late now yields the new name, exactly as in production.</p>
     *
     * @param persistedTableName what the database holds BEFORE this update; null means unresolvable
     * @param afterMerge         what a query would see once the merge has flushed
     */
    private FormDefinitionDaoImpl formDao(String persistedTableName, final String afterMerge) {
        FormDefinitionDaoImpl dao = new FormDefinitionDaoImpl();
        dao.setSessionFactory(sessionFactory);
        dao.setCache(appDefCache);
        dao.setFormColumnCache(formColumnCache);
        dao.appDefinitionDao = appDefinitionDao;

        final java.util.concurrent.atomic.AtomicBoolean merged =
                new java.util.concurrent.atomic.AtomicBoolean(false);
        doAnswer(inv -> {
            merged.set(true);
            return inv.getArgument(1);
        }).when(session).merge(anyString(), any());

        final String before = persistedTableName;
        Query q = mock(Query.class);
        when(q.list()).thenAnswer(inv -> {
            String visible = merged.get() ? afterMerge : before;
            return visible == null ? new ArrayList<String>() : (List) Arrays.asList(visible);
        });
        doReturn(q).when(session).createQuery(anyString());
        return dao;
    }

    /** The common case: the row still holds the old name until this update flushes. */
    private FormDefinitionDaoImpl formDao(String persistedTableName) {
        return formDao(persistedTableName, persistedTableName);
    }

    private FormDefinition formDef(String tableName) {
        FormDefinition def = mock(FormDefinition.class);
        when(def.getId()).thenReturn("f1");
        when(def.getTableName()).thenReturn(tableName);
        when(def.getJson()).thenReturn("{}");
        when(def.getAppId()).thenReturn("app1");
        when(def.getAppVersion()).thenReturn(1L);
        when(def.getAppDefinition()).thenReturn(appDef);
        return def;
    }

    /**
     * Retargeting a form from table A to table B must invalidate BOTH.
     *
     * <p>Red before the fix: only B was evicted, and A kept this form's columns until some unrelated
     * change happened to invalidate it.</p>
     *
     * <p>A gets {@code reset}, not {@code remove}: it LOST a definition, and the incremental
     * rebuild is additive, so evicting alone would re-derive the same union — including the moved
     * form's columns — from the surviving base.</p>
     *
     * <p><b>Also guards the ORDERING</b>, which is the whole fix. The session stub models auto-flush
     * (see {@link #formDao(String, String)}), so once the merge has run a query returns "tableB".
     * Reading the previous name after {@code super.update()} therefore resolves it to "tableB",
     * old-equals-new, and no reset is issued — red. Verified by moving the call.</p>
     */
    @Test
    public void retargetingAFormInvalidatesBothTheOldAndTheNewTable() {
        formDao("tableA", "tableB").update(formDef("tableB"));

        verify(formColumnCache, times(1)).remove("tableB");
        verify(formColumnCache, times(1)).reset("tableA");
    }

    /**
     * An ordinary edit that does not move the form must still evict exactly once. Guards against
     * "fixing" the retarget case by blindly evicting twice, which would double the invalidation
     * traffic — and every eviction writes a CACHE_LAST_CLEAR row that all nodes then re-read.
     */
    @Test
    public void anEditThatDoesNotRetargetEvictsExactlyOnce() {
        formDao("tableA").update(formDef("tableA"));

        verify(formColumnCache, times(1)).remove("tableA");
        verify(formColumnCache, times(1)).remove(anyString());
        // remove(), never reset(): dropping the incremental base on an ordinary edit would make
        // every form save a full column rebuild — 7.8 ms back to 274 ms at 1000 definitions.
        verify(formColumnCache, never()).reset(anyString());
    }

    /** If the previous name cannot be resolved, behaviour degrades to exactly what it was. */
    @Test
    public void anUnresolvablePreviousTableNameStillEvictsTheCurrentOne() {
        formDao(null).update(formDef("tableB"));

        verify(formColumnCache, times(1)).remove("tableB");
        verify(formColumnCache, times(1)).remove(anyString());
    }
}
