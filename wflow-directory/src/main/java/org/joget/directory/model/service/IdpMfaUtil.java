package org.joget.directory.model.service;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IdpMfaUtil {

    /**
     * Gets the callback URL of the IdP plugin. The callback URL has a fixed syntax and this method should be used to
     * obtain it.
     *
     * @param request    the request made to obtain the callback
     * @param pluginUuid the UUID string to identify the plugin
     * @return a string representing the callback URL
     */
    public static String getIdpCallbackUrl(HttpServletRequest request, String pluginUuid) {
        String callbackUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            callbackUrl += ":" + request.getServerPort();
        }
        callbackUrl += request.getContextPath() + "/web/idp/callback/" + pluginUuid;
        return callbackUrl;
    }

    /**
     * Gets the redirect URL from {@link HttpServletRequest} and {@link HttpServletResponse} objects.
     *
     * @param request  the request to obtain the redirect URL
     * @param response the response to obtain the redirect URL
     * @return the redirect URL
     */
    public static String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        String savedUrl;
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        if (savedRequest != null) {
            savedUrl = savedRequest.getRedirectUrl();
        } else {
            savedUrl = request.getContextPath();
        }

        if (savedUrl.contains("/web/ulogin") || savedUrl.contains("/web/embed/ulogin")) {
            String url = request.getContextPath() + "/web/";

            if (savedUrl.contains("/web/ulogin")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/ulogin"));
                savedUrl = savedUrl.replace("/web/ulogin/", "");
            } else {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/ulogin"));
                savedUrl = savedUrl.replace("/web/embed/ulogin/", "");
                url += "embed/";
            }
            url += "userview/";

            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[0];
            String userviewId = urlKey[1];
            String key = null;
            String menuId = null;
            if (urlKey.length > 2) {
                key = urlKey[2];

                if (urlKey.length > 3) {
                    menuId = urlKey[3];
                }
            }

            url += StringEscapeUtils.escapeHtml(appId) + "/" + StringEscapeUtils.escapeHtml(userviewId) + "/";
            if (key != null) {
                url += StringEscapeUtils.escapeHtml(key);
            }
            if (menuId != null) {
                url += "/" + StringEscapeUtils.escapeHtml(menuId);
            }
            return url;
        }
        return savedUrl;
    }
}
