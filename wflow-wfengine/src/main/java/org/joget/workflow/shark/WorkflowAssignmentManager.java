package org.joget.workflow.shark;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import java.util.List;
import java.util.Set;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.assignment.PerformerData;
import org.enhydra.shark.assignment.HistoryRelatedAssignmentManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.ApplicationContext;

public class WorkflowAssignmentManager extends HistoryRelatedAssignmentManager {

    @Override
    public List<String> getDefaultAssignments(WMSessionHandle shandle, String instanceId,
            String actId, String processRequesterId,
            PerformerData xpdlParticipant, List xpdlResponsibleParticipants)
            throws Exception {

        //initialization
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        String actDefId = actId.substring(actId.indexOf(instanceId) + instanceId.length() + 1);

        List<String> migrationUser = workflowManager.getMigrationAssignmentUserList(instanceId, actDefId);
        if (migrationUser != null) {
            return migrationUser;
        }
        
        String procDefId = workflowManager.getProcessDefIdByInstanceId(instanceId);
        WorkflowProcess process = workflowManager.getProcess(procDefId);
        String currentUsername = (String) shandle.getVendorData();

        if (currentUsername.equals(WorkflowUserManager.ROLE_ANONYMOUS)) {
            currentUsername = processRequesterId;
        }

        String[] temp = procDefId.split("#");
        String version = temp[1];
        List<String> resultList = WorkflowUtil.getAssignmentUsers(process.getPackageId(), procDefId, instanceId, version, actId, currentUsername, xpdlParticipant.participantIdOrExpression);

        if (resultList == null || resultList.isEmpty()) {
            resultList = super.getDefaultAssignments(shandle, procDefId, actId, currentUsername, xpdlParticipant, xpdlResponsibleParticipants);
        }
        LogUtil.info(getClass().getName(), "[processId=" + instanceId + ", processDefId=" + procDefId + ", participantId=" + xpdlParticipant.participantIdOrExpression + ", next user=" + resultList + "]");
        
        Collection<Class> paramTypes = new ArrayList<Class>();
        Collection<Object> args = new ArrayList<Object>();
        paramTypes.add(String.class);
        args.add(instanceId);//process instance id
        paramTypes.add(String.class);
        args.add(actId);//activity instance id
        paramTypes.add(WorkflowProcess.class);
        args.add(process); //process object
        
        //write to audit trail
        WorkflowUtil.addAuditTrail(this.getClass().getName(), "getDefaultAssignments", actId, paramTypes.toArray(new Class[]{}), args.toArray(), resultList);

        return resultList;
    }

    @Override
    protected Set findResources(WMSessionHandle shandle, PerformerData p) throws Exception {
        return super.findResources(shandle, p);
    }
}
