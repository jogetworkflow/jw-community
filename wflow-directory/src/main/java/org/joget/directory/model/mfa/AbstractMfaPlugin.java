package org.joget.directory.model.mfa;

import org.joget.directory.model.service.IdpMfaUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class AbstractMfaPlugin extends ExtDefaultPlugin implements MfaPlugin {

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public final String getClassName() {
        return IdpMfaUtil.getClassName(this);
    }
}
