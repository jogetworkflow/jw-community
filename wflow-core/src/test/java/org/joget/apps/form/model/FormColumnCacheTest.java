package org.joget.apps.form.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LongTermCache;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The cache-state decisions that used to live inside {@code FormDataDaoImpl} as static maps:
 * throttled database verification of a warm entry, and the lifecycle of the incremental rebuild
 * base. They are tested here rather than through the DAO because the DAO mocks this class, so a
 * DAO-level test could only assert which method was called, never what it does.
 */
public class FormColumnCacheTest {

    private static final String TABLE = "t2085";

    private LongTermCache longTermCache;
    private FormColumnCache cache;

    @Before
    public void setUp() {
        // getCacheKey resolves the tenant through DynamicDataSourceManager, which dereferences a
        // static datasource cache only Spring populates unless HostManager already has a profile.
        HostManager.setCurrentProfile("test");
        longTermCache = mock(LongTermCache.class);
        cache = new FormColumnCache();
        cache.setCache(longTermCache);
    }

    @After
    public void tearDown() {
        HostManager.setCurrentProfile(null);
    }

    // ------------------------------------------------------------------ verified reads

    /**
     * The throttle is what keeps the database read off the hot path: a datalist request asks for
     * the column set O(columns x joined tables) times, and an unthrottled check would turn each of
     * those into a round trip.
     *
     * <p>Falsifiable: make {@code isVerificationDue} return true unconditionally and the count
     * comes back at 25.</p>
     */
    @Test
    public void aWarmEntryIsVerifiedAgainstTheDatabaseAtMostOncePerInterval() {
        Collection<String> warm = new HashSet<String>();
        warm.add("title");
        when(longTermCache.getObject(anyString())).thenReturn(warm);
        when(longTermCache.getObjectFresh(anyString())).thenReturn(warm);

        for (int i = 0; i < 25; i++) {
            assertNotNull(cache.getVerified(TABLE));
        }

        // The interval is 1s and the loop takes well under that, so exactly one may reach the db.
        verify(longTermCache, times(1)).getObjectFresh(anyString());
        verify(longTermCache, times(25)).getObject(anyString());
    }

    /** An absent entry needs no verification — there is nothing a database read could correct. */
    @Test
    public void aColdEntryIsNotVerified() {
        when(longTermCache.getObject(anyString())).thenReturn(null);

        assertNull(cache.getVerified(TABLE));

        verify(longTermCache, never()).getObjectFresh(anyString());
    }

    /** The verified read is what decides: a warm entry the database condemns must not be served. */
    @Test
    public void anEntryTheDatabaseSaysWasInvalidatedIsNotServed() {
        Collection<String> stale = new HashSet<String>();
        stale.add("title");
        when(longTermCache.getObject(anyString())).thenReturn(stale);
        when(longTermCache.getObjectFresh(anyString())).thenReturn(null);

        assertNull("a warm entry another node invalidated must not be served", cache.getVerified(TABLE));
    }

    // ------------------------------------------------------------------ incremental base

    /**
     * A NORMAL invalidation must KEEP the base — that is the whole point of the feature.
     *
     * <p>Learned the hard way: coupling the base reset to every {@code remove()} made every form
     * save a full rebuild, taking the 1000-definition edit case from 7.8 ms back to 274 ms. The
     * measurement caught it; the unit tests did not, so this pins it.</p>
     */
    @Test
    public void removeKeepsTheBase() {
        cache.putBase(TABLE, base(), null);

        cache.remove(TABLE);

        assertNotNull("an ordinary invalidation must leave the base for the next rebuild to reuse",
                cache.getBase(TABLE));
    }

    /**
     * A removal must DROP the base. The rebuild is additive, so evicting the entry alone would
     * re-derive the same union from the surviving base and the departed columns would never go.
     */
    @Test
    public void resetDropsTheBase() {
        cache.putBase(TABLE, base(), null);

        cache.reset(TABLE);

        assertNull(cache.getBase(TABLE));
    }

    @Test
    public void clearDropsEveryBaseOfTheCurrentTenant() {
        cache.putBase(TABLE, base(), null);
        cache.putBase("other", base(), null);

        cache.clear();

        assertNull(cache.getBase(TABLE));
        assertNull(cache.getBase("other"));
    }

    /** One tenant clearing its cache must not cost every other tenant a full rebuild. */
    @Test
    public void clearLeavesOtherTenantsBasesAlone() {
        HostManager.setCurrentProfile("otherTenant");
        cache.putBase(TABLE, base(), null);

        HostManager.setCurrentProfile("test");
        cache.clear();

        HostManager.setCurrentProfile("otherTenant");
        assertNotNull("clearing one tenant must not discard another tenant's base",
                cache.getBase(TABLE));
    }

    /**
     * A build already in flight must not resurrect a base the operator just discarded.
     *
     * <p>Otherwise "clear this table's cache" — the only route back to an exact column set once
     * additive drift has accumulated — is silently undone by whichever request happened to be
     * rebuilding at the time.</p>
     */
    @Test
    public void aStoreFromABuildThatStartedBeforeAResetIsDiscarded() {
        Date buildStartedEarlier = new Date(System.currentTimeMillis() - 5000L);

        cache.reset(TABLE);
        cache.putBase(TABLE, base(), buildStartedEarlier);

        assertNull("a build that started before the reset must not restore the base",
                cache.getBase(TABLE));
    }

    /** The mirror: a build that started AFTER the reset is the current truth and must be kept. */
    @Test
    public void aStoreFromABuildThatStartedAfterAResetIsKept() {
        cache.reset(TABLE);
        Date buildStartedLater = new Date(System.currentTimeMillis() + 5000L);

        cache.putBase(TABLE, base(), buildStartedLater);

        assertNotNull(cache.getBase(TABLE));
    }

    /** The base and the columns it was derived from must never be scoped to different tenants. */
    @Test
    public void theBaseIsScopedToTheSameTenantAsTheColumns() {
        cache.putBase(TABLE, base(), null);

        HostManager.setCurrentProfile("otherTenant");
        try {
            assertNull("another tenant must not see this tenant's base", cache.getBase(TABLE));
        } finally {
            HostManager.setCurrentProfile("test");
        }
        assertNotNull(cache.getBase(TABLE));
    }

    /** The prefixed and unprefixed table names are the same table, so they share one base. */
    @Test
    public void theTablePrefixIsStrippedBeforeKeying() {
        cache.putBase("app_fd_" + TABLE, base(), null);

        assertNotNull(cache.getBase(TABLE));
    }

    // ------------------------------------------------------------------ expiry

    @Test
    public void aBaseExpiresOnceItHasRunTooManyIncrementalBuilds() {
        FormColumnCache.ColumnBase fresh = new FormColumnCache.ColumnBase(
                new HashMap<String, Long>(), new HashSet<String>(), new HashMap<String, String>(),
                System.currentTimeMillis(), 0);
        FormColumnCache.ColumnBase worn = new FormColumnCache.ColumnBase(
                new HashMap<String, Long>(), new HashSet<String>(), new HashMap<String, String>(),
                System.currentTimeMillis(), FormColumnCache.BASE_MAX_INCREMENTAL_BUILDS);

        assertFalse(fresh.isExhausted());
        assertTrue(worn.isExhausted());
    }

    @Test
    public void aBaseExpiresOnceItIsTooOld() {
        FormColumnCache.ColumnBase old = new FormColumnCache.ColumnBase(
                new HashMap<String, Long>(), new HashSet<String>(), new HashMap<String, String>(),
                System.currentTimeMillis() - FormColumnCache.BASE_MAX_AGE_MS - 1, 0);

        assertTrue(old.isExhausted());
    }

    private FormColumnCache.ColumnBase base() {
        return new FormColumnCache.ColumnBase(new HashMap<String, Long>(), new HashSet<String>(),
                new HashMap<String, String>(), System.currentTimeMillis(), 0);
    }
}
