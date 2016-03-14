package org.joget.workflow.model.service;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility methods to deal with logged in user
 * 
 */
public class WorkflowUserManager {
    
    public static final String ROLE_ANONYMOUS = "roleAnonymous";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private ThreadLocal currentThreadUser = new ThreadLocal();
    private ThreadLocal systemThreadUser = new ThreadLocal();
    
    /**
     * Set the current processing is trigger by system
     * @param isSystemUser 
     */
    public void setSystemThreadUser(boolean isSystemUser) {
        systemThreadUser.set(isSystemUser);
    }
    
    /**
     * Check the current processing is triggered by system
     * @return 
     */
    public boolean isSystemUser() {
        Boolean isSystemUser = (Boolean) systemThreadUser.get();
        if (isSystemUser != null && isSystemUser) {
            return true;
        }
        return false;
    }

    /**
     * Method used by system to set current logged in user
     * @param username 
     */
    public void setCurrentThreadUser(String username) {
        currentThreadUser.set(username);
    }

    /**
     * Method used by system to clear the user 
     */
    public void clearCurrentThreadUser() {
        currentThreadUser.remove();
        systemThreadUser.remove();
    }

    /**
     * Method used by system to get current thread user
     * @return 
     */
    public String getCurrentThreadUser() {
        String username = (String)currentThreadUser.get();
        return username;
    }

    /**
     * Gets current logged in user
     * @return 
     */
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

    /**
     * Retrieve the roles of current logged in user 
     * @return 
     */
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

    /**
     * Check current user has a role
     * @param role
     * @return 
     */
    public boolean isCurrentUserInRole(String role) {
        Collection<String> roles = getCurrentRoles();
        boolean result = roles.contains(role);
        return result;
    }

    /**
     * Check whether the current user is an anonymous
     * @return 
     */
    public boolean isCurrentUserAnonymous() {
        String username = getCurrentUsername();
        boolean result = WorkflowUserManager.ROLE_ANONYMOUS.equals(username);
        return result;
    }

}
