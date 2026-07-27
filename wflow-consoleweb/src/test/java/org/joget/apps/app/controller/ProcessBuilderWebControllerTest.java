package org.joget.apps.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.ui.ModelMap;

import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A process update/migration in progress can leave the Process Builder showing an older committed
 * snapshot. The page may display that snapshot, but the client must block saves and merge the
 * latest committed definition before allowing another save (T8086 / #2196).
 */
public class ProcessBuilderWebControllerTest {

    private ProcessBuilderWebController newController(AppService appService, boolean processUpdateInProgress) {
        return newController(appService, processUpdateInProgress, mock(PackageDefinitionDao.class));
    }

    private ProcessBuilderWebController newController(AppService appService, boolean processUpdateInProgress, PackageDefinitionDao packageDefinitionDao) {
        ProcessBuilderWebController controller = new ProcessBuilderWebController();
        controller.appService = appService;
        controller.packageDefinitionDao = packageDefinitionDao;
        controller.setupManager = mock(SetupManager.class);
        when(controller.setupManager.getSettingValue("systemTheme")).thenReturn("default");

        ConsoleWebPlugin consoleWebPlugin = mock(ConsoleWebPlugin.class);
        when(consoleWebPlugin.verifyAppVersion("t8086", "1")).thenReturn(null);

        PluginManager pluginManager = mock(PluginManager.class);
        when(pluginManager.getPlugin(ConsoleWebPlugin.class.getName())).thenReturn(consoleWebPlugin);
        controller.pluginManager = pluginManager;

        return controller;
    }

    private AppDefinition appDefWithPackage() {
        return appDefWithPackage(3L);
    }

    private AppDefinition appDefWithPackage(long packageVersion) {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId("t8086");
        packageDef.setVersion(packageVersion);

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086");
        appDef.setVersion(1L);
        appDef.setPackageDefinitionList(java.util.Collections.singletonList(packageDef));
        return appDef;
    }

    @Test
    public void testProcessBuilder_migrationInProgress_embedsExistingPackageJson() throws Exception {
        AppService appService = mock(AppService.class);
        AppDefinition appDef = appDefWithPackage();
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDef);
        when(appService.hasProcessUpdate(appDef)).thenReturn(true);

        ProcessBuilderWebController controller = newController(appService, true);
        ModelMap model = new ModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class)) {
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDef)).thenReturn("{\"xpdl\":{\"Package\":{}}}");

            String view = controller.processBuilder(model, request, response, "t8086", "1");

            assertEquals("pbuilder/pbuilder", view);
            assertEquals(Boolean.TRUE, model.get("processMigrationInProgress"));
            assertTrue("The existing committed design remains visible during migration", ((String) model.get("json")).contains("xpdl"));
            appUtil.verify(() -> AppUtil.getXpdlAndMappingJson(appDef));
        }
    }

    /**
     * hasProcessUpdate() alone cannot catch a write whose entire acquire/modify/release cycle
     * happens between the initial check and the recheck. This simulates that: the package version
     * observed before the read differs from a genuinely fresh metadata read taken after, even
     * though hasProcessUpdate reports false at both checks. The fresh read must come from the
     * database (not the Hibernate-managed, possibly stale, AppDefinition), so it is modeled here
     * with the scalar getPackageMetadata query. The snapshot may be displayed, but it must be
     * marked for a client-side merge before saving.
     */
    @Test
    public void testProcessBuilder_packageChangedDuringRead_fallsBackToBlockedState() throws Exception {
        AppService appService = mock(AppService.class);
        PackageDefinitionDao packageDefinitionDao = mock(PackageDefinitionDao.class);
        AppDefinition appDefBefore = appDefWithPackage(3L);
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDefBefore);
        when(appService.hasProcessUpdate(any(AppDefinition.class))).thenReturn(false);
        // A concurrent save committed a newer package version while the JSON was being generated.
        when(packageDefinitionDao.getPackageMetadata("t8086", 1L)).thenReturn(new Object[]{4L, null});

        ProcessBuilderWebController controller = newController(appService, false, packageDefinitionDao);
        ModelMap model = new ModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class)) {
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDefBefore)).thenReturn("{\"xpdl\":{}}");

            String view = controller.processBuilder(model, request, response, "t8086", "1");

            assertEquals("pbuilder/pbuilder", view);
            assertEquals(Boolean.TRUE, model.get("processMigrationInProgress"));
            assertTrue("The loaded snapshot remains visible until the client merges", ((String) model.get("json")).contains("xpdl"));
            assertSame("The originally resolved app definition (same id/version) remains on the model", appDefBefore, model.get("appDefinition"));
        }
    }

    @Test
    public void testProcessBuilder_noMigration_embedsPackageJsonAsUsual() throws Exception {
        AppService appService = mock(AppService.class);
        PackageDefinitionDao packageDefinitionDao = mock(PackageDefinitionDao.class);
        AppDefinition appDef = appDefWithPackage();
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDef);
        when(appService.hasProcessUpdate(appDef)).thenReturn(false);
        // Fresh read matches the metadata the JSON was built from: no concurrent change.
        when(packageDefinitionDao.getPackageMetadata("t8086", 1L)).thenReturn(new Object[]{3L, null});

        ProcessBuilderWebController controller = newController(appService, false, packageDefinitionDao);
        ModelMap model = new ModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class)) {
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDef)).thenReturn("{\"xpdl\":{}}");

            String view = controller.processBuilder(model, request, response, "t8086", "1");

            assertEquals("pbuilder/pbuilder", view);
            assertEquals(Boolean.FALSE, model.get("processMigrationInProgress"));
            assertTrue(((String) model.get("json")).contains("xpdl"));
            appUtil.verify(() -> AppUtil.getXpdlAndMappingJson(appDef));
        }
    }

    /**
     * The JSON GET endpoint only checks the lock before generating the payload. A save can acquire
     * the lock, commit, and release entirely while the JSON is being assembled, so the endpoint
     * must re-validate afterwards against a genuinely fresh package metadata read and reject with
     * 503 if it changed, rather than return stale or internally mixed JSON (T8086 / #2196).
     */
    @Test
    public void testGetJson_packageChangedDuringGeneration_returns503() throws Exception {
        AppService appService = mock(AppService.class);
        PackageDefinitionDao packageDefinitionDao = mock(PackageDefinitionDao.class);
        AppDefinition appDef = appDefWithPackage(3L);
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDef);
        when(appService.hasProcessUpdate(appDef)).thenReturn(false);
        when(packageDefinitionDao.getPackageMetadata("t8086", 1L)).thenReturn(new Object[]{4L, null});

        ProcessBuilderWebController controller = newController(appService, false, packageDefinitionDao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter writer = new StringWriter();

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
                MockedStatic<PropertyUtil> propertyUtil = Mockito.mockStatic(PropertyUtil.class)) {
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDef)).thenReturn("{\"xpdl\":{}}");
            propertyUtil.when(() -> PropertyUtil.propertiesJsonLoadProcessing(any())).thenAnswer(inv -> inv.getArgument(0));

            controller.getJson(writer, request, response, "t8086", "1");

            verify(response).sendError(eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE), any());
            assertEquals("No JSON should be written once a concurrent change is detected", "", writer.toString());
        }
    }

    @Test
    public void testGetJson_noMigration_writesJson() throws Exception {
        AppService appService = mock(AppService.class);
        PackageDefinitionDao packageDefinitionDao = mock(PackageDefinitionDao.class);
        AppDefinition appDef = appDefWithPackage(3L);
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDef);
        when(appService.hasProcessUpdate(appDef)).thenReturn(false);
        when(packageDefinitionDao.getPackageMetadata("t8086", 1L)).thenReturn(new Object[]{3L, null});

        ProcessBuilderWebController controller = newController(appService, false, packageDefinitionDao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter writer = new StringWriter();

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
                MockedStatic<PropertyUtil> propertyUtil = Mockito.mockStatic(PropertyUtil.class)) {
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDef)).thenReturn("{\"xpdl\":{}}");
            propertyUtil.when(() -> PropertyUtil.propertiesJsonLoadProcessing(any())).thenAnswer(inv -> inv.getArgument(0));

            controller.getJson(writer, request, response, "t8086", "1");

            verify(response, never()).sendError(eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE), any());
            assertTrue("The generated JSON should be written when no concurrent change is detected", writer.toString().contains("xpdl"));
        }
    }

    /**
     * save() runs in a transaction that commits (or rolls back) only after this method returns.
     * Releasing the process-update lock in a plain finally block would drop it while the save is
     * still uncommitted, letting a concurrent reader observe no lock in place and read the old,
     * still-current committed package. The release must instead be deferred until the
     * transaction's outcome is final (T8086 / #2196).
     */
    @Test
    public void testSave_deferReleaseLockUntilTransactionCompletes() throws Exception {
        AppService appService = mock(AppService.class);
        AppDefinition appDef = appDefWithPackage();
        when(appService.getAppDefinition("t8086", "1")).thenReturn(appDef);
        when(appService.lockProcessUpdate(appDef)).thenReturn(true);
        when(appService.hasProcessUpdate(appDef)).thenReturn(false);

        ProcessBuilderWebController controller = newController(appService, false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        StringWriter writer = new StringWriter();

        try (MockedStatic<AppUtil> appUtil = Mockito.mockStatic(AppUtil.class);
                MockedStatic<PropertyUtil> propertyUtil = Mockito.mockStatic(PropertyUtil.class)) {
            appUtil.when(() -> AppUtil.getSubmittedJsonDefinition(any())).thenAnswer(inv -> inv.getArgument(0));
            appUtil.when(() -> AppUtil.getXpdlAndMappingJson(appDef)).thenReturn("{\"xpdl\":{}}");
            propertyUtil.when(() -> PropertyUtil.propertiesJsonStoreProcessing(any(), any())).thenAnswer(inv -> inv.getArgument(1));
            propertyUtil.when(() -> PropertyUtil.propertiesJsonLoadProcessing(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try {
                controller.save(writer, request, "t8086", "1", "{}");

                verify(appService, never()).tryReleaseProcessUpdate(any());

                List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
                assertEquals("Exactly one release-on-completion callback should be registered", 1, synchronizations.size());

                synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

                verify(appService, times(1)).tryReleaseProcessUpdate(appDef);
            } finally {
                TransactionSynchronizationManager.setActualTransactionActive(false);
                TransactionSynchronizationManager.clearSynchronization();
            }
        }
    }
}
