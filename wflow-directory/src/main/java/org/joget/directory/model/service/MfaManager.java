package org.joget.directory.model.service;

import org.joget.directory.model.User;
import org.joget.directory.model.mfa.exception.MfaExecutionException;

/**
 * Interface to provide external usage of private implementation via Spring beans.
 */
public interface MfaManager {

    /**
     * Handle login request from the controller and delegates it to the specified plugin
     *
     * @param pluginClassName the class name of the delegated plugin
     * @param username        the username of the user of the login request
     * @param data            the form data submitted from the request
     * @return true if MFA verification success; false if undefined error. Prefer to throw {@link MfaExecutionException} if error.
     * @throws ClassNotFoundException when the plugin does not exist
     * @throws MfaExecutionException  when error occurs during MFA verification
     */
    boolean login(String pluginClassName, String username, String data) throws ClassNotFoundException, MfaExecutionException;

    /**
     * Handle registration request from the controller and delegates it to the specified plugin
     *
     * @param pluginClassName the class name of the delegated plugin
     * @param username        the username of the user of the registration request
     * @param data            the form data submitted from the request
     * @return true if MFA verification success; false if undefined error. Prefer to throw {@link MfaExecutionException} if error.
     * @throws ClassNotFoundException when the plugin does not exist
     * @throws MfaExecutionException  when error occurs during MFA verification
     */
    boolean register(String pluginClassName, String username, String data) throws ClassNotFoundException, MfaExecutionException;

    /**
     * Unlinks the specified MFA method by class name from the user
     *
     * @param pluginClassName the class name of the delegated plugin
     * @param username        the username of the user of the unlink request
     * @return true if unlink success; false if failed to unlink
     */
    boolean unlink(String pluginClassName, String username);

    /**
     * Checks if the specified user requires MFA authentication before logging in.
     *
     * @param username the username of the user to check
     * @return true if MFA is required; false if not required
     */
    boolean isMfaRequired(String username);

    /**
     * Gets the URL path to the MFA landing page of the user.
     * The content returned by this page is determined by the MfaController.
     *
     * @param username the username to be associated with the generated nonce. Used to keep context of the request.
     * @return a String representation of the landing page URL
     */
    String getMfaLoginLandingPage(String username);

    /**
     * Gets the MFA section to be displayed in the user's profile page
     *
     * @param user a {@link User} object
     * @return HTML string of the rendered MFA section
     */
    String getProfileFooterHtml(User user);
}
