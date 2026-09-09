package org.joget.commons.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * #2261 H1 (ported from 9.1, with a materially different root cause on this line -- see {@code
 * PlatformCacheRegistrar}'s own {@code READ_SENSITIVE_BEAN_NAMES} javadoc for the full 8.2-vs-9.1
 * architecture difference): {@code nonceCache}/{@code formPersistentClassCache}
 * (appsApplicationContext.xml) were never registered with {@link ManagedCacheRegistry} by bean
 * name on this line either. Drives {@code onApplicationEvent(..)} directly against a minimal fake
 * {@code ApplicationContext} (a JDK dynamic proxy needing only {@code getBean(String)}) rather
 * than standing up real Spring wiring just to prove this registration call happens -- the same
 * pattern the 9.1 line's own equivalent test uses.
 */
public class TestPlatformCacheRegistrar {

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

    private static Cache newFixtureCache(String cacheName) {
        CacheManager cacheManager = CacheManager.create();
        if (cacheManager.getCache(cacheName) == null) {
            Cache cache = new Cache(cacheName, 100, false, false, 300, 300);
            cacheManager.addCache(cache);
        }
        return cacheManager.getCache(cacheName);
    }

    @Test
    public void testUnrelatedBeanIsNotRegistered() {
        Cache unrelated = newFixtureCache("probeUnrelatedCache-2261-F01-82");
        ApplicationContext context = fakeContextWithBeans(
                java.util.Collections.singletonMap("someOtherBeanEntirely", (Object) unrelated));

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertFalse("a cache never handed to PlatformCacheRegistrar as one of its own "
                        + "platform bean names must not become managed as a side effect",
                ManagedCacheRegistry.isManaged(unrelated));
    }

    /**
     * #2261 H1: {@code nonceCache} and {@code formPersistentClassCache} each get their OWN,
     * distinct, separately-named cache region on this line (unlike 9.1's shared {@code "default"}
     * cache -- see this class's own header javadoc) -- two DIFFERENT fixture cache names below,
     * matching that shape, prove registration keys on the bean object handed to
     * {@code getBean(..)}, not on any single shared underlying cache name.
     */
    @Test
    public void testNonceCacheAndFormPersistentClassCacheBeansAreRegisteredReadSensitive() {
        Cache nonceCache = newFixtureCache("probeNonceCache-2261-F01-82");
        Cache formPersistentClassCache = newFixtureCache("probeFormPersistentClassCache-2261-F01-82");

        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("nonceCache", nonceCache);
        beans.put("formPersistentClassCache", formPersistentClassCache);
        ApplicationContext context = fakeContextWithBeans(beans);

        new PlatformCacheRegistrar().onApplicationEvent(new ContextRefreshedEvent(context));

        Assert.assertTrue("the nonceCache bean must be registered as managed",
                ManagedCacheRegistry.isManaged(nonceCache));
        Assert.assertTrue("the nonceCache bean must be registered read-sensitive -- it wraps this "
                        + "line's real dataEncryption bean's own nonce cache",
                ManagedCacheRegistry.isReadSensitive(nonceCache));
        Assert.assertTrue("the formPersistentClassCache bean must be registered as managed",
                ManagedCacheRegistry.isManaged(formPersistentClassCache));
        Assert.assertTrue("the formPersistentClassCache bean must be registered read-sensitive -- "
                        + "it holds Hibernate PersistentClass metadata, and (before this fix) was "
                        + "the ONLY registration this bean ever got, since formDataDao's own "
                        + "property pointing at it was misconfigured (see appsApplicationContext.xml)",
                ManagedCacheRegistry.isReadSensitive(formPersistentClassCache));

        Cache unrelatedFixture = newFixtureCache("probeUnrelatedAfter-2261-F01-82");
        Assert.assertFalse("an unrelated cache, never itself handed to this registrar, must stay "
                        + "unmanaged as a side effect of this registration",
                ManagedCacheRegistry.isManaged(unrelatedFixture));
    }
}
