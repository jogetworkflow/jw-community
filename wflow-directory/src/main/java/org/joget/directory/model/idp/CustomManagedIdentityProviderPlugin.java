package org.joget.directory.model.idp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This interface enables an Identity Provider plugin to provide and self-manage "sub-IDs" as well as performing custom
 * logic for validating/verifying their "sub-IDs".
 * <br><br>
 *
 * An example plugin that might implement this interface is a passkeys
 * IdP plugin which allows each user to register multiple passkeys under one IdP plugin. This example plugin will be
 * required to perform its own validation & verification of each user's passkeys via the
 * {@link IdentityProviderPlugin#handleCallback(HttpServletRequest)} method.
 * <br><br>
 *
 * Identity Provider plugins that implement this interface will have to perform all necessary actions that are
 * originally performed by the Identity Provider Manager such as:
 * <ul>
 *     <li>Keeping track of each "sub-ID" and their corresponding user for use during IdP registration and login</li>
 *     <li>Handling validation/verification of the "sub-ID" during IdP registration and login</li>
 *     <li>
 *         Unlinking logic when a user unlinks a "sub-ID" provided by this plugin.
 *         See {@link IdentityProviderPlugin#onUnlink(String, HttpServletRequest)} for executing custom unlinking logic.
 *     </li>
 * </ul>
 *
 * Implementing this interface will change the "Link/Unlink" button for this plugin in the user profile to "Manage".
 * Clicking on this "Manage" button will display a popup which allows the user to perform custom link/unlink procedures
 * defined by the plugin that otherwise cannot be performed with the "Link/Unlink" button
 * <br><br>
 *
 * The plugin's managed service UI can be accessed via this URL:
 * <pre>/&lt;context_path&gt;/web/idp/&lt;plugin_uuid&gt;/managedService</pre>
 * and subsequently, the URL provided by {@link IdentityProviderPlugin#getAuthorizationEndpointUrl()} will be ignored.
 * <br><br>
 *
 * The contents of the popup must be controlled by the plugin via the
 * {@link CustomManagedIdentityProviderPlugin#managedService(HttpServletRequest, HttpServletResponse)} method.
 * No permissions/limit will be enforced, therefore it is up to the plugin to perform access control.
 * <br><br>
 *
 * Although there are no limitations on this, it is recommended that plugins implementing this interface
 * should not implement {@link MultiInstanceIdentityProviderPlugin} or extend any classes that implement that interface
 * as behavior is undefined. If implementing said interface is required, additional care should be taken to consider
 * potential edge cases not supported by the existing implementation.
 */
public interface CustomManagedIdentityProviderPlugin extends IdentityProviderPlugin {
    void managedService(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
