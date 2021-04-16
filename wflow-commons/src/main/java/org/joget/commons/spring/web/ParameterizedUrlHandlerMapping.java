package org.joget.commons.spring.web;

import java.util.HashMap;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Iterator;

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
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

/**
 * Substitute for Spring's SimpleUrlHandlerMapping that is capable of parsing parameters out of
 * URLs and adding them to the request.  When used alone, parameters will be placed in a Map which
 * is added to the request attributes.  If the
 * {@link carbonfive.spring.web.pathparameter.ParameterizedPathFilter} has been configured in
 * web.xml to handle the current request, each path parameter will appear as a request parameter
 * for all subsequent stages of the request processing, including command and form binding in Spring MVC.
 *
 * <p>To use this class, replace the SimpleUrlHandlerMapping bean in the dispatcher configuration file
 * with something that looks like this:
 * <code><pre>
 * &lt;bean class="carbonfive.spring.web.pathparameter.ParameterizedUrlHandlerMapping"&gt;
 *   &lt;property name="alwaysUseFullPath" value="true"/&gt;
 *   &lt;property name="mappings"&gt;
 *      &lt;value&gt;
 *        /view/noparameters=controller1
 *        /view/(bar\:foo)=controller2
 *        /view/(*.html\:html)=controller3
 *        /view/(**&#47;*:view).view=controller4
 *        /view/c/(*\:controller)/(*\:id)=controller5
 *      &lt;/value&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre></code>
 * The ParameterizedUrlHandlerMapping supports all mappings that are valid using SimpleUrlHandlerMapping's
 * default AntPathMatcher.  The special parenthetical paths allow one to parse parameters out of the
 * given path and add them to the request for further use.</p>
 *
 * <p>In the above example, hitting the URL, "/view/bar",
 * will cause the request to be routed to controller2 and a map containing the key/value pair 'foo'-&gt;'bar'
 * will be added as a request parameter with the key "ParameterizedUrlHandlerMapping.path-parameters".
 * A request to "/view/piglet.html" will route to controller3 and the parameter map will contain
 * 'html'-&gt;'piglet.html'. Controller4 will handle a request to "/view/this/that/the-other.view" and will
 * receive the parameter 'view'-&gt;'this/that/the-other'.  Finally a request to "/view/c/save/2342443' will
 * provide a parameters containing both 'controller'-&gt;'save' and 'id'-&gt;'2342443' to controller5.</p>
 *
 * <p>The backslash before the colon isn't really part of the pattern which is:
 * '(' + [ant_style_path] + ':' + [parameter_name] + ')'.  The backslash is required because the list
 * is parsed as a java properties file and it will interpret the colons as separators if you don't escape them.
 * This ugliness can be avoided by using the &lt;props&gt; tag instead.</p>
 *
 * <p>Since parsing parameterized URLs requires a custom path matcher, this class does not support setting
 * the <code>pathMatcher</code> property.  Attempts to do so will be ignored.</p>
 */
public class ParameterizedUrlHandlerMapping extends DefaultAnnotationHandlerMapping { //SimpleUrlHandlerMapping {

    public static final String PATH_PARAMETERS = "ParameterizedUrlHandlerMapping.path-parameters";
    private ParameterizedPathMatcher pathMatcher = null;

    private static Map<String, String> pathCache = new HashMap<String, String>();
    private static Map<String, Map<String, String>> parameterCache = new HashMap<String, Map<String, String>>();
    
    public ParameterizedUrlHandlerMapping() {
        pathMatcher = new ParameterizedPathMatcher();
        super.setPathMatcher(pathMatcher);
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        // do not replace parameterized matcher
    }

    protected Object lookupHandler(String urlPath, HttpServletRequest request) {
        Object handler = null;

        Map<String, Object> handlerMap = (Map<String, Object>) getHandlerMap();

        // Pattern match?
        Map<String, String> bestParameters = parameterCache.get(urlPath);
        String bestPathMatch = pathCache.get(urlPath);
        if (bestPathMatch == null || bestParameters == null) {
            for (Iterator it = handlerMap.keySet().iterator(); it.hasNext();) {
                String registeredPath = (String) it.next();
                Map<String, String> parameters = pathMatcher.namedParameters(registeredPath, urlPath);
                if ((parameters != null) && (bestPathMatch == null || bestPathMatch.length() <= registeredPath.length())) {
                    bestPathMatch = registeredPath;
                    bestParameters = parameters;
                }
            }
            pathCache.put(urlPath, bestPathMatch);
            parameterCache.put(urlPath, bestParameters);
        }
        if (bestPathMatch != null) {
            handler = handlerMap.get(bestPathMatch);
            exposePathWithinMapping(this.pathMatcher.extractPathWithinPattern(bestPathMatch, urlPath), urlPath, request);
            request.setAttribute(PATH_PARAMETERS, bestParameters);
        }
        return handler;
    }
}
