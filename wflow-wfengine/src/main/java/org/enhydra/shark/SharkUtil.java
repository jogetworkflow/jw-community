package org.enhydra.shark;

import com.lutris.appserver.server.sql.CachedDBTransaction;
import com.lutris.appserver.server.sql.DBTransaction;
import com.lutris.dods.builder.generator.query.DataObjectException;
import com.lutris.util.ConfigException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.enhydra.dods.DODS;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfProcess;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.instancepersistence.data.ActivityDO;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLInterface;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class SharkUtil {
    
    public static Map<String, String> getNonExceptionalOutgoingTransitions(WMSessionHandle shandle, String processDefId, String actDefId) {
        Map<String, String> list = new HashMap<String, String>();
        
        try {
            WorkflowProcess process = getWorkflowProcess(shandle, processDefId);
            Activity activity = process.getActivity(actDefId);
            
            Collection<Transition> transitions = activity.getNonExceptionalOutgoingTransitions();
            for (Transition t : transitions) {
                if (t.getName() != null && !t.getName().isEmpty()) {
                    list.put(t.getId(), t.getName());
                } else {
                    Activity toActivity = process.getActivity(t.getTo());
                    list.put(t.getId(), t.getId() + " ("+toActivity.getName()+")");
                }
            }
            
        } catch (Exception e) {}
        
        return list;
    }
    
    public static WorkflowProcess getWorkflowProcess(WMSessionHandle shandle, String processDefId) {
        try {
            String arg[] = processDefId.split("#");
            return SharkUtilities.getWorkflowProcess(shandle, arg[0], arg[1], arg[2]);
        } catch (Exception e) {}
        
        return null;
    }
    
    public static CustomWfProcessImpl getProcess(WMSessionHandle shandle, String processId) {
        try {
            return (CustomWfProcessImpl) SharkUtilities.getProcess(shandle, processId, 0);
        } catch (Exception e) {}
        
        return null;
    }
    
    public static WMEntity createBasicEntity(XMLComplexElement el) throws Exception {
        return SharkUtilities.createBasicEntity(el);
    }
    
    /**
     * Get the next potential activities based on the current activity
     * @param processDefId
     * @param activityDefId
     * @param processId
     * @param activityId
     * @param includeTools Set to true to also include Tool elements in the results
     * @param activities Optional Collection to add results into, used for recursion
     * @return 
     */
    public static Collection<WorkflowActivity> getNextActivities(String processDefId, String activityDefId, String processId, String activityId, boolean includeTools, Collection<WorkflowActivity> activities) {
        SharkConnection sc = null;
        Collection<WorkflowActivity> nextActivities = (activities != null) ? activities : new ArrayList<WorkflowActivity>();

        try {
            sc = connect();
            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WMEntity processEntity = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);                        
            XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();
            // get current activity
            Activity currentActivity = SharkUtilities.getActivityDefinition(sessionHandle, processEntity.getPkgId(), processEntity.getPkgVer(), processEntity.getId(), activityDefId);
            // get and loop through non-exception outgoing transitions
            List transitions = currentActivity.getNonExceptionalOutgoingTransitions();
            if (!transitions.isEmpty()) {
                // handle normal outgoing transitions
                for (Iterator i=transitions.iterator(); i.hasNext();) {
                    Transition transition = (Transition)i.next();
                    Activity nextActivity = transition.getToActivity();
                    evaluateActivity(xmlInterface, processEntity, nextActivity, processDefId, processId, activityId, includeTools, nextActivities);
                }                         
            } else {
                // handle subflow exit
                WfActivityWrapper wfActivity = null;
                Object wfRequester = sc.getProcess(processId).requester();
                if (wfRequester instanceof WfActivityWrapper) {
                    wfActivity = (WfActivityWrapper)wfRequester;
                }
                if (wfActivity != null) {
                    WfProcess requesterProcess = wfActivity.container();
                    WMEntity activityEntity = admin.getActivityDefinitionInfo(sessionHandle, requesterProcess.key(), wfActivity.key());
                    String requesterActivityDefId = activityEntity.getId();
                    String requesterProcessId = requesterProcess.key();
                    String requesterProcessDefId = requesterProcess.manager().name();
                    String requesterProcessDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(requesterProcessDefId);
                    Activity requesterActivity = SharkUtilities.getActivityDefinition(sessionHandle, processEntity.getPkgId(), processEntity.getPkgVer(), requesterProcessDefIdWithoutVersion, requesterActivityDefId);
                    getNextActivities(requesterProcessDefId, requesterActivity.getId(), requesterProcessId, requesterActivityDefId, includeTools, nextActivities);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(SharkUtil.class.getName(), ex, "Error getting next activities");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(SharkUtil.class.getName(), e, "Error closing Shark connection");
            }
        }
        return nextActivities;
    }    

    /**
     * Evaluates an activity to determine the cause of action
     * @param xmlInterface 
     * @param processEntity
     * @param activity
     * @param processDefId
     * @param processId
     * @param activityId
     * @param includeTools
     * @param nextActivities
     */
    static void evaluateActivity(XMLInterface xmlInterface, WMEntity processEntity, Activity activity, String processDefId, String processId, String activityId, boolean includeTools, Collection<WorkflowActivity> nextActivities) {
        String activityType;
        switch(activity.getActivityType()) {
            case XPDLConstants.ACTIVITY_TYPE_ROUTE:
                // it's a route, ignore and proceed to the next activity
                activityType = "Route";
                getNextActivities(processDefId, activity.getId(), processId, activityId, includeTools, nextActivities);
                break;
            case XPDLConstants.ACTIVITY_TYPE_TOOL:
                // it's a tool, optionally include in results and proceed to the next activity
                activityType = "Tool";
                if (includeTools) {
                    addActivity(activity, activityType, processEntity.getPkgId(), processEntity.getId(), processEntity.getPkgVer(), processId, activityId, nextActivities);
                }
                getNextActivities(processDefId, activity.getId(), processId, activityId, includeTools, nextActivities);
                break;
            case XPDLConstants.ACTIVITY_TYPE_SUBFLOW:
                // it's a subflow, get the subflow process and proceed to the starting activities
                activityType = "Subflow";
                WorkflowProcess subflow = XMLUtil.getSubflowProcess(xmlInterface, activity);
                ArrayList subflowActivities = subflow.getStartingActivities();
                for (Iterator j=subflowActivities.iterator(); j.hasNext();) {
                    Activity subflowActivity = (Activity)j.next();
                    evaluateActivity(xmlInterface, processEntity, subflowActivity, processDefId, processId, activityId, includeTools, nextActivities);
                }
                if (!activity.isSubflowSynchronous()) {
                    // asynchronous subflow, directly proceed to the next activity
                    getNextActivities(processDefId, activity.getId(), processId, activityId, includeTools, nextActivities);
                }
                break;
            default:
                // it's a normal activity, include in results
                activityType = "Activity";
                addActivity(activity, activityType, processEntity.getPkgId(), processEntity.getId(), processEntity.getPkgVer(), processId, activityId, nextActivities);
        }
    }

    /**
     * Converts an Activity into a WorkflowActivity and adds into the Collection of results.
     * @param activityToAdd
     * @param activityType
     * @param packageId
     * @param processDefId
     * @param processVersion
     * @param processId
     * @param activityId
     * @param activities 
     */
    static void addActivity(Activity activityToAdd, String activityType, String packageId, String processDefId, String processVersion, String processId, String activityId, Collection<WorkflowActivity> activities) {
        WorkflowActivity activity = new WorkflowActivity();
        activity.setActivityDefId(activityToAdd.getId());
        activity.setName(activityToAdd.getName());
        activity.setType(activityType);
        activity.setPerformer(activityToAdd.getPerformer());
        activity.setProcessDefId(processDefId);
        activity.setProcessVersion(processVersion);
        activity.setProcessName(((WorkflowProcess)activityToAdd.getParent().getParent()).getName());
        List<String> assignmentUsers = WorkflowUtil.getAssignmentUsers(packageId, processDefId, processId, processVersion, activityId, "", activityToAdd.getPerformer());
        if (assignmentUsers != null) {
            activity.setAssignmentUsers(assignmentUsers.toArray(new String[0]));
        }
        // check for hash variable
        if (WorkflowUtil.containsHashVariable(activity.getName()) || WorkflowUtil.containsHashVariable(activity.getProcessName())) {
            WorkflowAssignment ass = new WorkflowAssignment();
            ass.setProcessId(processId);
            ass.setProcessDefId(processDefId);
            ass.setProcessName(activity.getProcessName());
            ass.setProcessVersion(processVersion);
            ass.setActivityId(activityId);
            ass.setActivityName(activity.getName());
            ass.setActivityDefId(activity.getActivityDefId());
            ass.setAssigneeId(activity.getPerformer());
            activity.setName(WorkflowUtil.processVariable(activity.getName(), null, ass));
            activity.setProcessName(WorkflowUtil.processVariable(activity.getProcessName(), null, ass));
        }
        activities.add(activity);
    }
    
    /*--- Internal methods to Shark ---*/
    /**
     * Connect to the Shark engine using the current username.
     * @return
     * @throws Exception
     */
    static SharkConnection connect() throws Exception {
        return connect(null);
    }

    /**
     * Connect to the Shark.
     * @param username
     * @return
     * @throws Exception
     */
    static SharkConnection connect(String username) throws Exception {
        SharkConnection sConn = Shark.getInstance().getSharkConnection();
        if (username == null) {
            WorkflowUserManager workflowUserManager = (WorkflowUserManager)WorkflowUtil.getApplicationContext().getBean("workflowUserManager");
            username = workflowUserManager.getCurrentUsername();
        }
        WMConnectInfo wmconnInfo = new WMConnectInfo(username, username, "WorkflowManager", "");
        sConn.connect(wmconnInfo);
        return sConn;
    }

    /**
     * Disconnect from the Shark engine. Must be called in a finally block.
     * @param sConn
     * @throws Exception
     */
    static void disconnect(SharkConnection sConn) throws Exception {
        if (sConn != null) {
            sConn.disconnect();
        }
    } 
    
    /**
     * write transaction immediately instead of write before next query execute, so that any exception can be handled directly
     * @param shandle
     * @throws Exception 
     */
    public static void transactionWrite(WMSessionHandle shandle) throws Exception {
        String dbName = ActivityDO.get_logicalDBName();
        try {
            if (DODS.getDatabaseManager().getConfig().getBoolean("DB." + dbName + ".JTA", DODS.getDatabaseManager().getConfig().getBoolean("defaults.JTA", false))){
                DBTransaction transaction = DODS.getDatabaseManager().createTransaction(dbName);
                
                if ((transaction != null) && ((transaction instanceof CachedDBTransaction))) {
                    if (((CachedDBTransaction)transaction).getAutoWrite()) {
                        try {
                            transaction.write();
                        } catch (SQLException sqle) {
                            throw new DataObjectException("Couldn't write transaction: " + sqle);
                        }
                        ((CachedDBTransaction) transaction).dontAggregateDOModifications();
                    }
                }
            }
        } catch (ConfigException e) {
            //ignore
        }
    }
}
