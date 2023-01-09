package org.joget.apps.userview.model;

import org.joget.directory.model.User;

public abstract class Permission extends ExtElement {
    
    public static final String DEFAULT = "default"; 
    
    private User currentUser;

    /**
     * Gets current logged in user. 
     * @return NULL if anonymous.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets current logged in user.
     * @param currentUser 
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Check the current user is authorized to proceed.
     * @return 
     */
    public abstract boolean isAuthorize();
}
