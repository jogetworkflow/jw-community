package org.joget.directory.ext;

import org.joget.directory.model.service.DirectoryManagerAuthenticator;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.ExtDefaultPlugin;

/**
 * Delegate class to perform user authentication.
 */
public class DirectoryManagerAuthenticatorImpl extends ExtDefaultPlugin implements DirectoryManagerAuthenticator {

    @Override
    public String getName() {
        return "DirectoryManager Authenticator";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "DirectoryManager Authenticator";
    }
    
    /**
     * Authenticate a user based on the username and password using the specified DirectoryManager.
     * @param directoryManager
     * @param username
     * @param password
     * @return 
     */
    @Override
    public boolean authenticate(DirectoryManager directoryManager, String username, String password) {
        boolean authenticated = directoryManager.authenticate(username, password);
        return authenticated;
    }
    
}
