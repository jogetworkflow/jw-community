package org.enhydra.shark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.instancepersistence.PersistentManagerInterface;
import org.enhydra.shark.api.internal.instancepersistence.ProcessPersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.ProcessVariablePersistenceObject;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessMgrInternal;
import org.enhydra.shark.api.internal.working.WfRequesterInternal;
import org.enhydra.shark.utilities.MiscUtilities;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Condition;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.DecisionResult;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.shark.model.CustomActivityPersistenceObject;
import org.joget.workflow.shark.model.CustomDeadlinePersistenceObject;
import org.joget.workflow.shark.model.dao.DeadlineDao;
import org.joget.workflow.util.WorkflowUtil;

public class CustomWfProcessImpl extends WfProcessImpl {

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

        List toTrans = toActivityDef.getIncomingTransitions();

        AndJoinHelperStruct ajhs = new AndJoinHelperStruct(fromActivity.block_activity_id(shandle), toActivityDef);

        int followed = restoreActivityToFollowedTransitionsMap(shandle, ajhs);
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

            this.activityToFollowedTransitions.put(ajhs, new Integer(0));

            persistActivityToFollowedTransitions(shandle);

            startActivity(shandle, toActivityDef, getActiveActivity(shandle, fromActivity.block_activity_id(shandle)));
            
            this.activityCache.clear(); //Customise: clear cache so that the remaining open activities is update
        } else {
            this.activityToFollowedTransitions.put(ajhs, new Integer(followed + 1));
            Set currentTrans = (Set) this.newActivityToFollowedTransitions.get(ajhs);
            if (currentTrans == null) {
                currentTrans = new HashSet();
                this.newActivityToFollowedTransitions.put(ajhs, currentTrans);
            }
            currentTrans.add(fromActivity.key(shandle));

            persistActivityToFollowedTransitions(shandle);
        }
    }
    
    @Override
    public WfActivityInternal[] checkDeadlines(WMSessionHandle shandle) throws Exception {
        WorkflowHelper workflowMapper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        workflowMapper.updateAppDefinitionForDeadline(key, MiscUtilities.getProcessMgrPkgId(managerName), mgrVer);
        
        checkReadOnly();
        if (!state(shandle).equals("open.running")) {
          throw new Exception("Process " + toString() + " - can't check deadlines for the closed process instance");
        }

        List deadlineActs = new ArrayList();

        List<WfActivityInternal> activeActs = getDeadlineActivities();
        long timeLimitBoundary = System.currentTimeMillis();
        Map actToExcNames = new HashMap();
        for (int i = 0; i < activeActs.size(); i++) {
            WfActivityInternal act = (WfActivityInternal) activeActs.get(i);
            if (act.block_activity_id(shandle) == null) {
                Map ataens = new HashMap();
                boolean syncDeadlineHappened = act.checkDeadlines(shandle, timeLimitBoundary, ataens);

                if (syncDeadlineHappened) {
                    deadlineActs.add(act);
                } else if ((ataens != null) && (ataens.size() > 0)) {
                    deadlineActs.add(act);
                    actToExcNames.putAll(ataens);
                }
            }
        }
        if (actToExcNames.size() > 0) {
            handleBrokenAsyncDeadlines(shandle, actToExcNames);
        }

        WfActivityInternal[] daArr = new WfActivityInternal[deadlineActs.size()];
        deadlineActs.toArray(daArr);
        
        return daArr;
    }
    
    protected List<WfActivityInternal> getDeadlineActivities() {
        DeadlineDao dlDao = (DeadlineDao) WorkflowUtil.getApplicationContext().getBean("deadlineDao");
        Map<CustomActivityPersistenceObject, Collection<CustomDeadlinePersistenceObject>> deadlineMap = dlDao.getDeadlines(key, null);
        
        List<WfActivityInternal> acts = new ArrayList<WfActivityInternal>();
        for (CustomActivityPersistenceObject apo : deadlineMap.keySet()) {
            try {
                WfActivityInternal wai = SharkEngineManager.getInstance().getObjectFactory().createActivity(apo.getActivityPersistenceObject(), this);
                if (wai instanceof CustomWfActivityImpl) {
                    ((CustomWfActivityImpl) wai).setDeadlines(deadlineMap.get(apo));
                }
                acts.add(wai);
            } catch (Exception e) {
                LogUtil.error(CustomWfProcessImpl.class.getName(), e, "");
            }
        }
        
        return acts;
    }
    
    @Override
    protected List getTransFrom(WMSessionHandle shandle, WfActivityInternal fromActivity, Activity fromActDef) throws Exception {
        List orderedOutTransitions = fromActDef.getNonExceptionalOutgoingTransitions();
        List transList = new ArrayList();

        DecisionResult result = null;
        Map processContext = process_context(shandle);
        if (fromActDef.getActivityType() == 0) {
            try {
                WorkflowHelper workflowMapper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
                result = workflowMapper.executeDecisionPlugin(fromActivity.manager_name(shandle), fromActivity.process_id(shandle), fromActDef.getId(), fromActivity.key(shandle), processContext);
            } catch (Exception e) {
                LogUtil.error(CustomWfProcessImpl.class.getName(), e, "");
            }
            
            if (result != null && !result.getVariables().isEmpty()) {
                processContext.putAll(result.getVariables());
            }
        }
        
        boolean isAndSplit = fromActDef.isAndTypeSplit();
        
        if (result != null && result.getIsAndSplit() != null) {
            isAndSplit = result.getIsAndSplit();
        }
        
        Transition otherwiseTransition = null;

        Iterator transitions = orderedOutTransitions.iterator();

        while (transitions.hasNext()) {
            Transition trans = (Transition) transitions.next();
            Condition condition = trans.getCondition();
            String condType = condition.getType();
            if (condType.equals("OTHERWISE")) {
                otherwiseTransition = trans;
                boolean handleOtherwiseTransitionLast = new Boolean(SharkEngineManager.getInstance().getCallbackUtilities().getProperty("SharkKernel.handleOtherwiseTransitionLast", "false")).booleanValue();

                if ((!isAndSplit) && (!handleOtherwiseTransitionLast)) {
                    break;
                }
            } else {
                boolean evalRes = false;
                
                //check overiden by decision result
                if (result != null && result.getTransitions() != null) {
                    if (result.getTransitions().contains(trans.getId()) || result.getTransitions().contains(trans.getName())) {
                        evalRes = true;
                    }
                } else {
                    String cond = condition.toValue();
                    if (cond.trim().length() == 0) {
                        evalRes = true;
                    } else {
                        evalRes = evaluator(shandle).evaluateCondition(shandle, this.key, null, cond, processContext);
                    }
                }

                if (evalRes) {
                    transList.add(trans);
                    if (!isAndSplit) {
                        break;
                    }
                }
            }

        }

        if ((transList.size() == 0) && (otherwiseTransition != null)) {
            transList.add(otherwiseTransition);
            SharkEngineManager.getInstance().getCallbackUtilities().info(shandle, "Process" + toString() + " - process is proceeding with otherwise transition of Activity" + fromActivity);
        }

        return transList;
    }
    
    public Set<String> migration(WMSessionHandle shandle) {
        Set<String> missingVariables = new HashSet<String>();
        try {
            Map processVariables = getContext(shandle);
            WorkflowProcess wp = getProcessDefinition(shandle);
            Collection dfsAndFPs = wp.getAllVariables().values();
            Iterator itDfs = dfsAndFPs.iterator();
            
            while (itDfs.hasNext()) {
                XMLCollectionElement dfOrFp = (XMLCollectionElement)itDfs.next();
                String id = dfOrFp.getId();
                
                if (!processVariables.containsKey(id)) {
                    missingVariables.add(id);
                }
            }
            
            if (!missingVariables.isEmpty()) {
                PersistentManagerInterface pmgr = SharkEngineManager.getInstance().getInstancePersistenceManager();
                for (String id : missingVariables) {
                    ProcessVariablePersistenceObject var = new ProcessVariablePersistenceObject();
                    var.setProcessId(this.key);
                    var.setDefinitionId(id);
                    var.setValue("");
                    
                    pmgr.persist(shandle, var, true);
                }
            }
        } catch (Exception e) {
            LogUtil.error(CustomWfProcessImpl.class.getName(), e, "");
        }
        return missingVariables;
    }
}
