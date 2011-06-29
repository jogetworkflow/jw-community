package org.joget.plugin.base;

import org.joget.plugin.property.model.PropertyEditable;

public abstract class DefaultAuditTrailPlugin extends DefaultPlugin implements AuditTrailPlugin, PropertyEditable {
    public final PluginProperty[] getPluginProperties() {
        return null;
    }
}
