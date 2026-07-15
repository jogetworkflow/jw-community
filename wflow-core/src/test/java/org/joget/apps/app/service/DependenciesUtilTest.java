package org.joget.apps.app.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.context.ApplicationContext;

public class DependenciesUtilTest {

    private HttpServletRequest request;

    @Before
    public void setup() {
        request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/jw");
    }

    @Test
    public void testAppDefinitionDependenciesAreFoundWithoutXmlExport() {
        JSONArray usages = DependenciesUtil.getDependencies(createAppDefinitionWithDependencies(), "testtableusage", "1", "table", "tableTN", request);

        JSONObject formUsage = findUsage(usages, "form", "tableTN");
        assertNotNull(formUsage);
        assertEquals("tableTN", formUsage.getString("label"));
        assertEquals("/jw/web/console/app/testtableusage/1/form/builder/tableTN", formUsage.getString("link"));
        assertTrue(formUsage.getJSONArray("found").length() > 0);

        JSONObject datalistUsage = findUsage(usages, "datalist", "list");
        assertNotNull(datalistUsage);
        assertEquals("list", datalistUsage.getString("label"));
        assertEquals("/jw/web/console/app/testtableusage/1/datalist/builder/list", datalistUsage.getString("link"));
        assertTrue(containsFoundText(datalistUsage, "app_fd_tableTN"));
    }

    @Test
    public void testPublicGetDependenciesDoesNotExportAppXml() {
        AppService appService = mock(AppService.class);
        when(appService.getAppDefinition("testtableusage", "1")).thenReturn(createAppDefinitionWithDependencies());

        ApplicationContext appContext = mock(ApplicationContext.class);
        when(appContext.getBean("appService")).thenReturn(appService);

        try (MockedStatic<AppUtil> appUtilMock = Mockito.mockStatic(AppUtil.class)) {
            appUtilMock.when(AppUtil::getApplicationContext).thenReturn(appContext);

            JSONArray usages = DependenciesUtil.getDependencies("testtableusage", "1", "table", "tableTN", request);

            JSONObject datalistUsage = findUsage(usages, "datalist", "list");
            assertNotNull(datalistUsage);
            assertTrue(containsFoundText(datalistUsage, "app_fd_tableTN"));
            verify(appService).getAppDefinition("testtableusage", "1");
            verify(appService, never()).getAppDefinitionXml("testtableusage", 1L, false);
        }
    }

    @Test
    public void testAppDefinitionProcessDependenciesAreFoundWithoutXmlExport() {
        JSONArray toolUsages = DependenciesUtil.getDependencies(createAppDefinitionWithDependencies(), "testtableusage", "1", "form", "beanshellTN", request);

        JSONObject toolUsage = findUsage(toolUsages, "process_tool", "process1::tool1");
        assertNotNull(toolUsage);
        assertEquals("process1::tool1", toolUsage.getString("label"));
        assertEquals("/jw/web/console/app/testtableusage/1/processes/process1?view=listViewer&id=tool1", toolUsage.getString("link"));
        assertTrue(containsFoundText(toolUsage, "beanshellTN"));

        JSONArray activityUsages = DependenciesUtil.getDependencies(createAppDefinitionWithDependencies(), "testtableusage", "1", "form", "targetForm", request);
        JSONObject activityUsage = findUsage(activityUsages, "process_activity", "process1::activity1");
        assertNotNull(activityUsage);
        assertEquals("/jw/web/console/app/testtableusage/1/process/builder#process1?view=listViewer&id=activity1", activityUsage.getString("link"));
        assertFalse(activityUsage.has("found"));

        JSONArray participantUsages = DependenciesUtil.getDependencies(createAppDefinitionWithDependencies(), "testtableusage", "1", "form", "participantForm", request);
        JSONObject participantUsage = findUsage(participantUsages, "process_participant", "process1::participant1");
        assertNotNull(participantUsage);
        assertEquals("/jw/web/console/app/testtableusage/1/processes/process1?view=listViewer&id=participant1", participantUsage.getString("link"));
        assertTrue(containsFoundText(participantUsage, "participantForm"));
    }

    @Test
    public void testAppDefinitionPluginDefaultDependencyIsFoundWithoutXmlExport() {
        JSONArray usages = DependenciesUtil.getDependencies(createAppDefinitionWithDependencies(), "testtableusage", "1", "form", "targetForm", request);

        JSONObject usage = findUsage(usages, "plugin_default_properties", "org.example.Plugin");
        assertNotNull(usage);
        assertEquals("Example Plugin", usage.getString("label"));
        assertEquals("/jw/web/console/app/testtableusage/1/builders?view=pluginDefaultProperties&plugin=org.example.Plugin", usage.getString("link"));
        assertTrue(containsFoundText(usage, "targetForm"));
    }

    private JSONObject findUsage(JSONArray usages, String type, String where) {
        for (int i = 0; i < usages.length(); i++) {
            JSONObject usage = usages.getJSONObject(i);
            if (type.equals(usage.optString("type")) && where.equals(usage.optString("where"))) {
                return usage;
            }
        }
        return null;
    }

    private boolean containsFoundText(JSONObject usage, String text) {
        JSONArray found = usage.getJSONArray("found");
        for (int i = 0; i < found.length(); i++) {
            if (found.getString(i).contains(text)) {
                return true;
            }
        }
        return false;
    }

    private AppDefinition createAppDefinitionWithDependencies() {
        AppDefinition appDef = new AppDefinition();

        FormDefinition formDef = new FormDefinition();
        formDef.setId("tableTN");
        formDef.setName("tableTN");
        formDef.setJson("{\"id\":\"tableTN\",\"name\":\"tableTN\",\"tableName\":\"quotedTN\"}");
        appDef.setFormDefinitionList(Arrays.asList(formDef));

        DatalistDefinition datalistDef = new DatalistDefinition();
        datalistDef.setId("list");
        datalistDef.setName("list");
        datalistDef.setJson("{\"binder\":{\"properties\":{\"tableName\":\"app_fd_tableTN\"}},\"columns\":[{\"name\":\"app_fd_tableTN.id\"}]}");
        appDef.setDatalistDefinitionList(Arrays.asList(datalistDef));

        PluginDefaultProperties pluginDefaultProperties = new PluginDefaultProperties();
        pluginDefaultProperties.setId("org.example.Plugin");
        pluginDefaultProperties.setPluginName("Example Plugin");
        pluginDefaultProperties.setPluginProperties("{\"formId\":\"targetForm\"}");
        appDef.setPluginDefaultPropertiesList(Arrays.asList(pluginDefaultProperties));

        appDef.setPackageDefinitionList(Arrays.asList(createPackageDefinition()));
        return appDef;
    }

    private PackageDefinition createPackageDefinition() {
        PackageDefinition packageDef = new PackageDefinition();

        PackageActivityForm activityForm = new PackageActivityForm();
        activityForm.setFormId("targetForm");
        Map<String, PackageActivityForm> activityFormMap = new HashMap<String, PackageActivityForm>();
        activityFormMap.put("process1::activity1", activityForm);
        packageDef.setPackageActivityFormMap(activityFormMap);

        PackageActivityPlugin activityPlugin = new PackageActivityPlugin();
        activityPlugin.setPluginProperties("{\"script\":\"String tableName = \\\"beanshellTN\\\";\"}");
        Map<String, PackageActivityPlugin> activityPluginMap = new HashMap<String, PackageActivityPlugin>();
        activityPluginMap.put("process1::tool1", activityPlugin);
        packageDef.setPackageActivityPluginMap(activityPluginMap);

        PackageParticipant participant = new PackageParticipant();
        participant.setPluginProperties("{\"formId\":\"participantForm\"}");
        Map<String, PackageParticipant> participantMap = new HashMap<String, PackageParticipant>();
        participantMap.put("process1::participant1", participant);
        packageDef.setPackageParticipantMap(participantMap);
        return packageDef;
    }
}
