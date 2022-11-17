package org.joget.workflow.shark;

import org.joget.commons.util.LogUtil;
import org.enhydra.shark.StandardToolActivityHandler;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.springframework.context.ApplicationContext;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.util.WorkflowUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;

public class WorkflowToolActivityHandler extends StandardToolActivityHandler {

    public WorkflowToolActivityHandler() {
        super();
    }

    @Override
    public void executeActivity(WMSessionHandle shandle, WfActivityInternal act) throws Exception, ToolAgentGeneralException {

        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");
        WorkflowAssignment workflowAssignment = null;
        
        try {
            String processId = act.container(shandle).manager(shandle).name(shandle);
            String activityId = act.activity_definition_id(shandle);
            String version = act.container(shandle).manager(shandle).version(shandle);
            
            // retrieve assignment
            workflowAssignment = new WorkflowAssignment();
            workflowAssignment.setProcessId(act.process_id(shandle));
            workflowAssignment.setProcessDefId(processId);
            workflowAssignment.setProcessName(act.container(shandle).name(shandle));
            workflowAssignment.setProcessVersion(version);
            workflowAssignment.setProcessRequesterId((String) shandle.getVendorData());
            workflowAssignment.setDescription(act.description(shandle));
            workflowAssignment.setActivityDefId(activityId);
            workflowAssignment.setActivityId(act.key(shandle));
            workflowAssignment.setActivityName(act.name(shandle));
            workflowAssignment.setAssigneeId(act.getPerformerId(shandle));
            
            //call this just to set process link for subflow
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            workflowManager.getProcessDefIdByInstanceId(workflowAssignment.getProcessId());

            // retrieve workflow variables
            List<WorkflowVariable> processVariableList = new ArrayList();
            Map variableMap = act.process_context(shandle);
            Iterator it = variableMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry) it.next();
                WorkflowVariable var = new WorkflowVariable();
                var.setId(pairs.getKey());
                var.setVal(pairs.getValue());
                processVariableList.add(var);
            }
            workflowAssignment.setProcessVariableList(processVariableList);
            
            // execute tool
            WorkflowUtil.addAuditTrail(this.getClass().getName(), "executeTool", workflowAssignment.getActivityId(), new Class[]{WorkflowAssignment.class}, new Object[]{workflowAssignment}, null);
            if(!workflowMapper.executeTool(workflowAssignment)){
                WorkflowUtil.addAuditTrail(this.getClass().getName(), "executeActivity", "Could not execute tool [processId=" + act.container(shandle).manager(shandle).name(shandle) + ", version=" + act.container(shandle).manager(shandle).version(shandle) + ", activityId=" + act.activity_definition_id(shandle) + "]", new Class[]{WorkflowAssignment.class}, new Object[]{workflowAssignment}, null);
            }else{
                WorkflowUtil.addAuditTrail(this.getClass().getName(), "executeToolCompleted", workflowAssignment.getActivityId(), new Class[]{WorkflowAssignment.class}, new Object[]{workflowAssignment}, null);
            }

        } catch (Exception ex) {
            workflowMapper.addAuditTrail(this.getClass().getName(), "executeActivity", "Could not execute tool [processId=" + act.container(shandle).manager(shandle).name(shandle) + ", version=" + act.container(shandle).manager(shandle).version(shandle) + ", activityId=" + act.activity_definition_id(shandle) + "]", new Class[]{WorkflowAssignment.class}, new Object[]{workflowAssignment}, null);
            LogUtil.error(getClass().getName(), ex, "Could not execute tool [processId=" + act.container(shandle).manager(shandle).name(shandle) + ", version=" + act.container(shandle).manager(shandle).version(shandle) + ", activityId=" + act.activity_definition_id(shandle) + "]");
        }
    }
}
