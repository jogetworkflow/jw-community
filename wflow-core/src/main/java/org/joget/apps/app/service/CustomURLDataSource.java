package org.joget.apps.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.activation.URLDataSource;
import javax.servlet.http.HttpServletRequest;
import org.joget.workflow.util.WorkflowUtil;

public class CustomURLDataSource extends URLDataSource {
    private HttpURLConnection httpConn;
    private String referer;
    private String cookie;
    
    public CustomURLDataSource(URL u) {  
        super(u);
        HttpServletRequest httpRequest = WorkflowUtil.getHttpServletRequest();
        if (httpRequest != null && u.getHost().equals(httpRequest.getServerName())) {
            referer = httpRequest.getHeader("referer");
            if (referer == null || referer.isEmpty()) {
                referer = httpRequest.getRequestURL().toString();
            }
            cookie = httpRequest.getHeader("Cookie");
        }
    }
    
    @Override
    public String getContentType() {
        String type = null;
        
        if (httpConn != null) {
	    type = httpConn.getContentType();
        }

	if (type == null) {
	    type = "application/octet-stream";
        }
	
	return type;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        httpConn = (HttpURLConnection) super.getURL().openConnection();
        if (referer != null && cookie != null) {
            httpConn.setRequestProperty("referer", referer);
            httpConn.setRequestProperty("Cookie", cookie);
        }
        return httpConn.getInputStream();
    }
    
}
