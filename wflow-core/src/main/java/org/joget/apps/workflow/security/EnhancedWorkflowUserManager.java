package org.joget.apps.workflow.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
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
    public static final String ROLE_APPADMIN = "ROLE_APPADMIN";
    public static final String ROLE_ADMIN_GROUP = "ROLE_ADMIN_GROUP";
    public static final String ROLE_ADMIN_ORG = "ROLE_ADMIN_ORG";
    
    public static boolean checkCustomAppAdmin() {
        boolean isAppAdmin = false;
        AppDefinition appDef = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            // determine appId from request path
            String url = (String)request.getAttribute("javax.servlet.forward.request_uri");
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
                // check for custom app admin role assignments
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
                    isAppAdmin = adminUserSet.contains(currentUsername);
                }
            }
            if (isAppAdmin) {
                // add app admin role to session
                String key = EnhancedWorkflowUserManager.ROLE_APPADMIN;
                request.getSession().setAttribute(key, "true");
            }
        }

        return isAppAdmin;
    }    
    
    public static boolean isAppAdminRole() {
        boolean isAppAdmin = false;
        // check session for app admin role
        String key = EnhancedWorkflowUserManager.ROLE_APPADMIN;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            isAppAdmin = Boolean.valueOf((String)request.getSession().getAttribute(key));
        }
        return isAppAdmin;        
    }
    
    public static boolean isSysAdminRoleAvailable() {
        RoleDao roleDao = (RoleDao)AppUtil.getApplicationContext().getBean("roleDao");
        Role role = roleDao.getRole(ROLE_SYSADMIN);
        return (role != null);
    }
    
    @Override
    public Collection<String> getCurrentRoles() {
        String username = WorkflowUtil.getCurrentUsername();
        String key = "userRole_" + username;
        Collection<String> result = (Collection<String>)WorkflowUtil.readRequestCache(key);
        if (result != null) {
            return result;
        }
        
        Collection<String> roles = super.getCurrentRoles();

        // check for sys admin role, add if not in db
        if (roles.contains(ROLE_ADMIN) && !EnhancedWorkflowUserManager.isSysAdminRoleAvailable()) {
            roles.add(ROLE_SYSADMIN);
        }
        
        // add app admin role configured for specific users
        if (!roles.contains(ROLE_ADMIN) && EnhancedWorkflowUserManager.checkCustomAppAdmin()) {
            roles.add(ROLE_ADMIN);
        }
        if (EnhancedWorkflowUserManager.isAppAdminRole()) {
            roles.add(EnhancedWorkflowUserManager.ROLE_APPADMIN);

            // set admin role for backward compatibility on plugin webService calls checking for ROLE_ADMIN
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();            
            String url = (String)request.getAttribute("javax.servlet.forward.request_uri");
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

}
