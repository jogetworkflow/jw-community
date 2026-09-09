package org.enhydra.shark;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * The private Shark/DODS id-allocator members {@link SharkIdAllocatorReflection} opens once for the
 * id-allocator advice. Pinned here so a Shark change that renames or retypes one fails the build
 * rather than a workflow call at runtime ("couldn't allocate an Id").
 */
public class TestSharkIdAllocatorReflection {

    @Test
    public void testHandlesResolveOnceAndStayAccessible() throws Exception {
        SharkIdAllocatorReflection.init();
        Object handles = handlesSlot();
        Assert.assertNotNull("init() must publish the handles", handles);
        SharkIdAllocatorReflection.init();
        Assert.assertSame("handles must be resolved once and reused, not looked up per allocation",
                handles, handlesSlot());

        Map<String, AccessibleObject> members = members(handles);
        Assert.assertEquals(8, members.size());
        for (Map.Entry<String, AccessibleObject> e : members.entrySet()) {
            Assert.assertNotNull(e.getKey(), e.getValue());
            Assert.assertTrue(e.getKey() + " must already be accessible", isAccessible(e.getValue()));
        }
    }

    @Test
    public void testHandlesMatchSharkSignatures() throws Exception {
        SharkIdAllocatorReflection.init();
        Map<String, AccessibleObject> members = members(handlesSlot());

        for (String name : new String[]{"counterMax", "counterCurrent", "counterNext"}) {
            Field f = (Field) members.get(name);
            Assert.assertEquals("org.enhydra.shark.utilities.dods.DODSUtilities", f.getDeclaringClass().getName());
            Assert.assertTrue(name + " must be a Map", Map.class.isAssignableFrom(f.getType()));
            Assert.assertTrue(name + " must be static", Modifier.isStatic(f.getModifiers()));
        }
        for (String name : new String[]{"allocNext", "allocMax"}) {
            Field f = (Field) members.get(name);
            Assert.assertEquals("com.lutris.appserver.server.sql.standard.StandardObjectIdAllocator", f.getDeclaringClass().getName());
            Assert.assertTrue(name + " must be a Map", Map.class.isAssignableFrom(f.getType()));
        }
        Assert.assertEquals(String.class, ((Field) members.get("allocOidMinValue")).getType());
        for (String name : new String[]{"allocUpdateCache", "dodsUpdateCaches"}) {
            Assert.assertArrayEquals(name, new Class[]{String.class}, ((Method) members.get(name)).getParameterTypes());
        }
    }

    private static Object handlesSlot() throws Exception {
        Field slot = SharkIdAllocatorReflection.class.getDeclaredField("handles");
        slot.setAccessible(true);
        return slot.get(null);
    }

    private static Map<String, AccessibleObject> members(Object handles) throws Exception {
        Map<String, AccessibleObject> members = new HashMap<String, AccessibleObject>();
        for (Field f : handles.getClass().getDeclaredFields()) {
            if (AccessibleObject.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                members.put(f.getName(), (AccessibleObject) f.get(handles));
            }
        }
        return members;
    }

    @SuppressWarnings("deprecation")
    private static boolean isAccessible(AccessibleObject member) {
        return member.isAccessible();
    }
}
