package org.joget.apps.userview.lib;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

public class UserPermission extends UserviewPermission implements PluginWebSupport, FormPermission {

    @Override
    public boolean isAuthorize() {
        User user = getCurrentUser();

        if (user != null) {
            StringTokenizer strToken = new StringTokenizer(getPropertyString("allowedUsernames"), ";");
            while (strToken.hasMoreTokens()) {
                String username = (String) strToken.nextElement();
                if (username.equals(user.getUsername())) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getName() {
        return "User Permission";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "User";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/userPermission.json", null, true, "message/userview/userPermission");
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String action = request.getParameter("action");

        if ("getOrgs".equals(action)) {
            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");

                Map<String, String> empty = new HashMap<String, String>();
                empty.put("value", "");
                empty.put("label", ResourceBundleUtil.getMessage("console.directory.user.empty.option.label"));
                jsonArray.put(empty);

                Collection<Organization> orgList = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);

                for (Organization o : orgList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", o.getId());
                    option.put("label", o.getName());
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Organization options Error!");
            }
        } else if ("getUsers".equals(action)) {
            String orgId = request.getParameter("orgId");

            if ("null".equals(orgId) || "".equals(orgId)) {
                orgId = null;
            }

            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");

                Collection<User> userList = directoryManager.getUsers(null, orgId, null, null, null, null, null, "firstName", false, null, null);

                for (User u : userList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", u.getUsername());
                    option.put("label", u.getFirstName() + " " + u.getLastName() + "(" + u.getUsername() + ")");
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Users options Error!");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
