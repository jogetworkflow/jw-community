package org.joget.apps.workflow.security;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.directory.model.Role;
import org.joget.directory.model.service.DirectoryManagerProxyImpl;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.workflow.model.service.WorkflowUserManager;

public class EnhancedDirectoryManagerProxyImpl extends DirectoryManagerProxyImpl {
    
    @Override
    public Collection<Role> getUserRoles(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {

            Collection<Role> roles = getDirectoryManagerImpl().getUserRoles(username);

            // check for sys admin role, add if not in db
            Role ra = new Role();
            ra.setId(WorkflowUserManager.ROLE_ADMIN);
            if (roles.contains(ra) && !EnhancedWorkflowUserManager.isSysAdminRoleAvailable()) {
                Role sa = new Role();
                sa.setId(EnhancedWorkflowUserManager.ROLE_SYSADMIN);
                sa.setName(EnhancedWorkflowUserManager.ROLE_SYSADMIN);
                roles.add(sa);
            }

            // add app designer role configured for specific users
            if (!roles.contains(ra) && EnhancedWorkflowUserManager.checkCustomAppDesigner()) {
                roles.add(ra);
            }
            if (EnhancedWorkflowUserManager.isAppDesignerRole()) {
                Role aa = new Role();
                aa.setId(EnhancedWorkflowUserManager.ROLE_APPDESIGNER);
                aa.setName(EnhancedWorkflowUserManager.ROLE_APPDESIGNER);
                roles.add(aa);
            }
            return roles;
        }
        return new ArrayList<>();
    }
    
}
