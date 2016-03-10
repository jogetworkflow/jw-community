package org.joget.directory.model.service;

/**
 * An interface that can be implement by a DirectoryManager implementation to 
 * provide a UserSecurity implementation
 * 
 */
public interface UserSecurityFactory {
    
    /**
     * Method to return a UserSecurity implementation to enhance the user security
     * @return 
     */
    public UserSecurity getUserSecurity();
}
