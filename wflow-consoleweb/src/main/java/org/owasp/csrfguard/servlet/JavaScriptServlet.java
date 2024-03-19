/*
 * The OWASP CSRFGuard Project, BSD License
 * Copyright (c) 2011, Eric Sheridan (eric@infraredsecurity.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *     3. Neither the name of OWASP nor the names of its contributors may be used
 *        to endorse or promote products derived from this software without specific
 *        prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.owasp.csrfguard.servlet;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.CsrfGuardServletContextListener;
import org.owasp.csrfguard.CsrfValidator;
import org.owasp.csrfguard.config.properties.javascript.JavaScriptConfigParameters;
import org.owasp.csrfguard.session.LogicalSession;
import org.owasp.csrfguard.token.storage.LogicalSessionExtractor;
import org.owasp.csrfguard.token.transferobject.TokenTO;
import org.owasp.csrfguard.util.CsrfGuardUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;

public final class JavaScriptServlet extends HttpServlet {

    private static final long serialVersionUID = -1459584282530150483L;

    private static final String TOKEN_NAME_IDENTIFIER = "%TOKEN_NAME%";

    private static final String TOKEN_VALUE_IDENTIFIER = "%TOKEN_VALUE%";

    private static final String DOMAIN_ORIGIN_IDENTIFIER = "%DOMAIN_ORIGIN%";

    private static final String CONTEXT_PATH_IDENTIFIER = "%CONTEXT_PATH%";

    private static final String SERVLET_PATH_IDENTIFIER = "%SERVLET_PATH%";

    private static final String X_REQUESTED_WITH_IDENTIFIER = "%X_REQUESTED_WITH%";

    private static final String UNPROTECTED_EXTENSIONS_IDENTIFIER = "%UNPROTECTED_EXTENSIONS%";

    private static final String DYNAMIC_NODE_CREATION_EVENT_NAME_IDENTIFIER = "%DYNAMIC_NODE_CREATION_EVENT_NAME%";

    /**
     * Non-string configuration placeholder names that has to be replaced together with the single quotes
     * The single quotes around the attribute names are needed so the template code would be parsable by linters and automated code minifiers.
     */
    private static final String DOMAIN_STRICT_IDENTIFIER = "'%DOMAIN_STRICT%'";

    private static final String INJECT_INTO_XHR_IDENTIFIER = "'%INJECT_XHR%'";

    private static final String INJECT_INTO_FORMS_IDENTIFIER = "'%INJECT_FORMS%'";

    private static final String INJECT_GET_FORMS_IDENTIFIER = "'%INJECT_GET_FORMS%'";

    private static final String INJECT_FORM_ATTRIBUTES_IDENTIFIER = "'%INJECT_FORM_ATTRIBUTES%'";

    private static final String INJECT_INTO_ATTRIBUTES_IDENTIFIER = "'%INJECT_ATTRIBUTES%'";

    private static final String INJECT_INTO_DYNAMIC_NODES_IDENTIFIER = "'%INJECT_DYNAMIC_NODES%'";

    private static final String TOKENS_PER_PAGE_IDENTIFIER = "'%TOKENS_PER_PAGE%'";

    private static final String ASYNC_XHR = "'%ASYNC_XHR%'";

    private static final Map<String, BiFunction<CsrfGuard, HttpServletRequest, String>> JS_REPLACEMENT_MAP = new HashMap<>();

    static {
        /* CUSTOM : Adding a system property flag to disable domainStrict or set checking domain*/
        String domainStrict = System.getProperty("wflow.domainStrict");
        String domainOrigin = System.getProperty("wflow.csrfDomain");
        
        JS_REPLACEMENT_MAP.put(TOKEN_NAME_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(csrfGuard.getTokenName()));
        JS_REPLACEMENT_MAP.put(TOKEN_VALUE_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(getMasterToken(request, csrfGuard)));
        JS_REPLACEMENT_MAP.put(UNPROTECTED_EXTENSIONS_IDENTIFIER, (csrfGuard, request) -> String.valueOf(csrfGuard.getJavascriptUnprotectedExtensions()));
        JS_REPLACEMENT_MAP.put(CONTEXT_PATH_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(request.getContextPath()));
        JS_REPLACEMENT_MAP.put(SERVLET_PATH_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(request.getContextPath() + request.getServletPath()));
        JS_REPLACEMENT_MAP.put(X_REQUESTED_WITH_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(csrfGuard.getJavascriptXrequestedWith()));
        JS_REPLACEMENT_MAP.put(DYNAMIC_NODE_CREATION_EVENT_NAME_IDENTIFIER, (csrfGuard, request) -> StringUtils.defaultString(csrfGuard.getJavascriptDynamicNodeCreationEventName()));
        
        /* CUSTOM : allow custom domain used for validation */
        JS_REPLACEMENT_MAP.put(DOMAIN_ORIGIN_IDENTIFIER, (csrfGuard, request) -> ObjectUtils.defaultIfNull(domainOrigin != null?domainOrigin:csrfGuard.getDomainOrigin(), StringUtils.defaultString(parseDomain(request.getRequestURL()))));
        
        JS_REPLACEMENT_MAP.put(INJECT_INTO_FORMS_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isJavascriptInjectIntoForms()));
        JS_REPLACEMENT_MAP.put(INJECT_GET_FORMS_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isJavascriptInjectGetForms()));
        JS_REPLACEMENT_MAP.put(INJECT_FORM_ATTRIBUTES_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isJavascriptInjectFormAttributes()));
        JS_REPLACEMENT_MAP.put(INJECT_INTO_ATTRIBUTES_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isJavascriptInjectIntoAttributes()));
        JS_REPLACEMENT_MAP.put(INJECT_INTO_DYNAMIC_NODES_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isJavascriptInjectIntoDynamicallyCreatedNodes()));
        JS_REPLACEMENT_MAP.put(INJECT_INTO_XHR_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isAjaxEnabled()));
        JS_REPLACEMENT_MAP.put(TOKENS_PER_PAGE_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(csrfGuard.isTokenPerPageEnabled()));
        
        /* CUSTOM : override the domain strict value if system property exist*/
        JS_REPLACEMENT_MAP.put(DOMAIN_STRICT_IDENTIFIER, (csrfGuard, request) -> Boolean.toString(("false".equalsIgnoreCase(domainStrict))?false:csrfGuard.isJavascriptDomainStrict()));
        JS_REPLACEMENT_MAP.put(ASYNC_XHR, (csrfGuard, request) -> Boolean.toString(!csrfGuard.isForceSynchronousAjax()));
    }

    /* MIME Type constants */
    private static final String JSON_MIME_TYPE = "application/json";
    private static final String JAVASCRIPT_MIME_TYPE = "text/javascript; charset=utf-8";

    /**
     * whitelist the javascript servlet from csrf errors
     */
    private static final Set<String> javascriptUris = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptServlet.class);

    private static ServletConfig servletConfig = null;

    public static ServletConfig getStaticServletConfig() {
        return servletConfig;
    }

    /**
     * whitelist the javascript servlet from csrf errors
     *
     * @return the javascriptUris
     */
    public static Set<String> getJavascriptUris() {
        return javascriptUris;
    }

    @Override
    public void init(final ServletConfig theServletConfig) {
        servletConfig = theServletConfig;

        final CsrfGuard csrfGuard = CsrfGuard.getInstance();
        csrfGuard.initializeJavaScriptConfiguration();

        // print again since it might change based on servlet config of javascript servlet
        /* CUSTOM START : print config to log throw reflection exception
        CsrfGuardServletContextListener.printConfigIfConfigured(servletConfig.getServletContext(), "Printing properties after JavaScript servlet, note, the javascript properties have now been initialized: ");
        CUSTOM END */
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final CsrfGuard csrfGuard = CsrfGuard.getInstance();

        if (csrfGuard.isEnabled()) {
            if (CsrfGuardUtils.isPermittedUserAgent(request, response, csrfGuard)) {
                writeJavaScript(csrfGuard, request, response);
            }
        } else {
            response.setContentType(JAVASCRIPT_MIME_TYPE);
            final String javaScriptCode = "console.log('CSRFGuard is disabled');";
            response.getWriter().write(javaScriptCode);
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final CsrfGuard csrfGuard = CsrfGuard.getInstance();

        if (CsrfGuardUtils.isPermittedUserAgent(request, response, csrfGuard)) {
            /* CUSTOM START : for backward compatible to retrieve CSRF Token*/
            if (request.getHeader("FETCH-CSRF-TOKEN") != null 
                    || request.getHeader("FETCH-CSRF-TOKEN-PARAM") != null 
                    || request.getParameter("FETCH-CSRF-TOKEN-PARAM") != null) {

                try {
                    response.setContentType("text/plain");
                    response.getWriter().write(SecurityUtil.getCsrfTokenName() + ":" + SecurityUtil.getCsrfTokenValue(request));
                } catch (Exception e) {
                    LogUtil.error(JavaScriptServlet.class.getName(), e, "");
                }

                return;
            }
            /* CUSTOM END */
        
            if (new CsrfValidator().isValid(request, response)) {
                if (csrfGuard.isTokenPerPageEnabled()) {
                    // TODO pass the logical session downstream, see whether the null check can be done from here
                    final LogicalSession logicalSession = csrfGuard.getLogicalSessionExtractor().extract(request);
                    if (Objects.isNull(logicalSession)) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not create a logical session from the current request.");
                    } else {
                        final Map<String, String> pageTokens = csrfGuard.getTokenService().getPageTokens(logicalSession.getKey());
                        final TokenTO tokenTO = new TokenTO(pageTokens);
                        writeTokens(response, tokenTO);
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "This endpoint should not be invoked if the Token-Per-Page functionality is disabled!");
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Master token missing from the request.");
            }
        }
    }

    private static void writeTokens(final HttpServletResponse response, final TokenTO tokenTO) throws IOException {
        final String jsonTokenTO = tokenTO.toString();

        response.setContentType(JSON_MIME_TYPE);
        response.setContentLength(jsonTokenTO.length());
        response.setCharacterEncoding(Charset.defaultCharset().displayName());

        response.getWriter().write(jsonTokenTO);
    }

    private static void writeJavaScript(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final CsrfGuard csrfGuard = CsrfGuard.getInstance();

        /* cannot cache if rotate or token-per-page is enabled */
        if (csrfGuard.isRotateEnabled() || csrfGuard.isTokenPerPageEnabled()) {
            response.setHeader("Cache-Control", "no-cache, no-store");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        } else {
            response.setHeader("Cache-Control", csrfGuard.getJavascriptCacheControl());
        }

        response.setContentType(JAVASCRIPT_MIME_TYPE);

        final String[] replacementList = JS_REPLACEMENT_MAP.values().stream().map(v -> v.apply(csrfGuard, request)).toArray(String[]::new);

        final String code = StringUtils.replaceEach(csrfGuard.getJavascriptTemplateCode(), JS_REPLACEMENT_MAP.keySet().toArray(new String[0]), replacementList);

        response.getWriter().write(code);
    }

    private static String getMasterToken(final HttpServletRequest request, final CsrfGuard csrfGuard) {
        final LogicalSessionExtractor sessionKeyExtractor = csrfGuard.getLogicalSessionExtractor();
        final LogicalSession logicalSession = sessionKeyExtractor.extractOrCreate(request);

        return csrfGuard.getTokenService().getMasterToken(logicalSession.getKey());
    }

    private static String parseDomain(final StringBuffer url) {
        try {
            return new URL(url.toString()).getHost();
        } catch (final MalformedURLException e) {
            // Should not occur. javax.servlet.http.HttpServletRequest.getRequestURL should only return valid URLs.
            return "INVALID_URL: " + url;
        }
    }

    private void writeJavaScript(final CsrfGuard csrfGuard, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String refererHeader = request.getHeader("referer");

        final Pattern javascriptRefererPattern = csrfGuard.getJavascriptRefererPattern();
        final String javaScriptReferer = javascriptRefererPattern.pattern();

        if (refererHeader != null) {
            if (!javascriptRefererPattern.matcher(refererHeader).matches()) {
                LOGGER.error("Referer domain '{}' does not match regex: '{}'", refererHeader, javaScriptReferer);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (csrfGuard.isJavascriptRefererMatchDomain()) {
                final boolean isJavascriptRefererMatchProtocol = csrfGuard.isJavascriptRefererMatchProtocol();
                // this is something like http://something.com/path or https://something.com/path
                final String url = request.getRequestURL().toString();
                final String requestProtocolAndDomain = CsrfGuardUtils.httpProtocolAndDomain(url, isJavascriptRefererMatchProtocol);
                final String refererProtocolAndDomain = CsrfGuardUtils.httpProtocolAndDomain(refererHeader, isJavascriptRefererMatchProtocol);
                if (!refererProtocolAndDomain.equals(requestProtocolAndDomain)) {
                    LOGGER.error("Referer domain '{}' does not match request domain: '{}'", refererHeader, url);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        } else {
            if (!javaScriptReferer.equals(JavaScriptConfigParameters.DEFAULT_REFERER_PATTERN)) {
                LOGGER.error("Missing referer headers are not accepted if a non-default referer pattern '{}' is configured!", javaScriptReferer);

                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // save this path so javascript is whitelisted
        final String javascriptPath = request.getContextPath() + request.getServletPath();

        // don't know why there would be more than one... hmmm
        if (javascriptUris.size() < 100) {
            javascriptUris.add(javascriptPath);
        }

        writeJavaScript(request, response);
    }
}
