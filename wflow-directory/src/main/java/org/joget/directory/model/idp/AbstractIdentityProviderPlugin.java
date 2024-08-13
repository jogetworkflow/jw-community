package org.joget.directory.model.idp;

import org.joget.commons.util.StringUtil;
import org.joget.directory.model.service.IdpMfaUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

import javax.servlet.http.HttpServletRequest;

/**
 * It is recommended that {@link IdentityProviderPlugin} implementations extend this class
 * since this class provides default and expected implementations for required elements of certain methods.
 * <p>
 * It is also possible to implement {@link IdentityProviderPlugin} interface without extending this class.
 */
public abstract class AbstractIdentityProviderPlugin extends ExtDefaultPlugin implements IdentityProviderPlugin {

    private String uuid;

    /**
     * Get the URL for identity provider to return authorization token to the server.
     * <br><br>
     * "Conventional" identity providers such as OpenID use a callback URL to determine where to redirect the user
     * after authentication is successful. This callback URL should be provided during plugin configuration or anytime
     * during the plugin's lifetime so that it can be used to configure the external identity provider.
     * <br><br>
     * Ideally, it should be injected into the plugin properties so that it can be shown in the configuration page.
     * It is not recommended to construct the callback URL manually as the URL may change in a future API
     * update.
     *
     * @param request the request used to obtain the callback URL, can be obtained by calling
     *                {@code WorkflowUtil.getHttpServletRequest()}
     * @return a string representing the callback URL
     */
    public String getCallbackUrl(HttpServletRequest request) {
        return IdpMfaUtil.getIdpCallbackUrl(request, getUuid());
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getAuthorizationEndpoint() {
        return getPropertyString("authEndpoint");
    }

    @Override
    public boolean isUserProvisioningEnabled() {
        return "true".equals(getPropertyString("userProvisioning"));
    }

    @Override
    public boolean isProvisionedUserProfileEditable() {
        return "true".equals(getPropertyString("editableUserProfile"));
    }

    @Override
    public boolean isAutomaticLinkingEnabled() {
        return "true".equals(getPropertyString("automaticLinking"));
    }

    @Override
    public IdpLoginButtonTemplate getLoginButtonTemplate() {
        // perform relaxed HTML strip because icon-textfield generates its own HTML for icons (<i> tag) and label
        String content = getPropertyString("loginButtonTemplate");
        content = StringUtil.stripHtmlRelaxed(content);
        return new IdpLoginButtonTemplate(true, content);
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
