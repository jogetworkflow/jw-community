package org.joget.directory.model.identityprovider;

import org.joget.directory.model.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides identity provider plugin support in Joget.
 * Please extend {@link AbstractIdentityProviderPlugin} to begin implementing the plugin.
 */
public interface IdentityProviderPlugin {

    String getUuid();

    void setUuid(String uuid);

    /**
     * Handles callback delegation from the identity provider manager
     *
     * @param callbackRequest the request received from the callback
     * @return user object of the claimed user; {@code null} if no user or error
     * @implSpec If the plugin determines that a user can be logged in,
     * the returned user object should contain all the necessary information required to potentially provision the user.
     * <p>
     * This method should return {@code null} if the plugin determines that no user should be logged in
     * or if it encounters an error.
     * </p>
     */
    User handleCallback(HttpServletRequest callbackRequest);

    /**
     * Get the identity provider's authorization endpoint URL
     * <p>
     * This URL should be the page where the user logs in to the identity provider.
     *
     * @return a string representing the authorization endpoint URL
     */
    String getAuthorizationEndpointUrl();

    /**
     * Get the URL for identity provider to return authorization token to the server.
     * <p>
     * The "callbackUrl" property will be automatically injected the into the property options.
     *
     * @return a string representing the callback URL
     */
    String getCallbackUrl();

    /**
     * Get policy for user provisioning
     * <p>
     * This policy determines if a user should be automatically created when the claimed user does not exist
     * in Joget's directory manager or if this IdP is not linked to any existing user.
     * </p>
     *
     * @return {@code true} if enabled; {@code false} otherwise
     * @apiNote User provisioning can only be enabled if Joget is using a directory manager that can be modified by Joget.
     * Directory managers such as the default LDAP implementation cannot be used as Joget is unable to modify the directory.
     */
    boolean isUserProvisioningEnabled();

    /**
     * Get policy for automatic identity provider linking
     * <p>
     * This policy determines if the IdP should be automatically linked to the user when the following criteria is true:
     *     <ul><li>Claimed email equals only one Joget user's email in directory manager</li></ul>
     * </p>
     *
     * @return {@code true} if enabled; {@code false} otherwise
     */
    boolean isAutomaticLinkingEnabled();

    /**
     * Get the text used for the login button on the login screen.
     *
     * @return a string for login button label
     */
    String getLoginButtonLabel();

    /**
     * Get the image used for the login button on the login screen.
     *
     * @return a base64 encoded string of the image
     */
    String getLoginButtonIcon();
}
