package org.joget.workflow.model.service;

import org.joget.workflow.model.*;
import java.util.Collection;

public interface WorkflowActivityAssigner {
	
	public Collection<String> getActivityAssignments(WorkflowAssignment newAssignment, String requesterId);

}
