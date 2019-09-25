package org.enhydra.shark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.CannotAcceptSuspended;
import org.enhydra.shark.api.client.wfmodel.CannotComplete;
import org.enhydra.shark.api.client.wfmodel.InvalidData;
import org.enhydra.shark.api.client.wfmodel.ResultNotAvailable;
import org.enhydra.shark.api.client.wfmodel.UpdateNotAllowed;
import org.enhydra.shark.api.internal.instancepersistence.ActivityPersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.ActivityVariablePersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.PersistentManagerInterface;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.workflow.util.WorkflowUtil;

public class CustomWfActivityImpl extends WfActivityImpl {
    private final static String MULTI_APPROVAL_PERFORMERS = "MULTI_APPROVAL_PERFORMERS";
    private boolean isHandleAllAssignments = false;
    
    public CustomWfActivityImpl(WMSessionHandle shandle, WfProcessInternal process, String key, String activityDefId, WfActivityInternal blockActivity) throws Exception {
        super(shandle, process, key, activityDefId, blockActivity);
    }
    
    protected CustomWfActivityImpl(ActivityPersistenceObject po, WfProcessInternal proc){
        super(po, proc);
    }
    
    @Override
    protected void runSubFlow(WMSessionHandle shandle) throws Exception, ToolAgentGeneralException {
        super.runSubFlow(shandle);
        
        //write to audit trail
        WorkflowUtil.addAuditTrail(this.getClass().getName(), "runSubFlow", key);
    }
    
    @Override
    public void finish(WMSessionHandle shandle) throws Exception, CannotComplete {
        super.finish(shandle);
        
        int type = getActivityDefinition(shandle).getActivityType();
        if (type == 3) {
            //write to audit trail
            WorkflowUtil.addAuditTrail(this.getClass().getName(), "finishSubFlow", key);
        }
    }
    
    @Override
    protected void initializeActivityContext(WMSessionHandle shandle) throws Exception {
        int type = getActivityDefinition(shandle).getActivityType();

        if (type != 4) {
            this.activitiesProcessContext = this.process.process_context(shandle);
        }
        this.contextInitialized = true;
    }
    
    @Override
    protected synchronized void setActivityVariables(WMSessionHandle shandle) throws Exception {
        int type = getActivityDefinition(shandle).getActivityType();
        if (type == 0) {
            if (this.contextInitialized) {
                return;
            }
            
            this.resultVariableIds = new HashSet();
            
            WorkflowProcess wp = getProcessDefinition(shandle);
            List l = new ArrayList(wp.getAllVariables().values());

            if (l.size() == 0) {
                return;
            }
            PersistentManagerInterface ipm = SharkEngineManager.getInstance()
                    .getInstancePersistenceManager();

            Iterator it = l.iterator();
            List variableIds = new ArrayList();
            while (it.hasNext()) {
                XMLCollectionElement dfOrFp = (XMLCollectionElement) it.next();
                String vdId = dfOrFp.getId();
                variableIds.add(vdId);
            }
            l = ipm.getActivityVariables(shandle, this.processId, this.key, variableIds);
            it = l.iterator();
            while (it.hasNext()) {
                ActivityVariablePersistenceObject var = (ActivityVariablePersistenceObject) it.next();
                String vdId = var.getDefinitionId();
                Object val = var.getValue();

                this.activitiesProcessContext.put(vdId, val);
                if (var.isResultVariable()) {
                    this.resultVariableIds.add(vdId);
                }
            }

            this.contextInitialized = true;
        } else {
            super.setActivityVariables(shandle);
        }
    }
    
    public void finishWithoutContinue(WMSessionHandle shandle) throws Exception, CannotComplete {
        try {
            removeAssignments(shandle, true, true);
            change_state(shandle, "closed.completed");
            this.process.set_process_context(shandle, resultMap(shandle));
            //this.process.activity_complete(shandle, this); comment this to not continue the process
        } catch (InvalidData e) {
            throw new CannotComplete("Activity "
                    + this
                    + " -> can't complete activity. Invalid result data was passed.");
        } catch (ResultNotAvailable rne) {
            throw new CannotComplete("Activity "
                    + this
                    + " -> can't complete activity. Result of activity is not available.");
        } catch (UpdateNotAllowed una) {
            throw new CannotComplete("Activity "
                    + this
                    + " -> can't complete activity. Process context update is not allowed.");
        }
    }
    
    @Override
    protected boolean handleAllAssignments(WMSessionHandle shandle) throws Exception {
        return isHandleAllAssignments;
    }
    
    public void setHandleAllAssignments(WMSessionHandle shandle, boolean handleAllAssignments) throws Exception {
        if (handleAllAssignments) {
            this.set_priority(shandle, (short) 5); //since priority is not use in joget, use it to indicate multi approval
        }
        isHandleAllAssignments = handleAllAssignments;
    }
    
    @Override
    public String getResourceUsername(WMSessionHandle shandle) throws Exception {
        if (this.priority == 5) {
            String usernames = (String) getContext(shandle).get(MULTI_APPROVAL_PERFORMERS);
            return usernames;
        }
        return this.resourceUsername;
    }
    
    @Override
    public void set_accepted_status(WMSessionHandle shandle, boolean accept, String resourceUname) throws Exception, CannotAcceptSuspended {
        boolean temp = isHandleAllAssignments;
        if (accept && this.priority == 5) {
            String usernames = (String) getContext(shandle).get(MULTI_APPROVAL_PERFORMERS);
            if (usernames == null || usernames.isEmpty()) {
                usernames = resourceUname;
            } else if (!(usernames + ";").contains(resourceUname+";")) {
                usernames += ";" + resourceUname;
            }
            internalVariable(shandle, MULTI_APPROVAL_PERFORMERS, usernames);
            isHandleAllAssignments = true;
        }
        super.set_accepted_status(shandle, accept, resourceUname);
        isHandleAllAssignments = temp;
    }
    
    protected void internalVariable(WMSessionHandle shandle, String key, String value) throws Exception {
        getContext(shandle).put(key, value);
        this.variableIdsToPersist.add(key);
        persistActivityContext(shandle);
    }
    
    @Override
    public Map process_context(WMSessionHandle shandle) throws Exception {
        Map m = super.process_context(shandle);
        //remove internal variable
        if (m.containsKey(MULTI_APPROVAL_PERFORMERS)) {
            m.remove(MULTI_APPROVAL_PERFORMERS);
        }
        return m;
    }    
}
