package org.joget.commons.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * #2261 (A4, external security review): a transparent, per-profile key-scoping decorator over the
 * shared {@code org.joget.cache.NONCE_CACHE} region.
 *
 * <p>The region is deliberately left unregistered with {@link ManagedCacheRegistry} because its one
 * legitimate consumer -- the OIDC directory-manager plugin -- keys entries by an opaque OAuth
 * {@code state} token that no {@link ManagedCacheRegistry.KeyScope} could recognise as
 * "the caller's own". Without scoping, any non-admin tenant that resolves {@code getBean("nonceCache")}
 * can enumerate, bulk-evict or plant other tenants' pending {@code state -> nonce} entries --
 * cross-tenant disruption of, and CSRF/fixation against, other tenants' OIDC logins.
 *
 * <p>This decorator prepends {@code <currentProfile>_} to every key transparently and scopes every
 * bulk operation ({@code iterator}/{@code spliterator}/{@code forEach}/{@code removeAll}/{@code clear})
 * to the caller's profile. The OIDC plugin keeps using its opaque {@code state} keys unchanged.
 *
 * <p>The sole consumer only calls {@link #put(Element)} and {@link #get(Object)}. Every other
 * mutator/reader that would otherwise reach the shared backing is either scoped here or, where scoping
 * it safely would need fragile value-equality logic and it has no consumer, refused outright
 * (fail-closed) -- in particular {@link #unwrap(Class)} (which would hand back the raw, unscoped
 * backing) and {@link #close()} (which would tear down the region for every tenant). This closes the
 * bypasses that scoping only get/put/remove would leave open.
 *
 * <p>Note the shim's own backward-compat {@code put(Element)}/{@code remove(Serializable)} touch its
 * private backing field directly (not via virtual dispatch), so they are overridden here explicitly;
 * {@code get(Serializable)} routes through the virtual {@code get(Object)} and so is covered by that
 * override.
 */
public class ProfileScopedNonceCache extends Cache {

    // Default lifetime for a scoped nonce entry. The NONCE_CACHE region's DynamicExpiryPolicy derives
    // the TTL from the stored value's DynamicCacheElement.getExpiry(); a plain net.sf.ehcache.Element
    // is not a DynamicCacheElement, so the policy fails and NOTHING is cached (which silently breaks
    // the OIDC plugin's plain-Element puts). Wrapping the value in a DynamicCacheElement here keeps
    // the plugin working unchanged. 600s matches the sibling MFA_NONCE region; the OIDC plugin owner
    // may tune it.
    private static final long NONCE_TTL_SECONDS = 600L;

    public ProfileScopedNonceCache(javax.cache.Cache cache) {
        super(cache);
    }

    // The separator MUST be a character that cannot appear in a profile name; otherwise the bulk
    // startsWith(prefix) filters below would leak across tenants whose names share a prefix (e.g.
    // profile "acme" vs "acme_corp" -- profile names can contain '_'). NUL cannot appear in a Joget
    // profile name (they map to datasource/schema/host names), so "acme\0" never matches "acme_corp\0".
    private static final char SEP = '\u0000';

    // #2261 (Sol review finding, round 14): a call on a thread with no profile explicitly set
    // (e.g. a shared worker/thread-pool thread that has not gone through the per-request profile
    // filter) is NOT an error case for DynamicDataSourceManager.getCurrentProfile() -- it silently
    // resolves a fallback/default profile (via the hostname/context-path/CURRENT_PROFILE_KEY chain
    // in its own source) and MEMOIZES that resolution onto the current thread for the rest of its
    // life (HostManager.setCurrentProfileWithoutTracking(..)). Using that method here meant an
    // unset-profile caller silently got scoped to the DEFAULT/ADMIN profile's own nonce entries
    // instead of being refused -- a real tenant-boundary violation, not merely a theoretical one,
    // for exactly this cache's own threat model (cross-tenant OIDC nonce exposure). Reading
    // HostManager.getCurrentProfile() directly bypasses that fallback/memoization entirely: it
    // answers only the profile actually set for THIS thread by the per-request filter, null
    // otherwise. Failing closed (throwing, not silently defaulting to "") when it is null, empty,
    // or itself contains SEP (which would let a profile string collide with the separator-based
    // scoping this method exists to enforce) closes this off.
    private static String prefix() {
        String p = HostManager.getCurrentProfile();
        if (p == null || p.trim().isEmpty() || p.indexOf(SEP) >= 0) {
            throw new SecurityException("nonceCache: no valid ambient profile for this thread; refusing to scope");
        }
        return p + SEP;
    }

    private static String scoped(Object key) {
        return prefix() + String.valueOf(key);
    }

    private static Object wrap(Object value) {
        return new DynamicCacheElement(value, NONCE_TTL_SECONDS);
    }

    private static Object unwrapValue(Object v) {
        return (v instanceof DynamicCacheElement) ? ((DynamicCacheElement) v).getValue() : v;
    }

    private static UnsupportedOperationException denied(String op) {
        // Fail-closed: the sole consumer (OIDC plugin) does not use this method, and scoping it safely
        // is not worth the risk, so it is refused rather than left delegating to the shared backing.
        return new UnsupportedOperationException("nonceCache: " + op + " is not supported on the profile-scoped nonce cache");
    }

    // NOTE: every super.* call below passes the key as Object, not String. The shim declares both
    // get(Serializable)/remove(Serializable) and get(Object)/remove(Object); a String argument binds
    // to the Serializable overload, whose body re-dispatches through the virtual Object overload --
    // i.e. back into these overrides -- causing infinite recursion. Casting to Object selects the
    // shim's Object overload, which talks to the backing cache directly.

    // --- single-key reads: scope the key, unwrap the value ---
    @Override
    public Object get(Object k) {
        return unwrapValue(super.get((Object) scoped(k)));
    }

    @Override
    public boolean containsKey(Object k) {
        return super.containsKey((Object) scoped(k));
    }

    // --- single-key writes: scope the key, wrap the value ---
    @Override
    public void put(Element e) {
        super.put((Object) scoped(e.getKey()), wrap(e));
    }

    @Override
    public void put(Object k, Object v) {
        super.put((Object) scoped(k), wrap(v));
    }

    // --- single-key removes: scope the key ---
    @Override
    public boolean remove(Object k) {
        return super.remove((Object) scoped(k));
    }

    @Override
    public boolean remove(Serializable key) {
        return super.remove((Object) scoped(key));
    }

    @Override
    public Object getAndRemove(Object k) {
        return unwrapValue(super.getAndRemove((Object) scoped(k)));
    }

    // --- bulk operations: scope to the current profile so they cannot touch other tenants ---
    @Override
    public Iterator iterator() {
        return scopedIterator();
    }

    @Override
    public java.util.Spliterator spliterator() {
        return java.util.Spliterators.spliteratorUnknownSize(scopedIterator(), 0);
    }

    @Override
    public void forEach(Consumer action) {
        Iterator it = scopedIterator();
        while (it.hasNext()) {
            action.accept(it.next());
        }
    }

    @Override
    public void removeAll() {
        for (Serializable key : ownKeys()) {
            super.remove((Object) key);
        }
    }

    @Override
    public void clear() {
        // clear() on the shared backing would wipe every tenant's entries; scope it to the caller's.
        removeAll();
    }

    // --- refused (fail-closed): no consumer, and each is a cross-tenant escape if delegated ---
    @Override
    public Object unwrap(Class type) {
        throw denied("unwrap");
    }

    @Override
    public void close() {
        throw denied("close");
    }

    // #2261 (Sol review finding, round 14): getCacheManager()/getConfiguration(..) were not
    // overridden, so both silently delegated to the shared, UNSCOPED backing (inherited from
    // Cache). getCacheManager() hands back the raw javax.cache.CacheManager, from which
    // cacheManager.getCache("org.joget.cache.NONCE_CACHE") reaches every tenant's entries directly
    // -- the exact raw-backing escape the other fail-closed overrides above exist to prevent, just
    // reached one call earlier. getConfiguration(..) hands back the backing's own
    // Eh107Configuration, from which the raw CacheRuntimeConfiguration is unwrappable and a cache
    // entry listener can be registered directly on the SHARED (unscoped) region -- receiving every
    // tenant's keys/values, bypassing the scoping this class exists to enforce entirely.
    @Override
    public javax.cache.CacheManager getCacheManager() {
        throw denied("getCacheManager");
    }

    @Override
    public javax.cache.configuration.Configuration getConfiguration(Class type) {
        throw denied("getConfiguration");
    }

    @Override
    public java.util.Map getAll(Set set) {
        throw denied("getAll");
    }

    @Override
    public void putAll(java.util.Map map) {
        throw denied("putAll");
    }

    @Override
    public boolean putIfAbsent(Object k, Object v) {
        throw denied("putIfAbsent");
    }

    @Override
    public Object getAndPut(Object k, Object v) {
        throw denied("getAndPut");
    }

    @Override
    public boolean remove(Object k, Object v) {
        throw denied("remove(key,value)");
    }

    @Override
    public boolean replace(Object k, Object v, Object v1) {
        throw denied("replace(key,old,new)");
    }

    @Override
    public boolean replace(Object k, Object v) {
        throw denied("replace(key,value)");
    }

    @Override
    public Object getAndReplace(Object k, Object v) {
        throw denied("getAndReplace");
    }

    @Override
    public void removeAll(Set set) {
        throw denied("removeAll(keys)");
    }

    @Override
    public Object invoke(Object k, javax.cache.processor.EntryProcessor ep, Object... os) {
        throw denied("invoke");
    }

    @Override
    public java.util.Map invokeAll(Set set, javax.cache.processor.EntryProcessor ep, Object... os) {
        throw denied("invokeAll");
    }

    @Override
    public void loadAll(Set set, boolean replaceExistingValues, javax.cache.integration.CompletionListener cl) {
        throw denied("loadAll");
    }

    @Override
    public void registerCacheEntryListener(javax.cache.configuration.CacheEntryListenerConfiguration celc) {
        throw denied("registerCacheEntryListener");
    }

    @Override
    public void deregisterCacheEntryListener(javax.cache.configuration.CacheEntryListenerConfiguration celc) {
        throw denied("deregisterCacheEntryListener");
    }

    // --- helpers ---

    /** An iterator over ONLY the current profile's entries (filtered by the key prefix). */
    private Iterator scopedIterator() {
        final String pfx = prefix();
        final Iterator base = super.iterator();
        return new Iterator() {
            private Object next;
            private boolean fetched;
            private boolean advance() {
                while (base.hasNext()) {
                    Object entry = base.next();
                    Object key = (entry instanceof javax.cache.Cache.Entry)
                            ? ((javax.cache.Cache.Entry) entry).getKey() : null;
                    if (key != null && key.toString().startsWith(pfx)) {
                        next = entry;
                        return true;
                    }
                }
                return false;
            }
            @Override
            public boolean hasNext() {
                if (!fetched) { fetched = advance(); }
                return fetched;
            }
            @Override
            public Object next() {
                if (!hasNext()) { throw new java.util.NoSuchElementException(); }
                fetched = false;
                return next;
            }
        };
    }

    /** The current profile's keys, collected up front so removal does not disturb the live iterator. */
    private List<Serializable> ownKeys() {
        final String pfx = prefix();
        List<Serializable> mine = new ArrayList<Serializable>();
        Iterator base = super.iterator();
        while (base.hasNext()) {
            Object entry = base.next();
            if (entry instanceof javax.cache.Cache.Entry) {
                Object key = ((javax.cache.Cache.Entry) entry).getKey();
                if (key != null && key.toString().startsWith(pfx) && key instanceof Serializable) {
                    mine.add((Serializable) key);
                }
            }
        }
        return mine;
    }
}
