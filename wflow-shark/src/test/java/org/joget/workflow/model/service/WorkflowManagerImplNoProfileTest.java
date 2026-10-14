package org.joget.workflow.model.service;

import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.workflow.shark.JSPClientUtilities;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

public class WorkflowManagerImplNoProfileTest {

    /**
     * Fresh install before /setup has no datasource profile. workflowManager is created eagerly
     * at startup, so its constructor must not initialize Shark then: Shark initialized without a
     * database stays broken (null repository persistence manager) after the setup wizard rebuilds
     * the application context, and every process upload fails.
     */
    @Test
    public void testConstructorSkipsSharkInitWhenNoProfileConfigured() {
        // static flag shared with other tests in this JVM
        boolean wasInitialized = WorkflowManagerImpl.initialized;
        WorkflowManagerImpl.initialized = false;
        try (MockedStatic<HostManager> host = mockStatic(HostManager.class);
                MockedStatic<DynamicDataSourceManager> ds = mockStatic(DynamicDataSourceManager.class);
                MockedStatic<JSPClientUtilities> shark = mockStatic(JSPClientUtilities.class)) {
            host.when(HostManager::isVirtualHostEnabled).thenReturn(false);
            ds.when(DynamicDataSourceManager::getCurrentProfile).thenReturn(null);

            new WorkflowManagerImpl();

            shark.verify(() -> JSPClientUtilities.initProperties(anyString()), never());
            shark.verify(JSPClientUtilities::init, never());
            assertFalse(WorkflowManagerImpl.initialized);
        } finally {
            WorkflowManagerImpl.initialized = wasInitialized;
        }
    }
}
