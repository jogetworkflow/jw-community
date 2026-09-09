package org.joget.commons.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.Test;

/**
 * The profile-list memo must be dropped by this JVM's own provisioning calls: a settled, still-fresh
 * snapshot would otherwise keep answering with the old membership (the directory-mtime check only
 * covers changes made elsewhere), and the key-scope collision checks that consult the list decide
 * which cache keys a restricted caller may treat as its own.
 */
public class TestDynamicDataSourceManagerProfileListMemo {

    // Unique per JVM: the profiles directory is shared, so suites of two lines may run at once.
    private static final String PROFILE = "memoInvalidationProbe" + Long.toHexString(System.nanoTime());

    private static Object snapshot() throws Exception {
        java.lang.reflect.Field f = DynamicDataSourceManager.class.getDeclaredField("cachedProfileListSnapshot");
        f.setAccessible(true);
        return f.get(null);
    }

    /** Lists until the directory has settled past the racy-mtime slack and the listing is memoised. */
    private static void listUntilMemoised() throws Exception {
        for (int i = 0; i < 40 && snapshot() == null; i++) {
            Thread.sleep(500L);
            assertNotNull(DynamicDataSourceManager.getProfileList());
        }
        assertNotNull("sanity: a settled listing must have been memoised", snapshot());
    }

    @Test
    public void testCreateAndDeleteProfileDropTheMemo() throws Exception {
        File file = new File(SetupManager.getBaseSharedDirectory(),
                DynamicDataSourceManager.FILE_PREFIX + PROFILE + DynamicDataSourceManager.FILE_EXTENSION);
        file.delete(); // a leftover from an aborted run must not break this test
        assertFalse("fixture profile must not pre-exist", file.exists());
        DynamicDataSourceManager.invalidateProfileListCacheForTest();
        try {
            listUntilMemoised();

            assertTrue(DynamicDataSourceManager.createProfile(PROFILE));
            assertNull("createProfile must drop the memo", snapshot());
            assertTrue("the new profile must be listed at once",
                    DynamicDataSourceManager.getProfileList().contains(PROFILE));

            listUntilMemoised();
            DynamicDataSourceManager.deleteProfile(PROFILE);
            assertNull("deleteProfile must drop the memo", snapshot());
            assertFalse(DynamicDataSourceManager.getProfileList().contains(PROFILE));
        } finally {
            file.delete();
            DynamicDataSourceManager.invalidateProfileListCacheForTest();
        }
    }
}
