package org.joget.apps.app.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apm.APMUtil;

public class ExpireFilter implements Filter {
    
    public static final long DEFAULT_HEADER_CACHE_EXPIRY = 3600000L; // 1 hour
    public static long expires = DEFAULT_HEADER_CACHE_EXPIRY;
    
    private final static List<String> EXTS = Arrays.asList(new String[]{"css", "less", "js", "jpeg", "jpg", "png", "gif", "ico", "otf", "eot", "svg", "ttf", "woff", "woff2"});
    
    public void init(FilterConfig filterConfig) throws ServletException {
        String sysExpireStr = System.getProperty("resources.expires");
        if (sysExpireStr != null && !sysExpireStr.trim().isEmpty()) {
            try {
                expires = Long.parseLong(sysExpireStr) * 1000;
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            APMUtil.setTransactionName(httpRequest.getRequestURL().toString(), null);
            
            if (isWebResouces(httpRequest.getRequestURI())) {
                httpResponse.addDateHeader("Expires", System.currentTimeMillis() + expires);
                httpResponse.addHeader("Cache-Control", "private, max-age=" + expires/1000);
            }
        }
        
        // Continue
        chain.doFilter(request, response);
    }

    public void destroy() {
        
    }
    
    protected boolean isWebResouces (String url) {
        if (url != null) {
            if (url.endsWith(".")) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.contains(".")) {
                String extension = url.substring(url.lastIndexOf(".") + 1);
                if (EXTS.contains(extension)) {
                    return true;
                }
            }
        }
        return false;
    }
}
