package org.joget.commons.spring.web;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.*;
import java.util.*;
import java.io.IOException;

/*
Copyright 2007, Carbon Five, Inc.
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in
writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
 */
/**
 * Wraps the servlet request with an HttpServletRequestWrapper that, when given a request
 * attribute with the key "ParameterizedUrlHandlerMapping.path-parameters" whose value is a map,
 * will take all the Map's key/value pairs and add them as request parameters.  The parameters
 * will be available to all subsequent Filters, Servlet, Controllers, JSP's etc.
 * <br />
 * This filter should be mapped to all URLs that will be handled by a Spring DispatcherServlet
 * which is using the {@link carbonfive.spring.web.pathparameter.ParameterizedUrlHandlerMapping}
 * to route its requests.
 * <br />
 * Since this filter uses the {@link HttpServletRequestWrapper}, it may only be used in Servlet 2.3
 * applications.
 *
 * @author alex cruikshank
 */
public class ParameterizedPathFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Wraps the request with one that will add request parameters when given a request attribute
     * with the appropriate key.
     * @param request
     * @param response
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(new ParameterizedPathServletRequest((HttpServletRequest) request), response);
    }

    public void destroy() {
    }

    private class ParameterizedPathServletRequest extends HttpServletRequestWrapper {

        private Map<String, String[]> parameters = null;

        public ParameterizedPathServletRequest(javax.servlet.http.HttpServletRequest request) {
            super(request);
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
                return values;
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
