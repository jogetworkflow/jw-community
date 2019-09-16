package org.enhydra.shark;

import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.CannotComplete;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

public class CustomWfActivityWrapper extends WfActivityWrapper {
    protected WfActivityInternal internal;
    protected Activity activityDefinition;
    protected WorkflowProcess processDefinition;
    protected String packageId;
    protected String packageVer;
    protected WfProcessInternal process;
    
    public CustomWfActivityWrapper(WMSessionHandle shandle, String processDefId, String processId, String id) {
        super(shandle, processDefId, processId, id);
    }
    
    public WfProcessInternal getProcessImpl() throws Exception {
        if (process == null) {
            process = SharkUtilities.getProcess(shandle, processId, SharkUtilities.READ_ONLY_MODE);
        }
        return process;
    }
    
    public WfActivityInternal getActivityImpl() throws Exception {
        if (internal == null) {
            internal = getProcessImpl().getActivity(shandle, id);
        }
        return internal;
    }
    
    public List<String> getAssignmentResourceIds() throws Exception {
        return getActivityImpl().getAssignmentResourceIds(shandle);
    }
    
    public Activity getActivityDefinition() throws Exception {
        if (activityDefinition == null) {
            activityDefinition = SharkUtilities.getActivityDefinition(shandle, getActivityImpl(), getProcessDefinition());
        }
        return activityDefinition;
    }
    
    public WorkflowProcess getProcessDefinition() throws Exception
    {
        if (processDefinition == null) {
            processDefinition =  SharkUtilities.getWorkflowProcess(shandle, getProcessImpl().package_id(shandle), getProcessImpl().manager_version(shandle), getProcessImpl().process_definition_id(shandle));
        }
        return this.processDefinition;
    }
    
    public String getSubflowProcessId() throws Exception {
        if (isSubflow()) {
            String performerId = getActivityImpl().getPerformerId(shandle);
            if (performerId != null) {
                return performerId;
            }
        }
        return null;
    }
    
    public boolean isSubflow() throws Exception {
        int type = getActivityDefinition().getActivityType();
        return 3 == type;
    }
    
    public static List<String> getAssignmentResourceIds(WMSessionHandle shandle,
                               String mgrName,
                               String processId,
                               String id) throws Exception{
        WfActivityWrapper wrapper = new WfActivityWrapper(shandle, mgrName, processId, id);
        WfActivityInternal internal = wrapper.getActivityImpl(processId, id, SharkUtilities.READ_ONLY_MODE);
        return internal.getAssignmentResourceIds(shandle);
    }
    
    @Override
    public void complete() throws Exception, CannotComplete {
        long tStamp = SharkUtilities.methodStart(this.shandle, "WfActivityWrapper.complete");
        try {
            checkSecurity("complete", null);

            WfActivityInternal actInternal = getActivityImpl();
            
            int type = getActivityDefinition().getActivityType();
            if (type == 2) {
                actInternal.finish(this.shandle);
            } else {
                actInternal.complete(this.shandle);
            }
        } finally {
            SharkUtilities.methodEnd(this.shandle, tStamp, "WfActivityWrapper.complete", this);
        }
    }
    
    public void completeWithoutContinue() throws Exception, CannotComplete {
        long tStamp = SharkUtilities.methodStart(this.shandle, "WfActivityWrapper.complete");
        try {
            checkSecurity("complete", null);

            CustomWfActivityImpl actInternal = (CustomWfActivityImpl) getActivityImpl();
            
            int type = getActivityDefinition().getActivityType();
            if (type != 2) {
                actInternal.finishWithoutContinue(this.shandle);
            }
        } finally {
            SharkUtilities.methodEnd(this.shandle, tStamp, "WfActivityWrapper.complete", this);
        }
    }
    
    public String getPerformers() throws Exception {
        WfActivityInternal actInternal = getActivityImpl();
        return actInternal.getResourceUsername(this.shandle);
    }
}
