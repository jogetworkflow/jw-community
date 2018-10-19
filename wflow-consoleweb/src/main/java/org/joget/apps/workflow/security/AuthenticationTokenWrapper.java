package org.joget.apps.workflow.security;

import java.util.ArrayList;
import java.util.Collection;
import javax.security.auth.Subject;
import static org.joget.workflow.model.service.WorkflowUserManager.ROLE_ADMIN;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthenticationTokenWrapper implements Authentication {
 
    Authentication authentication;

    public AuthenticationTokenWrapper(Authentication auth) {
        this.authentication = auth;
    }
    
    @Override
    public String getName() {
        return authentication.getName();
    }

    @Override
    public boolean implies(Subject subject) {
        return authentication.implies(subject);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection tempAuthorities = new ArrayList<>(authentication.getAuthorities());
        
        // check for sys admin role, add if not in db
        GrantedAuthority ga = new SimpleGrantedAuthority(ROLE_ADMIN);
        if (tempAuthorities.contains(ga) && !EnhancedWorkflowUserManager.isSysAdminRoleAvailable()) {
            tempAuthorities.add(new SimpleGrantedAuthority(EnhancedWorkflowUserManager.ROLE_SYSADMIN));
        }
        
        // check for app admin role, remove if app admin role configured for specific users
        if (tempAuthorities.contains(ga) && EnhancedWorkflowUserManager.isAppAdminRoleRestricted()) {
            tempAuthorities.remove(ga);
        }

        return tempAuthorities;
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return authentication.isAuthenticated();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authentication.setAuthenticated(isAuthenticated);
    }
    
}
