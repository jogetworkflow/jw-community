package org.joget.directory.model.service;

import org.joget.directory.model.User;
import org.joget.directory.model.idp.IdentityProviderPlugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * Interface to provide external usage of private implementation via Spring beans.
 */
public interface IdentityProviderManager {

    /**
     * This session key can be used to determine whether the current session was logged in using an IdP.
     * <p>
     * If it is logged in using IdP, the session value should be set to the UUID of the IdP that was used to log in;
     * else, it should not be set (i.e.: {@code null}).
     */
    String LOGGED_IN_IDP_SESSION_KEY = "LOGGED_IN_IDP";

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

    /**
     * Logs out the user from an IdP.
     * <p>
     * Support for Single Log Out (SLO) is handled by the IdP plugin by implementing the
     * {@link IdentityProviderPlugin#onLogout(HttpSession)} method.
     *
     * @param pluginUuid the UUID of the plugin that was invoked during logout
     * @param session    the session of the currently logged in user
     */
    void logout(String pluginUuid, HttpSession session);
}
