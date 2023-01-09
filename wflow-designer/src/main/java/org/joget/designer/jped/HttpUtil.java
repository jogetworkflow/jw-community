package org.joget.designer.jped;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.net.ssl.SSLException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.enhydra.jawe.ResourceManager;
import org.joget.designer.Designer;

/**
 * Utility methods for making HTTP requests.
 */
public class HttpUtil {
    
    public static boolean SSL_TRUSTED = false;
    
    /**
     * Make a HTTP POST request to a URL, passing in a JSESSIONID cookie, 
     * with support for SSL (including prompting a warning for self-signed certs).
     * If the JSESSIONID is invalid, then a password dialog appears.
     * @param cookieStore
     * @param url
     * @param port
     * @param sessionId
     * @param cookieDomain
     * @param cookiePath
     * @param username
     * @param password
     * @param trustAllSsl
     * @param failOnError
     * @param filename
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws AuthenticationException 
     */
    public static String httpPost(CookieStore cookieStore, String url, int port, String sessionId, String cookieDomain, String cookiePath, String username, String password, boolean trustAllSsl, boolean failOnError, String filename, File file) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, AuthenticationException {
        String contents = null;
        
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        // Set no redirect
        httpClientBuilder.setRedirectStrategy(new RedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
                return false;
            }

            @Override
            public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
                return null;
            }
        });

        // set csrf token in URL
        if (url.contains("?" + Designer.TOKEN_NAME)) {
            url = url.substring(0, url.indexOf("?" + Designer.TOKEN_NAME));
        }
        url += "?" + Designer.TOKEN_NAME + "=" + URLEncoder.encode(Designer.TOKEN_VALUE, "UTF-8");
        // Prepare a request object
        HttpPost httpRequest = new HttpPost(url);
        if (file != null) {
            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("packageXpdl", new FileBody(file)).build();
            httpRequest.setEntity(reqEntity);            
        } else {
            if (username != null && password != null) {
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("j_username", username));
                formparams.add(new BasicNameValuePair("j_password", password));
                formparams.add(new BasicNameValuePair("username", username));
                formparams.add(new BasicNameValuePair("password", password));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpRequest.setEntity(entity);
            }
        }
        // set referer header
        String referer = "http://" + Designer.DOMAIN;
        httpRequest.addHeader("Referer", referer);
        
        // Set session cookie
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
        }
        if (sessionId != null) {
            BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", sessionId);
            cookie.setDomain(cookieDomain);
            cookie.setPath(cookiePath);
            cookieStore.addCookie(cookie); 
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);                 

        // Prepare SSL trust
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        if (SSL_TRUSTED || trustAllSsl) {
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }
        
        // Execute the request
        CloseableHttpClient httpClient = httpClientBuilder.build();
        try {
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpRequest);
            } catch (SSLException se) {
                int result = JOptionPane.showConfirmDialog(null, ResourceManager.getLanguageDependentString("InvalidSSLPrompt"), ResourceManager.getLanguageDependentString("InvalidSSLTitle"), JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    httpClientBuilder.setSSLSocketFactory(sslsf);
                    httpClient = httpClientBuilder.build();
                    response = httpClient.execute(httpRequest);
                    SSL_TRUSTED = true;
                }
            }

            // Examine the response status
            if (response == null) {
                throw new HttpResponseException(403, ResourceManager.getLanguageDependentString("InvalidSSLMessage"));
            }
            StatusLine status = response.getStatusLine();
            if (status  == null || status.getStatusCode() == 302 || status.getStatusCode() == 401 || status.getStatusCode() == 500) {
                if (failOnError) {
                    throw new AuthenticationException(ResourceManager.getLanguageDependentString("AuthenticationFailed"));
                }
                // Request is unauthenticated, attempt to authenticate
                String credentials = password;
                if (credentials == null) {
                    // prompt for username and password
                    JTextField uField = new JTextField(15);
                    uField.setText(username);
                    JPasswordField pField = new JPasswordField(15);
                    pField.addHierarchyListener(new HierarchyListener() {
                        public void hierarchyChanged(HierarchyEvent e) {
                            final Component c = e.getComponent();
                            if (c.isShowing() && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                                Window toplevel = SwingUtilities.getWindowAncestor(c);
                                toplevel.addWindowFocusListener(new WindowAdapter() {
                                    @Override
                                    public void windowGainedFocus(WindowEvent e) {
                                        c.requestFocus();
                                    }
                                });
                            }
                        }
                    });
                    JPanel pPanel = new JPanel(new GridLayout(2,2));
                    pPanel.add(new JLabel(ResourceManager.getLanguageDependentString("UsernameKey")));
                    pPanel.add(uField);
                    pPanel.add(new JLabel(ResourceManager.getLanguageDependentString("PasswordKey")));
                    pPanel.add(pField);
                    int okCxl = JOptionPane.showConfirmDialog(null, pPanel, ResourceManager.getLanguageDependentString("SessionTimedOut"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (okCxl == JOptionPane.OK_OPTION) {
                        username = uField.getText();
                        credentials = new String(pField.getPassword());
                        Designer.USERNAME = username;
                    } else if (okCxl == JOptionPane.CANCEL_OPTION) {
                        return null;
                    }
                }
                try {
                    // login and store session cookie
                    String loginUrl = Designer.URLPATH + "/web/json/directory/user/sso";
                    String ssoResponse = HttpUtil.httpPost(cookieStore, loginUrl, port, null, cookieDomain, cookiePath, username, credentials, true, true, null, null);
                    String isAdminStr = "isAdmin";
                    if (!ssoResponse.contains(isAdminStr)) {
                        throw new AuthenticationException();
                    }
                    List<Cookie> cookies = cookieStore.getCookies();
                    for (Cookie ck: cookies) {
                        if ("JSESSIONID".equalsIgnoreCase(ck.getName())) {
                            sessionId = ck.getValue();
                            Designer.SESSION = sessionId;
                        }
                    }

                    // set new csrf token
                    String tokenAttr = "\"token\":\"";
                    if (ssoResponse.contains(tokenAttr)) {
                        String csrfToken = ssoResponse.substring(ssoResponse.indexOf(tokenAttr) + tokenAttr.length(), ssoResponse.length()-2);
                        StringTokenizer st = new StringTokenizer(csrfToken, "=");
                        if (st.countTokens() == 2) {
                            Designer.TOKEN_NAME = st.nextToken();
                            Designer.TOKEN_VALUE = st.nextToken();
                        }
                    }

                    // repeat request with session cookie
                    contents = HttpUtil.httpPost(cookieStore, url, port, sessionId, cookieDomain, cookiePath, null, null, true, true, filename, file);

                    // return contents
                    return contents;
                } catch(AuthenticationException ate) {
                    JOptionPane.showMessageDialog(null, ResourceManager.getLanguageDependentString("InvalidLogin"));
                    // repeat request
                    return HttpUtil.httpPost(null, url, port, sessionId, cookieDomain, cookiePath, username, null, false, false, filename, file);
                } catch(HttpResponseException hre) {
                    throw new AuthenticationException(ResourceManager.getLanguageDependentString("InvalidLogin"));
                }            
            }

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();

            // If the response does not enclose an entity, there is no need
            // to worry about connection release
            if (entity != null) {
                InputStream instream = null;
                try {
                    instream = entity.getContent();
                    contents = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                    String line = reader.readLine();
                    while (line != null) {
                        contents += line;
                        line = reader.readLine();
                    }

                } catch (IOException ex) {

                    // In case of an IOException the connection will be released
                    // back to the connection manager automatically
                    throw ex;

                } catch (RuntimeException ex) {

                    // In case of an unexpected exception you may want to abort
                    // the HTTP request in order to shut down the underlying
                    // connection and release it back to the connection manager.
                    httpRequest.abort();
                    throw ex;

                } finally {

                    // Closing the input stream will trigger connection release
                    if (instream != null) {
                        instream.close();
                    }
                    
                }
            }    
        } finally {
            // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpClient.close();
        }
        return contents;
    }
    
}
