package org.joget.apps.form.service;

import com.lowagie.text.Image;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;

public class CustomITexResourceLoaderUserAgent extends ITextUserAgent {
    private ITextOutputDevice _outputDevice;

    public CustomITexResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
        super(outputDevice);
        
        this._outputDevice = outputDevice;
    }
    
    public ImageResource getImageResource(String uri) {
        ImageResource resource = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        if (uri != null && uri.startsWith("data:")) {
            // cut off "data:"
            try {
                String raw = uri.substring(5);
                String mime = raw.substring(0, raw.indexOf(';'));
                // looking for MimeTyp image/...
                if (mime != null && mime.startsWith("image/")) {
                    String base64data = raw.substring(mime.length() + 1);
                    if (base64data.startsWith("base64,")) {
                        // cut off "base64,"
                        String imgData = base64data.substring(7).trim();
                        byte[] img = Base64.decode(imgData);
                        Image image = Image.getInstance(img);
                        scaleToOutputResolution(image);
                        resource = new ImageResource(uri, new ITextFSImage(image));
                    }
                }
            } catch (Exception e) {
                LogUtil.error(CustomITexResourceLoaderUserAgent.class.getName(), e, "");
            }
        } else if (request != null && uri != null && uri.startsWith(request.getContextPath())) {
            uri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + uri;

            if (uri.contains("/web/client/app/") && uri.contains("/form/download/")) {
                CloseableHttpClient httpClient = null;
                ByteArrayOutputStream bos = null;
                InputStream is = null;
                try {
                    HttpClientBuilder httpClientBuilder = HttpClients.custom();
                    HttpGet get = new HttpGet(uri);

                    CookieStore cookieStore = new BasicCookieStore(); 
                    Cookie[] cookies = request.getCookies();
                    for (Cookie c : cookies) {
                        if (c.getName().equalsIgnoreCase("JSESSIONID")) {
                            BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", c.getValue());
                            cookie.setPath(request.getContextPath());
                            cookie.setDomain(request.getServerName());
                            cookieStore.addCookie(cookie); 
                        }
                    }

                    httpClientBuilder.setDefaultCookieStore(cookieStore);

                    if ("https".equals(request.getScheme())) {
                        SSLContextBuilder builder = new SSLContextBuilder();
                        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
                        httpClientBuilder.setSSLSocketFactory(sslsf);
                    }

                    // execute request
                    httpClient = httpClientBuilder.build();

                    HttpResponse response = httpClient.execute(get);
                    HttpEntity entity = response.getEntity();
                    if (entity != null && entity.getContentType() != null) {
                        is = entity.getContent();
                        entity.getContentType();

                        bos = new ByteArrayOutputStream();
                        int next = is.read();
                        while (next > -1) {
                            bos.write(next);
                            next = is.read();
                        }
                        bos.flush();
                        byte[] result = bos.toByteArray();

                        Image image = Image.getInstance(result);
                        scaleToOutputResolution(image);
                        resource = new ImageResource(uri, new ITextFSImage(image));
                    }
                } catch (Exception e) {
                    LogUtil.error(CustomITexResourceLoaderUserAgent.class.getName(), e, "");
                } finally {
                    try {
                        if (bos != null) {
                            bos.close();
                        }
                    } catch (Exception ex) {
                        LogUtil.error(CustomITexResourceLoaderUserAgent.class.getName(), ex, "");
                    }
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception ex) {
                        LogUtil.error(CustomITexResourceLoaderUserAgent.class.getName(), ex, "");
                    }
                    try {
                        if (httpClient != null) {
                            httpClient.close();
                        }
                    } catch (Exception ex) {
                        LogUtil.error(CustomITexResourceLoaderUserAgent.class.getName(), ex, "");
                    }
                }
            } else {
                resource = super.getImageResource(uri);
            }
        } else {
            resource = super.getImageResource(uri);
        }
        if (resource == null) {
            resource = new ImageResource(uri, null);
        }
        return resource;
    }
        
    private void scaleToOutputResolution(Image image) {
        float factor = getSharedContext().getDotsPerPixel();
        image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
    }
}