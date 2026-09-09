package org.joget.workflow.model.service;

import org.joget.directory.model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * GitLab #2261: {@code WorkflowUserManager.captureUsernameForPluginThreadPropagation()} is what
 * {@code PluginThread} (wflow-commons) calls to decide what identity, if any, to propagate into a
 * worker thread it spawns. Covers the one safety-critical branch this method adds: a system-scoped
 * caller must never have its identity propagated through this general-purpose mechanism (see that
 * method's own comment for why -- {@code getCurrentUsername()} would otherwise return the literal
 * sentinel string {@code "roleSystem"}, which a spawned worker would then treat as an ordinary,
 * non-existent directory username instead of "system").
 */
public class TestWorkflowUserManagerThreadPropagation {

    private final WorkflowUserManager manager = new WorkflowUserManager();

    @After
    public void tearDown() {
        manager.clearCurrentThreadUser();
    }

    @Test
    public void testCapturesOrdinaryUsername() {
        User user = new User();
        user.setUsername("someTenantUser");
        manager.setCurrentThreadUser(user);

        Assert.assertEquals("an ordinary (non-system) thread's username must be captured verbatim, "
                + "safe to restore later on a different thread",
                "someTenantUser", manager.captureUsernameForPluginThreadPropagation());
    }

    @Test
    public void testDoesNotCaptureSystemScopedIdentity() {
        manager.setSystemThreadUser(true);

        Assert.assertNull("a system-scoped thread's identity must NOT be captured for propagation "
                + "at all -- getCurrentUsername() would otherwise return the literal sentinel "
                + "string \"roleSystem\", which a spawned worker would wrongly treat as an "
                + "ordinary, non-existent directory username instead of \"system\"",
                manager.captureUsernameForPluginThreadPropagation());
    }
}
