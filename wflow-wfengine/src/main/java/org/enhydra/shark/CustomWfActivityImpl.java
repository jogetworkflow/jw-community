package org.enhydra.shark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.instancepersistence.ActivityPersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.ActivityVariablePersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.PersistentManagerInterface;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

public class CustomWfActivityImpl extends WfActivityImpl {
    
    public CustomWfActivityImpl(WMSessionHandle shandle, WfProcessInternal process, String key, String activityDefId, WfActivityInternal blockActivity) throws Exception {
        super(shandle, process, key, activityDefId, blockActivity);
    }
    
    protected CustomWfActivityImpl(ActivityPersistenceObject po, WfProcessInternal proc){
        super(po, proc);
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
}
