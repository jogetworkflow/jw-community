package org.joget.workflow.shark;

import java.util.ArrayList;
import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.eventaudit.AssignmentEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.CreateProcessEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.DataEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditManagerInterface;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

public class WorkflowEventAuditManager implements EventAuditManagerInterface {

    public void configure(CallbackUtilities arg0) throws Exception {
        // do nothing
    }

    public void persist(WMSessionHandle arg0, AssignmentEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void persist(WMSessionHandle arg0, CreateProcessEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void persist(WMSessionHandle arg0, DataEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void persist(WMSessionHandle arg0, StateEventAuditPersistenceObject arg1) throws EventAuditException {
        if ("processStateChanged".equalsIgnoreCase(arg1.getType()) && !arg1.getNewState().equals(arg1.getOldState()) && arg1.getNewState().startsWith("closed") && !arg1.getNewState().equals("closed.aborted")) {
            final String processId = arg1.getProcessId();
            
            //run in background to prevent the deadline aborted subflow is deleted before it completed by Shark
            Thread backgroudThread = new PluginThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200); // add a delay to wait for process completion
                        ((WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager")).internalRemoveProcessOnComplete(processId);
                    } catch (Exception e) {
                        LogUtil.error(WorkflowEventAuditManager.class.getName(), e, "Fail to save process history");
                    }
                }
            });
            backgroudThread.setDaemon(true);
            backgroudThread.start();
        }
    }

    public boolean restore(WMSessionHandle arg0, AssignmentEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
        return false;
    }

    public boolean restore(WMSessionHandle arg0, CreateProcessEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
        return false;
    }

    public boolean restore(WMSessionHandle arg0, DataEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
        return false;
    }

    public boolean restore(WMSessionHandle arg0, StateEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
        return false;
    }

    public List restoreProcessHistory(WMSessionHandle arg0, String arg1) throws EventAuditException {
        // do nothing
        return new ArrayList();
    }

    public List restoreActivityHistory(WMSessionHandle arg0, String arg1, String arg2) throws EventAuditException {
        // do nothing
        return new ArrayList();
    }

    public void delete(WMSessionHandle arg0, AssignmentEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void delete(WMSessionHandle arg0, CreateProcessEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void delete(WMSessionHandle arg0, DataEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }

    public void delete(WMSessionHandle arg0, StateEventAuditPersistenceObject arg1) throws EventAuditException {
        // do nothing
    }
}
