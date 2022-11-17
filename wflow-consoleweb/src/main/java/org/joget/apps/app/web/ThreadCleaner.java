package org.joget.apps.app.web;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import org.joget.commons.util.LogUtil;

public class ThreadCleaner {

    Boolean debug;

    public ThreadCleaner() {
        debug = false;
    }

    public ThreadCleaner(Boolean debug) {
        this.debug = debug;
    }

    public Integer cleanThreadLocals() {
        int count = 0;
        try {
            final Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            final Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocalsField.setAccessible(true);
            for (final Thread thread : Thread.getAllStackTraces().keySet()) {
                    count += clear(threadLocalsField.get(thread));
                    count += clear(inheritableThreadLocalsField.get(thread));
            }
            
            LogUtil.info(getClass().getName(), "cleaned " + count + " values in ThreadLocals");
        } catch (Exception e) {
            throw new Error("ThreadLocalCleaner.cleanThreadLocals()", e);
        }
        return count;
    }
    
    public Integer cleanThreads() {
        int count = 0;
        try {
            final Field field = Class.forName("java.lang.ApplicationShutdownHooks").getDeclaredField("hooks");
            field.setAccessible(true);
            Map<Thread, Thread> shutdownHooks = (Map<Thread, Thread>) field.get(null);
            // Iterate copy to avoid ConcurrentModificationException
            for (Thread t : new ArrayList<Thread>(shutdownHooks.keySet())) {
                if (true || t.getClass().getName().indexOf("DODSPersistentManager") > 0) { // TODO: Set name
                    // Make sure it's from this web app instance
                    if (t.getClass().getClassLoader() != null && t.getClass().getClassLoader().equals(this.getClass().getClassLoader())) {
                        Runtime.getRuntime().removeShutdownHook(t); // Remove hook to avoid PermGen leak
                        LogUtil.info(getClass().getName(), "Cleaning thread " + t);
                        count++;
                        t.start(); // Wait up to 1 minute for thread to run
                        t.join(60 * 1000); // Wait up to 1 minute for thread to run
                    }
                }
            }
        } catch (Exception e) {        
            throw new Error("ThreadLocalCleaner.cleanThreads()", e);        
        }
        return count;
    }

    private int clear(final Object threadLocalMap) throws Exception {
        if (threadLocalMap == null)
                return 0;
        int count = 0;
        final Field tableField = threadLocalMap.getClass().getDeclaredField("table");
        tableField.setAccessible(true);
        final Object table = tableField.get(threadLocalMap);
        for (int i = 0, length = Array.getLength(table); i < length; ++i) {
            final Object entry = Array.get(table, i);
            if (entry != null) {
                final Object threadLocal = ((WeakReference)entry).get();
                if (threadLocal != null) {
                    log(i, threadLocal);
                    Array.set(table, i, null);
                    ++count;
                }
            }
        }
        return count;
    }

    private void log(int i, final Object threadLocal) {
        if (!debug) {
            return;
        }
        if (threadLocal.getClass() != null &&
            threadLocal.getClass().getEnclosingClass() != null &&
            threadLocal.getClass().getEnclosingClass().getName() != null) {
            LogUtil.info(getClass().getName(), "threadLocalMap(" + i + "): " + threadLocal.getClass().getEnclosingClass().getName());
        }
        else if (threadLocal.getClass() != null &&
                 threadLocal.getClass().getName() != null) {
            LogUtil.info(getClass().getName(), "threadLocalMap(" + i + "): " + threadLocal.getClass().getName());
        }
        else {
            LogUtil.info(getClass().getName(), "threadLocalMap(" + i + "): cannot identify threadlocal class name");
        }
    }

}