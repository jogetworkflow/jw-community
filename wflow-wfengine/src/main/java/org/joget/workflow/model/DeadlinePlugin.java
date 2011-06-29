package org.joget.workflow.model;

import java.util.Map;

public interface DeadlinePlugin {
    WorkflowDeadline evaluateDeadline(Map props);
}
