package org.joget.plugin.base;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The per-profile plugin cache must not be reachable from a PluginManager subclass outside this
 * package (e.g. a tenant plugin), or it could insert unwoven plugin objects that platform code then
 * invokes directly.
 */
public class TestPluginManagerCacheAccess {

    @Test
    public void testGetCacheIsPackagePrivate() throws Exception {
        Method getCache = PluginManager.class.getDeclaredMethod("getCache");
        int mod = getCache.getModifiers();
        assertTrue(Modifier.isStatic(mod));
        assertFalse("getCache() must not be public", Modifier.isPublic(mod));
        assertFalse("getCache() must not be protected", Modifier.isProtected(mod));
        assertFalse(Modifier.isPrivate(mod));
    }
}
