package org.joget.directory.model.mfa;

import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class AbstractMfaPlugin extends ExtDefaultPlugin implements MfaPlugin, PropertyEditable {

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
