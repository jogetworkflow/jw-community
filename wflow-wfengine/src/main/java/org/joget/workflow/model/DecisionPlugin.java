package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public interface DecisionPlugin extends PropertyEditable {
    
    DecisionResult getDecision(String processDefId, String processId, String routeId, Map<String, String> variables);
    
}
