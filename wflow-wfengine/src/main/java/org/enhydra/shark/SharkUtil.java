package org.enhydra.shark;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

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
}
