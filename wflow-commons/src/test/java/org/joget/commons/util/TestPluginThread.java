package org.joget.commons.util;

import java.lang.reflect.Field;
import java.util.Map;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestPluginThread {

    @After
    public void resetProfile() {
        // HostManager's profile ThreadLocals are inheritable and this JVM fork's
        // thread is reused across tests; don't let the injected profile leak out.
        HostManager.resetProfile();
        HostManager.setCurrentProfile(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Thread> activeThreads() throws Exception {
        Field registryField = PluginThread.class.getDeclaredField("ACTIVE_THREADS");
        registryField.setAccessible(true);
        return (Map<String, Thread>) registryField.get(null);
    }

    /**
     * GitLab #2310: a PluginThread registers itself in the static
     * ACTIVE_THREADS map on construction, but only unregistered from the
     * finally block wrapping super.run(). If setup that used to run before
     * that try/finally throws, the entry (and the Thread) was leaked forever.
     */
    @Test
    public void testActiveThreadsCleanedUpWhenPreRunSetupThrows() throws Exception {
        // Give DynamicDataSourceManager.getCurrentProfile() a value directly
        // from HostManager, so the constructor doesn't need the cache
        // DynamicDataSourceManager only wires up via a live Spring context.
        HostManager.setCurrentProfile("validprofile");

        PluginThread thread = new PluginThread(() -> { });
        Map<String, Thread> activeThreads = activeThreads();
        try {
            Field profileField = PluginThread.class.getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(thread, "invalid profile with spaces");

            assertTrue("thread should be registered in ACTIVE_THREADS after construction",
                    activeThreads.containsKey(thread.getName()));

            try {
                thread.run();
                fail("expected setProfile() to throw for an invalid profile value");
            } catch (IllegalArgumentException expected) {
                // HostManager.setCurrentProfile() rejects the injected value
            }

            assertFalse("PluginThread must not leak its ACTIVE_THREADS entry when pre-run setup throws",
                    activeThreads.containsKey(thread.getName()));
        } finally {
            activeThreads.remove(thread.getName());
        }
    }

    /**
     * GitLab #2310, Finding 1: getAsyncExecutorService() (and
     * AppServiceImpl#startProcessMigrationThread) rename the Thread after
     * construction. ACTIVE_THREADS must be keyed by the name captured at
     * registration time, not by a fresh getName() call in the finally block
     * — otherwise a completely normal (non-throwing) run permanently leaks
     * the entry under the original name.
     */
    @Test
    public void testActiveThreadsCleanedUpAfterThreadIsRenamed() throws Exception {
        HostManager.setCurrentProfile("validprofile");

        PluginThread thread = new PluginThread(() -> { });
        String originalName = thread.getName();
        Map<String, Thread> activeThreads = activeThreads();
        try {
            thread.setName("AsyncExecutor-" + originalName);

            assertTrue("thread should still be registered under its original constructor-time name",
                    activeThreads.containsKey(originalName));

            thread.run();

            assertFalse("PluginThread must not leak its ACTIVE_THREADS entry under the original name "
                    + "when the caller renames the thread after construction",
                    activeThreads.containsKey(originalName));
            assertFalse("the renamed key must not be left behind in ACTIVE_THREADS either",
                    activeThreads.containsKey(thread.getName()));
        } finally {
            activeThreads.remove(originalName);
            activeThreads.remove(thread.getName());
        }
    }
}
