package org.joget.workflow.shark;

import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
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
        if (arg1 != null && arg1.getNewState().contains("closed")) { //only need when activity status is closed
            ((WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager")).internalRemoveProcessOnComplete(arg1.getProcessId());
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
