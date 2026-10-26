package org.joget.plugin.base;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that listing OSGi plugins by type weaves only the plugins of that type,
 * each at most once per cache generation, and never calls a plugin method on an
 * unwoven service object.
 */
public class TestPluginManagerOsgiWeaving {

    public interface TypeA extends Plugin {
    }

    public interface TypeB extends Plugin {
    }

    private StubPluginManager pluginManager;

    @Before
    public void setUp() {
        PluginManager.getCache().clearCache();
        pluginManager = new StubPluginManager();
    }

    @After
    public void tearDown() {
        PluginManager.getCache().clearCache();
    }

    @Test
    public void testListByTypeWeavesOnlyThatType() {
        Plugin a1 = service(TypeA.class, "a1");
        Plugin a2 = service(TypeA.class, "a2");
        Plugin b1 = service(TypeB.class, "b1");
        pluginManager.services.add(a1);
        pluginManager.services.add(b1);
        pluginManager.services.add(a2);

        Collection<Plugin> listA = pluginManager.list(TypeA.class);
        assertEquals(setOf("a1", "a2"), names(listA));
        assertEquals(2, pluginManager.weaveCount);
        assertEquals(1, pluginManager.servicesLoadCount);
        assertFalse(pluginManager.woven.contains(b1));

        // a different type of a cached profile weaves only its own plugins
        Collection<Plugin> listB = pluginManager.list(TypeB.class);
        assertEquals(setOf("b1"), names(listB));
        assertEquals(3, pluginManager.weaveCount);

        // listing everything reuses the woven instances instead of weaving again
        Collection<Plugin> all = pluginManager.listOsgiPlugin(null);
        assertEquals(setOf("a1", "a2", "b1"), names(all));
        assertEquals(3, pluginManager.weaveCount);
        assertEquals(1, pluginManager.servicesLoadCount);
        for (Plugin p : listA) {
            assertTrue(all.contains(p));
        }
    }

    @Test
    public void testFailedWeaveIsExcludedAndNotRetriedUntilCacheCleared() {
        Plugin a1 = service(TypeA.class, "a1");
        Plugin bad = service(TypeA.class, "bad");
        pluginManager.services.add(a1);
        pluginManager.services.add(bad);
        pluginManager.failing.add(bad);

        assertEquals(setOf("a1"), names(pluginManager.listOsgiPlugin(TypeA.class)));
        assertEquals(setOf("a1"), names(pluginManager.listOsgiPlugin(null)));
        assertEquals(2, pluginManager.weaveCount);

        pluginManager.clearCache();
        pluginManager.failing.clear();
        assertEquals(setOf("a1", "bad"), names(pluginManager.listOsgiPlugin(TypeA.class)));
        assertEquals(4, pluginManager.weaveCount);
        assertEquals(2, pluginManager.servicesLoadCount);
    }

    @Test
    public void testClearCacheReloadsServices() {
        Plugin a1 = service(TypeA.class, "a1");
        pluginManager.services.add(a1);
        assertEquals(setOf("a1"), names(pluginManager.listOsgiPlugin(TypeA.class)));

        pluginManager.services.add(service(TypeA.class, "a2"));
        assertEquals(setOf("a1"), names(pluginManager.listOsgiPlugin(TypeA.class)));

        pluginManager.clearCache();
        assertEquals(setOf("a1", "a2"), names(pluginManager.listOsgiPlugin(TypeA.class)));
    }

    @Test
    public void testNameCollisionResolvedWithinRequestedType() {
        // Same name in two different types: each type keeps its own plugin.
        pluginManager.services.add(service(TypeA.class, "dup"));
        pluginManager.services.add(service(TypeB.class, "dup"));

        Map<String, Plugin> mapA = pluginManager.loadPluginMap(TypeA.class);
        assertEquals(1, mapA.size());
        assertTrue(TypeA.class.isInstance(mapA.values().iterator().next()));
        Map<String, Plugin> mapB = pluginManager.loadPluginMap(TypeB.class);
        assertEquals(1, mapB.size());
        assertTrue(TypeB.class.isInstance(mapB.values().iterator().next()));
    }

    @Test
    public void testGetAllOsgiPluginsWeavesEverything() {
        pluginManager.services.add(service(TypeA.class, "a1"));
        pluginManager.services.add(service(TypeB.class, "b1"));
        Map<String, Plugin> all = pluginManager.getAllOsgiPlugins();
        assertEquals(setOf("a1", "b1"), all.keySet());
        assertEquals(2, pluginManager.weaveCount);
        assertNull(all.get("missing"));
    }

    @Test
    public void testWovenInstanceIsStableAcrossTypes() {
        Plugin a1 = service(TypeA.class, "a1");
        pluginManager.services.add(a1);
        Plugin first = pluginManager.listOsgiPlugin(TypeA.class).iterator().next();
        Plugin second = pluginManager.listOsgiPlugin(Plugin.class).iterator().next();
        assertSame(first, second);
    }

    @Test
    public void testSameNameWithinTypeLaterServiceWins() {
        Plugin first = service(TypeA.class, "dup");
        Plugin second = service(TypeA.class, "dup");
        pluginManager.services.add(first);
        pluginManager.services.add(second);

        Collection<Plugin> listA = pluginManager.list(TypeA.class);
        assertEquals(1, listA.size());
        assertSame(pluginManager.wovenOf.get(second), listA.iterator().next());
    }

    @Test
    public void testBlackListedPluginExcludedFromList() {
        pluginManager.services.add(service(TypeA.class, "a1"));
        pluginManager.services.add(service(TypeB.class, "b1"));
        Set<String> blackList = new HashSet<>();
        blackList.add(weave(service(TypeB.class, "probe")).getClass().getName());
        pluginManager.setBlackList(blackList);

        Set<String> listed = names(pluginManager.list(Plugin.class));
        assertTrue(listed.contains("a1"));
        assertFalse(listed.contains("b1"));
    }

    @Test
    public void testListReweavesAfterClearCache() {
        pluginManager.services.add(service(TypeA.class, "a1"));
        Plugin before = pluginManager.list(TypeA.class).iterator().next();
        assertEquals(1, pluginManager.weaveCount);

        pluginManager.clearCache();
        Plugin after = pluginManager.list(TypeA.class).iterator().next();
        assertEquals(2, pluginManager.weaveCount);
        assertFalse(before == after);
    }

    @Test
    public void testMissingContainerIsNotCached() {
        pluginManager.services.add(service(TypeA.class, "a1"));
        pluginManager.containerAvailable = false;
        assertTrue(pluginManager.listOsgiPlugin(TypeA.class).isEmpty());

        pluginManager.containerAvailable = true;
        assertEquals(setOf("a1"), names(pluginManager.listOsgiPlugin(TypeA.class)));
    }

    /** An unwoven service: any plugin method call on it fails the test. */
    private static Plugin service(Class<? extends Plugin> type, String name) {
        return (Plugin) Proxy.newProxyInstance(TestPluginManagerOsgiWeaving.class.getClassLoader(),
                new Class<?>[]{type, Named.class}, (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Named.class) {
                        return name;
                    }
                    if (method.getDeclaringClass() == Object.class) {
                        switch (method.getName()) {
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            default:
                                return "service:" + name;
                        }
                    }
                    throw new AssertionError("Plugin method " + method.getName() + " called on unwoven " + name);
                });
    }

    /** The woven form of a service: implements the same types and answers getName(). */
    private static Plugin weave(Plugin service) {
        List<Class<?>> types = new ArrayList<>();
        for (Class<?> i : service.getClass().getInterfaces()) {
            if (i != Named.class) {
                types.add(i);
            }
        }
        String name = ((Named) service).stubName();
        return (Plugin) Proxy.newProxyInstance(TestPluginManagerOsgiWeaving.class.getClassLoader(),
                types.toArray(new Class<?>[0]), (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getName":
                            return name;
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            return null;
                    }
                });
    }

    public interface Named {
        String stubName();
    }

    private static Set<String> names(Collection<Plugin> plugins) {
        Set<String> names = new HashSet<>();
        for (Plugin p : plugins) {
            names.add(p.getName());
        }
        return names;
    }

    private static Set<String> setOf(String... values) {
        Set<String> set = new HashSet<>();
        for (String v : values) {
            set.add(v);
        }
        return set;
    }

    private static class StubPluginManager extends PluginManager {
        final List<Plugin> services = new ArrayList<>();
        final Set<Plugin> failing = new HashSet<>();
        final Set<Plugin> woven = new HashSet<>();
        final Map<Plugin, Plugin> wovenOf = new java.util.IdentityHashMap<>();
        boolean containerAvailable = true;
        int weaveCount = 0;
        int servicesLoadCount = 0;

        @Override
        protected void init() {
            // no Felix container or file monitor
        }

        @Override
        protected List<Plugin> loadOsgiPluginServices() {
            if (!containerAvailable) {
                return null;
            }
            servicesLoadCount++;
            return new ArrayList<>(services);
        }

        @Override
        protected Plugin weavePluginAspect(Plugin plugin) {
            weaveCount++;
            woven.add(plugin);
            Plugin result = failing.contains(plugin) ? null : weave(plugin);
            wovenOf.put(plugin, result);
            return result;
        }
    }
}
