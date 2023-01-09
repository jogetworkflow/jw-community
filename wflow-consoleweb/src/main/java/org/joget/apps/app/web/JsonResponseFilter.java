package org.joget.apps.app.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.json.JSONObject;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

public class JsonResponseFilter implements Filter {

    private static class StatusCodeCaptureWrapper extends HttpServletResponseWrapper {

        public StatusCodeCaptureWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            buildJsonBody(sc, "");
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            buildJsonBody(sc, msg);
        }
        
        @Override
        public void setStatus(int sc) {
            buildJsonBody(sc, "");
        }

        @Override
        public void setStatus(int sc, String msg) {
            buildJsonBody(sc, msg);
        }
        
        private void buildJsonBody(int sc, String msg) {
            if (!super.isCommitted()) {
                if (sc >= 400) {
                    // replace error 500 with generic message
                    if (sc == 500) {
                        msg = "System error, please refer to log files";
                    }
                    try {
                        setContentType("application/json; charset=utf-8");

                        JSONObject jsonObject = new JSONObject();
                        Map error = new HashMap();
                        error.put("code", Integer.toString(sc));
                        error.put("message", msg);
                        error.put("date", new Date());
                        jsonObject.accumulate("error", error);

                        jsonObject.write(getWriter());

                        super.setStatus(sc, msg);
                        super.flushBuffer();
                    } catch (Exception e) {
                        LogUtil.error(JsonResponseFilter.class.getName(), e, "Unable to build json");
                    }
                } else {
                    super.setStatus(sc, msg);
                }
            }
        }
    }
    
    public void init(FilterConfig fc) throws ServletException {
        // nothing to init
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // reset profile and set hostname
            HostManager.initHost();
            
            StatusCodeCaptureWrapper wrappedResponse = new StatusCodeCaptureWrapper(httpResponse);
            
            String callback = httpRequest.getParameter("callback");
            
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            String jsonpWhitelist = setupManager.getSettingValue("jsonpWhitelist");
            String jsonpIPWhitelist = setupManager.getSettingValue("jsonpIPWhitelist");
            if (!("*".equals(jsonpWhitelist) || "*".equals(jsonpIPWhitelist))) {
                String domain = SecurityUtil.getDomainName(httpRequest.getHeader("referer"));
                String ip = AppUtil.getClientIp(httpRequest);
                List<String> whitelist = new ArrayList<String>();
                whitelist.add(httpRequest.getServerName());
                if (jsonpWhitelist != null) {
                    whitelist.addAll(Arrays.asList(jsonpWhitelist.split(";")));
                }
                List<String> ipWhitelist = new ArrayList<String>();
                if (jsonpIPWhitelist != null) {
                    ipWhitelist.addAll(Arrays.asList(jsonpIPWhitelist.split(";")));
                }
                if (!(SecurityUtil.isAllowedDomain(domain, whitelist) || SecurityUtil.isAllowedDomain(ip, ipWhitelist))) {
                    LogUtil.info(JsonResponseFilter.class.getName(), "Possible CSRF attack from url("+httpRequest.getRequestURI()+") referer(" + httpRequest.getHeader("referer") + ") IP(" + ip + ")");
                    wrappedResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST, "");
                    return;
                }
            }
            
            if (callback != null && !callback.isEmpty()) {
                wrappedResponse.setContentType("application/javascript; charset=utf-8");
            } else {
                wrappedResponse.setContentType("application/json; charset=utf-8");
                
                if (httpRequest.getHeader("Origin") != null) {
                    String origin = httpRequest.getHeader("Origin");
                    if (origin != null) {
                        origin = origin.replace("\n", "").replace("\r", "");
                    }
                    wrappedResponse.setHeader("Access-Control-Allow-Origin", origin);
                    wrappedResponse.setHeader("Access-Control-Allow-Credentials", "true");
                }
            }
            
            Throwable throwable = null;
            Integer status = null;
            
            try {
		filterChain.doFilter(httpRequest, wrappedResponse);	
            } catch (MissingServletRequestParameterException e) {
                throwable = e;
                status = HttpServletResponse.SC_BAD_REQUEST;
            } catch (ServletException e) {
                throwable = e.getRootCause();
                if (throwable == null) {
                    throwable = e;
                }
                if (throwable instanceof TypeMismatchException) {
                    status = HttpServletResponse.SC_BAD_REQUEST;
                }
            } catch(IllegalStateException ise) {
                //ignore
	    } catch (IllegalArgumentException e) {
                throwable = e;
                status = HttpServletResponse.SC_BAD_REQUEST;
            } catch (Exception e) {
		throwable = e;
	    } 
            
            if (throwable != null) {
                if (status == null) {
                    status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    LogUtil.error(JsonResponseFilter.class.getName(), throwable, null);
                }
                
                String message = throwable.getMessage();
                
                if (message == null) {
                    message = throwable.toString();
                }
	        wrappedResponse.setStatus(status, message);
            }
        }
    }

    public void destroy() {
        // nothing to destroy
    }
}
