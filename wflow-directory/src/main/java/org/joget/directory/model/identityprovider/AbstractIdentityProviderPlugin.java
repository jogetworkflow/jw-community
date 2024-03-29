package org.joget.directory.model.identityprovider;

import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * It is recommended that all {@link IdentityProviderPlugin} implementations extend this class
 * since this class provides default and expected implementations for required elements of certain methods.
 * <p>
 * It is also possible to implement {@link IdentityProviderPlugin} interface without extending this class.
 */
public abstract class AbstractIdentityProviderPlugin extends ExtDefaultPlugin implements IdentityProviderPlugin, PropertyEditable {

    private String uuid;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getAuthorizationEndpointUrl() {
        String unescapedJson = StringUtil.unescapeJSON(getPropertyString("authEndpointUrl"));
        String escapedHtml = StringUtil.escapeString(unescapedJson, StringUtil.TYPE_HTML);
        return escapedHtml;
    }

    @Override
    public String getCallbackUrl() {
        String unescapedJson = StringUtil.unescapeJSON(getPropertyString("callbackUrl"));
        String escapedHtml = StringUtil.escapeString(unescapedJson, StringUtil.TYPE_HTML);
        return escapedHtml;
    }

    @Override
    public boolean isUserProvisioningEnabled() {
        return "true".equals(getPropertyString("userProvisioning"));
    }

    @Override
    public boolean isAutomaticLinkingEnabled() {
        return "true".equals(getPropertyString("automaticLinking"));
    }

    @Override
    public String getLoginButtonIconBase64() {
        String unescapedJson = StringUtil.unescapeJSON(getPropertyString("loginButtonIconBase64"));
        String escapedHtml = StringUtil.escapeString(unescapedJson, StringUtil.TYPE_HTML);
        return escapedHtml;
    }

    @Override
    public String getLoginButtonLabel() {
        String unescapedJson = StringUtil.unescapeJSON(getPropertyString("loginButtonLabel"));
        String escapedHtml = StringUtil.escapeString(unescapedJson, StringUtil.TYPE_HTML);
        return escapedHtml;
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
