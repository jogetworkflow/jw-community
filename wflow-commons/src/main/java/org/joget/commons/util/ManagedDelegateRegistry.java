package org.joget.commons.util;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * Generalizes {@link DynamicDataSourceManager#isManagedCache(Object)}/{@link
 * ManagedCacheRegistry}'s identity-guard pattern to arbitrary platform-owned delegate objects, not
 * just caches.
 *
 * {@code DirectoryManagerProxyImpl
 * .getDirectoryManagerImpl()} cannot itself be guarded the way other collaborator escapes are --
 * it is called internally by nearly every other method on that class for perfectly ordinary
 * directory operations (a plugin's routine {@code getUserList()} call reaches it), so blocking it
 * outright for non-admin callers would break ordinary directory access, not just the escape.
 * {@code DirectoryManagerImpl.setUserDao}/{@code setRoleDao} are the actual dangerous operations
 * (substituting the DAO an authentication path trusts), but the default {@code DirectoryManagerImpl}
 * delegate bean is Spring-XML-wired once at startup (safe to guard unconditionally, `active` is
 * false then), while the enterprise {@code SecureDirectoryManagerImpl} delegate is instead
 * constructed LAZILY, on first use, from inside {@code SecureDirectoryManager}'s own {@code
 * DirectoryManagerPlugin.getDirectoryManagerImpl(Map)} -- itself an OSGi plugin method, so its own
 * constructor's self-initializing {@code setUserDao}/{@code setRoleDao} calls run with a genuine
 * OSGi frame already on the stack. An unconditional guard would block that legitimate
 * self-construction the first time a non-admin tenant happens to be the caller that triggers it,
 * corrupting the shared static singleton for every tenant afterward.
 *
 * Registering each real delegate here only AFTER it is fully constructed and wired (see {@code
 * DirectoryManagerProxyImpl.setDefaultDirectoryManagerImpl(..)} and {@code SecureDirectoryManager
 * .getDirectoryManagerImpl(Map)}) means a guard keyed off {@link #isManaged(Object)} naturally
 * exempts a delegate's own constructor/self-init calls (not yet registered when they run) while
 * still blocking a later, external caller who obtained the same (now-registered) live reference.
 */
public final class ManagedDelegateRegistry {

    // Weakly-held, IDENTITY-compared entries -- not a strong IdentityHashMap, and not a plain
    // WeakHashMap<Object,Boolean> either.
    //
    // Not a strong map: unlike ManagedCacheRegistry's registered beans (Spring singletons, alive
    // for the whole webapp's lifetime), some registrants -- e.g. SyncLdapUserDirectoryManager
    // .getDirectoryManagerImpl(Map) -- construct a brand-new delegate on every call and register
    // it each time; with no way for those entries to ever be collected, ordinary LDAP traffic
    // would grow this set forever and eventually exhaust the heap.
    //
    // Not a plain WeakHashMap: it keys on the entry's own equals()/hashCode(). No current
    // registrant overrides either, but nothing enforces that on this public register(Object) API
    // -- a future registrant that does (over fields not yet assigned when it registers itself)
    // would become permanently unfindable, and a registry miss makes MultiTenantPluginAspect's
    // DAO-substitution guard fail OPEN, not deny. Worse, register(Object) is deliberately left
    // unguarded (any non-admin tenant can call it directly), so a hostile caller could supply a
    // crafted hashCode() to degrade lookups via attacker-influenced bucket placement -- something
    // IdentityHashMap's identity hash was never subject to.
    //
    // IdentityWeakRef below restores pure identity comparison (System.identityHashCode, captured
    // once at construction, never the registrant's own possibly-overridden hashCode()) while still
    // allowing GC once nothing else holds a strong reference.
    private static final class IdentityWeakRef extends WeakReference<Object> {
        private final int identityHash;

        IdentityWeakRef(Object referent) {
            super(referent);
            this.identityHash = System.identityHashCode(referent);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IdentityWeakRef)) {
                return false;
            }
            Object mine = get();
            return mine != null && mine == ((IdentityWeakRef) o).get();
        }

        @Override
        public int hashCode() {
            return identityHash;
        }
    }

    private static final Set<IdentityWeakRef> MANAGED_DELEGATES = new HashSet<>();

    private ManagedDelegateRegistry() {
    }

    /**
     * Registers a delegate as one this registry's callers should recognize. Safe to call more than
     * once for the same object.
     *
     * @param delegate the object to register; ignored when {@code null}
     */
    public static synchronized void register(Object delegate) {
        if (delegate != null) {
            expungeStale();
            MANAGED_DELEGATES.add(new IdentityWeakRef(delegate));
        }
    }

    /**
     * @param candidate the object to test
     * @return true if candidate is exactly one of this registry's registered delegates
     */
    public static synchronized boolean isManaged(Object candidate) {
        if (candidate == null) {
            return false;
        }
        expungeStale();
        return MANAGED_DELEGATES.contains(new IdentityWeakRef(candidate));
    }

    // HashSet (unlike WeakHashMap) does not expunge collected entries on its own -- without this,
    // the IdentityWeakRef wrapper objects themselves (empty husks once their referent is
    // collected) would accumulate without bound, reintroducing a smaller-scale version of the
    // exact leak this class exists to avoid. Called from both entry points under the same
    // monitor, so this never races with itself.
    private static void expungeStale() {
        MANAGED_DELEGATES.removeIf(ref -> ref.get() == null);
    }
}
