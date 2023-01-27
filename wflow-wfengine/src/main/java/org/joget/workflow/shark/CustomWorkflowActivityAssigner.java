package org.joget.workflow.shark;

import org.joget.workflow.model.service.WorkflowActivityAssigner;
import org.joget.workflow.model.WorkflowAssignment;
import java.util.ArrayList;
import java.util.Collection;

public class CustomWorkflowActivityAssigner implements WorkflowActivityAssigner {

    public Collection<String> getActivityAssignments(WorkflowAssignment newAssignment, String requesterId) {
        Collection<String> assignees = new ArrayList<String>();
        assignees.add("demo");
        return assignees;
    }
}
