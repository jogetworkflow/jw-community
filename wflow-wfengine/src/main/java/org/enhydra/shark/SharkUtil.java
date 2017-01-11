package org.enhydra.shark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLInterface;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class SharkUtil {
    
    public static WorkflowProcess getWorkflowProcess(WMSessionHandle shandle, String processDefId) {
        try {
            String arg[] = processDefId.split("#");
            return SharkUtilities.getWorkflowProcess(shandle, arg[0], arg[1], arg[2]);
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
            for (Iterator i=transitions.iterator(); i.hasNext();) {
                Transition transition = (Transition)i.next();
                Activity nextActivity = transition.getToActivity();
                String activityType;
                switch(nextActivity.getActivityType()) {
                    case XPDLConstants.ACTIVITY_TYPE_ROUTE: 
                        // it's a route, ignore and proceed to the next activity
                        activityType = "Route";
                        getNextActivities(processDefId, nextActivity.getId(), processId, activityId, includeTools, nextActivities);
                        break;
                    case XPDLConstants.ACTIVITY_TYPE_TOOL: 
                        // it's a tool, optionally include in results and proceed to the next activity
                        activityType = "Tool";
                        if (includeTools) {
                            addActivity(nextActivity, activityType, processEntity.getPkgId(), processEntity.getId(), processEntity.getPkgVer(), processId, activityId, nextActivities);
                        }
                        getNextActivities(processDefId, nextActivity.getId(), processId, activityId, includeTools, nextActivities);
                        break;
                    case XPDLConstants.ACTIVITY_TYPE_SUBFLOW: 
                        // it's a subflow, get the subflow process and proceed to the starting activities
                        activityType = "Subflow";
                        WorkflowProcess subflow = XMLUtil.getSubflowProcess(xmlInterface, nextActivity);
                        org.enhydra.shark.xpdl.elements.Package pkg = (org.enhydra.shark.xpdl.elements.Package)subflow.getParent().getParent();
                        String subflowProcessDefId = pkg.getId() + "#" + pkg.getInternalVersion() + "#" + subflow.getId();
                        ArrayList subflowActivities = subflow.getStartingActivities();
                        for (Iterator j=subflowActivities.iterator(); j.hasNext();) {
                            Activity subflowActivity = (Activity)j.next();
                            getNextActivities(subflowProcessDefId, subflowActivity.getId(), processId, activityId, includeTools, nextActivities);
                        }
                        break;
                    default: 
                        // it's a normal activity, include in results
                        activityType = "Activity";
                        addActivity(nextActivity, activityType, processEntity.getPkgId(), processEntity.getId(), processEntity.getPkgVer(), processId, activityId, nextActivities);
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
        List<String> assignmentUsers = WorkflowUtil.getAssignmentUsers(packageId, processDefId, processId, processVersion, activityId, "", activityToAdd.getPerformer());
        if (assignmentUsers != null) {
            activity.setAssignmentUsers(assignmentUsers.toArray(new String[0]));
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
}
