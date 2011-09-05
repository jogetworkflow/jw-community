package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DefaultDeadlinePlugin extends ExtDefaultPlugin implements DeadlinePlugin {
    public final Object execute(Map props) {
        return evaluateDeadline(props);
    }
}
