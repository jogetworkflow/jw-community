package org.joget.apps.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.commons.util.LogUtil;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.UserMetaData;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.directory.model.service.UserSecurity;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.dao.WorkflowHelper;
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
     * Return Key used to store MFA status & data
     * @return 
     */
    public abstract String getKey();
    
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
     * HTML for rendering the MFA interface to activate/deactivate the MFA
     * @param username
     * @return 
     */
    public String userProfileHtml(String username, HttpServletRequest request) {
        Map model = new HashMap();
        model.put("request", WorkflowUtil.getHttpServletRequest());
        model.put("mfaActivateURL", activateOtpUrl(username));
        model.put("name", getKey().toLowerCase());
        
        UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
        UserMetaData data = dao.getUserMetaData(username, getKey());
        String submittedValue = request.getParameter(getKey().toLowerCase());
        if (submittedValue == null || submittedValue.isEmpty()) {
            model.put("data", (data != null)?PropertyUtil.PASSWORD_PROTECTED_VALUE:"");
        } else {
            model.put("data", submittedValue);
        }
        
        model.put("mfaEnabled", !model.get("data").toString().isEmpty());
        
        return getTemplate("mfaDefaultTemplate", model);
    }
    
    /**
     * Processing after a user profile is updated to update MFA status.
     * @param username
     * @param request
     * @return 
     */
    public void updateUserProfileProcessing(String username, HttpServletRequest request) {
        UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
        UserMetaData data = dao.getUserMetaData(username, getKey());
        String submittedValue = request.getParameter(getKey().toLowerCase());
        
        if (PropertyUtil.PASSWORD_PROTECTED_VALUE.equals(submittedValue)) {
            //ignore
        } else if (submittedValue != null & !submittedValue.isEmpty()) {
            if (data == null) {
                data = new UserMetaData();
                data.setUsername(username);
                data.setKey(getKey());
                data.setValue(submittedValue);
                dao.addUserMetaData(data);
            } else {
                data.setValue(submittedValue);
                dao.updateUserMetaData(data);
            }
        } else if (data != null) {
            clearOtp(username);
        }
    }

    /**
     * Checks whether OTP is required (MFA is enabled) for a user.
     * @param username
     * @return 
     */    
    public boolean isOtpRequired(String username) {
        UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
        return dao.getUserMetaData(username, getKey()) != null;
    }

    /**
     * Deletes the current OTP for the user.
     * @param username 
     */
    public void clearOtp(String username) {
        UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
        dao.deleteUserMetaData(username, getKey());
    }
    
    /**
     * Login the user
     * @param username
     * @return
     * @throws IOException 
     */
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
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user.getUsername(), "", gaList);
        result.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(result);
        
        HttpServletRequest httpRequest = WorkflowUtil.getHttpServletRequest();
        String ip = AppUtil.getClientIp(httpRequest);
        LogUtil.info(getClass().getName(), "Authentication for user " + username + " ("+ip+") : true");
        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
        workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + " ("+ip+") : true"); 
        
        //post login processing
        UserSecurity userSecurity = DirectoryUtil.getUserSecurity();
        if (userSecurity != null) {
            userSecurity.loginPostProcessing(user, null, true);
        }
        
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
    
    /**
     * Method to retrieve the html template
     * @param template
     * @param model
     * @return 
     */
    protected String getTemplate(String template, Map model) {
        // display license page
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(model, getClass().getName(), "/templates/" + template + ".ftl", null);
        return content;
    }
}
