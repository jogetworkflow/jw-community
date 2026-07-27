package org.joget.apps.app.service;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.UuidGenerator;

/**
 * File-based mutual-exclusion lock guarding a single app version's process update/instance
 * migration across an entire cluster (the lock file lives on shared storage, not just in this
 * JVM's memory).
 * <p>
 * A lock file's mere presence is the cross-node signal; this JVM additionally tracks, per lock
 * path, the owner token it last wrote there ({@link #processUpdateLockOwners}), so a heartbeat or
 * release only ever acts on a lock generation this instance actually created or reclaimed, never
 * one a different node (or a different claim on the same node) has since reclaimed out from under
 * it. Access to the lock file is arbitrated through an operating-system file lock held on a
 * separately named mutex file, so a paused claimant cannot have its arbitration generation
 * deleted and replaced underneath it. The operating system releases that mutex automatically if
 * its process dies; see {@link #claimProcessUpdateLock(Path)} for the full reasoning.
 * </p>
 * <p>
 * One instance of this class represents one JVM's view of these locks; {@code AppServiceImpl},
 * a singleton, owns a single instance and delegates its public locking API to it.
 * </p>
 */
public class ProcessUpdateLockManager {

    private final static String PROCESS_MIGRATION_PATH = "app_migration";
    // Lives on shared cluster storage; a node that dies mid-migration cannot delete it itself,
    // so other nodes reclaim it once it goes this long without a heartbeat touch. Overridable
    // for deployments with unusually long-running migrations.
    private final static long PROCESS_UPDATE_LOCK_EXPIRY_MS = Long.getLong("wflow.processUpdateLockExpiryMs", 60 * 60 * 1000L);
    // Suffix for the persistent file whose OS-level lock serializes all access to one main lock.
    private final static String PROCESS_UPDATE_LOCK_TICKET_SUFFIX = ".reclaiming";
    // Release is normally called only once. Retry brief mutex/filesystem contention on the
    // caller thread so the tenant/profile context remains intact and no application-lifetime
    // executor is needed. The retry window is deliberately bounded; persistent failures retain
    // the ownership record and are ultimately covered by stale-lock recovery.
    private final static int PROCESS_UPDATE_LOCK_RELEASE_ATTEMPTS = Math.max(1, Integer.getInteger("wflow.processUpdateLockReleaseAttempts", 5));
    private final static long PROCESS_UPDATE_LOCK_RELEASE_RETRY_DELAY_MS = Math.max(0L, Long.getLong("wflow.processUpdateLockReleaseRetryDelayMs", 50L));
    // Owner token this JVM believes it holds for each process update lock key, so a heartbeat or
    // release only ever acts on a lock generation this node actually created or reclaimed, never
    // one a different node has since reclaimed out from under it.
    private final Map<String, String> processUpdateLockOwners = new ConcurrentHashMap<String, String>();
    // Tokens for locks whose owning operation has already finished but whose release could not be
    // completed (e.g. it exhausted its retries on transient contention). Unlike
    // processUpdateLockOwners, these must NOT keep the lock alive: the operation is over, so a
    // status check or a reclaim is free to clear the file. The token is kept only so that cleanup,
    // under the mutex, can confirm the on-disk generation is still the one this node abandoned
    // before deleting it. Without this separation an exhausted release on a single-node deployment
    // would leave a token in processUpdateLockOwners that both blocks stale-lock recovery (it looks
    // locally owned and live) and blocks re-claiming (claim refuses to clobber its own record),
    // stranding that app version's Process Builder indefinitely.
    private final Map<String, String> processUpdateLockReleasePending = new ConcurrentHashMap<String, String>();
    private final Supplier<Path> baseDirectorySupplier;

    public ProcessUpdateLockManager() {
        this(new Supplier<Path>() {
            @Override
            public Path get() {
                return Paths.get(SetupManager.getBaseDirectory());
            }
        });
    }

    ProcessUpdateLockManager(final Path baseDirectory) {
        this(new Supplier<Path>() {
            @Override
            public Path get() {
                return baseDirectory;
            }
        });
    }

    private ProcessUpdateLockManager(Supplier<Path> baseDirectorySupplier) {
        this.baseDirectorySupplier = baseDirectorySupplier;
    }

    /**
     * Lock and processing process update & process instance migration
     * @param appDef
     * @return
     */
    public boolean lockProcessUpdate(AppDefinition appDef) {
        try {
            Path path = processUpdateLockPath(appDef);
            Files.createDirectories(path.getParent());
            return claimProcessUpdateLock(path);
        } catch (Exception e) {
            LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Fail to acquire lock for " + appDef.getAppId() + "_" + appDef.getVersion().toString() + ". Will retry again.");
        }
        return false;
    }

    /**
     * Release the lock after done process update and process instance migration
     * <p>
     * Gated behind the same OS-backed mutex as claims, heartbeats, and stale cleanup so the owner
     * check and delete happen as one step with respect to a concurrent reclaim. Without that
     * mutex, a reclaimer could replace the lock file between this method reading the current token
     * and acting on it, making it delete the reclaimer's fresh lock instead of the caller's own.
     * </p>
     * @param appDef
     */
    public void tryReleaseProcessUpdate(AppDefinition appDef) {
        Path path = processUpdateLockPath(appDef);
        String ownerMapKey = path.toString();
        String ownedToken = processUpdateLockOwners.get(ownerMapKey);
        if (ownedToken == null) {
            // This node never recorded owning the lock (e.g. process restarted); nothing safe to release.
            return;
        }

        String lockLabel = appDef.getAppId() + "_" + appDef.getVersion();
        for (int attempt = 1; attempt <= PROCESS_UPDATE_LOCK_RELEASE_ATTEMPTS; attempt++) {
            if (attemptReleaseProcessUpdate(path, lockLabel, ownedToken)) {
                return;
            }

            if (attempt < PROCESS_UPDATE_LOCK_RELEASE_ATTEMPTS && PROCESS_UPDATE_LOCK_RELEASE_RETRY_DELAY_MS > 0L) {
                try {
                    Thread.sleep(PROCESS_UPDATE_LOCK_RELEASE_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Retries exhausted. The operation is finished, so stop treating this as a live-owned lock:
        // hand the token to the release-pending map so a subsequent status check or claim can clear
        // the file (see hasProcessUpdate/claimProcessUpdateLock), and drop it from the live-owner
        // map so it no longer looks like an in-progress migration that blocks recovery. On a
        // single-node deployment this is what prevents the lock from being stranded forever; on a
        // cluster, another node's stale-lock recovery would also eventually clear it.
        processUpdateLockReleasePending.put(ownerMapKey, ownedToken);
        processUpdateLockOwners.remove(ownerMapKey, ownedToken);
        LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Unable to release process update lock for " + lockLabel + " after " + PROCESS_UPDATE_LOCK_RELEASE_ATTEMPTS + " attempts. The lock will be cleared by the next status check or claim, or by stale-lock recovery.");
    }

    /**
     * Attempts one generation-safe release while preserving the caller's profile context.
     *
     * @return true when no further attempt is required; false for transient contention/failure
     */
    private boolean attemptReleaseProcessUpdate(Path path, String lockLabel, String ownedToken) {
        String ownerMapKey = path.toString();
        if (!ownedToken.equals(processUpdateLockOwners.get(ownerMapKey))) {
            return true;
        }

        Path ticket = processUpdateLockTicketPath(path);
        ProcessUpdateLockTicket ticketLock = acquireProcessUpdateLockTicket(ticket);
        if (ticketLock == null) {
            return false;
        }

        try {
            String currentToken = readLockOwnerToken(path);
            if (ownedToken.equals(currentToken)) {
                Files.deleteIfExists(path);
                // Only forget our ownership once the file is actually gone; if deletion had
                // thrown instead, keeping the record lets a later retry still release the lock
                // we genuinely hold, rather than stranding the file until the stale timeout.
                processUpdateLockOwners.remove(ownerMapKey, ownedToken);
            } else {
                // Another node already reclaimed this lock as stale; deleting now would release
                // their lock generation instead of ours. Our record is stale too, so drop it.
                processUpdateLockOwners.remove(ownerMapKey, ownedToken);
                LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Skipped releasing process update lock for " + lockLabel + " because it is no longer owned by this node.");
            }
            return true;
        } catch (Exception e) {
            // Leave the ownership record in place; a transient failure here should not make a
            // later retry believe it has nothing to release.
            LogUtil.info(ProcessUpdateLockManager.class.getName(), e.getMessage());
            return false;
        } finally {
            ticketLock.close();
        }
    }

    /**
     * Check there is process update & process instance migration.
     * <p>
     * A lock left behind by a node that crashed mid-migration (no heartbeat, no owner still
     * running to release it) is treated as stale and cleared here so reads are not blocked
     * forever, instead of only when the next save attempt happens to reclaim it.
     * </p>
     * @param appDef
     */
    public boolean hasProcessUpdate(AppDefinition appDef) {
        Path path = processUpdateLockPath(appDef);

        // Fast path: keep plain reads lock-free (as they were before this mutex existed) so a
        // concurrent hasProcessUpdate can never contend on the shared mutex file and thereby make
        // a save's lockProcessUpdate spuriously fail, or a getJson read return a spurious 503. A
        // claim only becomes effective once its atomic move publishes the main lock file, so an
        // absent file unambiguously means no lock is held; only when a file is actually present do
        // we take the mutex to distinguish a live lock from a stale one.
        if (!Files.exists(path)) {
            // The file is gone (released, or reclaimed and released elsewhere); drop any leftover
            // release-pending record so it cannot linger.
            processUpdateLockReleasePending.remove(path.toString());
            return false;
        }

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            // Unknown filesystem state is not evidence that no update is running.
            return true;
        }

        ProcessUpdateLockTicket ticketLock = acquireProcessUpdateLockTicket(processUpdateLockTicketPath(path));
        if (ticketLock == null) {
            // A claimant/releaser currently owns the mutex. Report busy rather than observe the
            // main file in the middle of that operation.
            return true;
        }
        try {
            if (!Files.exists(path)) {
                processUpdateLockReleasePending.remove(path.toString());
                return false;
            }

            // A lock this node already finished with but could not release: the operation is over,
            // so clear it now rather than reporting a phantom in-progress update (which, before the
            // expiry elapsed, this otherwise would). Safe because the mutex is held and the on-disk
            // token still matches the generation this node abandoned.
            String releasePendingToken = processUpdateLockReleasePending.get(path.toString());
            if (releasePendingToken != null && releasePendingToken.equals(readLockOwnerToken(path))) {
                try {
                    Files.deleteIfExists(path);
                    processUpdateLockReleasePending.remove(path.toString(), releasePendingToken);
                    return false;
                } catch (IOException e) {
                    LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Failed to clear finished-but-unreleased process update lock " + path + ": " + e.getMessage());
                    return true;
                }
            }

            if (!isProcessUpdateLockStale(path)) {
                return true;
            }

            String localToken = processUpdateLockOwners.get(path.toString());
            if (localToken != null && localToken.equals(readLockOwnerToken(path))) {
                // A slow but still-live operation in this JVM must not have its own lock cleared
                // by a concurrent status check merely because its heartbeat is overdue.
                return true;
            }

            try {
                Files.deleteIfExists(path);
                return false;
            } catch (IOException e) {
                LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Failed to clear stale process update lock " + path + ": " + e.getMessage());
                return true;
            }
        } catch (IOException e) {
            // A read failure is an unknown/busy state, never a safe all-clear.
            return true;
        } finally {
            ticketLock.close();
        }
    }

    /**
     * Refreshes the process update lock's last-modified time so an in-progress migration is not
     * mistaken for one abandoned by a crashed node.
     * <p>
     * Only refreshes when this node's recorded owner token still matches the file's current
     * content; if another node has since reclaimed the lock as stale, blindly touching it would
     * revive a lock generation this node no longer legitimately holds. Gated behind the same
     * OS-backed mutex as claim/release/clear so that check-then-touch cannot race a concurrent
     * reclaim replacing the file in between; if the mutex is contended, this heartbeat is simply
     * skipped and retried on the next call rather than acting on a token that might already be
     * stale.
     * </p>
     * @param appDef the app definition whose process update lock should be refreshed
     */
    public void touchProcessUpdateLock(AppDefinition appDef) {
        try {
            Path path = processUpdateLockPath(appDef);
            String ownedToken = processUpdateLockOwners.get(path.toString());
            if (ownedToken == null) {
                return;
            }

            Path ticket = processUpdateLockTicketPath(path);
            ProcessUpdateLockTicket ticketLock = acquireProcessUpdateLockTicket(ticket);
            if (ticketLock == null) {
                // Another caller is concurrently claiming/clearing/releasing this lock; skip this
                // heartbeat rather than race it, the next call will try again.
                return;
            }
            try {
                String currentToken = readLockOwnerToken(path);
                if (!ownedToken.equals(currentToken)) {
                    LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Process update lock for " + appDef.getAppId() + "_" + appDef.getVersion() + " is no longer owned by this node; skipping heartbeat.");
                    return;
                }
                if (Files.exists(path)) {
                    Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
                }
            } finally {
                ticketLock.close();
            }
        } catch (Exception e) {
            // Best effort heartbeat; a missed touch only risks another node reclaiming the lock early.
            LogUtil.debug(ProcessUpdateLockManager.class.getName(), "Unable to refresh process update lock for " + appDef.getAppId() + "_" + appDef.getVersion());
        }
    }

    /**
     * Builds the process update lock file path for an app version.
     * <p>
     * The path is profile-specific (via {@link SetupManager#getBaseDirectory()}), so ownership
     * tracking must key off this full path rather than just the file name — otherwise two virtual
     * host profiles deploying the same app id/version would collide on the same in-memory key.
     * </p>
     * @param appDef the app definition whose lock path to build
     * @return the lock file path
     */
    private Path processUpdateLockPath(AppDefinition appDef) {
        String key = SecurityUtil.normalizedFileName(appDef.getAppId() + "_" + appDef.getVersion().toString() + ".lock");
        return baseDirectorySupplier.get().resolve(PROCESS_MIGRATION_PATH).resolve(key);
    }

    /**
     * Checks whether a process update lock file has gone stale, i.e. no owning migration has
     * touched it within {@link #PROCESS_UPDATE_LOCK_EXPIRY_MS}. This is the only signal available
     * across cluster nodes since the lock has no owner id or heartbeat channel beyond its own
     * last-modified time.
     *
     * @param path the lock file path
     * @return true if the lock is old enough to be considered abandoned
     */
    private boolean isProcessUpdateLockStale(Path path) {
        try {
            long age = System.currentTimeMillis() - Files.getLastModifiedTime(path).toMillis();
            return age > PROCESS_UPDATE_LOCK_EXPIRY_MS;
        } catch (IOException e) {
            // Deleted concurrently by another node; nothing to reclaim.
            return false;
        }
    }

    /**
     * Claims the process update lock, whether it is currently absent or merely stale.
     * <p>
     * Every operation that reads or writes the main lock first acquires an exclusive OS-level lock
     * on the persistent mutex file. Unlike an expiring ticket implemented by deleting and
     * recreating a pathname, this mutex cannot be stolen while its holder is paused: the operating
     * system retains it until the channel closes or the owning process dies. Holding that mutex,
     * this method replaces the main lock's content with a fresh owner token and records the same
     * token locally for generation-safe heartbeat and release.
     * </p>
     * <p>
     * A path this JVM already believes it owns (its recorded token still matches the file's
     * current content) is never reclaimed here even if the file's mtime looks stale — e.g. a
     * still-running migration that simply hasn't heartbeat recently enough. {@link
     * #processUpdateLockOwners} holds at most one token per path, so letting a second, unrelated
     * claim overwrite that entry would leave the first (still-live) operation's eventual release
     * acting on the second operation's token instead of its own.
     * </p>
     * @param path the lock file path
     * @return true if this call claimed the lock
     */
    private boolean claimProcessUpdateLock(Path path) throws IOException {
        String pathKey = path.toString();
        Path ticket = processUpdateLockTicketPath(path);
        ProcessUpdateLockTicket ticketLock = acquireProcessUpdateLockTicket(ticket);
        if (ticketLock == null) {
            return false;
        }
        try {
            String existingOwnedToken = processUpdateLockOwners.get(pathKey);
            if (existingOwnedToken != null) {
                if (existingOwnedToken.equals(readLockOwnerToken(path))) {
                    // We still believe we own the current lock generation on this exact path;
                    // refuse to claim over ourselves rather than clobber our ownership record.
                    return false;
                }
                // Our record no longer reflects reality (another node reclaimed it, or it is
                // gone); safe to drop and fall through to a normal claim attempt.
                processUpdateLockOwners.remove(pathKey, existingOwnedToken);
            }

            // A generation this node finished with but could not release: reclaiming it is safe
            // even though it has not yet aged into staleness, because the operation that held it is
            // over. Take it over now instead of waiting for the expiry window.
            boolean ownReleasePending = false;
            String releasePendingToken = processUpdateLockReleasePending.get(pathKey);
            if (releasePendingToken != null && releasePendingToken.equals(readLockOwnerToken(path))) {
                ownReleasePending = true;
                processUpdateLockReleasePending.remove(pathKey, releasePendingToken);
            }

            if (!ownReleasePending && Files.exists(path) && !isProcessUpdateLockStale(path)) {
                return false;
            }

            String token = UuidGenerator.getInstance().getUuid();
            Path temp = path.resolveSibling(path.getFileName().toString() + ".claim-" + token);
            try {
                Files.write(temp, token.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignore) {
                    // Best effort cleanup of our own unused candidate file.
                }
                LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Failed to claim process update lock " + path + ": " + e.getMessage());
                return false;
            }

            try {
                try {
                    Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    // Every reader and writer of the main lock is serialized by ticketLock, so a
                    // same-directory replacement remains safe even on a provider that cannot
                    // expose the stronger ATOMIC_MOVE option.
                    Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignore) {
                    // Best effort cleanup of our own unused candidate file.
                }
                LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Failed to claim process update lock " + path + ": " + e.getMessage());
                return false;
            }

            processUpdateLockOwners.put(pathKey, token);
            return true;
        } finally {
            ticketLock.close();
        }
    }

    /**
     * Builds the ticket file path used to arbitrate a single lock file's claim attempts.
     * @param path the lock file path
     * @return the corresponding ticket file path
     */
    private Path processUpdateLockTicketPath(Path path) {
        return path.resolveSibling(path.getFileName().toString() + PROCESS_UPDATE_LOCK_TICKET_SUFFIX);
    }

    /**
     * Acquires the exclusive operating-system lock that serializes access to one main lock file.
     * <p>
     * The mutex file itself is persistent and is never deleted as part of arbitration. Only its
     * OS-level file lock conveys ownership, so there is no stale pathname generation to inspect or
     * delete. A process pause keeps the mutex held; a process crash closes the channel and the
     * operating system releases it automatically.
     * </p>
     * @param ticket the ticket file path
     * @return a handle that holds the file lock until closed; null if it could not be acquired
     */
    private ProcessUpdateLockTicket acquireProcessUpdateLockTicket(Path ticket) {
        FileChannel channel = null;
        try {
            channel = FileChannel.open(ticket, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                return null;
            }
            return new ProcessUpdateLockTicket(channel, lock);
        } catch (OverlappingFileLockException e) {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException ignore) {
                // Best effort cleanup of a channel that did not acquire the mutex.
            }
            return null;
        } catch (IOException e) {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException ignore) {
                // Best effort cleanup of a channel that did not acquire the mutex.
            }
            LogUtil.warn(ProcessUpdateLockManager.class.getName(), "Failed to acquire process update lock mutex " + ticket + ": " + e.getMessage());
            return null;
        }
    }

    private static final class ProcessUpdateLockTicket implements AutoCloseable {
        private final FileChannel channel;
        private final FileLock lock;

        private ProcessUpdateLockTicket(FileChannel channel, FileLock lock) {
            this.channel = channel;
            this.lock = lock;
        }

        @Override
        public void close() {
            try {
                lock.release();
            } catch (IOException ignore) {
                // Closing the channel below also releases any lock still associated with it.
            }
            try {
                channel.close();
            } catch (IOException ignore) {
                // Best effort; the operating system also releases it when the process exits.
            }
        }
    }

    /**
     * Reads the owner token currently stored in a lock file, if any.
     * <p>
     * Only a missing file is reported as {@code null} (i.e. "no owner"). Any other read failure
     * is a transient/unknown condition, not evidence that another node has taken over — it is
     * thrown so callers can leave ownership state untouched rather than mistakenly concluding
     * they no longer own the lock.
     * </p>
     * @param path the lock file path
     * @return the owner token, or null if the file does not exist
     * @throws IOException if the file exists but could not be read
     */
    private String readLockOwnerToken(Path path) throws IOException {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
        } catch (NoSuchFileException e) {
            return null;
        }
    }
}
