package org.joget.commons.util;

/**
 * Minimal cross-module contract letting {@link PluginThread} (wflow-commons, the lowest-level
 * module) propagate the spawning thread's already-established user identity into a worker thread it
 * creates, without a compile-time dependency on {@code org.joget.workflow.model.service
 * .WorkflowUserManager} (wflow-directory, a higher-level module -- the correct dependency direction
 * is the reverse). {@code WorkflowUserManager} implements this interface; {@link PluginThread}
 * reaches the Spring-wired instance via {@link SecurityUtil#getApplicationContext()} (the same
 * low-level, already-established hook {@code LogUtil}/{@code FileStore} in this same package already
 * use to reach beans defined in higher modules).
 *
 * <p>GitLab #2261: added so a plugin using {@link PluginThread} to spawn a background worker (e.g.
 * {@code process_enhancement}'s {@code AssignmentFormActions}) gets identity propagation as a
 * platform default, instead of needing its own {@code setCurrentThreadUser(..)} call individually
 * whitelisted by fully-qualified class name -- a whack-a-mole approach that also required the
 * genuine plugin's jar to live in the shared {@code wflow/app_plugins/} directory (not a per-tenant
 * {@code app_profiles/<profile>/app_plugins/} one) to survive {@code MultiTenantPluginManager
 * .verifyTrustedClasses()}'s anti-shadowing check, silently uninstalling a non-admin tenant's own
 * legitimate Marketplace install of the same plugin.
 */
public interface ThreadUserIdentitySource {

    /**
     * Returns the current thread's username, safe to later restore verbatim on a different thread
     * via {@link #setCurrentThreadUser(String)} -- or {@code null} if there is nothing safe to
     * propagate this way. Deliberately returns {@code null} (not the raw {@code ROLE_SYSTEM}
     * sentinel) when the current thread is system-scoped: system privilege must never be inherited
     * by an ordinary plugin-spawned worker through this general-purpose mechanism.
     */
    String captureUsernameForPluginThreadPropagation();

    /**
     * Same method {@code WorkflowUserManager} already exposes publicly; declared here so a caller
     * holding only this interface reference can invoke it without a compile-time dependency on the
     * concrete class.
     */
    void setCurrentThreadUser(String username);

    /**
     * Same method {@code WorkflowUserManager} already exposes publicly; declared here so a caller
     * holding only this interface reference can invoke it without a compile-time dependency on the
     * concrete class.
     */
    void clearCurrentThreadUser();
}
