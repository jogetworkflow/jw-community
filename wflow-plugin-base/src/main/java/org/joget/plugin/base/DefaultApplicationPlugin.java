package org.joget.plugin.base;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class DefaultApplicationPlugin extends ExtDefaultPlugin implements ApplicationPlugin, PropertyEditable {
    @Override
    public abstract Object execute(Map props);
}
