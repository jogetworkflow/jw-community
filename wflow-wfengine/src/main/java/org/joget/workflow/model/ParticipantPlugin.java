package org.joget.workflow.model;

import java.util.Collection;
import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public interface ParticipantPlugin extends PropertyEditable {

    Collection<String> getActivityAssignments(Map props);
}
