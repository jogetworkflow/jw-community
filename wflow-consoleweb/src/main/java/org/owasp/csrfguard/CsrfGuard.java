/**
 * The OWASP CSRFGuard Project, BSD License Eric Sheridan
 * (eric@infraredsecurity.com), Copyright (c) 2011 All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of OWASP nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.owasp.csrfguard;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.owasp.csrfguard.action.IAction;
import org.owasp.csrfguard.config.ConfigurationProvider;
import org.owasp.csrfguard.config.ConfigurationProviderFactory;
import org.owasp.csrfguard.config.NullConfigurationProvider;
import org.owasp.csrfguard.config.PropertiesConfigurationProvider;
import org.owasp.csrfguard.config.overlay.ExpirableCache;
import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;
import org.owasp.csrfguard.servlet.JavaScriptServlet;
import org.owasp.csrfguard.util.CsrfGuardUtils;
import org.owasp.csrfguard.util.RandomGenerator;
import org.owasp.csrfguard.util.Streams;
import org.owasp.csrfguard.util.Writers;

public final class CsrfGuard {

    public final static String PAGE_TOKENS_KEY = "Owasp_CsrfGuard_Pages_Tokens_Key";

    private Properties properties = null;

    /**
     * cache the configuration for a minute
     */
    private static ExpirableCache<Boolean, ConfigurationProvider> configurationProviderExpirableCache = new ExpirableCache<Boolean, ConfigurationProvider>(1);

    private ConfigurationProvider config() {
        if (this.properties == null) {
            return new NullConfigurationProvider();
        }

        ConfigurationProvider configurationProvider = configurationProviderExpirableCache.get(Boolean.TRUE);

        synchronized (CsrfGuard.class) {
            if (configurationProvider == null) {
                configurationProvider = retrieveNewConfig();
            } else if (!configurationProvider.isCacheable()) {
                //dont synchronize if not cacheable
                configurationProvider = retrieveNewConfig();
            }
        }
        
        return configurationProvider;
    }

    /**
     * @return new provider
     */
    private ConfigurationProvider retrieveNewConfig() {
        ConfigurationProvider configurationProvider = null;
        //lets see what provider we are using
        String configurationProviderFactoryClassName = this.properties.getProperty(
                "org.owasp.csrfguard.configuration.provider.factory", PropertiesConfigurationProvider.class.getName());

        Class<ConfigurationProviderFactory> configurationProviderFactoryClass = CsrfGuardUtils.forName(configurationProviderFactoryClassName);

        ConfigurationProviderFactory configurationProviderFactory = CsrfGuardUtils.newInstance(configurationProviderFactoryClass);

        configurationProvider = configurationProviderFactory.retrieveConfiguration(this.properties);
        configurationProviderExpirableCache.put(Boolean.TRUE, configurationProvider);
        return configurationProvider;
    }

    private static class SingletonHolder {

        public static final CsrfGuard instance = new CsrfGuard();
    }

    public static CsrfGuard getInstance() {
        return SingletonHolder.instance;
    }

    public static void load(Properties theProperties) throws NoSuchAlgorithmException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, NoSuchProviderException {

        getInstance().properties = theProperties;
    }

    public CsrfGuard() {
    }

    public ILogger getLogger() {
        return config().getLogger();
    }

    public String getTokenName() {
        return config().getTokenName();
    }

    public int getTokenLength() {
        return config().getTokenLength();
    }

    public boolean isRotateEnabled() {
        return config().isRotateEnabled();
    }

    public boolean isTokenPerPageEnabled() {
        return config().isTokenPerPageEnabled();
    }

    public boolean isTokenPerPagePrecreate() {
        return config().isTokenPerPagePrecreateEnabled();
    }

    /**
     * If csrf guard filter should check even if there is no session for the
     * user Note: this changed in 2014/04/20, the default behavior used to be to
     * not check if there is no session. If you want the legacy behavior (if
     * your app is not susceptible to CSRF if the user has no session), set this
     * to false
     *
     * @return if true
     */
    public boolean isValidateWhenNoSessionExists() {
        return config().isValidateWhenNoSessionExists();
    }

    public SecureRandom getPrng() {
        return config().getPrng();
    }

    public String getNewTokenLandingPage() {
        return config().getNewTokenLandingPage();
    }

    public boolean isUseNewTokenLandingPage() {
        return config().isUseNewTokenLandingPage();
    }

    public boolean isAjaxEnabled() {
        return config().isAjaxEnabled();
    }

    public boolean isProtectEnabled() {
        return config().isProtectEnabled();
    }

    /**
     * @see ConfigurationProvider#isEnabled()
     * @return if enabled
     */
    public boolean isEnabled() {
        return config().isEnabled();
    }

    public String getSessionKey() {
        return config().getSessionKey();
    }

    public Set<String> getProtectedPages() {
        return config().getProtectedPages();
    }

    public Set<String> getUnprotectedPages() {
        return config().getUnprotectedPages();
    }

    /**
     * cache regex patterns here
     */
    private Map<String, Pattern> regexPatternCache = new HashMap<String, Pattern>();

    public Set<String> getProtectedMethods() {
        return config().getProtectedMethods();
    }

    public List<IAction> getActions() {
        return config().getActions();
    }

    public String getJavascriptSourceFile() {
        return config().getJavascriptSourceFile();
    }

    /**
     * @see ConfigurationProvider#isJavascriptInjectFormAttributes()
     * @return if inject
     */
    public boolean isJavascriptInjectFormAttributes() {
        return config().isJavascriptInjectFormAttributes();
    }

    /**
     * @see ConfigurationProvider#isJavascriptInjectGetForms()
     * @return if inject
     */
    public boolean isJavascriptInjectGetForms() {
        return config().isJavascriptInjectGetForms();
    }

    public boolean isJavascriptDomainStrict() {
        return config().isJavascriptDomainStrict();
    }

    public boolean isJavascriptRefererMatchDomain() {
        return config().isJavascriptRefererMatchDomain();
    }

    public String getJavascriptCacheControl() {
        return config().getJavascriptCacheControl();
    }

    public Pattern getJavascriptRefererPattern() {
        return config().getJavascriptRefererPattern();
    }

    public boolean isJavascriptInjectIntoForms() {
        return config().isJavascriptInjectIntoForms();
    }

    public boolean isJavascriptInjectIntoAttributes() {
        return config().isJavascriptInjectIntoAttributes();
    }

    public String getJavascriptXrequestedWith() {
        return config().getJavascriptXrequestedWith();
    }

    public String getJavascriptTemplateCode() {
        return config().getJavascriptTemplateCode();
    }

    public String getTokenValue(HttpServletRequest request) {
        return getTokenValue(request, request.getRequestURI());
    }

    public String getTokenValue(HttpServletRequest request, String uri) {
        String tokenValue = null;
        HttpSession session = request.getSession(false);

        if (session != null) {
            if (isTokenPerPageEnabled()) {
                @SuppressWarnings("unchecked")
                Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

                if (pageTokens != null) {
                    if (isTokenPerPagePrecreate()) {
                        createPageToken(pageTokens, uri);
                    }
                    tokenValue = pageTokens.get(uri);

                }
            }

            if (tokenValue == null) {
                tokenValue = (String) session.getAttribute(getSessionKey());
            }
        }

        return tokenValue;
    }

    public boolean isValidRequest(HttpServletRequest request, HttpServletResponse response) {
        boolean valid = !isProtectedPageAndMethod(request);
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());

        /**
         * sending request to protected resource - verify token *
         */
        if (tokenFromSession != null && !valid) {
            try {
                if (isAjaxEnabled() && isAjaxRequest(request)) {
                    verifyAjaxToken(request);
                } else if (isTokenPerPageEnabled()) {
                    verifyPageToken(request);
                } else {
                    verifySessionToken(request);
                }
                valid = true;
            } catch (CsrfGuardException csrfe) {
                callActionsOnError(request, response, csrfe);
            }

            /**
             * rotate session and page tokens *
             */
            if (!isAjaxRequest(request) && isRotateEnabled()) {
                rotateTokens(request);
            }
            /**
             * expected token in session - bad state and not valid *
             */
        } else if (tokenFromSession == null && !valid) {
            try {
                throw new CsrfGuardException("CsrfGuard expects the token to exist in session at this point");
            } catch (CsrfGuardException csrfe) {
                callActionsOnError(request, response, csrfe);

            }
        } else {
            /**
             * unprotected page - nothing to do *
             */
        }

        return valid;
    }

    /**
     * @param request
     * @param response
     * @param csrfe
     */
    private void callActionsOnError(HttpServletRequest request,
            HttpServletResponse response, CsrfGuardException csrfe) {
        for (IAction action : getActions()) {
            try {
                action.execute(request, response, csrfe, this);
            } catch (CsrfGuardException exception) {
                getLogger().log(LogLevel.Error, exception);
            }
        }
    }

    public void updateToken(HttpSession session) {
        String tokenValue = (String) session.getAttribute(getSessionKey());

        /**
         * Generate a new token and store it in the session. *
         */
        if (tokenValue == null) {
            try {
                tokenValue = RandomGenerator.generateRandomId(getPrng(), getTokenLength());
            } catch (Exception e) {
                throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
            }

            session.setAttribute(getSessionKey(), tokenValue);
        }
    }

    public void updateTokens(HttpServletRequest request) {
        /**
         * cannot create sessions if response already committed *
         */
        HttpSession session = request.getSession(false);

        if (session != null) {
            /**
             * create master token if it does not exist *
             */
            updateToken(session);

            /**
             * create page specific token *
             */
            if (isTokenPerPageEnabled()) {
                @SuppressWarnings("unchecked")
                Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

                /**
                 * first time initialization *
                 */
                if (pageTokens == null) {
                    pageTokens = new HashMap<String, String>();
                    session.setAttribute(CsrfGuard.PAGE_TOKENS_KEY, pageTokens);
                }

                /**
                 * create token if it does not exist *
                 */
                if (isProtectedPageAndMethod(request)) {
                    createPageToken(pageTokens, request.getRequestURI());
                }
            }
        }
    }

    /**
     * Create page token if it doesn't exist.
     *
     * @param pageTokens A map of tokens. If token doesn't exist it will be
     * added.
     * @param uri The key for the tokens.
     */
    private void createPageToken(Map<String, String> pageTokens, String uri) {

        if (pageTokens == null) {
            return;
        }

        /**
         * create token if it does not exist *
         */
        if (pageTokens.containsKey(uri)) {
            return;
        }
        try {
            pageTokens.put(uri, RandomGenerator.generateRandomId(getPrng(), getTokenLength()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
        }
    }

    public void writeLandingPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String landingPage = getNewTokenLandingPage();

        /**
         * default to current page *
         */
        if (landingPage == null) {
            StringBuilder sb = new StringBuilder();

            sb.append(request.getContextPath());
            sb.append(request.getServletPath());

            landingPage = sb.toString();
        }

        /**
         * create auto posting form *
         */
        StringBuilder sb = new StringBuilder();

        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>OWASP CSRFGuard Project - New Token Landing Page</title>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body>\r\n");
        sb.append("<script type=\"text/javascript\">\r\n");
        sb.append("var form = document.createElement(\"form\");\r\n");
        sb.append("form.setAttribute(\"method\", \"post\");\r\n");
        sb.append("form.setAttribute(\"action\", \"");
        sb.append(landingPage);
        sb.append("\");\r\n");

        /**
         * only include token if needed *
         */
        if (isProtectedPage(landingPage)) {
            sb.append("var hiddenField = document.createElement(\"input\");\r\n");
            sb.append("hiddenField.setAttribute(\"type\", \"hidden\");\r\n");
            sb.append("hiddenField.setAttribute(\"name\", \"");
            sb.append(getTokenName());
            sb.append("\");\r\n");
            sb.append("hiddenField.setAttribute(\"value\", \"");
            sb.append(getTokenValue(request, landingPage));
            sb.append("\");\r\n");
            sb.append("form.appendChild(hiddenField);\r\n");
        }

        sb.append("document.body.appendChild(form);\r\n");
        sb.append("form.submit();\r\n");
        sb.append("</script>\r\n");
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        String code = sb.toString();

        /**
         * setup headers *
         */
        response.setContentType("text/html");
        response.setContentLength(code.length());

        /**
         * write auto posting form *
         */
        OutputStream output = null;
        PrintWriter writer = null;

        try {
            output = response.getOutputStream();
            writer = new PrintWriter(output);

            writer.write(code);
            writer.flush();
        } finally {
            Writers.close(writer);
            Streams.close(output);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\r\n*****************************************************\r\n");
        sb.append("* Owasp.CsrfGuard Properties\r\n");
        sb.append("*\r\n");
        sb.append(String.format("* Logger: %s\r\n", getLogger().getClass().getName()));
        sb.append(String.format("* NewTokenLandingPage: %s\r\n", getNewTokenLandingPage()));
        sb.append(String.format("* PRNG: %s\r\n", getPrng().getAlgorithm()));
        sb.append(String.format("* SessionKey: %s\r\n", getSessionKey()));
        sb.append(String.format("* TokenLength: %s\r\n", getTokenLength()));
        sb.append(String.format("* TokenName: %s\r\n", getTokenName()));
        sb.append(String.format("* Ajax: %s\r\n", isAjaxEnabled()));
        sb.append(String.format("* Rotate: %s\r\n", isRotateEnabled()));
        sb.append(String.format("* Javascript cache control: %s\r\n", getJavascriptCacheControl()));
        sb.append(String.format("* Javascript domain strict: %s\r\n", isJavascriptDomainStrict()));
        sb.append(String.format("* Javascript inject attributes: %s\r\n", isJavascriptInjectIntoAttributes()));
        sb.append(String.format("* Javascript inject forms: %s\r\n", isJavascriptInjectIntoForms()));
        sb.append(String.format("* Javascript referer pattern: %s\r\n", getJavascriptRefererPattern()));
        sb.append(String.format("* Javascript referer match domain: %s\r\n", isJavascriptRefererMatchDomain()));
        sb.append(String.format("* Javascript source file: %s\r\n", getJavascriptSourceFile()));
        sb.append(String.format("* Javascript X requested with: %s\r\n", getJavascriptXrequestedWith()));
        sb.append(String.format("* Protected methods: %s\r\n", CsrfGuardUtils.toStringForLog(getProtectedMethods())));
        sb.append(String.format("* Protected pages size: %s\r\n", CsrfGuardUtils.length(getProtectedPages())));
        sb.append(String.format("* Unprotected methods: %s\r\n", CsrfGuardUtils.toStringForLog(getUnprotectedMethods())));
        sb.append(String.format("* Unprotected pages size: %s\r\n", CsrfGuardUtils.length(getUnprotectedPages())));
        sb.append(String.format("* TokenPerPage: %s\r\n", isTokenPerPageEnabled()));
        sb.append(String.format("* Enabled: %s\r\n", isEnabled()));
        sb.append(String.format("* ValidateWhenNoSessionExists: %s\r\n", isValidateWhenNoSessionExists()));

        for (IAction action : getActions()) {
            sb.append(String.format("* Action: %s\r\n", action.getClass().getName()));

            for (String name : action.getParameterMap().keySet()) {
                String value = action.getParameter(name);

                sb.append(String.format("*\tParameter: %s = %s\r\n", name, value));
            }
        }
        sb.append("*****************************************************\r\n");

        return sb.toString();
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        return ajaxHeader != null && ajaxHeader.contains("XMLHttpRequest"); // CUSTOM: added fix to support native android browser
    }

    private void verifyAjaxToken(HttpServletRequest request) throws CsrfGuardException {
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getHeader(getTokenName());

        if (tokenFromRequest == null) {
            /**
             * FAIL: token is missing from the request *
             */
            throw new CsrfGuardException("required token is missing from the request");
        } else {
            //if there are two headers, then the result is comma separated
            if (!tokenFromSession.equals(tokenFromRequest)) {
                if (tokenFromRequest.contains(",")) {
                    tokenFromRequest = tokenFromRequest.substring(0, tokenFromRequest.indexOf(',')).trim();
                }
                if (!tokenFromSession.equals(tokenFromRequest)) {
                    /**
                     * FAIL: the request token does not match the session token *
                     */
                    throw new CsrfGuardException("request token does not match session token");
                }
            }
        }
    }

    private void verifyPageToken(HttpServletRequest request) throws CsrfGuardException {
        HttpSession session = request.getSession(true);
        @SuppressWarnings("unchecked")
        Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

        String tokenFromPages = (pageTokens != null ? pageTokens.get(request.getRequestURI()) : null);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getParameter(getTokenName());

        if (tokenFromRequest == null) {
            /**
             * FAIL: token is missing from the request *
             */
            throw new CsrfGuardException("required token is missing from the request");
        } else if (tokenFromPages != null) {
            if (!tokenFromPages.equals(tokenFromRequest)) {
                /**
                 * FAIL: request does not match page token *
                 */
                throw new CsrfGuardException("request token does not match page token");
            }
        } else if (!tokenFromSession.equals(tokenFromRequest)) {
            /**
             * FAIL: the request token does not match the session token *
             */
            throw new CsrfGuardException("request token does not match session token");
        }
    }

    private void verifySessionToken(HttpServletRequest request) throws CsrfGuardException {
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getParameter(getTokenName());

        if (tokenFromRequest == null) {
            /**
             * FAIL: token is missing from the request *
             */
            throw new CsrfGuardException("required token is missing from the request");
        } else if (!tokenFromSession.equals(tokenFromRequest)) {
            /**
             * FAIL: the request token does not match the session token *
             */
            throw new CsrfGuardException("request token does not match session token");
        }
    }

    private void rotateTokens(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        /**
         * rotate master token *
         */
        String tokenFromSession = null;

        try {
            tokenFromSession = RandomGenerator.generateRandomId(getPrng(), getTokenLength());
        } catch (Exception e) {
            throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
        }

        session.setAttribute(getSessionKey(), tokenFromSession);

        /**
         * rotate page token *
         */
        if (isTokenPerPageEnabled()) {
            @SuppressWarnings("unchecked")
            Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

            try {
                pageTokens.put(request.getRequestURI(), RandomGenerator.generateRandomId(getPrng(), getTokenLength()));
            } catch (Exception e) {
                throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
            }
        }
    }

    public boolean isProtectedPage(String uri) {

        //if this is a javascript page, let it go through
        if (JavaScriptServlet.getJavascriptUris().contains(uri)) {
            return false;
        }

        boolean retval = !isProtectEnabled();

        for (String protectedPage : getProtectedPages()) {
            if (isUriExactMatch(protectedPage, uri)) {
                return true;
            } else if (isUriMatch(protectedPage, uri)) {
                retval = true;
            }
        }

        for (String unprotectedPage : getUnprotectedPages()) {
            if (isUriExactMatch(unprotectedPage, uri)) {
                return false;
            } else if (isUriMatch(unprotectedPage, uri)) {
                retval = false;
            }
        }

        return retval;
    }

    /**
     * if the HTTP method is protected, i.e. should be checked for token
     *
     * @param method
     * @return if protected
     */
    public boolean isProtectedMethod(String method) {
        boolean isProtected = true;

        {
            Set<String> theProtectedMethods = getProtectedMethods();
            if (!theProtectedMethods.isEmpty() && !theProtectedMethods.contains(method)) {
                isProtected = false;
            }
        }

        {
            Set<String> theUnprotectedMethods = getUnprotectedMethods();
            if (!theUnprotectedMethods.isEmpty() && theUnprotectedMethods.contains(method)) {
                isProtected = false;
            }
        }

        return isProtected;
    }

    public boolean isProtectedPageAndMethod(String page, String method) {
        return (isProtectedPage(page) && isProtectedMethod(method));
    }

    public boolean isProtectedPageAndMethod(HttpServletRequest request) {
        return isProtectedPageAndMethod(request.getRequestURI(), request.getMethod());
    }

    public boolean isPrintConfig() {
        return config().isPrintConfig();
    }

    /**
     * FIXME: taken from Tomcat - ApplicationFilterFactory
     *
     * @param testPath the pattern to match.
     * @param requestPath the current request path.
     * @return {@code true} if {@code requestPath} matches {@code testPath}.
     */
    private boolean isUriMatch(String testPath, String requestPath) {

        //case 4, if it is a regex
        if (isTestPathRegex(testPath)) {

            Pattern pattern = this.regexPatternCache.get(testPath);
            if (pattern == null) {
                pattern = Pattern.compile(testPath);
                this.regexPatternCache.put(testPath, pattern);
            }

            return pattern.matcher(requestPath).matches();
        }

        boolean retval = false;

        /**
         * Case 1: Exact Match MCH 140419: ??? isnt this checks in
         * isUriExactMatch() ???  *
         */
        if (testPath.equals(requestPath)) {
            retval = true;
        }

        /**
         * Case 2 - Path Match ("/.../*") *
         */
        if (testPath.equals("/*")) {
            retval = true;
        }
        if (testPath.endsWith("/*")) {
            if (testPath
                    .regionMatches(0, requestPath, 0, testPath.length() - 2)) {
                if (requestPath.length() == (testPath.length() - 2)) {
                    retval = true;
                } else if ('/' == requestPath.charAt(testPath.length() - 2)) {
                    retval = true;
                }
            }
        }

        /**
         * Case 3 - Extension Match *
         */
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');

            if ((slash >= 0)
                    && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period) == (testPath.length() - 1))) {
                retval = testPath.regionMatches(2, requestPath, period + 1,
                        testPath.length() - 2);
            }
        }

        return retval;
    }

    /**
     * see if a test path starts with ^ and ends with $ thus making it a regex
     *
     * @param testPath
     * @return true if regex
     */
    private static boolean isTestPathRegex(String testPath) {
        return testPath != null && testPath.startsWith("^") && testPath.endsWith("$");
    }

    private boolean isUriExactMatch(String testPath, String requestPath) {

        //cant be an exact match if this is a regex
        if (isTestPathRegex(testPath)) {
            return false;
        }

        boolean retval = false;

        /**
         * Case 1: Exact Match *
         */
        if (testPath.equals(requestPath)) {
            retval = true;
        }

        return retval;
    }

    /**
     * if there are methods specified, then they (e.g. GET) are unprotected, and
     * all others are protected
     *
     * @return the unprotected HTTP methods
     */
    public Set<String> getUnprotectedMethods() {
        return config().getUnprotectedMethods();
    }

}
