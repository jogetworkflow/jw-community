package org.joget.plugin.base;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class DefaultAuditTrailPlugin extends ExtDefaultPlugin implements AuditTrailPlugin, PropertyEditable {
    @Override
    public abstract Object execute(Map props);
}
