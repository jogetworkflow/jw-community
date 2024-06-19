package org.joget.directory.model.mfa;

import org.joget.directory.model.mfa.exception.MfaExecutionException;

public interface MfaPlugin {

    /**
     * Process MFA login request.
     * This method will be called by the MfaManager to delegate MFA login to the plugin.
     *
     * @param username username of the Joget user
     * @param data     the data submitted by the form
     * @param secret   the secret (if applicable) for the plugin to validate the data with
     * @return true if the validation passes; false if it fails
     * @throws MfaExecutionException when the processing fails such as invalid data, secret, etc.
     *                               The exception message will be shown in a banner above the MFA login page.
     */
    boolean login(String username, String data, String secret) throws MfaExecutionException;

    /**
     * Process MFA registration request.
     * This method will be called by the MfaManager to delegate MFA registration to the plugin.
     *
     * @param username username of the Joget user
     * @param data     the data submitted by the form
     * @return string representation of secret associated with username to be saved to database for future login calls
     * @throws MfaExecutionException when the processing fails such as invalid username, data, etc.
     *                               The exception message will be shown in a banner above the MFA register page.
     */
    String register(String username, String data) throws MfaExecutionException;

    /**
     * Gets the (friendly) display name of the plugin. Shown in MFA list and user profile.
     *
     * @return display name in string
     */
    String getDisplayName();

    /**
     * Get the login HTML of this plugin to be shown to the user
     *
     * @return the HTML in string
     */
    String getLoginTemplate();

    /**
     * Get the register HTML of this plugin to be shown to the user
     *
     * @return the HTML in string
     */
    String getRegisterTemplate();
}
