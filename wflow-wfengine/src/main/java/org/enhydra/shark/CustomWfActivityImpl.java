package org.enhydra.shark;

import com.lutris.appserver.server.sql.CachedDBTransaction;
import com.lutris.appserver.server.sql.DBTransaction;
import com.lutris.dods.builder.generator.query.DataObjectException;
import com.lutris.util.ConfigException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.enhydra.dods.DODS;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.CannotAcceptSuspended;
import org.enhydra.shark.api.client.wfmodel.CannotComplete;
import org.enhydra.shark.api.client.wfmodel.InvalidData;
import org.enhydra.shark.api.client.wfmodel.InvalidState;
import org.enhydra.shark.api.client.wfmodel.ResultNotAvailable;
import org.enhydra.shark.api.client.wfmodel.TransitionNotAllowed;
import org.enhydra.shark.api.client.wfmodel.UpdateNotAllowed;
import org.enhydra.shark.api.internal.instancepersistence.ActivityPersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.ActivityVariablePersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.DeadlinePersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.PersistentManagerInterface;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfAssignmentInternal;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.api.internal.working.WfResourceInternal;
import org.enhydra.shark.instancepersistence.data.ActivityDO;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Deadline;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.model.CustomDeadlinePersistenceObject;
import org.joget.workflow.shark.model.dao.DeadlineDao;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.util.WorkflowUtil;

public class CustomWfActivityImpl extends WfActivityImpl {
    private final static String MULTI_APPROVAL_PERFORMERS = "MULTI_APPROVAL_PERFORMERS";
    private boolean isHandleAllAssignments = false;
    private Collection<CustomDeadlinePersistenceObject> deadlines;
    
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
    
    /**
     * Override the original change_state function to write transaction immediately after persist
     * 
     * @param shandle
     * @param new_state
     * @throws Exception
     * @throws InvalidState
     * @throws TransitionNotAllowed 
     */
    @Override
    protected void change_state(WMSessionHandle shandle, String new_state) throws Exception, InvalidState, TransitionNotAllowed {
        try {
            if (!SharkUtilities.valid_activity_states(state(shandle)).contains(new_state)) {
                throw new TransitionNotAllowed("Activity " + this + " - current state is " + this.state + ", can't change to state " + new_state + "!");
            }
            
            String oldState = this.state;
            this.state = new_state;
            
            this.lastStateTime = System.currentTimeMillis();
            
            persist(shandle);
            
            //write transaction now instead of write before next query execute to catch the exception
            SharkUtil.transactionWrite(shandle); 
            
            this.lastStateEventAudit = SharkEngineManager.getInstance().getObjectFactory().createStateEventAuditWrapper(shandle, this, "activityStateChanged", oldState, new_state);
        } catch (DataObjectException e) {
            //cannot change state as the activity already updated by another transaction
            if (e.getMessage() != null 
                    && e.getMessage().contains("Couldn't write transaction: java.sql.SQLException: Update failed") 
                    && e.getMessage().contains("does exist with version=")) {
                //throw exception to stop process continue as it should already handled by another transaction
                throw new TransitionNotAllowed("Activity " + this + " cannot write transaction to change state ("+new_state+") due to data version changed.");
            } else {
                throw e;
            }
        }
    }
    
    @Override
    public void finish(WMSessionHandle shandle) throws Exception, CannotComplete {
        try {
            super.finish(shandle);
        } catch (TransitionNotAllowed e) {
            LogUtil.warn(CustomWfActivityImpl.class.getName(), e.getLocalizedMessage());
        }
        
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
    protected Activity getActivityDefinition(WMSessionHandle shandle) throws Exception {
        if (this.activityDefinition == null) {
            this.activityDefinition = SharkUtilities.getActivityDefinition(shandle, this,  getProcessDefinition(shandle));
        }
        if (this.activityDefinition == null) {
            WorkflowAssignmentDao dao = (WorkflowAssignmentDao) WorkflowUtil.getApplicationContext().getBean("workflowAssignmentDao");
            MigrateActivity act = dao.getActivityProcessDefId(this.key);
            
            String pkgId = WorkflowUtil.getProcessDefPackageId(act.getProcessDefId());
            String pkgVer = WorkflowUtil.getProcessDefVersion(act.getProcessDefId());
            String wpId = WorkflowUtil.getProcessDefIdWithoutVersion(act.getProcessDefId());
            
            this.activityDefinition = SharkUtilities.getActivityDefinition(shandle, pkgId, pkgVer, wpId, act.getDefId());
        }
        return this.activityDefinition;
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

    public Collection<CustomDeadlinePersistenceObject> getDeadlines() {
        return deadlines;
    }

    public void setDeadlines(Collection<CustomDeadlinePersistenceObject> deadlines) {
        this.deadlines = deadlines;
    }
    
    @Override
    public boolean checkDeadlines(WMSessionHandle shandle, long timeLimitBoundary, Map actsToAsyncExcNames) throws Exception {
        checkReadOnly();
        String syncDeadlineExcName = null;
        List brokenDeadlines = null;
        List excNames = new ArrayList();
        if (performDeadlineReevaluation()) {
            List pDeadlines = new ArrayList();
            
            if (deadlines != null && !deadlines.isEmpty()) {
                for (CustomDeadlinePersistenceObject d : deadlines) {
                    pDeadlines.add(d.getDeadlinePersistenceObject());
                }
            }
            
            Collections.sort(pDeadlines, new DeadlineComparator());
            brokenDeadlines = new ArrayList();
            reevaluateDeadlines(shandle, timeLimitBoundary, pDeadlines, brokenDeadlines);
        } else {
            brokenDeadlines = SharkEngineManager.getInstance()
                    .getInstancePersistenceManager()
                    .getAllDeadlinesForActivity(shandle,
                            this.processId,
                            this.key,
                            timeLimitBoundary);
        }

        if ((brokenDeadlines != null) && (brokenDeadlines.size() > 0)) {
            boolean raiseAsyncDeadlineOnce = new Boolean(
                    SharkEngineManager.getInstance().getCallbackUtilities()
                            .getProperty("Deadlines.raiseAsyncDeadlineOnlyOnce", "true")).booleanValue();

            for (int i = 0; i < brokenDeadlines.size(); i++) {
                DeadlinePersistenceObject dpi = (DeadlinePersistenceObject) brokenDeadlines.get(i);
                if ((!dpi.isExecuted()) || (!raiseAsyncDeadlineOnce)) {
                    persistExecutedDeadline(shandle, dpi.getUniqueId());
                    String excName = dpi.getExceptionName();
                    if (dpi.isSynchronous()) {
                        syncDeadlineExcName = excName;
                        break;
                    }
                    if (!excNames.contains(excName)) {
                        excNames.add(excName);
                    }
                }
            }
        }
        if (syncDeadlineExcName != null) {
            finishImproperlyAndNotifyProcess(shandle, syncDeadlineExcName);
        } else {
            if (excNames.size() > 0) {
                actsToAsyncExcNames.put(this, excNames);
            }
            int type = getActivityDefinition(shandle).getActivityType();

            if (type == 4) {
                List actActs = this.process.getAllActiveActivitiesForBlockActivity(shandle, this.key);
                Iterator it = actActs.iterator();
                while (it.hasNext()) {
                    Map ataens = new HashMap();
                    WfActivityInternal act = (WfActivityInternal) it.next();
                    boolean syncDeadlineHappened = act.checkDeadlines(shandle,
                            timeLimitBoundary,
                            ataens);
                    if (!syncDeadlineHappened) {
                        if (ataens.size() > 0) {
                            actsToAsyncExcNames.putAll(ataens);
                        }
                    }
                }
            }
        }
        return syncDeadlineExcName != null;
    }
    
    protected void reevaluateDeadlines(WMSessionHandle shandle, long timeLimitBoundary, List pDeadlines, List brokenDeadlines) throws Exception {
        Iterator dls = getActivityDefinition(shandle).getDeadlines()
            .toElements()
            .iterator();

        int i = 0;
        while (dls.hasNext()) {
            DeadlinePersistenceObject dpo = (DeadlinePersistenceObject) pDeadlines.get(i);
            Deadline dl = (Deadline) dls.next();
            
            if (dpo.getTimeLimit() <= timeLimitBoundary) {
                String dc = dl.getDeadlineCondition();
                String en = dl.getExceptionName();
                
                Map context = null;
                String useProcessContextStr = SharkEngineManager.getInstance()
                        .getCallbackUtilities()
                        .getProperty("Deadlines.useProcessContext", "false");

                if (Boolean.valueOf(useProcessContextStr).booleanValue()) {
                    context = this.process.process_context(shandle);
                } else {
                    context = process_context(shandle);
                }

                context.put("PROCESS_STARTED_TIME",
                        new Date(this.process.getStartTime(shandle)));
                context.put("ACTIVITY_ACCEPTED_TIME",
                        new Date(this.acceptedTime));
                context.put("ACTIVITY_ACTIVATED_TIME",
                        new Date(this.activatedTime));
                long timeLimit = ((Date) evaluator(shandle).evaluateExpression(shandle,
                        this.processId,
                        this.key,
                        dc,
                        context,
                        Date.class)).getTime();
                
                if (timeLimit <= timeLimitBoundary) {
                    dpo.setTimeLimit(timeLimit);
                    brokenDeadlines.add(dpo);
                }
            }
            i++;
        }
    }
    
    public void migration(WMSessionHandle shandle, Set<String> missingVariables) {
        try {
            //workflow variable
            if (missingVariables != null && !missingVariables.isEmpty()) {
                PersistentManagerInterface pmgr = SharkEngineManager.getInstance().getInstancePersistenceManager();
                for (String id : missingVariables) {
                    ActivityVariablePersistenceObject var = new ActivityVariablePersistenceObject();
                    var.setProcessId(this.processId);
                    var.setActivityId(this.key);
                    var.setDefinitionId(id);
                    var.setValue("");
                    var.setResultVariable(false);
                    pmgr.persist(shandle, var, true);
                }
            }
            
            //deadlines
            DeadlineDao dao = (DeadlineDao) WorkflowUtil.getApplicationContext().getBean("deadlineDao");
            dao.deleteForActivity(this.key);
            this.justCreatedDeadlines = true;
            reevaluateDeadlines(shandle);
            persistDeadlines(shandle);
        } catch (Exception e) {
            LogUtil.error(CustomWfActivityImpl.class.getName(), e, "");
        }
    }
    
    /**
     * Check and restart the tool activity
     * @param shandle
     * @throws Exception 
     */
    public void restartToolActivity(WMSessionHandle shandle) throws Exception {
        Activity act = getActivityDefinition(shandle);
        if (act != null) {
            int type = act.getActivityType();
            if (type == XPDLConstants.ACTIVITY_TYPE_TOOL) {
                startActivity(shandle);
            }
        }
    }
    
    @Override
    protected void createAssignments(WMSessionHandle shandle, List users) throws Exception {
        if ((users == null) || (users.size() == 0)) {
            return;
        }

        int lrLimit = 5;
        try {
            lrLimit = Integer.parseInt(SharkEngineManager.getInstance()
                    .getCallbackUtilities()
                    .getProperty("SharkKernel.LimitForRetrievingAllResourcesWhenCreatingAssignments",
                            "5"));
        } catch (Exception localException) {
        }
        if (users.size() > lrLimit) {
            SharkEngineManager.getInstance()
                    .getInstancePersistenceManager()
                    .getAllResources(shandle);
        }

        Iterator resourcesIt = users.iterator();
        while (resourcesIt.hasNext()) {
            String username = (String) resourcesIt.next();
            WfResourceInternal wr = SharkUtilities.getResource(shandle, username);
            if (wr == null) {
                wr = SharkEngineManager.getInstance()
                        .getObjectFactory()
                        .createResource(shandle, username);
            }
            WfAssignmentInternal ass = SharkEngineManager.getInstance()
                    .getObjectFactory()
                    .createAssignment(shandle, this, wr);
            
            //add logic to retry once in case it not created
            if (ass == null) {
                //retry
                ass = SharkEngineManager.getInstance()
                    .getObjectFactory()
                    .createAssignment(shandle, this, wr);
            }
            
            if (LogUtil.isDebugEnabled(CustomWfActivityImpl.class.getName())) {
                if (ass != null) {
                    LogUtil.debug(CustomWfActivityImpl.class.getName(), "Assingment of " + ass.activityId(shandle) + " created for " + username);
                } else {
                    LogUtil.debug(CustomWfActivityImpl.class.getName(), "Assingment of " + this.key + " failed to create for " + username);
                }
            }
            wr.addAssignment(shandle, ass);
            getAssignmentResourceIds(shandle).add(username);
        }
    }
}
