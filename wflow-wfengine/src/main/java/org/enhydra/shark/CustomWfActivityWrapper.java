package org.enhydra.shark;

import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.WfActivityInternal;

public class CustomWfActivityWrapper {
    public static List<String> getAssignmentResourceIds(WMSessionHandle shandle,
                               String mgrName,
                               String processId,
                               String id) throws Exception{
        WfActivityWrapper wrapper = new WfActivityWrapper(shandle, mgrName, processId, id);
        WfActivityInternal internal = wrapper.getActivityImpl(processId, id, SharkUtilities.READ_ONLY_MODE);
        return internal.getAssignmentResourceIds(shandle);
    }
}
