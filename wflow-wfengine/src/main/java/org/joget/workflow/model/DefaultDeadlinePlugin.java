package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class DefaultDeadlinePlugin extends DefaultPlugin implements DeadlinePlugin, PropertyEditable {
    public final PluginProperty[] getPluginProperties() {
        return null;
    }
    
    public final Object execute(Map props) {
        return evaluateDeadline(props);
    }
}
