package org.enhydra.shark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.instancepersistence.PersistentManagerInterface;
import org.enhydra.shark.api.internal.instancepersistence.ProcessPersistenceObject;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessMgrInternal;
import org.enhydra.shark.api.internal.working.WfRequesterInternal;
import org.enhydra.shark.xpdl.elements.Activity;

public class CustomWfProcessImpl extends WfProcessImpl {
    
    private final static Map<String, Integer> CUSTOM_ACTIVITY_TO_FOLLOWED_TRANSITIONS = new HashMap<String, Integer>();
    private final static Map<String, Set<String>> CUSTOM_COMPLETED_ACTIVE_ACTIVITIES = new HashMap<String, Set<String>>();
    private final static Map<String, Integer> CUSTOM_RUNNING = new HashMap<String, Integer>();

    protected CustomWfProcessImpl(WMSessionHandle shandle, WfProcessMgrInternal manager, WfRequesterInternal requester, String key) throws Exception {
        super(shandle, manager, requester, key);
    }

    protected CustomWfProcessImpl(ProcessPersistenceObject po) {
        super(po);
    }

    @Override
    protected void joinTransition(WMSessionHandle shandle, WfActivityInternal fromActivity, Activity toActivityDef)
            throws Exception, ToolAgentGeneralException {
        synchronizeProcess(shandle);
        removeActiveActivity(fromActivity.activity_definition_id(shandle));

        List toTrans = toActivityDef.getIncomingTransitions();

        AndJoinHelperStruct ajhs = new AndJoinHelperStruct(fromActivity.block_activity_id(shandle), toActivityDef);

        int followed = customRestoreActivityToFollowedTransitionsMap(shandle, ajhs);
        SharkEngineManager.getInstance().getCallbackUtilities().info(shandle, "Process" + toString() + " - " + (followed + 1) + " of " + toTrans.size() + " transitions followed to activity with definition " + toActivityDef.getId() + (fromActivity.block_activity_id(shandle) == null ? "" : new StringBuffer().append(" inside block instance ").append(fromActivity.block_activity_id(shandle)).toString()));
        
        if (toTrans.size() == followed + 1) {
            SharkEngineManager.getInstance().getCallbackUtilities().info(shandle, "Process" + toString() + " - All transition have been followed to activity with definition " + toActivityDef.getId() + (fromActivity.block_activity_id(shandle) == null ? "" : new StringBuffer().append(" inside block instance ").append(fromActivity.block_activity_id(shandle)).toString()));

            Set currentTrans = (Set) this.newActivityToFollowedTransitions.get(ajhs);
            if ((currentTrans != null) && (currentTrans.size() == followed)) {
                this.newActivityToFollowedTransitions.remove(ajhs);
            } else if (currentTrans != null) {
                currentTrans.clear();
            } else {
                this.newActivityToFollowedTransitions.put(ajhs, currentTrans);
            }

            CUSTOM_ACTIVITY_TO_FOLLOWED_TRANSITIONS.remove(this.key + ":" + ajhs.getActivityDef().getId());

            persistActivityToFollowedTransitions(shandle);

            startActivity(shandle, toActivityDef, getActiveActivity(shandle, fromActivity.block_activity_id(shandle)));
            
            this.activityCache.clear(); //Customise: clear cache so that the remaining open activities is update
        } else {
            CUSTOM_ACTIVITY_TO_FOLLOWED_TRANSITIONS.put(this.key + ":" + ajhs.getActivityDef().getId(), new Integer(followed + 1));
            Set currentTrans = (Set) this.newActivityToFollowedTransitions.get(ajhs);
            if (currentTrans == null) {
                currentTrans = new HashSet();
                this.newActivityToFollowedTransitions.put(ajhs, currentTrans);
            }
            currentTrans.add(fromActivity.key(shandle));

            persistActivityToFollowedTransitions(shandle);
        }
    }
    
    protected int customRestoreActivityToFollowedTransitionsMap(WMSessionHandle shandle, AndJoinHelperStruct ajhs)
            throws Exception {
        Integer followed = CUSTOM_ACTIVITY_TO_FOLLOWED_TRANSITIONS.get(this.key + ":" + ajhs.getActivityDef().getId());
        
        if (followed == null) {
            PersistentManagerInterface pmi = SharkEngineManager.getInstance().getInstancePersistenceManager();

            int noOfFollowed = 0;

            noOfFollowed = pmi.howManyAndJoinEntries(shandle, this.key, ajhs.getBlockActId(), ajhs.getActivityDef().getId());

            followed = noOfFollowed;
            CUSTOM_ACTIVITY_TO_FOLLOWED_TRANSITIONS.put(this.key + ":" + ajhs.getActivityDef().getId(), followed);
        }

        return followed;
    }
    
    @Override
    public List getActiveActivities(WMSessionHandle shandle) throws Exception {
        List list = this.activityCache.getOpen(shandle);
        
        if (list != null && list.size() > 0) {
            Set<String> removedActiveIds = CUSTOM_COMPLETED_ACTIVE_ACTIVITIES.get(this.key);
            if (removedActiveIds == null) {
                removedActiveIds = new HashSet<String>();
                CUSTOM_COMPLETED_ACTIVE_ACTIVITIES.put(this.key, removedActiveIds);
            } else if (!removedActiveIds.isEmpty()){
                List remove = new ArrayList();
                for (Object o : list) {
                    WfActivityInternal act = (WfActivityInternal) o;
                    if (removedActiveIds.contains(act.activity_definition_id(shandle))) {
                        remove.add(o);
                    }
                }
                if (!remove.isEmpty()) {
                    list.removeAll(remove);
                }
            }
        }
        
        return list;
    }
    
    @Override
    protected void run(WMSessionHandle shandle, WfActivityInternal lastFinishedActivity) throws Exception, ToolAgentGeneralException {
        try {
            Integer runCount = CUSTOM_RUNNING.get(this.key);
            if (runCount == null) {
                runCount = 1;
            } else {
                runCount++;
            }
            CUSTOM_RUNNING.put(this.key, runCount);
            
            super.run(shandle, lastFinishedActivity);
        } finally {
            Integer runCount = CUSTOM_RUNNING.get(this.key);
            runCount--;
            
            if (runCount == 0) {
                CUSTOM_RUNNING.remove(this.key);
                CUSTOM_COMPLETED_ACTIVE_ACTIVITIES.remove(this.key);
            } else {
                CUSTOM_RUNNING.put(this.key, runCount);
            }
        }
    }
    
    protected void removeActiveActivity(String actDefId) {
        Set<String> removedActiveIds = CUSTOM_COMPLETED_ACTIVE_ACTIVITIES.get(this.key);
        if (removedActiveIds == null) {
            removedActiveIds = new HashSet<String>();
            CUSTOM_COMPLETED_ACTIVE_ACTIVITIES.put(this.key, removedActiveIds);
        }
        removedActiveIds.add(actDefId);
    }
    
    @Override
    protected void queueNext(WMSessionHandle shandle, WfActivityInternal fromActivity)
            throws Exception, ToolAgentGeneralException {
        removeActiveActivity(fromActivity.activity_definition_id(shandle));
        super.queueNext(shandle, fromActivity);
    }
}
