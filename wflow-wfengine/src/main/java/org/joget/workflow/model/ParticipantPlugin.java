package org.joget.workflow.model;

import java.util.Collection;
import java.util.Map;

public interface ParticipantPlugin {

    Collection<String> getActivityAssignments(Map props);
}
