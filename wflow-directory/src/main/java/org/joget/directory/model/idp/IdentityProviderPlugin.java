package org.joget.directory.model.idp;

import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.property.model.PropertyEditable;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides identity provider plugin support in Joget.
 * Please extend {@link AbstractIdentityProviderPlugin} to begin implementing the plugin.
 */
public interface IdentityProviderPlugin extends Plugin, PropertyEditable {

    String getUuid();

    void setUuid(String uuid);

    /**
     * Handles callback delegation from the identity provider manager.
     * <p>
     * If the plugin determines that a user can be logged in, the returned user object should contain all the necessary
     * information required to potentially provision the user.
     * <p>
     * If this plugin implements {@link CustomManagedIdentityProviderPlugin}, the user returned is expected to be a User
     * object that exists within the Joget user directory. Failure to do so will result in undefined behavior.
     * <p>
     * This method should return {@code null} if the plugin determines that no user should be logged in
     * or if it encounters an error.
     *
     * @param callbackRequest the request received from the callback
     * @return user object of the claimed user; {@code null} if no user or error
     */
    User handleCallback(HttpServletRequest callbackRequest);

    /**
     * Get the identity provider's authorization endpoint
     * <p>
     * This should be the URL of the page where the user logs in to the identity provider.
     *
     * @return a string representing the authorization endpoint
     */
    String getAuthorizationEndpoint();

    /**
     * Get policy for user provisioning
     * <p>
     * This policy determines if a user should be automatically created when the claimed user does not exist
     * in Joget's directory manager or if this IdP is not linked to any existing user.
     *
     * @return {@code true} if enabled; {@code false} otherwise
     * @apiNote User provisioning can only be enabled if Joget is using a directory manager that can be modified by Joget.
     * Directory managers such as the default LDAP implementation cannot be used as Joget is unable to modify the directory.
     */
    boolean isUserProvisioningEnabled();

    /**
     * Whether the provisioned user's profile is editable.
     * In other words, whether the provisioned user is able to edit their profile.
     * <br><br>
     * Note: This setting only affects newly provisioned users. Previously provisioned users WILL NOT be affected.
     * @return true if user is allowed to edit; false otherwise.
     */
    boolean isProvisionedUserProfileEditable();

    /**
     * Get policy for automatic identity provider linking
     * <p>
     * This policy determines if the IdP should be automatically linked to the user when the following criteria is true:
     * <ul>
     *     <li>Claimed email equals only one Joget user's email in directory manager</li>
     * </ul>
     *
     * @return {@code true} if enabled; {@code false} otherwise
     */
    boolean isAutomaticLinkingEnabled();

    /**
     * Get the template used for the login button on the login screen.
     *
     * @return an {@link IdpLoginButtonTemplate} object
     */
    IdpLoginButtonTemplate getLoginButtonTemplate();

    /**
     * Handle custom unlinking user event. This method should return {@code true} if no custom unlinking is required.
     * <p>
     * This method will be called first before the Identity Provider Manager executes its own unlink procedure.
     *
     * @param username the username of the user who initiated the unlink event
     * @param request  the request associated
     * @return true if unlinking success; false otherwise
     * @throws IdpPluginUnlinkException when an error occurs during unlinking. Prefer throwing exception instead of
     *                                  returning false.
     */
    boolean onUnlink(String username, HttpServletRequest request) throws IdpPluginUnlinkException;
}
