package org.joget.workflow.model;

import java.util.Collection;
import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface of Process Participant Plugin
 * 
 */
public interface ParticipantPlugin extends PropertyEditable {

    /**
     * Gets usernames for the assignment.
     * 
     * A org.joget.workflow.model.WorkflowActivity object is passed as "workflowActivity" property.
     * 
     * @param props
     * @return 
     */
    Collection<String> getActivityAssignments(Map props);
}
