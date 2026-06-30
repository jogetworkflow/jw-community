package org.joget.logs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;

public class LogViewerThread implements Runnable {
    private static final long DEFAULT_TAIL_DELAY_MS = 1000L;

    // One live file poller per log source prevents every websocket from polling the same file.
    private static final Map<String, SharedTailer> SHARED_TAILERS = new HashMap<String, SharedTailer>();

    private volatile InputStream inputStream = null;
    private volatile BufferedReader reader = null;
    private final String key;
    private final LogViewerEndpoint endpoint;
    private final File rollingFile;
    private final File currentFile;
    private final long tailDelayMillis;
    private final Object streamMonitor = new Object();
    private final Object lifecycleMonitor = new Object();
    private volatile boolean closed = false;
    private boolean registered = false;
    private EndpointSubscriber subscriber = null;

    public LogViewerThread(String profile, String appId, LogViewerEndpoint endpoint, String node) {
        this(profile, appId, endpoint, node,
                new File(LogViewerAppender.getFileName(appId, node) + LogViewerAppender.LOG_ROLLING_EXT),
                new File(LogViewerAppender.getFileName(appId, node)),
                DEFAULT_TAIL_DELAY_MS);
    }

    LogViewerThread(String profile, String appId, LogViewerEndpoint endpoint, String node, File rollingFile, File currentFile, long tailDelayMillis) {
        this.key = node + ":" + profile + ":" + appId;
        this.endpoint = endpoint;
        this.rollingFile = rollingFile;
        this.currentFile = currentFile;
        this.tailDelayMillis = tailDelayMillis;
    }

    @Override
    public void run() {
        readFile(rollingFile);

        // Register before reading the current file so any new log lines written during
        // backfill are queued for this viewer instead of being missed.
        SharedTailer.Subscription subscription = registerEndpoint();
        if (subscription != null) {
            try {
                // Only replay bytes that existed at subscription time. Lines appended
                // after this snapshot are delivered by the subscriber queue after activate().
                readFile(currentFile, subscription.snapshotLength);
            } finally {
                // Backfill is complete, so queued live lines can now be sent in order.
                subscription.activate();
            }
        }
    }
    
    protected void readFile(File file) {
        readFile(file, Long.MAX_VALUE);
    }

    private void readFile(File file, long maxBytes) {
        InputStream currentInputStream = null;
        BufferedReader currentReader = null;
        try {
            currentInputStream = new FileInputStream(file);
            InputStream boundedStream = maxBytes == Long.MAX_VALUE ? currentInputStream : new LimitedInputStream(currentInputStream, maxBytes);
            currentReader = new BufferedReader(new InputStreamReader(boundedStream, StandardCharsets.UTF_8));
            synchronized (streamMonitor) {
                inputStream = currentInputStream;
                reader = currentReader;
            }
            
            String line;
            try {
                while(!closed && (line = currentReader.readLine()) != null) {
                    if (!sendLine(line)) {
                        close();
                        break;
                    }
                }
            } catch (IOException e) {
                if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                    LogUtil.error(LogViewerThread.class.getName(), e, "");
                }
            }
        } catch(FileNotFoundException ex) {
            if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                LogUtil.error(LogViewerThread.class.getName(), ex, "");
            }
        } finally {
            closeFileHandles(currentReader, currentInputStream);
            synchronized (streamMonitor) {
                if (reader == currentReader) {
                    reader = null;
                }
                if (inputStream == currentInputStream) {
                    inputStream = null;
                }
            }
        }
    }

    private SharedTailer.Subscription registerEndpoint() {
        synchronized (lifecycleMonitor) {
            if (closed || registered) {
                return null;
            }

            // If close() happens while backfill is still running, this synchronized block
            // prevents a closed websocket from being added to the shared live stream.
            SharedTailer.Subscription subscription = SharedTailer.register(key, currentFile, tailDelayMillis, endpoint);
            subscriber = subscription.subscriber;
            registered = true;
            return subscription;
        }
    }

    static int getActiveTailerCount() {
        synchronized (SHARED_TAILERS) {
            return SHARED_TAILERS.size();
        }
    }

    private static class SharedTailer implements Runnable {
        private final String key;
        private final File currentFile;
        private final long tailDelayMillis;
        private final Set<EndpointSubscriber> subscribers = new HashSet<EndpointSubscriber>();
        private long position;
        private volatile boolean stopped = false;

        private SharedTailer(String key, File currentFile, long tailDelayMillis, long position) {
            this.key = key;
            this.currentFile = currentFile;
            this.tailDelayMillis = tailDelayMillis;
            this.position = position;
        }

        private static Subscription register(String key, File currentFile, long tailDelayMillis, LogViewerEndpoint endpoint) {
            SharedTailer tailerToStart = null;
            EndpointSubscriber subscriber;
            long snapshotLength;
            synchronized (SHARED_TAILERS) {
                SharedTailer sharedTailer = SHARED_TAILERS.get(key);
                if (sharedTailer == null) {
                    sharedTailer = new SharedTailer(key, currentFile, tailDelayMillis, 0L);
                    SHARED_TAILERS.put(key, sharedTailer);
                    tailerToStart = sharedTailer;
                }

                synchronized (sharedTailer) {
                    // Snapshot and subscriber insertion must happen under the same lock.
                    // Otherwise a line could be broadcast between those two operations.
                    snapshotLength = currentFile.exists() ? currentFile.length() : 0L;
                    if (tailerToStart != null) {
                        // The first viewer replays current-file history itself, so the shared
                        // poller starts at EOF and only handles future writes.
                        sharedTailer.position = snapshotLength;
                    }
                    subscriber = new EndpointSubscriber(key, endpoint, snapshotLength);
                    sharedTailer.subscribers.add(subscriber);
                }
            }

            // Start outside the registry lock so the polling loop never blocks other viewers
            // from registering or unregistering.
            if (tailerToStart != null) {
                startDaemonThread(tailerToStart);
            }

            return new Subscription(subscriber, snapshotLength);
        }

        private static void unregister(String key, EndpointSubscriber subscriber) {
            synchronized (SHARED_TAILERS) {
                SharedTailer sharedTailer = SHARED_TAILERS.get(key);
                if (sharedTailer != null) {
                    synchronized (sharedTailer) {
                        sharedTailer.subscribers.remove(subscriber);
                        subscriber.close();
                        if (sharedTailer.subscribers.isEmpty()) {
                            SHARED_TAILERS.remove(key);

                            // No subscribers remain, so stop polling the file and release the worker.
                            sharedTailer.stop();
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    tailCurrentFile();
                    sleep();
                }
            } finally {
                synchronized (SHARED_TAILERS) {
                    if (SHARED_TAILERS.get(key) == this) {
                        SHARED_TAILERS.remove(key);
                    }
                }
                HostManager.resetProfile();
            }
        }

        private void tailCurrentFile() {
            if (!currentFile.exists()) {
                return;
            }

            // If rotation/truncation happens, start reading from the beginning of the new file.
            long length = currentFile.length();
            if (length < position) {
                position = 0L;
            }
            if (length == position) {
                return;
            }

            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(currentFile, "r");
                randomAccessFile.seek(position);

                String line;
                while (!stopped && (line = readLine(randomAccessFile)) != null) {
                    // Keep the byte offset with each line so subscribers can ignore historical
                    // lines already covered by their own current-file backfill.
                    position = randomAccessFile.getFilePointer();
                    broadcast(line, position);
                }
            } catch (FileNotFoundException e) {
                // The appender may rotate or create the file between polling intervals.
            } catch (IOException e) {
                if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                    LogUtil.error(LogViewerThread.class.getName(), e, "");
                }
            } finally {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private static String readLine(RandomAccessFile randomAccessFile) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            boolean hasBytes = false;
            int value;
            while ((value = randomAccessFile.read()) != -1) {
                hasBytes = true;
                if (value == '\n') {
                    break;
                }
                output.write(value);
            }

            if (!hasBytes) {
                return null;
            }

            byte[] bytes = output.toByteArray();
            int length = bytes.length;
            if (length > 0 && bytes[length - 1] == '\r') {
                length--;
            }
            return new String(bytes, 0, length, StandardCharsets.UTF_8);
        }

        private void broadcast(String line, long endOffset) {
            List<EndpointSubscriber> invalidSubscribers = new ArrayList<EndpointSubscriber>();
            boolean cleanupTailer;
            synchronized (this) {
                for (EndpointSubscriber subscriber : subscribers) {
                    if (!subscriber.offer(line, endOffset)) {
                        invalidSubscribers.add(subscriber);
                    }
                }
                subscribers.removeAll(invalidSubscribers);
                cleanupTailer = !invalidSubscribers.isEmpty() && subscribers.isEmpty();
            }

            for (EndpointSubscriber subscriber : invalidSubscribers) {
                subscriber.close();
            }
            if (cleanupTailer) {
                cleanupIfEmpty();
            }
        }

        private void cleanupIfEmpty() {
            synchronized (SHARED_TAILERS) {
                synchronized (this) {
                    if (subscribers.isEmpty() && SHARED_TAILERS.get(key) == this) {
                        SHARED_TAILERS.remove(key);
                        stop();
                    }
                }
            }
        }

        private void stop() {
            stopped = true;
        }

        private void sleep() {
            try {
                Thread.sleep(tailDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopped = true;
            }
        }

        private static class Subscription {
            private final EndpointSubscriber subscriber;
            private final long snapshotLength;

            private Subscription(EndpointSubscriber subscriber, long snapshotLength) {
                this.subscriber = subscriber;
                this.snapshotLength = snapshotLength;
            }

            private void activate() {
                subscriber.activate();
            }
        }
    }

    private static class EndpointSubscriber implements Runnable {
        private final String key;
        private final LogViewerEndpoint endpoint;
        private final long liveStartOffset;
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        private volatile boolean active = false;
        private volatile boolean closed = false;
        private Thread worker = null;

        private EndpointSubscriber(String key, LogViewerEndpoint endpoint, long liveStartOffset) {
            this.key = key;
            this.endpoint = endpoint;
            this.liveStartOffset = liveStartOffset;
        }

        private synchronized boolean offer(String line, long endOffset) {
            if (closed || endOffset <= liveStartOffset) {
                return !closed;
            }
            queue.offer(line);
            startWorkerIfActive();
            return true;
        }

        private synchronized void activate() {
            // Worker startup is delayed until backfill completes, preserving this viewer's order:
            // rolling file, current-file snapshot, then queued live lines.
            active = true;
            startWorkerIfActive();
        }

        private synchronized void startWorkerIfActive() {
            if (!active || closed || worker != null) {
                return;
            }
            worker = createDaemonThread(this);
            worker.start();
        }

        @Override
        public void run() {
            try {
                while (!closed) {
                    String line = queue.poll(1, TimeUnit.SECONDS);
                    if (line != null && !sendLine(endpoint, line)) {
                        SharedTailer.unregister(key, this);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                close();
            }
        }

        private synchronized void close() {
            closed = true;
            queue.clear();
        }
    }

    protected boolean sendLine(String line) {
        return sendLine(endpoint, line);
    }

    private static void startDaemonThread(Runnable runnable) {
        createDaemonThread(runnable).start();
    }

    private static Thread createDaemonThread(Runnable runnable) {
        Thread thread;
        try {
            thread = new PluginThread(runnable);
        } catch (RuntimeException e) {
            // Unit tests and other bare JVM contexts may not have DynamicDataSourceManager
            // initialized. These workers only poll files or send queued websocket lines.
            thread = new Thread(runnable);
        }
        thread.setDaemon(true);
        return thread;
    }

    private static boolean sendLine(LogViewerEndpoint endpoint, String line) {
        try {
            synchronized (endpoint) {
                endpoint.session.getBasicRemote().sendText(line, true);
            }
            return true;
        } catch (IllegalArgumentException | IllegalStateException ise) {
            // ignore
            return false;
        } catch (IOException e) {
            if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                LogUtil.error(LogViewerThread.class.getName(), e, "");
            }
            return false;
        }
    }
    
    public void close() {
        boolean shouldUnregister;
        EndpointSubscriber subscriberToUnregister;
        synchronized (lifecycleMonitor) {
            closed = true;
            shouldUnregister = registered;
            subscriberToUnregister = subscriber;
            registered = false;
            subscriber = null;
        }
        try {
            if (shouldUnregister && subscriberToUnregister != null) {
                SharedTailer.unregister(key, subscriberToUnregister);
            }
            BufferedReader readerToClose;
            InputStream inputStreamToClose;
            synchronized (streamMonitor) {
                readerToClose = reader;
                inputStreamToClose = inputStream;
            }
            closeFileHandles(readerToClose, inputStreamToClose);
        } finally {
            HostManager.resetProfile();
        }
    }   

    private void closeFileHandles(BufferedReader readerToClose, InputStream inputStreamToClose) {
        try {
            if (readerToClose != null) {
                readerToClose.close();
            }
            if (inputStreamToClose != null) {
                inputStreamToClose.close();
            }
        } catch (IOException e) {
        }
    }

    private static class LimitedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;

        private LimitedInputStream(InputStream delegate, long remaining) {
            this.delegate = delegate;
            this.remaining = remaining;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int value = delegate.read();
            if (value != -1) {
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int bytesToRead = (int) Math.min(length, remaining);
            int bytesRead = delegate.read(buffer, offset, bytesToRead);
            if (bytesRead != -1) {
                remaining -= bytesRead;
            }
            return bytesRead;
        }
    }
}
