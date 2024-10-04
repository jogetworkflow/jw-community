package org.joget.directory.model.mfa;

import org.joget.directory.model.mfa.exception.MfaExecutionException;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.property.model.PropertyEditable;

import jakarta.servlet.http.HttpServletRequest;

public interface MfaPlugin extends Plugin, PropertyEditable {

    /**
     * Process MFA login request.
     * This method will be called by the MfaManager to delegate MFA login to the plugin.
     *
     * @param username the username of the request user
     * @param request  the HTTP request of the current session
     * @param secret   the secret (if applicable) for the plugin to validate the data with
     * @return true if MFA verification success; false if undefined error. Consider throwing {@link MfaExecutionException}
     * with custom message if error.
     * @throws MfaExecutionException when the processing fails such as invalid data, secret, etc.
     *                               The exception message will be shown in a banner above the MFA login page.
     */
    boolean login(String username, HttpServletRequest request, String secret) throws MfaExecutionException;

    /**
     * Process MFA registration request.
     * This method will be called by the MfaManager to delegate MFA registration to the plugin.
     *
     * @param username the username of the request user
     * @param request  the HTTP request of the current session
     * @return string representation of secret associated with username to be saved to database for future login calls
     * @throws MfaExecutionException when the processing fails such as invalid username, data, etc.
     *                               The exception message will be shown in a banner above the MFA register page.
     */
    String register(String username, HttpServletRequest request) throws MfaExecutionException;

    /**
     * Gets the (friendly) display name of the plugin. Shown in MFA list and user profile.
     *
     * @return display name in string
     */
    String getDisplayName();

    /**
     * Get the login HTML of this plugin to be shown to the user
     *
     * @param username the username of the request user
     * @param request  the HTTP request of the current session
     * @return the HTML in string
     * @throws MfaExecutionException when a fatal error occurs such as processing fails, etc.
     *                               The exception message will be shown in a banner above the MFA register page.
     */
    String getLoginTemplate(String username, HttpServletRequest request) throws MfaExecutionException;

    /**
     * Get the register HTML of this plugin to be shown to the user
     *
     * @param username the username of the request user
     * @param request  the HTTP request of the current session
     * @return the HTML in string
     * @throws MfaExecutionException when a fatal error occurs such as processing fails, etc.
     *                               The exception message will be shown in a banner above the MFA register page.
     */
    String getRegisterTemplate(String username, HttpServletRequest request) throws MfaExecutionException;
}
