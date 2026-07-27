package org.joget.apps.app.service;

import java.util.Collections;
import org.hibernate.ObjectNotFoundException;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AppUtilTest {

    @Test
    public void testIsStalePackageCacheExceptionRecognizesPackageMetadataObjects() {
        assertTrue(AppUtil.isStalePackageCacheException(objectNotFound("t8086#11", "PackageDefinition")));
        assertTrue(AppUtil.isStalePackageCacheException(objectNotFound("t8086#11#activity", "PackageActivityForm")));
        assertTrue(AppUtil.isStalePackageCacheException(objectNotFound("t8086#11#tool", "PackageActivityPlugin")));
        assertTrue(AppUtil.isStalePackageCacheException(objectNotFound("t8086#11#participant", "PackageParticipant")));
    }

    @Test
    public void testIsStalePackageCacheExceptionIgnoresUnrelatedExceptions() {
        assertFalse(AppUtil.isStalePackageCacheException(objectNotFound("form#1", "FormDefinition")));
        assertFalse(AppUtil.isStalePackageCacheException(new IllegalStateException("PackageDefinition")));
    }

    /**
     * A genuinely new app with no deployed package at all must still fall back to the bundled
     * default XPDL template — the fail-closed change must not break app creation.
     */
    @Test
    public void testGetXpdlWithException_noPackageDefinition_fallsBackToDefaultXpdl() throws Exception {
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086new");
        appDef.setName("T8086 New App");
        appDef.setVersion(1L);
        appDef.setPackageDefinitionList(Collections.<PackageDefinition>emptyList());

        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        when(pluginManager.getPluginResource(anyString(), anyString())).thenAnswer(invocation ->
                getClass().getResourceAsStream("/org/joget/apps/app/model/default.xpdl"));

        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getBean("pluginManager")).thenReturn(pluginManager);

        try (MockedStatic<WorkflowUtil> workflowUtil = Mockito.mockStatic(WorkflowUtil.class)) {
            workflowUtil.when(WorkflowUtil::getApplicationContext).thenReturn(context);

            String xpdl = AppUtil.getXpdlWithException(appDef);

            assertTrue("Default XPDL template should be returned for a package-less app", xpdl != null && xpdl.contains("t8086new"));
        }
    }

    /**
     * A package definition that exists but whose deployed Shark content cannot be found is
     * corruption (or a cleanup bug), not a new app. It must fail closed instead of silently
     * substituting the default/blank XPDL, which could then be saved back over the real design.
     */
    @Test
    public void testGetXpdlWithException_packageContentMissing_throwsInsteadOfDefaulting() throws Exception {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId("t8086");
        packageDef.setVersion(3L);

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086");
        appDef.setVersion(1L);
        appDef.setPackageDefinitionList(Collections.singletonList(packageDef));

        WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
        when(workflowManager.getPackageContent("t8086", "3")).thenReturn(null);

        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getBean("workflowManager")).thenReturn(workflowManager);

        try (MockedStatic<WorkflowUtil> workflowUtil = Mockito.mockStatic(WorkflowUtil.class)) {
            workflowUtil.when(WorkflowUtil::getApplicationContext).thenReturn(context);

            try {
                AppUtil.getXpdlWithException(appDef);
                fail("Expected an exception instead of a silently substituted default/blank XPDL");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("t8086"));
            }
        }
    }

    /**
     * getXpdlAndMappingJson(Obj) must propagate the same failure rather than returning a
     * partial/empty JSON object dressed up as a successful process builder definition.
     */
    @Test
    public void testGetXpdlAndMappingJsonObj_packageContentMissing_throwsInsteadOfReturningEmptyJson() throws Exception {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId("t8086");
        packageDef.setVersion(3L);

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086");
        appDef.setVersion(1L);
        appDef.setPackageDefinitionList(Collections.singletonList(packageDef));

        WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
        when(workflowManager.getPackageContent(anyString(), anyString())).thenReturn(null);

        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getBean("workflowManager")).thenReturn(workflowManager);

        try (MockedStatic<WorkflowUtil> workflowUtil = Mockito.mockStatic(WorkflowUtil.class)) {
            workflowUtil.when(WorkflowUtil::getApplicationContext).thenReturn(context);

            try {
                AppUtil.getXpdlAndMappingJsonObj(appDef);
                fail("Expected an exception instead of a silently returned empty/partial JSON definition");
            } catch (IllegalStateException expected) {
                // expected: failure must propagate, never a quiet empty JSONObject
            }
        }
    }

    private ObjectNotFoundException objectNotFound(Object identifier, String entityName) {
        return new ObjectNotFoundException(identifier, entityName);
    }
}
