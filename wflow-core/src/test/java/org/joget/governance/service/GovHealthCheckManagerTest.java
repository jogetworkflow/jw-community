package org.joget.governance.service;

import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.SetupManager;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GovHealthCheckManagerTest {

    /**
     * Fresh install before /setup has no datasource profile. The bean is created eagerly at
     * startup, so its constructor must not read settings then: SetupManager would look up the
     * setup cache with a null profile and throw, failing the whole /jw context.
     */
    @Test
    public void testConstructorSkipsCheckerWhenNoProfileConfigured() {
        SetupManager setupManager = mock(SetupManager.class);
        when(setupManager.getSettingValue(anyString())).thenThrow(new NullPointerException());

        try (MockedStatic<HostManager> host = mockStatic(HostManager.class);
                MockedStatic<DynamicDataSourceManager> ds = mockStatic(DynamicDataSourceManager.class)) {
            host.when(HostManager::isVirtualHostEnabled).thenReturn(false);
            ds.when(DynamicDataSourceManager::getCurrentProfile).thenReturn(null);

            new GovHealthCheckManager(new ThreadPoolTaskScheduler(), setupManager);
        }

        verify(setupManager, never()).getSettingValue(GovHealthCheckManager.SETTING);
    }

    @Test
    public void testConstructorReadsIntervalWhenProfileConfigured() {
        SetupManager setupManager = mock(SetupManager.class);

        try (MockedStatic<HostManager> host = mockStatic(HostManager.class);
                MockedStatic<DynamicDataSourceManager> ds = mockStatic(DynamicDataSourceManager.class)) {
            host.when(HostManager::isVirtualHostEnabled).thenReturn(false);
            ds.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("default");

            new GovHealthCheckManager(new ThreadPoolTaskScheduler(), setupManager);
        }

        verify(setupManager).getSettingValue(GovHealthCheckManager.SETTING);
    }
}
