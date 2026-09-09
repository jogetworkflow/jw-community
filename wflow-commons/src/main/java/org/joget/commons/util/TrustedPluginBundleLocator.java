package org.joget.commons.util;

/**
 * Answers whether a {@link Class} was loaded from a plugin bundle installed from a deployment's
 * own shared, admin-controlled plugin location -- as opposed to a tenant's own uploaded plugin
 * directory -- in editions where that distinction exists and is tracked.
 *
 * <p>{@code wflow-commons} has no OSGi/bundle-management code of its own; the real answer (which
 * lives in the multi-tenant plugin manager) is Cloud-specific and not something this module can
 * depend on directly (Cloud modules are not a dependency of the on-prem Enterprise/Community
 * builds this module is shared with). This interface exists so a Cloud-specific implementation can
 * register itself as a Spring bean and be looked up dynamically, by type, from code (like SSO
 * programmatic-login trust checks) that needs this answer but must not force a compile-time
 * dependency on Cloud-only modules -- see {@code EnterpriseJogetSecurityContextImpl}'s own usage
 * for the concrete case this was built for. No implementation is registered at all in an edition
 * with no such distinction (e.g. on-prem, single-tenant); callers must treat "no bean found" as
 * "this question does not apply here", not as a yes or a no.
 */
public interface TrustedPluginBundleLocator {

    /**
     * @param clazz the class to check; must not be assumed non-null by implementations
     * @return {@code true} only if {@code clazz} was loaded by a plugin bundle that was installed
     *         from this deployment's own shared, admin-controlled plugin location -- verified
     *         against a record locked in at that bundle's install time, not re-derived from
     *         mutable state the bundle's own code could have altered since. {@code false} for
     *         {@code null}, for a class not loaded from any such bundle, or for one whose owning
     *         bundle was installed from anywhere else (including a tenant's own upload directory).
     */
    boolean isFromTrustedSharedLocation(Class<?> clazz);
}
