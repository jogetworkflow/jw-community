package org.joget.directory.model.service;

import org.joget.directory.model.service.DirectoryManager;

/**
 * Performs user authentication.
 */
public interface DirectoryManagerAuthenticator {

    /**
     * Authenticate a user based on the username and password using the specified DirectoryManager.
     * @param directoryManager
     * @param username
     * @param password
     * @return
     */
    boolean authenticate(DirectoryManager directoryManager, String username, String password);
    
}
