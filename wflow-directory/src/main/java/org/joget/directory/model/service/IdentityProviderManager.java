package org.joget.directory.model.service;

import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.directory.model.idp.IdentityProviderPlugin;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Interface to provide external usage of private implementation via Spring beans.
 */
public interface IdentityProviderManager {

    /**
     * Get the authorization endpoint URL from the specified plugin.
     * <p>
     * This URL is the page where the user logs in to the identity provider.
     *
     * @param pluginUuid the UUID of the plugin instance
     * @return a string representation of the URL
     */
    String getAuthorizationEndpointUrl(String pluginUuid);

    /**
     * To handle the login callback from the controller and delegates it to the specified plugin
     *
     * @param pluginUuid   the delegated plugin UUID
     * @param callbackRequest the request received from the callback
     * @return {@code true} if login success; {@code false} otherwise
     */
    boolean handleCallback(String pluginUuid, HttpServletRequest callbackRequest);

    /**
     * Gets installed identity providers that are configured (IdPs which have their configuration saved in database)
     * <p>
     * This method will always return {@link Plugin}s that are of type {@link IdentityProviderPlugin}
     *
     * @return a Map object that maps plugin uuid to plugin object
     */
    Map<String, Plugin> getConfiguredIdentityProviderPlugins();

    String getLoginFooterHtml();

    String getProfileFooterHtml(User user);
}
