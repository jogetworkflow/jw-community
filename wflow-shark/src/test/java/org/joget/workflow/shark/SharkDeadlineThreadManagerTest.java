package org.joget.workflow.shark;

import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.workflow.model.service.WorkflowManager;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class SharkDeadlineThreadManagerTest {

    /**
     * Fresh install before /setup has no datasource profile. workflowManager is created eagerly
     * at startup, so init must not touch the workflow engine then: Shark would initialize without
     * a database and stay broken after the setup wizard rebuilds the application context.
     */
    @Test
    public void testInitSkipsWorkflowEngineWhenNoProfileConfigured() {
        WorkflowManager workflowManager = mock(WorkflowManager.class);

        try (MockedStatic<HostManager> host = mockStatic(HostManager.class);
                MockedStatic<DynamicDataSourceManager> ds = mockStatic(DynamicDataSourceManager.class)) {
            host.when(HostManager::isVirtualHostEnabled).thenReturn(false);
            ds.when(DynamicDataSourceManager::getCurrentProfile).thenReturn(null);

            new SharkDeadlineThreadManager().init(workflowManager);
        }

        verifyNoInteractions(workflowManager);
    }

    @Test
    public void testInitStartsDeadlineCheckerWhenProfileConfigured() {
        WorkflowManager workflowManager = mock(WorkflowManager.class);

        try (MockedStatic<HostManager> host = mockStatic(HostManager.class);
                MockedStatic<DynamicDataSourceManager> ds = mockStatic(DynamicDataSourceManager.class)) {
            host.when(HostManager::isVirtualHostEnabled).thenReturn(false);
            ds.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("default");

            new SharkDeadlineThreadManager().init(workflowManager);
        }

        verify(workflowManager).internalRecoverStuckToolActivities();
        verify(workflowManager).internalUpdateDeadlineChecker();
    }
}
