package org.joget.apps.workflow.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.util.StringUtils;

public class EnhancedWorkflowUserManager extends WorkflowUserManager {

    public static final String ROLE_SYSADMIN = "ROLE_SYSADMIN";
    public static final String ROLE_APPDESIGNER = "ROLE_APPDESIGNER";
    public static final String ROLE_ADMIN_GROUP = "ROLE_ADMIN_GROUP";
    public static final String ROLE_ADMIN_ORG = "ROLE_ADMIN_ORG";
    
    public static boolean checkCustomAppDesigner() {
        boolean isAppDesigner = false;
        AppDefinition appDef = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            // determine appId from request path
            String url = (String)request.getAttribute("jakarta.servlet.forward.request_uri");
            if (url == null || url.isEmpty()) {
                url = request.getRequestURI();
            }
            // extract appDef from /web/userview, /web/embed/userview, /web/console/app or /web/json/console/app or /web/json/app
            String pattern = request.getContextPath() + "\\/web\\/(userview|embed\\/userview|console\\/app|json\\/console\\/app|json\\/app)\\/(.*)\\/(.*)";
            Matcher m = Pattern.compile(pattern).matcher(url);
            if (m.find()) {
                String appPath = m.group(2);
                String[] appPathElements = appPath.split("/");
                String appId = appPathElements[0];
                if (!"screenshot".equals(appId)) { // ignore screenshot preview URL
                    String appVersion = (appPathElements.length > 1) ? appPathElements[1] : null;                
                    AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
                    appDef = appService.getAppDefinition(appId, appVersion);
                }
            }
            if (appDef != null) {
                // check for custom app designer role assignments
                Collection<String> adminUserSet = new HashSet<>();
                Properties props = AppDevUtil.getAppDevProperties(appDef);
                String roleAdmin = props.getProperty(ROLE_ADMIN);
                String roleAdminGroup = props.getProperty(ROLE_ADMIN_GROUP);
                if ((roleAdmin != null && !roleAdmin.isEmpty()) || (roleAdminGroup != null && !roleAdminGroup.isEmpty())) {
                    String[] adminUsers = StringUtils.tokenizeToStringArray(roleAdmin, ";,", true, true);
                    if (adminUsers != null && adminUsers.length > 0) {
                        adminUserSet.addAll(Arrays.asList(adminUsers));
                    }
                    String[] adminGroups = StringUtils.tokenizeToStringArray(roleAdminGroup, ";,", true, true);
                    if (adminGroups != null && adminGroups.length > 0) {
                        UserDao userDao = (UserDao)AppUtil.getApplicationContext().getBean("userDao");
                        for (String groupId: adminGroups) {
                            Collection<User> groupUsers = userDao.getUsers(null, null, null, null, groupId, null, "1", null, null, null, null);
                            for (User user: groupUsers) {
                                adminUserSet.add(user.getUsername());
                            }
                        }
                    }
                String currentUsername = WorkflowUtil.getCurrentUsername();
                    isAppDesigner = adminUserSet.contains(currentUsername);
                }
            }
            if (isAppDesigner) {
                // add app designer role to session
                String key = EnhancedWorkflowUserManager.ROLE_APPDESIGNER;
                request.getSession().setAttribute(key, "true");
            }
        }

        return isAppDesigner;
    }

    public static boolean isAppDesignerRole() {
        boolean isAppDesigner = false;
        // check session for app designer role
        String key = EnhancedWorkflowUserManager.ROLE_APPDESIGNER;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        AppDefinition appDef = AppUtil.getCurrentAppDefinition(); // Assuming this fetches the current app definition.
        String currentUsername = WorkflowUtil.getCurrentUsername();

        // Set createdBy correctly
        String createdBy = null;
        if (appDef != null) {
            createdBy = appDef.getCreatedBy();
        }

        if (request != null && request.getSession() != null && request.getSession().getAttribute(key) != null) {
            isAppDesigner = Boolean.valueOf((String)request.getSession().getAttribute(key));
        }
        
        // remove the session attribute if the current user is the creator of the current app.
        if (createdBy != null && createdBy.equals(currentUsername)) {
            request.getSession().removeAttribute(key);
        }

        return isAppDesigner;
    }

    public static boolean isSysAdminRoleAvailable() {
        boolean isSysAdminRoleAvailable = false;
        
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            isSysAdminRoleAvailable = Boolean.valueOf((String)request.getSession().getAttribute(ROLE_SYSADMIN + "_AVAILABLE"));
        } else {
            RoleDao roleDao = (RoleDao)AppUtil.getApplicationContext().getBean("roleDao");
            Role role = roleDao.getRole(ROLE_SYSADMIN);
            isSysAdminRoleAvailable = (role != null);
            
            if (isSysAdminRoleAvailable && request != null && request.getSession() != null) {
                request.getSession().setAttribute(ROLE_SYSADMIN + "_AVAILABLE", true);
            }
        }
        
        return isSysAdminRoleAvailable;
    }
    
    @Override
    public Collection<String> getCurrentRoles() {
        String username = WorkflowUtil.getCurrentUsername();
        String key = "userRole_" + username;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        // Ensure proper role reset for app switching
        if (request != null && request.getSession() != null) {
            String appId = getCurrentAppIdFromRequest(request);
            String cachedAppId = (String) request.getSession().getAttribute("currentAppId");

            if (appId != null && !appId.equals(cachedAppId)) {
                // Clear app-specific roles
                request.getSession().removeAttribute(ROLE_APPDESIGNER);
                request.getSession().setAttribute("currentAppId", appId);
                WorkflowUtil.writeRequestCache(key, null); // Force role recalculation
            }
        }

        Collection<String> result = (Collection<String>) WorkflowUtil.readRequestCache(key);
        if (result != null) {
            return result;
        }

        Collection<String> roles = super.getCurrentRoles();

        // check for sys admin role, add if not in db
        if (roles.contains(ROLE_ADMIN) && !EnhancedWorkflowUserManager.isSysAdminRoleAvailable()) {
            roles.add(ROLE_SYSADMIN);
        }

        // add app designer role configured for specific users, should not check for admin & anonymous user
        if (!roles.contains(ROLE_ADMIN) && !isCurrentUserAnonymous() && EnhancedWorkflowUserManager.checkCustomAppDesigner()) {
            roles.add(ROLE_ADMIN);
        }

        // grant admin role to users with app admin or system admin roles.
        if (roles.contains(ROLE_APPADMIN) || roles.contains(ROLE_SYSTEMADMIN)) {
            roles.add(ROLE_ADMIN);
        }

        if (EnhancedWorkflowUserManager.isAppDesignerRole()) {
            roles.add(EnhancedWorkflowUserManager.ROLE_APPDESIGNER);

            // set admin role for backward compatibility on plugin webService calls checking for ROLE_ADMIN
            String url = (String)request.getAttribute("jakarta.servlet.forward.request_uri");
            if (url == null || url.isEmpty()) {
                url = request.getRequestURI();
            }
            if (url.startsWith(request.getContextPath() + "/web/json/plugin/")) {
                roles.add(ROLE_ADMIN);
            }
        }

        WorkflowUtil.writeRequestCache(key, roles);

        return roles;
    }

    private String getCurrentAppIdFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String url = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
        if (url == null || url.isEmpty()) {
            url = request.getRequestURI();
        }

        String pattern = request.getContextPath() + "\\/web\\/(userview|embed\\/userview|console\\/app|json\\/console\\/app|json\\/app)\\/(.*)\\/(.*)";
        Matcher m = Pattern.compile(pattern).matcher(url);
        if (m.find()) {
            String appPath = m.group(2);
            String[] appPathElements = appPath.split("/");
            return appPathElements[0]; // First element is the app ID
        }

        return null;
    }

}
