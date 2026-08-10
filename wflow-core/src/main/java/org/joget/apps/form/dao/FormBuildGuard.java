package org.joget.apps.form.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Node-local single-flight guard shared by the two expensive rebuilds that can call INTO each
 * other: form column resolution ({@code COL:} keys, {@link FormColumnResolver}) and Hibernate
 * mapping regeneration ({@code MAP:} keys, {@link FormDataDaoImpl}).
 *
 * <p><b>Both must share one guard, and that is the whole reason this class exists.</b> A column
 * build runs {@code createElementFromJson} and {@code getDynamicFieldNames()} — arbitrary plugin
 * code — which can call {@code find()} and land back in mapping regeneration; mapping regeneration
 * in turn resolves that table's columns. So both orderings exist in production, and two threads
 * taking the two locks in opposite orders would deadlock (ABBA).</p>
 *
 * <p>The invariant that rules that out: <b>a thread already inside a guard never waits for another
 * one.</b> {@link #isNested()} lets callers detect that and proceed without locking, so no thread
 * can hold one of these locks while blocked on another and no cycle can form. The cost is that a
 * nested build is not deduplicated — correct, just occasionally redundant, which is the right trade
 * against a hung request thread.</p>
 *
 * <p>Striped rather than a per-key map: an LRU map can evict a lock while it is still held or has
 * waiters, after which a later caller for the same key receives a DIFFERENT lock object and runs
 * concurrently with the holder — mutual exclusion silently lost, with no symptom other than the
 * duplicated work this exists to prevent. A fixed array has stable identity and is bounded by
 * construction. Two unrelated keys may share a stripe; that costs a short wait, never a deadlock.</p>
 *
 * <p>Node-local by design. This collapses redundant work; it is not a distributed lock and must
 * never be relied on as one.</p>
 */
public class FormBuildGuard {

    /**
     * Upper bound on waiting for another thread's build. What a caller does on expiry is its own
     * decision and they differ — see the two call sites.
     *
     * <p>Deliberately not generous. The wait happens inside a DAO call, typically within a Spring
     * transaction holding a JDBC connection, so every second spent here is a second that connection
     * is pinned. Too short and a slow legitimate build makes every waiter duplicate it (the
     * stampede this exists to stop); too long and a pathological build drains the connection pool.
     * Override with {@code -Dwflow.formColumnBuildLockTimeoutSeconds=N} for a large tenant.</p>
     */
    public static final int DEFAULT_LOCK_TIMEOUT_SECONDS =
            Integer.getInteger("wflow.formColumnBuildLockTimeoutSeconds", 20);

    private static final int STRIPES = 64;
    private static final ReentrantLock[] LOCKS = new ReentrantLock[STRIPES];
    static {
        for (int i = 0; i < STRIPES; i++) {
            LOCKS[i] = new ReentrantLock();
        }
    }

    /** The guard keys currently on this thread's stack, innermost last. */
    private static final ThreadLocal<Set<String>> IN_PROGRESS = new ThreadLocal<Set<String>>();

    private FormBuildGuard() {
    }

    /** Whether this thread is already inside any guarded build, and so must not wait for a lock. */
    public static boolean isNested() {
        Set<String> inProgress = IN_PROGRESS.get();
        return inProgress != null && !inProgress.isEmpty();
    }

    /** Whether this exact key is already on this thread's stack — i.e. a dependency cycle. */
    public static boolean holds(String key) {
        Set<String> inProgress = IN_PROGRESS.get();
        return inProgress != null && inProgress.contains(key);
    }

    /**
     * Acquires the stripe for a key, waiting at most {@code timeoutSeconds}.
     *
     * @return the locked lock, which the caller MUST unlock, or null on timeout or interruption
     */
    public static ReentrantLock tryLock(String key, int timeoutSeconds) {
        ReentrantLock lock = LOCKS[(key.hashCode() & 0x7fffffff) % STRIPES];
        try {
            if (lock.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                return lock;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Runs {@code body} with {@code key} on this thread's guard stack, so a re-entrant call for the
     * same key is detectable by {@link #holds} and any nested call is detectable by
     * {@link #isNested}.
     *
     * <p>The removal is in a {@code finally} and that is load-bearing: these are pooled request
     * threads, and the marker set this replaced was cleared only after a SUCCESSFUL build, so one
     * thrown build wedged that table for the life of the JVM with no way to reset it.</p>
     */
    public static <T> T runGuarded(String key, Supplier<T> body) {
        Set<String> inProgress = IN_PROGRESS.get();
        if (inProgress != null) {
            inProgress.add(key);
            try {
                return body.get();
            } finally {
                inProgress.remove(key);
            }
        }
        Set<String> outermost = new HashSet<String>();
        outermost.add(key);
        IN_PROGRESS.set(outermost);
        try {
            return body.get();
        } finally {
            IN_PROGRESS.remove();
        }
    }
}
