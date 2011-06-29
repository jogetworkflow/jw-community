package org.joget.commons.spring.web;

import java.io.UnsupportedEncodingException;
import org.joget.commons.util.FileStore;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

public class ParameterizedAnnotationMethodHandlerAdapter extends AnnotationMethodHandlerAdapter {

    public ParameterizedAnnotationMethodHandlerAdapter() {
        setPathMatcher(new ParameterizedPathMatcher());
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS) != null) {
            request = new ParameterizedPathServletRequest(request);
        }
        return super.handle(request, response, handler);
    }

    private class ParameterizedPathServletRequest extends HttpServletRequestWrapper {

        private Map<String, String[]> parameters = null;

        public ParameterizedPathServletRequest(javax.servlet.http.HttpServletRequest request) {
            super(request);

            // reset profile and set hostname
            HostManager.setCurrentProfile(null);
            String hostname = request.getServerName();
            HostManager.setCurrentHost(hostname);

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
                FileStore.setFileMap(req.getFileMap());
            }

            Map<String, String> pathParameters = (Map<String, String>) super.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS);
            if (pathParameters != null) {
                setAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS, pathParameters);
            } else {
                parameters = super.getParameterMap();
            }
        }

        @Override
        public String getParameter(String key) {
            String[] values = getParameterValues(key);
            if ((values == null) || (values.length < 1)) {
                return null;
            }
            return values[0];
        }

        @Override
        public Map getParameterMap() {
            return parameters;
        }

        @Override
        public Enumeration getParameterNames() {
            return new IteratorEnumeration(parameters.keySet().iterator());
        }

        @Override
        public String[] getParameterValues(String string) {
            //return parameters.get(string);
            String[] values = parameters.get(string);
            if (values != null && values.length > 0) {
                if (getRequest() instanceof MultipartHttpServletRequest) {
                    // workaround for encoding bug in Spring https://jira.springframework.org/browse/SPR-6247
                    String[] encodedValues = new String[values.length];
                    for (int i=0; i<values.length; i++) {
                        try {
                            encodedValues[i] = new String(values[i].getBytes("ISO-8859-1"), "UTF-8");
                        } catch (UnsupportedEncodingException ex) {
                            LogUtil.warn(getClass().getName(), "Unsupported encoding for " + string + ": " + ex.toString());
                        }
                    }
                    return encodedValues;
                } else {
                    return values;
                }
            }

            return super.getParameterValues(string);
        }

        @Override
        public void setAttribute(String key, Object value) {
            if ((ParameterizedUrlHandlerMapping.PATH_PARAMETERS.equals(key)) && (value instanceof Map)) {
                Map<String, String[]> newParameters = new HashMap<String, String[]>();
                newParameters.putAll(super.getParameterMap());
                for (Map.Entry<String, String> entry : ((Map<String, String>) value).entrySet()) {
                    newParameters.put(entry.getKey(), new String[]{entry.getValue()});
                }
                this.parameters = Collections.unmodifiableMap(newParameters);
            }

            super.setAttribute(key, value);
        }
    }

    private class IteratorEnumeration implements Enumeration {

        private Iterator it = null;

        public IteratorEnumeration(Iterator it) {
            this.it = it;
        }

        public boolean hasMoreElements() {
            return it.hasNext();
        }

        public Object nextElement() {
            return it.next();
        }
    }
}
