package org.joget.workflow.model.service;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class WorkflowUserManager {
    
    public static final String ROLE_ANONYMOUS = "anonymousUser";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private ThreadLocal currentThreadUser = new ThreadLocal();
    private ThreadLocal systemThreadUser = new ThreadLocal();
    
    public void setSystemThreadUser(boolean isSystemUser) {
        systemThreadUser.set(isSystemUser);
    }
    
    public boolean isSystemUser() {
        Boolean isSystemUser = (Boolean) systemThreadUser.get();
        if (isSystemUser != null && isSystemUser) {
            return true;
        }
        return false;
    }

    public void setCurrentThreadUser(String username) {
        currentThreadUser.set(username);
    }

    public void clearCurrentThreadUser() {
        currentThreadUser.remove();
        systemThreadUser.remove();
    }

    public String getCurrentThreadUser() {
        String username = (String)currentThreadUser.get();
        return username;
    }

    public String getCurrentUsername() {

        // check for user in current thread
        String threadUser = getCurrentThreadUser();
        if (threadUser != null && threadUser.trim().length() > 0) {
            return threadUser;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();

        if (auth == null) {
            return ROLE_ANONYMOUS;
        }

        Object userObj = auth.getPrincipal();
        if (userObj instanceof String) {
            return (String) userObj;
        } else if (userObj instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) userObj;
            return userDetails.getUsername();
        }else {
            User user = (User) userObj;
            return user.getUsername();
        }
    }

    public Collection<String> getCurrentRoles() {
        Collection<String> results = new HashSet<String>();
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();

        if (auth != null) {
            Object userObj = auth.getPrincipal();
            if (userObj instanceof UserDetails) {
                Collection<? extends GrantedAuthority> authorities = ((UserDetails)userObj).getAuthorities();
                for (GrantedAuthority ga: authorities) {
                    results.add(ga.getAuthority());
                }
            }
        }
        return results;
    }

    public boolean isCurrentUserInRole(String role) {
        Collection<String> roles = getCurrentRoles();
        boolean result = roles.contains(role);
        return result;
    }

    public boolean isCurrentUserAnonymous() {
        String username = getCurrentUsername();
        boolean result = WorkflowUserManager.ROLE_ANONYMOUS.equals(username);
        return result;
    }

}
