package org.joget.logs;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LogViewerThreadTest {

    private static final long TAIL_DELAY_MS = 50L;
    private static final long WAIT_TIMEOUT_MS = 3000L;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sendsRollingLinesBeforeCurrentFileLines() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");
        writeLines(rollingFile, "rolling-1", "rolling-2");
        writeLines(currentFile, "current-1");

        CapturingSession capturingSession = new CapturingSession();
        LogViewerEndpoint endpoint = endpoint(capturingSession.session);
        LogViewerThread logViewer = new LogViewerThread("default", "app", endpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread thread = start(logViewer);

        try {
            assertTrue(awaitSize(capturingSession.messages, 3));
            assertEquals(Arrays.asList("rolling-1", "rolling-2", "current-1"), new ArrayList<String>(capturingSession.messages));
        } finally {
            close(logViewer, thread);
        }
    }

    @Test
    public void streamsNewlyAppendedCurrentFileLines() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");
        writeLines(currentFile, "current-1");

        CapturingSession capturingSession = new CapturingSession();
        LogViewerEndpoint endpoint = endpoint(capturingSession.session);
        LogViewerThread logViewer = new LogViewerThread("default", "app", endpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread thread = start(logViewer);

        try {
            assertTrue(awaitContains(capturingSession.messages, "current-1"));
            appendLines(currentFile, "current-2");

            assertTrue(awaitContains(capturingSession.messages, "current-2"));
        } finally {
            close(logViewer, thread);
        }
    }

    @Test
    public void closeStopsTailingCurrentFile() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");
        writeLines(currentFile, "current-1");

        CapturingSession capturingSession = new CapturingSession();
        LogViewerEndpoint endpoint = endpoint(capturingSession.session);
        LogViewerThread logViewer = new LogViewerThread("default", "app", endpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread thread = start(logViewer);

        assertTrue(awaitContains(capturingSession.messages, "current-1"));
        close(logViewer, thread);

        int messageCountAfterClose = capturingSession.messages.size();
        appendLines(currentFile, "current-2");
        Thread.sleep(TAIL_DELAY_MS * 4);

        assertFalse(thread.isAlive());
        assertEquals(messageCountAfterClose, capturingSession.messages.size());
    }

    @Test
    public void streamsCurrentFileCreatedAfterViewerStarts() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = new File(temporaryFolder.getRoot(), "viewer.log");

        CapturingSession capturingSession = new CapturingSession();
        LogViewerEndpoint endpoint = endpoint(capturingSession.session);
        LogViewerThread logViewer = new LogViewerThread("default", "app", endpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread thread = start(logViewer);

        try {
            Thread.sleep(TAIL_DELAY_MS * 3);
            writeLines(currentFile, "created-later");

            assertTrue(awaitContains(capturingSession.messages, "created-later"));
        } finally {
            close(logViewer, thread);
        }
    }

    @Test
    public void sharesCurrentFileTailerAcrossViewersForSameLog() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");
        writeLines(currentFile, "current-1");

        CapturingSession firstSession = new CapturingSession();
        LogViewerEndpoint firstEndpoint = endpoint(firstSession.session);
        LogViewerThread firstLogViewer = new LogViewerThread("default", "app", firstEndpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread firstThread = start(firstLogViewer);

        CapturingSession secondSession = new CapturingSession();
        LogViewerEndpoint secondEndpoint = endpoint(secondSession.session);
        LogViewerThread secondLogViewer = new LogViewerThread("default", "app", secondEndpoint, "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread secondThread = null;

        try {
            assertTrue(awaitContains(firstSession.messages, "current-1"));
            assertTrue(awaitActiveTailerCount(1));

            secondThread = start(secondLogViewer);
            assertTrue(awaitContains(secondSession.messages, "current-1"));
            assertTrue(awaitActiveTailerCount(1));

            appendLines(currentFile, "current-2");

            assertTrue(awaitContains(firstSession.messages, "current-2"));
            assertTrue(awaitContains(secondSession.messages, "current-2"));
        } finally {
            close(firstLogViewer, firstThread);
            if (secondThread != null) {
                close(secondLogViewer, secondThread);
            } else {
                secondLogViewer.close();
            }
            assertTrue(awaitActiveTailerCount(0));
        }
    }

    @Test
    public void buffersLiveLinesAppendedWhileCurrentFileIsBackfilled() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");
        writeLines(currentFile, "current-1");

        CapturingSession firstSession = new CapturingSession();
        LogViewerThread firstLogViewer = new LogViewerThread("default", "app", endpoint(firstSession.session), "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread firstThread = start(firstLogViewer);

        CapturingSession secondSession = new CapturingSession();
        LogViewerThread secondLogViewer = new LogViewerThread("default", "app", endpoint(secondSession.session), "node", rollingFile, currentFile, TAIL_DELAY_MS) {
            private boolean appended = false;

            @Override
            protected boolean sendLine(String line) {
                boolean sent = super.sendLine(line);
                if ("current-1".equals(line) && !appended) {
                    appended = true;
                    try {
                        appendLines(currentFile, "live-during-backfill");
                        Thread.sleep(TAIL_DELAY_MS * 3);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return sent;
            }
        };
        Thread secondThread = null;

        try {
            assertTrue(awaitContains(firstSession.messages, "current-1"));
            assertTrue(awaitActiveTailerCount(1));

            secondThread = start(secondLogViewer);

            assertTrue(awaitContains(secondSession.messages, "current-1"));
            assertTrue(awaitContains(secondSession.messages, "live-during-backfill"));
            assertEquals(1, countOccurrences(secondSession.messages, "live-during-backfill"));
        } finally {
            close(firstLogViewer, firstThread);
            if (secondThread != null) {
                close(secondLogViewer, secondThread);
            } else {
                secondLogViewer.close();
            }
            assertTrue(awaitActiveTailerCount(0));
        }
    }

    @Test
    public void slowWebsocketDoesNotBlockOtherViewers() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");

        CapturingSession slowSession = new CapturingSession(1200L);
        LogViewerThread slowLogViewer = new LogViewerThread("default", "app", endpoint(slowSession.session), "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread slowThread = start(slowLogViewer);

        CapturingSession fastSession = new CapturingSession();
        LogViewerThread fastLogViewer = new LogViewerThread("default", "app", endpoint(fastSession.session), "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread fastThread = null;

        try {
            assertTrue(awaitActiveTailerCount(1));
            fastThread = start(fastLogViewer);
            Thread.sleep(TAIL_DELAY_MS * 2);

            appendLines(currentFile, "live-line");

            assertTrue(awaitContains(fastSession.messages, "live-line", 500L));
        } finally {
            close(slowLogViewer, slowThread);
            if (fastThread != null) {
                close(fastLogViewer, fastThread);
            } else {
                fastLogViewer.close();
            }
            assertTrue(awaitActiveTailerCount(0));
        }
    }

    @Test
    public void burstLargerThanLegacyQueueDoesNotDisconnectViewer() throws Exception {
        File rollingFile = temporaryFolder.newFile("viewer.log.rolling");
        File currentFile = temporaryFolder.newFile("viewer.log");

        CountDownLatch firstSendStarted = new CountDownLatch(1);
        CountDownLatch releaseSend = new CountDownLatch(1);
        CapturingSession capturingSession = new CapturingSession(firstSendStarted, releaseSend);
        LogViewerThread logViewer = new LogViewerThread("default", "app", endpoint(capturingSession.session), "node", rollingFile, currentFile, TAIL_DELAY_MS);
        Thread thread = start(logViewer);

        try {
            assertTrue(awaitActiveTailerCount(1));

            String[] burstLines = new String[1105];
            for (int i = 0; i < burstLines.length; i++) {
                burstLines[i] = "burst-" + i;
            }
            appendLines(currentFile, burstLines);

            assertTrue(firstSendStarted.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
            Thread.sleep(TAIL_DELAY_MS * 4);
            assertTrue(awaitActiveTailerCount(1));

            releaseSend.countDown();
            assertTrue(awaitContains(capturingSession.messages, "burst-1104"));
        } finally {
            releaseSend.countDown();
            close(logViewer, thread);
            assertTrue(awaitActiveTailerCount(0));
        }
    }

    private LogViewerEndpoint endpoint(Session session) {
        LogViewerEndpoint endpoint = new LogViewerEndpoint();
        endpoint.session = session;
        return endpoint;
    }

    private Thread start(LogViewerThread logViewer) {
        Thread thread = new Thread(logViewer);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private void close(LogViewerThread logViewer, Thread thread) throws Exception {
        logViewer.close();
        thread.join(WAIT_TIMEOUT_MS);
    }

    private void writeLines(File file, String... lines) throws Exception {
        Files.write(file.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    private void appendLines(File file, String... lines) throws Exception {
        Files.write(file.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private boolean awaitSize(List<String> messages, int size) throws InterruptedException {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (messages.size() >= size) {
                return true;
            }
            Thread.sleep(TAIL_DELAY_MS);
        }
        return false;
    }

    private boolean awaitContains(List<String> messages, String message) throws InterruptedException {
        return awaitContains(messages, message, WAIT_TIMEOUT_MS);
    }

    private boolean awaitContains(List<String> messages, String message, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (messages.contains(message)) {
                return true;
            }
            Thread.sleep(TAIL_DELAY_MS);
        }
        return false;
    }

    private boolean awaitActiveTailerCount(int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (LogViewerThread.getActiveTailerCount() == count) {
                return true;
            }
            Thread.sleep(TAIL_DELAY_MS);
        }
        return false;
    }

    private int countOccurrences(List<String> messages, String message) {
        int count = 0;
        for (String current : messages) {
            if (message.equals(current)) {
                count++;
            }
        }
        return count;
    }

    private static class CapturingSession {
        private final List<String> messages = new CopyOnWriteArrayList<String>();
        private final Session session;

        private CapturingSession() {
            this(0L);
        }

        private CapturingSession(final long sendDelayMillis) {
            this(sendDelayMillis, null, null);
        }

        private CapturingSession(final CountDownLatch sendStarted, final CountDownLatch releaseSend) {
            this(0L, sendStarted, releaseSend);
        }

        private CapturingSession(final long sendDelayMillis, final CountDownLatch sendStarted, final CountDownLatch releaseSend) {
            final RemoteEndpoint.Basic basicRemote = (RemoteEndpoint.Basic) Proxy.newProxyInstance(
                    RemoteEndpoint.Basic.class.getClassLoader(),
                    new Class[]{RemoteEndpoint.Basic.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("sendText".equals(method.getName()) && args != null && args.length > 0) {
                                if (sendStarted != null) {
                                    sendStarted.countDown();
                                    releaseSend.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                                }
                                if (sendDelayMillis > 0) {
                                    try {
                                        Thread.sleep(sendDelayMillis);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                                messages.add((String) args[0]);
                            }
                            return null;
                        }
                    });

            session = (Session) Proxy.newProxyInstance(
                    Session.class.getClassLoader(),
                    new Class[]{Session.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("getBasicRemote".equals(method.getName())) {
                                return basicRemote;
                            }
                            return null;
                        }
                    });
        }
    }
}
