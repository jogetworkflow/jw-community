package org.joget.apps.workflow.security;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.model.Role;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class EnhancedWorkflowUserManager extends WorkflowUserManager {

    public static final String ROLE_SYSADMIN = "ROLE_SYSADMIN";
    public static final String ROLE_APP_DESIGNER = "ROLE_APP_DESIGNER";
    public static final String ROLE_ADMIN_GROUP = "ROLE_ADMIN_GROUP";
    public static final String ROLE_ADMIN_ORG = "ROLE_ADMIN_ORG";
    
    public static boolean checkCustomAppDesigner(boolean isAppCreator) {
        boolean isAppDesigner = false;
        AppDefinition appDef = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            String[] appPathElements = getCurrentAppPathElementsFromRequest(request);
            String appId = (appPathElements != null && appPathElements.length > 0)?appPathElements[0] : null;
            
            if (appId != null && !"screenshot".equals(appId)) { // ignore screenshot preview URL
                String appVersion = (appPathElements.length > 1) ? appPathElements[1] : null;                
                AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
                appDef = appService.getAppDefinition(appId, appVersion);
            }
            if (appDef != null) {
                isAppDesigner = AppUtil.isAppEditableByCurrentUser(appDef, false, isAppCreator);
            }
            if (isAppDesigner) {
                // add app designer role to session
                String key = EnhancedWorkflowUserManager.ROLE_APP_DESIGNER;
                request.getSession().setAttribute(key, "true");
            }
        }

        return isAppDesigner;
    }

    public static boolean isAppDesignerRole() {
        boolean isAppDesigner = false;
        // check session for app admin role
        String key = EnhancedWorkflowUserManager.ROLE_APP_DESIGNER;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && request.getSession() != null && request.getSession().getAttribute(key) != null) {
            isAppDesigner = Boolean.valueOf((String)request.getSession().getAttribute(key));
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
                request.getSession().removeAttribute(ROLE_APP_DESIGNER);
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

        /* add admin role for app designer/app creator role configured for specific users and based on the app designer URL. 
           So that, they can access the app composer & builder
           should not check for admin & anonymous user 
        */
        if (!roles.contains(ROLE_ADMIN) && !isCurrentUserAnonymous() && EnhancedWorkflowUserManager.checkCustomAppDesigner(roles.contains(ROLE_APP_CREATOR))) {
            roles.add(ROLE_ADMIN);
        }

        if (EnhancedWorkflowUserManager.isAppDesignerRole()) {
            roles.add(EnhancedWorkflowUserManager.ROLE_APP_DESIGNER);

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

    private static String getCurrentAppIdFromRequest(HttpServletRequest request) {
        String[] parts = getCurrentAppPathElementsFromRequest(request);
        
        return (parts != null && parts.length > 0) ? parts[0] : null; // First element is the app ID
    }
        
    private static String[] getCurrentAppPathElementsFromRequest(HttpServletRequest request) {    
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
            return appPath.split("/");
        }

        return null;
    }

}
