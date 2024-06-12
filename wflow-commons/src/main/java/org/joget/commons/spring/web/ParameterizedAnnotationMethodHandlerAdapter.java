package org.joget.commons.spring.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.HostManager;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.util.UriUtils;

public class ParameterizedAnnotationMethodHandlerAdapter extends RequestMappingHandlerAdapter {

    public ParameterizedAnnotationMethodHandlerAdapter() {
        super();
    }

    @Override
    protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) throws Exception {
        if (request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS) != null) {
            request = new ParameterizedPathServletRequest(request);
        }
        return super.handleInternal(request, response, handler);
    }

    private class ParameterizedPathServletRequest extends HttpServletRequestWrapper {

        private Map<String, String[]> parameters = new HashMap<String, String[]>();

        public ParameterizedPathServletRequest(javax.servlet.http.HttpServletRequest request) {
            super(request);

            // reset profile and set hostname
            HostManager.initHost();

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
                FileStore.clear();
                Map<String, MultipartFile[]> fileMap = new HashMap<String, MultipartFile[]>();
                
                MultiValueMap<String, MultipartFile> multiValueFileMap= req.getMultiFileMap();
                for (String fieldName : multiValueFileMap.keySet()) {
                    fileMap.put(fieldName, multiValueFileMap.get(fieldName).toArray(new MultipartFile[]{}));
                }
                
                FileStore.setFileMap(fileMap);
            } else {
                FileStore.clear();
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
            return parameters.get(string);
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

        @Override
        public String getQueryString() {
            String queryString = super.getQueryString();
            if (queryString != null) {
                String escapedQueryString = UriUtils.encodeQuery(queryString, "UTF-8");
                return escapedQueryString;
            } else {
                return queryString;
            }
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
