package org.joget.directory.ext;

import org.joget.directory.model.service.DirectoryManagerAuthenticator;
import java.util.Map;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;

/**
 * Delegate class to perform user authentication.
 */
public class DirectoryManagerAuthenticatorImpl implements Plugin, DirectoryManagerAuthenticator {

    @Override
    public String getName() {
        return "DirectoryManager Authenticator";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "DirectoryManager Authenticator";
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
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
