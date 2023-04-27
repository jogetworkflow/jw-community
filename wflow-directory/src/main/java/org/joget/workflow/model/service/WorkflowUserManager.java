package org.joget.workflow.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public static final String ROLE_USER = "ROLE_USER";

    private ThreadLocal currentThreadUser = new ThreadLocal();
    private ThreadLocal currentThreadUserRoles = new ThreadLocal();
    private ThreadLocal currentThreadUserData = new ThreadLocal();
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
        currentThreadUserRoles.remove();
    }
    
    /**
     * Method used by system to set current logged in user
     * @param user 
     */
    public void setCurrentThreadUser(User user) {
        currentThreadUser.set(user);
    }

    /**
     * Method used by system to clear the user 
     */
    public void clearCurrentThreadUser() {
        currentThreadUser.remove();
        currentThreadUserRoles.remove();
        systemThreadUser.remove();
        currentThreadUserData.remove();
    }

    /**
     * Method used by system to get current thread user
     * @return 
     */
    public String getCurrentThreadUser() {
        Object user = currentThreadUser.get();
        
        if (user instanceof User) {
            return ((User) user).getUsername();
        } else {
            return (String) user;
        }
    }
    
    /**
     * To set current logged in user temporary data
     * @param key
     * @param value
     */
    public void setCurrentUserTempData(String key, Object value) {
        Map<String, Object> data = (Map<String, Object>)currentThreadUserData.get();
        if (data == null) {
            data = new HashMap<String, Object>();
            currentThreadUserData.set(data);
        }
        data.put(key, value);
    }
    
    /**
     * To get current user temporary data by key
     * @return 
     */
    public Object getCurrentUserTempData(String key) {
        Map<String, Object> data = (Map<String, Object>)currentThreadUserData.get();
        if (data != null) {
            return data.get(key);
        }
        
        return null;
    }
    
    public User getCurrentUser() {
        // check for user in current thread
        Object threadUser = currentThreadUser.get();
        if (threadUser != null) {
            if (threadUser instanceof User) {
                return (User) threadUser;
            } else if (threadUser instanceof String) {
                String username = threadUser.toString();
                
                if (ROLE_ANONYMOUS.equals(username)) {
                    return null;
                } else {
                    DirectoryManager directoryManager = (DirectoryManager) DirectoryUtil.getApplicationContext().getBean("directoryManager");
                    User user = directoryManager.getUserByUsername(username);
                
                    setCurrentThreadUser(user);

                    if (user != null) {
                        Collection<String> results = new ArrayList<String>();
                        Collection<Role> roles = user.getRoles();
                        if (roles != null && !roles.isEmpty()) {
                            for (Role role : roles) {
                                results.add(role.getId());
                            }
                            currentThreadUserRoles.set(results);
                        }
                    }
                    return user;
                }
            }
        }
        
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();

        if (auth == null) {
            setCurrentThreadUser(ROLE_ANONYMOUS);
            return null;
        }
        
        Object userObj = auth.getDetails();
        if (userObj == null) {
            userObj = auth.getPrincipal();
        }
        if (userObj instanceof String) {
            setCurrentThreadUser((String) userObj);
            return getCurrentUser();
        } else if (userObj instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) userObj;
            String username = userDetails.getUsername();
            setCurrentThreadUser(username);
            return getCurrentUser();
        } else if (userObj instanceof User) {
            User user = (User) userObj;
            setCurrentThreadUser(user);
            return user;
        }
        return null;
    }

    /**
     * Gets current logged in user
     * @return 
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        if (user != null) {
            return user.getUsername();
        } else {
            return ROLE_ANONYMOUS;
        }
    }

    /**
     * Retrieve the roles of current logged in user 
     * @return 
     */
    public Collection<String> getCurrentRoles() {
        Collection<String> results = (Collection<String>)currentThreadUserRoles.get();
        if (results == null) {
            results = new HashSet<String>();
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication auth = context.getAuthentication();

            if (auth != null) {
                Object userObj = auth.getDetails();
                if (userObj == null) {
                    userObj = auth.getPrincipal();
                }
                if (userObj instanceof UserDetails) {
                    Collection<? extends GrantedAuthority> authorities = ((UserDetails)userObj).getAuthorities();
                    for (GrantedAuthority ga: authorities) {
                        results.add(ga.getAuthority());
                    }
                } else {
                    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                    for (GrantedAuthority ga: authorities) {
                        results.add(ga.getAuthority());
                    }
                }
            }
            currentThreadUserRoles.set(results);
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
