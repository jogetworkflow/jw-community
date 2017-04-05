package org.joget.apps.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public abstract class MfaAuthenticator extends ExtDefaultPlugin implements PropertyEditable {
    /**
     * URL to show when OTP (one-time password) is required.
     * @param username
     * @return 
     */
    public abstract String validateOtpUrl(String username);
    
    /**
     * Message to show when OTP (one-time password) is required.
     * @param username
     * @return 
     */
    public abstract String validateOtpMessage(String username);
    
    /**
     * URL to activate OTP (one-time password).
     * @param username
     * @return 
     */
    public abstract String activateOtpUrl(String username);
    
    /**
     * URL to deactivate OTP (one-time password).
     * @param username
     * @return 
     */
    public abstract String deactivateOtpUrl(String username);

    /**
     * Checks whether OTP is required (MFA is enabled) for a user.
     * @param username
     * @return 
     */    
    public abstract boolean isOtpRequired(String username);

    /**
     * Deletes the current OTP for the user.
     * @param username 
     */
    public abstract void clearOtp(String username);
    
    public String loginUser (String username) throws IOException {
        ExtDirectoryManager dm = (ExtDirectoryManager) DirectoryUtil.getApplicationContext().getBean("directoryManager");
        User user = dm.getUserByUsername(username);

        Collection<Role> roles = dm.getUserRoles(username);
        List<GrantedAuthority> gaList = new ArrayList<GrantedAuthority>();
        if (roles != null && !roles.isEmpty()) {
            for (Role role : roles) {
                GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                gaList.add(ga);
            }
        }

        // return result
        UserDetails details = new WorkflowUserDetails(user);
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(details, "", gaList);
        result.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(result);
        
        return "<script>parent.window.location = '"+getRedirectUrl()+"';</script>";
    }
    
    protected String getRedirectUrl() {
        String savedUrl = "";
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        HttpServletResponse response = WorkflowUtil.getHttpServletResponse();
        if (request != null) {
            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
            if (savedRequest != null) {
                savedUrl = savedRequest.getRedirectUrl();
            } else if (request.getHeader("referer") != null) { //for userview logout
                savedUrl = request.getHeader("referer");
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
            return request.getContextPath() + "/web/console/home";
        }
        return "";
    }
}
