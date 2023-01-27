package org.enhydra.shark;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.instancepersistence.ActivityPersistenceObject;
import org.enhydra.shark.api.internal.instancepersistence.ProcessPersistenceObject;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.api.internal.working.WfProcessMgrInternal;
import org.enhydra.shark.api.internal.working.WfRequesterInternal;

public class CustomSharkObjectFactory extends SharkObjectFactory {

    @Override
    public WfProcessInternal createProcess(WMSessionHandle shandle, WfProcessMgrInternal manager, WfRequesterInternal requester, String key)
            throws Exception {
        return new CustomWfProcessImpl(shandle, manager, requester, key);
    }

    @Override
    public WfProcessInternal createProcess(ProcessPersistenceObject po) throws Exception {
        return new CustomWfProcessImpl(po);
    }

    @Override
    public WfActivityInternal createActivity(WMSessionHandle shandle, WfProcessInternal process, String key, String activityDefId, WfActivityInternal blockActivity) throws Exception {
        return new CustomWfActivityImpl(shandle, process, key, activityDefId, blockActivity);
    }
    
    @Override
    public WfActivityInternal createActivity(ActivityPersistenceObject po, WfProcessInternal process) throws Exception {
        return new CustomWfActivityImpl(po, process);
    }
}
