package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface of Deadline Plugin
 * 
 */
public interface DeadlinePlugin extends PropertyEditable {
    
    /**
     * Re-calculate the deadline limit and SLA limit.
     * 
     * Following are passed as properties by system:
     * - processId : java.lang.String
     * - activityId : java.lang.String
     * - workflowDeadline : org.joget.workflow.model.WorkflowDeadline
     * - processStartedTime : java.util.Date
     * - activityAcceptedTime : java.util.Date
     * - activityActivatedTime : java.util.Date
     * - pluginManager : org.joget.plugin.base.PluginManager
     * 
     * @param props
     * @return 
     */
    WorkflowDeadline evaluateDeadline(Map props);
}
