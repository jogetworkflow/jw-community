package org.joget.commons.util;

/**
 * Builds and recognizes cache keys scoped to the current tenant profile, for
 * {@link ManagedCacheRegistry.KeyScope} registrations. A cache owner builds every key it writes
 * via {@link #forCurrentProfile(String)}; {@link MultiTenantSecurityManager}'s guard (via
 * {@link #SCOPE}) recognizes a key it did not itself create by checking whether it belongs to the
 * CURRENTLY active profile -- exactly the "does this caller-supplied key belong to them" question
 * the guard needs, without the guard ever having to know or guess any individual cache's own key
 * shape.
 *
 * <p><b>Collision safety.</b> The original prefix-based exemption this class replaces
 * (documented in {@code MultiTenantPluginAspect}'s own now-superseded {@code
 * isSelfScopedPlatformCacheKeyMutation}) had to defend against one profile's name being a
 * separator-delimited PREFIX of another's (e.g. profile "acme" vs. "acme_dev") by rejecting any
 * profile whose own name contains the separator. This class closes that same class of collision
 * at the SOURCE instead of only at the recognition side: {@link #forCurrentProfile(String)}
 * returns {@code null} -- not a key -- whenever the current profile itself contains the
 * separator CHARACTER (not merely the two-character {@link #SEPARATOR} string -- a profile
 * containing a single {@code ':'}, e.g. {@code "acme:"}, would otherwise let
 * {@code "acme:" + "::" + k} == {@code "acme:::" + k} satisfy {@code isCurrentTenantKey(..)}'s
 * {@code startsWith("acme" + "::")} test for the UNRELATED profile {@code "acme"}, since
 * {@code "acme:::"} starts with {@code "acme::"}). Rejecting every {@code ':'}, not just the
 * doubled separator, means no profile name can ever contain a prefix of {@link #SEPARATOR} at
 * all, so two different profiles' scoped keys can never be confused by prefix ambiguity.
 */
public final class ProfileScopedCacheKey {

    private static final String SEPARATOR = "::";

    private ProfileScopedCacheKey() {
    }

    /**
     * @param localKey the cache owner's own, tenant-agnostic key content (never itself containing
     *                 tenant-identifying information -- that is this method's job to add)
     * @return {@code "<currentProfile>::<localKey>"}, or {@code null} if the current profile
     *         cannot be safely scoped (unset, empty, or itself containing {@code ':'}). A
     *         caller that gets {@code null} back must treat the cache as unavailable for this
     *         operation (e.g. compute the value without caching it) rather than fall back to an
     *         unscoped key -- an unscoped key would defeat the entire point of this class.
     */
    public static String forCurrentProfile(String localKey) {
        String profile = safeCurrentProfile();
        return profile == null ? null : profile + SEPARATOR + localKey;
    }

    /** Recognizes keys built by {@link #forCurrentProfile(String)} for the CURRENT profile. */
    public static final ManagedCacheRegistry.KeyScope SCOPE = new ManagedCacheRegistry.KeyScope() {
        @Override
        public boolean isCurrentTenantKey(Object key) {
            if (!(key instanceof String)) {
                return false;
            }
            String profile = safeCurrentProfile();
            return profile != null && ((String) key).startsWith(profile + SEPARATOR);
        }

        @Override
        public Object keyForCurrentTenant(Object localKey) {
            return (localKey instanceof String) ? forCurrentProfile((String) localKey) : null;
        }
    };

    // Uses HostManager.getCurrentProfile() -- the raw per-thread value, possibly null -- not
    // DynamicDataSourceManager.getCurrentProfile(), which falls back to the deployment's default
    // profile (typically admin) when unset. A pooled/scheduled worker thread that never set its
    // own profile must be treated as unscoped and rejected, not silently attributed to that
    // fallback profile.
    private static String safeCurrentProfile() {
        String profile = HostManager.getCurrentProfile();
        return (profile != null && !profile.isEmpty() && profile.indexOf(':') < 0) ? profile : null;
    }
}
