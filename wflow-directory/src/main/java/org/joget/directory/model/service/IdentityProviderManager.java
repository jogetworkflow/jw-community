package org.joget.directory.model.service;

import org.joget.directory.model.User;
import org.joget.directory.model.idp.IdentityProviderPlugin;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Interface to provide external usage of private implementation via Spring beans.
 */
public interface IdentityProviderManager {

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
     *
     * @return a Collection of {@link IdentityProviderPlugin}
     */
    Collection<IdentityProviderPlugin> getConfiguredIdentityProviderPlugins();

    /**
     * Get HTML for login footer section.
     *
     * @return HTML string
     */
    String getLoginFooterHtml();

    /**
     * Get HTML for profile footer section.
     *
     * @param user the user of the profile
     * @return HTML string
     */
    String getProfileFooterHtml(User user);
}
