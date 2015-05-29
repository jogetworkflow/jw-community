package org.joget.workflow.util;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.imageio.ImageIO;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

public class XpdlImageUtil {
    
    public static final String IMAGE_FOLDER = "app_xpdlImages";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String THUMBNAIL_PREFIX = "thumb-";
    public static final int THUMBNAIL_SIZE = 400;

    public static String getXpdlImagePath(String processDefId) {
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcess process = workflowManager.getProcess(processDefId);

        File path = new File(SetupManager.getBaseDirectory(), IMAGE_FOLDER + File.separator + process.getPackageId() + File.separator);
        return path.getAbsolutePath();
    }

    public static File getXpdlImage(String designerwebBaseUrl, String processDefId) {
        File file = new File(getXpdlImagePath(processDefId), processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId), processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    public static File getXpdlThumbnail(String designerwebBaseUrl, String processDefId) {
        File file = new File(getXpdlImagePath(processDefId), THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId), THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId) {
        generateXpdlImage(designerwebBaseUrl, processDefId, false);
    }

    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId, boolean asynchronous) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        
        TaskExecutor executor = (TaskExecutor) WorkflowUtil.getApplicationContext().getBean("xpdlImageExecutor");
        executor.execute(new XpdlImageTask(profile, designerwebBaseUrl, processDefId));
    }

    public static void createXpdlImage(String designerwebBaseUrl, String processDefId) {
        String baseDir = getXpdlImagePath(processDefId);
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcess process = workflowManager.getProcess(processDefId);
        byte[] xpdlBytes = workflowManager.getPackageContent(process.getPackageId(), process.getVersion());
            
        FileOutputStream fos = null;
        try {
            String fileName = processDefId + IMAGE_EXTENSION;
            File file = new File(baseDir);
            file.mkdirs();
            file = new File(baseDir, fileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            String url = designerwebBaseUrl + "/viewer/viewer.jsp?processId=" + process.getEncodedId();
            URL urlObj = new URL(url);
            if ("https".equals(urlObj.getProtocol())) {
                // add support for self-signed SSL
                String protocolPrefix = "custom";
                String protocol = protocolPrefix + "https";
                url = protocolPrefix + url;
                Protocol customHttps = new Protocol(protocol, new CustomSSLProtocolSocketFactory(), urlObj.getPort());
                Protocol.registerProtocol(protocol, customHttps);
            }

            HttpClient httpClient = new HttpClient();
            PostMethod post = new PostMethod(url);
            NameValuePair[] data = {
                new NameValuePair("xpdl", new String(xpdlBytes, "UTF-8")),
                new NameValuePair("packageId", process.getPackageId()),
                new NameValuePair("processId", processDefId)
            };
            post.setRequestBody(data);
            post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // execute request
            httpClient.executeMethod(post);

            InputStream is = post.getResponseBodyAsStream();

            byte[] buffer = new byte[1024];
            int byteReaded = is.read(buffer);
            while (byteReaded != -1) {
                bos.write(buffer, 0, byteReaded);
                byteReaded = is.read(buffer);
            }
            bos.flush();

            // output to file
            byte[] contents = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(contents);
            fos = new FileOutputStream(file);
            buffer = new byte[1024];
            byteReaded = bis.read(buffer);
            while (byteReaded != -1) {
                fos.write(buffer, 0, byteReaded);
                byteReaded = bis.read(buffer);
            }
            fos.flush();

            createThumbnail(baseDir, processDefId);
        } catch (Exception ex) {
            LogUtil.error(XpdlImageUtil.class.getName(), ex, "Error generating xpdl image [processDefId=" + processDefId + "]");
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ex) {
                LogUtil.error(XpdlImageUtil.class.getName(), ex, "");
            }
        }
    }
    
    public static void createThumbnail(String path, String processDefId) {
        int thumbWidth = THUMBNAIL_SIZE;
        int thumbHeight = THUMBNAIL_SIZE;

        BufferedOutputStream out = null;

        try{
            Image image = Toolkit.getDefaultToolkit().getImage(new File(path, processDefId + IMAGE_EXTENSION).getAbsolutePath());
            MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image, 0);
            mediaTracker.waitForID(0);

            double thumbRatio = (double) thumbWidth / (double) thumbHeight;
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            double imageRatio = (double) imageWidth / (double) imageHeight;
            if (thumbRatio < imageRatio) {
                thumbHeight = (int) (thumbWidth / imageRatio);
            } else {
                thumbWidth = (int) (thumbHeight * imageRatio);
            }

            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

            out = new BufferedOutputStream(new FileOutputStream(new File(path, THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION)));
            ImageIO.write(thumbImage, "jpeg", out);

            out.flush();
        } catch (Exception ex) {
            LogUtil.error(XpdlImageUtil.class.getName(), ex, "Error generating xpdl thumbnail [processDefId=" + processDefId + "]");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LogUtil.error(XpdlImageUtil.class.getName(), ex, "");
            }
        }
    }
    
    /**
     * TrustManager that accepts self-signed certificates. 
     */
    static class CustomX509TrustManager implements X509TrustManager {
        private X509TrustManager standardTrustManager = null;

        /**
         * Constructor for CustomX509TrustManager.
         */
        public CustomX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
            super();
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keystore);
            TrustManager[] trustmanagers = factory.getTrustManagers();
            if (trustmanagers.length == 0) {
                throw new NoSuchAlgorithmException("no trust manager found");
            }
            for (TrustManager tm: trustmanagers) {
                if (tm instanceof X509TrustManager) {
                    this.standardTrustManager = (X509TrustManager)trustmanagers[0];
                    break;
                }
            }
            if (this.standardTrustManager == null) {
                throw new NoSuchAlgorithmException("no trust manager found");
            }
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
         */
        public void checkClientTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
            standardTrustManager.checkClientTrusted(certificates,authType);
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
         */
        public void checkServerTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
            if ((certificates != null) && (certificates.length == 1)) {
                try {
                    certificates[0].checkValidity();
                } catch (CertificateExpiredException e) {
                    // accept expired certs
                }
            } else {
                standardTrustManager.checkServerTrusted(certificates,authType);
            }
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            return this.standardTrustManager.getAcceptedIssuers();
        }
    }

    /**
     * Used to create SSL links that accept self-signed certificates. 
     */
    static class CustomSSLProtocolSocketFactory implements ProtocolSocketFactory {

        private SSLContext sslcontext = null;

        /**
         * Constructor for CustomSSLProtocolSocketFactory.
         */
        public CustomSSLProtocolSocketFactory() {
            super();
        }

        private SSLContext createCustomSSLContext() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(
                  null, 
                  new TrustManager[] {new CustomX509TrustManager(null)}, 
                  null);
                return context;
            } catch (Exception e) {
                throw new HttpClientError(e.toString());
            }
        }

        private SSLContext getSSLContext() {
            if (this.sslcontext == null) {
                this.sslcontext = createCustomSSLContext();
            }
            return this.sslcontext;
        }

        /**
         * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
         */
        public Socket createSocket(
            String host,
            int port,
            InetAddress clientHost,
            int clientPort)
            throws IOException, UnknownHostException {

            return getSSLContext().getSocketFactory().createSocket(
                host,
                port,
                clientHost,
                clientPort
            );
        }

        /**
         * Attempts to get a new socket connection to the given host within the given time limit.
         * <p>
         * To circumvent the limitations of older JREs that do not support connect timeout a 
         * controller thread is executed. The controller thread attempts to create a new socket 
         * within the given limit of time. If socket constructor does not return until the 
         * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
         * </p>
         *  
         * @param host the host name/IP
         * @param port the port on the host
         * @param clientHost the local host name/IP to bind the socket to
         * @param clientPort the port on the local machine
         * @param params {@link HttpConnectionParams Http connection parameters}
         * 
         * @return Socket a new socket
         * 
         * @throws IOException if an I/O error occurs while creating the socket
         * @throws UnknownHostException if the IP address of the host cannot be
         * determined
         */
        public Socket createSocket(
            final String host,
            final int port,
            final InetAddress localAddress,
            final int localPort,
            final HttpConnectionParams params
        ) throws IOException, UnknownHostException, ConnectTimeoutException {
            if (params == null) {
                throw new IllegalArgumentException("Parameters may not be null");
            }
            int timeout = params.getConnectionTimeout();
            SocketFactory socketfactory = getSSLContext().getSocketFactory();
            if (timeout == 0) {
                return socketfactory.createSocket(host, port, localAddress, localPort);
            } else {
                Socket socket = socketfactory.createSocket();
                SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
                SocketAddress remoteaddr = new InetSocketAddress(host, port);
                socket.bind(localaddr);
                socket.connect(remoteaddr, timeout);
                return socket;
            }
        }

        /**
         * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
         */
        public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
            return getSSLContext().getSocketFactory().createSocket(
                host,
                port
            );
        }

        /**
         * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
         */
        public Socket createSocket(
            Socket socket,
            String host,
            int port,
            boolean autoClose)
            throws IOException, UnknownHostException {
            return getSSLContext().getSocketFactory().createSocket(
                socket,
                host,
                port,
                autoClose
            );
        }

        public boolean equals(Object obj) {
            return ((obj != null) && obj.getClass().equals(CustomSSLProtocolSocketFactory.class));
        }

        public int hashCode() {
            return CustomSSLProtocolSocketFactory.class.hashCode();
        }

    }    
}