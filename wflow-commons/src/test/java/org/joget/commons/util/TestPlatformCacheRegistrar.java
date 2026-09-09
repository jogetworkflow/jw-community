package org.joget.commons.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * dynamicDataSourceManagerCache was never in {@code
 * PlatformCacheRegistrar}'s own bean-name list, so neither of the guards' two identity checks
 * (this class's own {@code ManagedCacheRegistry.isManaged(..)}, nor {@code
 * DynamicDataSourceManager.isManagedCache(..)}, which recognizes only the raw, unwrapped {@code
 * javax.cache.Cache} a DIFFERENT object) ever matched a caller reaching this bean's {@code
 * net.sf.ehcache.Cache} wrapper directly -- see {@code PlatformCacheRegistrar}'s own updated
 * javadoc for the full reasoning. Drives {@code onApplicationEvent(..)} directly against a
 * minimal fake {@code ApplicationContext} (a JDK dynamic proxy, not a full Spring context --
 * this class's own logic needs only {@code getBean(String)}) rather than standing up real Spring
 * wiring just to prove this one registration call happens.
 */
public class TestPlatformCacheRegistrar {

    private static ApplicationContext fakeContextWithOneBean(final String beanName, final Object bean) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getBean".equals(method.getName()) && args != null && args.length == 1) {
                    if (beanName.equals(args[0])) {
                        return bean;
                    }
                    throw new org.springframework.beans.factory.NoSuchBeanDefinitionException((String) args[0]);
                }
                throw new UnsupportedOperationException(method.getName() + " not stubbed");
            }
        };
        return (ApplicationContext) Proxy.newProxyInstance(
                ApplicationContext.class.getClassLoader(), new Class[]{ApplicationContext.class}, handler);
    }

    private static ApplicationContext fakeContextWithBeans(final Map<String, Object> beans) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getBean".equals(method.getName()) && args != null && args.length == 1) {
                    if (beans.containsKey(args[0])) {
                        return beans.get(args[0]);
                    }
                    throw new org.springframework.beans.factory.NoSuchBeanDefinitionException((String) args[0]);
                }
                throw new UnsupportedOperationException(method.getName() + " not stubbed");
            }
        };
        return (ApplicationContext) Proxy.newProxyInstance(
                ApplicationContext.class.getClassLoader(), new Class[]{ApplicationContext.class}, handler);
    }

    private static net.sf.ehcache.Cache newFixtureCache(String cacheName) {
        javax.cache.CacheManager jsrManager = javax.cache.Caching.getCachingProvider().getCacheManager();
        javax.cache.Cache jsrCache = jsrManager.getCache(cacheName);
        if (jsrCache == null) {
            jsrCache = jsrManager.createCache(cacheName,
                    new javax.cache.configuration.MutableConfiguration<Object, Object>());
        }
        return new net.sf.ehcache.Cache(jsrCache);
    }

    @Test
    public void testDynamicDataSourceManagerCacheBeanIsRegisteredReadSensitive() {
        net.sf.ehcache.Cache cache = newFixtureCache("probeDynamicDataSourceManagerCache-2261-F01");
        ApplicationContext context = fakeContextWithOneBean("dynamicDataSourceManagerCache", cache);

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertTrue("the dynamicDataSourceManagerCache bean must be registered as managed",
                ManagedCacheRegistry.isManaged(cache));
        Assert.assertTrue("the dynamicDataSourceManagerCache bean must be registered read-sensitive "
                        + "-- it holds every tenant's live datasource Properties, so a single-key "
                        + "get() exposing another tenant's entry is as real a concern as a mutation",
                ManagedCacheRegistry.isReadSensitive(cache));
    }

    @Test
    public void testUnrelatedBeanIsNotRegistered() {
        net.sf.ehcache.Cache unrelated = newFixtureCache("probeUnrelatedCache-2261-F01");
        // A context with no dynamicDataSourceManagerCache bean at all -- every lookup throws,
        // matching PlatformCacheRegistrar's own "bean not present in this context" handling.
        ApplicationContext context = fakeContextWithOneBean("someOtherBeanEntirely", unrelated);

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertFalse("a cache never handed to PlatformCacheRegistrar as one of its own "
                        + "platform bean names must not become managed as a side effect",
                ManagedCacheRegistry.isManaged(unrelated));
    }

    /**
     * #2261 H1: {@code formPersistentClassCache} (commonsApplicationContext.xml) was an {@code
     * EhCacheFactoryBean} with no {@code cacheName} property, so it wrapped the shared {@code
     * "default"} cache -- the same cache {@code FormDataDaoImpl}/{@code SecureDataEncryptionImpl}
     * separately wrap and register their OWN instance of. Since the guards match by object
     * identity, the bean object {@code getBean("formPersistentClassCache")} returns was never
     * registered until now. The fixture cache below deliberately shares the SAME underlying cache
     * name ({@code "default"}, matching the real bean's actual default) to mirror that shape,
     * while still being a DIFFERENT Java object from any other wrapper of {@code "default"} --
     * proving registration is keyed on the object handed to {@code getBean(..)}, not on the
     * underlying cache name.
     *
     * <p>{@code nonceCache} is deliberately NOT covered by this test: it is no longer registered
     * at all (see {@code PlatformCacheRegistrar}'s own {@code READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES}
     * javadoc for why -- registering it the same way would break a real, legitimate OIDC
     * consumer). {@link #testNonceCacheBeanIsNotRegistered()} below covers it instead.
     */
    @Test
    public void testFormPersistentClassCacheBeanIsRegisteredReadSensitive() {
        net.sf.ehcache.Cache formPersistentClassCache = newFixtureCache("default");

        Map<String, Object> beans = new java.util.HashMap<String, Object>();
        beans.put("formPersistentClassCache", formPersistentClassCache);
        ApplicationContext context = fakeContextWithBeans(beans);

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertTrue("the formPersistentClassCache bean must be registered as managed",
                ManagedCacheRegistry.isManaged(formPersistentClassCache));
        Assert.assertTrue("the formPersistentClassCache bean must be registered read-sensitive",
                ManagedCacheRegistry.isReadSensitive(formPersistentClassCache));

        // A caller reaching FormDataDaoImpl's OWN, separately-constructed "default" wrapper
        // (a second distinct object, never handed to this test's fake context) must be unaffected
        // by this registration -- proving it is genuinely per-object, not a global "default" rule.
        net.sf.ehcache.Cache formDataDaoOwnWrapper = newFixtureCache("default");
        Assert.assertFalse("an unrelated wrapper of the same underlying \"default\" cache name, "
                        + "never itself registered, must stay unmanaged -- registration is per "
                        + "object identity, not per underlying cache name",
                ManagedCacheRegistry.isManaged(formDataDaoOwnWrapper));
    }

    /**
     * #2261 H1: {@code nonceCache} is deliberately NOT registered with {@link ManagedCacheRegistry}
     * -- a real, legitimate consumer ({@code OpenIDDirectoryManager}, an OIDC {@code
     * DirectoryManager} plugin run per-tenant) calls {@code getBean("nonceCache")
     * .put(..)/.get(..)} directly with a plain, opaque OAuth {@code state} token as the key, never
     * profile-prefixed, so {@code registerReadSensitive} (which has no per-key exemption at all)
     * would deny every one of its calls outright, breaking OIDC login for every non-admin tenant.
     * Instead the underlying exposure is closed by giving {@code nonceCache} its own dedicated
     * cache region ({@code org.joget.cache.NONCE_CACHE}, commonsApplicationContext.xml), separate
     * from the secret-holding {@code "default"} cache {@code formPersistentClassCache} above still
     * shares wrappers of -- see {@code PlatformCacheRegistrar}'s own
     * {@code READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES} javadoc for the full reasoning and the
     * accepted, disclosed residual risk (bulk enumeration of nonceCache's own, now-separate
     * region). This test exists so a future "helpfully" re-adding nonceCache to that set breaks
     * loudly here first, not silently in production OIDC logins.
     *
     * <p>Also proves the separate, live-verification-found fix alongside it: {@code
     * onApplicationEvent(..)} must still call {@code getBean("nonceCache")} at least once (forcing
     * the {@code lazy-init="true"} bean to be created here, in the system/admin context this
     * listener always runs in), even though the result is never registered. Live-confirmed
     * (2026-09-26, a real 2-tenant deployment) that skipping this touch entirely reopens the exact
     * "first caller wins" race {@code formPersistentClassCache} is already protected against: a
     * non-admin tenant being the FIRST request ever to touch this lazy bean got a raw {@code
     * BeanCreationException} wrapping {@code "SecurityException: No permission to access
     * org.ehcache.jsr107.Eh107CacheManager"} (the shared {@code cacheManager} bean's own {@code
     * getCache(..)} is itself guarded) -- a broken OIDC login, not a clean, expected denial.
     */
    @Test
    public void testNonceCacheBeanIsNotRegisteredButIsEagerlyTouched() {
        final net.sf.ehcache.Cache nonceCache = newFixtureCache("org.joget.cache.NONCE_CACHE-2261-F01");
        final java.util.concurrent.atomic.AtomicInteger nonceCacheLookups = new java.util.concurrent.atomic.AtomicInteger(0);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getBean".equals(method.getName()) && args != null && args.length == 1) {
                    if ("nonceCache".equals(args[0])) {
                        nonceCacheLookups.incrementAndGet();
                        return nonceCache;
                    }
                    throw new org.springframework.beans.factory.NoSuchBeanDefinitionException((String) args[0]);
                }
                throw new UnsupportedOperationException(method.getName() + " not stubbed");
            }
        };
        ApplicationContext context = (ApplicationContext) Proxy.newProxyInstance(
                ApplicationContext.class.getClassLoader(), new Class[]{ApplicationContext.class}, handler);

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertTrue("onApplicationEvent(..) must call getBean(\"nonceCache\") at least once, "
                        + "forcing this lazy-init bean's creation in the system/admin context this "
                        + "listener runs in -- otherwise a non-admin tenant can be the unlucky first "
                        + "caller and get a raw BeanCreationException instead of a clean denial "
                        + "(live-confirmed) -- was called: " + nonceCacheLookups.get() + " time(s)",
                nonceCacheLookups.get() >= 1);
        Assert.assertFalse("nonceCache must NOT be registered with ManagedCacheRegistry -- doing "
                        + "so would deny OpenIDDirectoryManager's own legitimate, per-tenant "
                        + "put(..)/get(..) calls outright, since a plain opaque OAuth state token "
                        + "has no KeyScope that could ever recognize it as the caller's own key",
                ManagedCacheRegistry.isManaged(nonceCache));
    }
}
