package org.enhydra.shark;

import com.lutris.appserver.server.sql.standard.StandardObjectIdAllocator;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.joget.commons.util.LogUtil;

/**
 * pre-accessible reflective handles for the two Shark ID/counter
 * allocators' private state.
 *
 * <p>Shark allocates an id for every persisted workflow object. The cloud {@code
 * MultiTenantSharkEngineManagerAspect} and the community {@code SharkUtilitiesAspect} advise those
 * allocations and read/update the allocators' private {@code next}/{@code max}/{@code
 * counterCaches*} fields (and invoke their private {@code updateCache}/{@code updateCaches}) with
 * commons-lang {@code FieldUtils.readField(..,true)} / {@code MethodUtils.invokeMethod(..,true,..)}
 * — each of which calls {@code setAccessible(true)} <em>on every allocation</em>. With
 * {@code MultiTenantSecurityManager} installed, that {@code setAccessible} is refused
 * ("No permission to access restricted members") whenever a {@code bsh.*} / OSGi-bundle frame is on
 * the stack — i.e. any process start or workflow operation initiated from a non-admin tenant's
 * BeanShell — breaking it even though the reflection is performed by trusted platform code.
 *
 * <p>This holder performs the {@code setAccessible(true)} exactly <b>once</b>; afterwards every
 * {@code get}/{@code invoke} carries the override flag and never re-enters {@code
 * SecurityManager.checkPermission}. {@link #init()} is called eagerly from
 * {@code WorkflowManagerImpl.setPath(..)} during Spring context refresh (the startup thread, no
 * {@code bsh.*}/bundle frame on the stack), so the one-time check is always allowed.
 *
 * <p>Deliberately NOT a {@code static final}/{@code <clinit>} holder: if the one-time
 * {@code setAccessible} were ever refused (a tenant BeanShell frame on the very first touch), a
 * {@code <clinit>} failure would mark this class erroneous for the life of the JVM (JLS 12.4.2). A
 * volatile slot published only on success simply lets the next clean caller retry.
 *
 * <p>The handle accessors are package-private, and the {@code Field}/{@code Method} objects are never handed out — only typed
 * operations bound to the exact declaring classes. {@code Field.get}/{@code Method.invoke} throw
 * {@code IllegalArgumentException} unless the target really is the matching allocator, so the public
 * advice methods cannot be turned into a confused-deputy against an arbitrary object.
 */
// Class + init() are public only so WorkflowManagerImpl.setPath (a different package) can trigger the
// one-time setup; the typed handle accessors below stay package-private, so only the two aspects in
// this package read the allocator state and the Field/Method objects are never exposed.
public final class SharkIdAllocatorReflection {

    private static final class Handles {
        final Field allocNext;
        final Field allocMax;
        final Field allocOidMinValue;
        final Method allocUpdateCache;
        final Field counterMax;
        final Field counterCurrent;
        final Field counterNext;
        final Method dodsUpdateCaches;

        Handles() throws ReflectiveOperationException {
            Class<?> alloc = StandardObjectIdAllocator.class; // class literal -> no <clinit> side effects here
            // 8.2: DODSUtilities is not on this module's compile classpath (the pre-existing
            // aspect also resolved it by name); load WITHOUT initialising, same as a class literal.
            Class<?> dods = Class.forName("org.enhydra.shark.utilities.dods.DODSUtilities", false,
                    SharkIdAllocatorReflection.class.getClassLoader());
            allocNext = open(alloc.getDeclaredField("next"));               // private HashMap
            allocMax = open(alloc.getDeclaredField("max"));                 // private HashMap
            allocOidMinValue = open(alloc.getDeclaredField("oidMinValue")); // private String
            allocUpdateCache = open(alloc.getDeclaredMethod("updateCache", String.class)); // private void
            counterMax = open(dods.getDeclaredField("counterCachesMax"));         // private static Map
            counterCurrent = open(dods.getDeclaredField("counterCachesCurrent")); // private static Map
            counterNext = open(dods.getDeclaredField("counterCachesNext"));       // private static Map
            dodsUpdateCaches = open(dods.getDeclaredMethod("updateCaches", String.class)); // private static
        }

        private static <T extends AccessibleObject> T open(T m) {
            m.setAccessible(true);
            return m;
        }
    }

    private static volatile Handles handles;

    /**
     * One-time eager initialisation hook. Call from a stack with no {@code bsh.*}/bundle frame
     * (see {@code WorkflowManagerImpl.setPath}). Safe to call more than once; idempotent.
     */
    public static void init() throws ReflectiveOperationException {
        handles();
        LogUtil.info(SharkIdAllocatorReflection.class.getName(),
                "Shark id-allocator reflective handles initialised");
    }

    private static Handles handles() throws ReflectiveOperationException {
        Handles h = handles; // hot path: a single volatile read
        if (h == null) {
            synchronized (SharkIdAllocatorReflection.class) {
                h = handles;
                if (h == null) {
                    h = new Handles();
                    handles = h; // publish only after full success
                }
            }
        }
        return h;
    }

    // ---- typed operations (the Field/Method objects themselves are never exposed) ----

    static Map allocatorNext(Object allocator) throws Exception {
        return (Map) handles().allocNext.get(allocator);
    }

    static Map allocatorMax(Object allocator) throws Exception {
        return (Map) handles().allocMax.get(allocator);
    }

    static String allocatorOidMinValue(Object allocator) throws Exception {
        return (String) handles().allocOidMinValue.get(allocator);
    }

    static void allocatorUpdateCache(Object allocator) throws Exception {
        invoke(handles().allocUpdateCache, allocator, (Object) null);
    }

    static Map counterCachesMax() throws Exception {
        return (Map) handles().counterMax.get(null);
    }

    static Map counterCachesCurrent() throws Exception {
        return (Map) handles().counterCurrent.get(null);
    }

    static Map counterCachesNext() throws Exception {
        return (Map) handles().counterNext.get(null);
    }

    static void dodsUpdateCaches(String objectName) throws Exception {
        invoke(handles().dodsUpdateCaches, null, objectName);
    }

    private static void invoke(Method m, Object target, Object arg) throws Exception {
        try {
            m.invoke(target, arg);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw e;
        }
    }

    private SharkIdAllocatorReflection() {
    }
}
