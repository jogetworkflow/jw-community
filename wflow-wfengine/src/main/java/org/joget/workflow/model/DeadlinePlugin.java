package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public interface DeadlinePlugin extends PropertyEditable {
    WorkflowDeadline evaluateDeadline(Map props);
}
