package org.joget.apps.app.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.userview.service.UserviewService;
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

    @Test
    public void testUserviewPageComponentLinksToNestedSubmenuAndDeepElement() {
        AppDefinition appDef = new AppDefinition();

        UserviewDefinition userviewDef = new UserviewDefinition();
        userviewDef.setId("myUserview");
        userviewDef.setJson("{\"categories\":["
                + "{\"menus\":[{\"properties\":{\"id\":\"menuA\"}}]},"
                + "{\"menus\":[{\"properties\":{\"id\":\"menuGroup\"},\"menus\":["
                + "{\"properties\":{\"id\":\"menuB\"}}"
                + "]}]}"
                + "]}");
        appDef.setUserviewDefinitionList(Arrays.asList(userviewDef));

        // page for menuB: a decoy label sibling before a container, with a decoy
        // label then the matching FormMenu nested inside that container, so the
        // walk has to skip a no-properties node, skip a non-matching sibling,
        // recurse into the container, and skip a non-matching child before
        // landing on the right index at each level.
        BuilderDefinition page = new BuilderDefinition();
        page.setId("up-menuB");
        page.setName("Page B");
        page.setType(UserviewService.TYPE);
        page.setDescription("myUserview");
        page.setJson("{\"className\":\"org.joget.apps.userview.model.UserviewPage\","
                + "\"properties\":{\"id\":\"up-menuB\"},"
                + "\"elements\":["
                + "{\"className\":\"menu-component\"},"
                + "{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"id\":\"label1\",\"text\":\"hello\"}},"
                + "{\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"container1\"},\"elements\":["
                + "{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"id\":\"label2\",\"text\":\"world\"}},"
                + "{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"formMenu1\",\"formId\":\"targetForm\"}}"
                + "]}"
                + "]}");
        appDef.setBuilderDefinitionList(Arrays.asList(page));

        JSONArray usages = DependenciesUtil.getDependencies(appDef, "testapp", "1", "form", "targetForm", request);

        JSONObject usage = findUsage(usages, "userview", "up-menuB");
        assertNotNull(usage);
        assertEquals("/jw/web/console/app/testapp/1/userview/builder/myUserview?menuId=menuB"
                + "&overview_path=categories%5B1%5D.menus%5B0%5D.menus%5B0%5D.referencePage.elements%5B2%5D.elements%5B1%5D",
                usage.getString("link"));
        assertTrue(containsFoundText(usage, "targetForm"));
    }

    @Test
    public void testUserviewPageComponentFallsBackToFirstElementWhenMatchIsNotInAnyElement() {
        AppDefinition appDef = new AppDefinition();

        UserviewDefinition userviewDef = new UserviewDefinition();
        userviewDef.setId("myUserview2");
        userviewDef.setJson("{\"categories\":[{\"menus\":[{\"properties\":{\"id\":\"menuC\"}}]}]}");
        appDef.setUserviewDefinitionList(Arrays.asList(userviewDef));

        // the keyword only appears in the page's own root properties, not inside
        // any element's properties, so no single element can account for it
        BuilderDefinition page = new BuilderDefinition();
        page.setId("up-menuC");
        page.setName("Page C");
        page.setType(UserviewService.TYPE);
        page.setDescription("myUserview2");
        page.setJson("{\"className\":\"org.joget.apps.userview.model.UserviewPage\","
                + "\"properties\":{\"id\":\"up-menuC\",\"note\":\"targetForm\"},"
                + "\"elements\":[{\"className\":\"menu-component\"}]}");
        appDef.setBuilderDefinitionList(Arrays.asList(page));

        JSONArray usages = DependenciesUtil.getDependencies(appDef, "testapp", "1", "form", "targetForm", request);

        JSONObject usage = findUsage(usages, "userview", "up-menuC");
        assertNotNull(usage);
        assertEquals("/jw/web/console/app/testapp/1/userview/builder/myUserview2?menuId=menuC"
                + "&overview_path=categories%5B0%5D.menus%5B0%5D.referencePage.elements%5B0%5D",
                usage.getString("link"));
    }

    @Test
    public void testUserviewPageComponentOmitsOverviewPathWhenOwningUserviewIsMissing() {
        AppDefinition appDef = new AppDefinition();
        // no UserviewDefinition registered for "missingUserview"

        BuilderDefinition page = new BuilderDefinition();
        page.setId("up-menuD");
        page.setName("Page D");
        page.setType(UserviewService.TYPE);
        page.setDescription("missingUserview");
        page.setJson("{\"className\":\"org.joget.apps.userview.model.UserviewPage\","
                + "\"properties\":{\"id\":\"up-menuD\"},"
                + "\"elements\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"formMenu2\",\"formId\":\"targetForm\"}}]}");
        appDef.setBuilderDefinitionList(Arrays.asList(page));

        JSONArray usages = DependenciesUtil.getDependencies(appDef, "testapp", "1", "form", "targetForm", request);

        JSONObject usage = findUsage(usages, "userview", "up-menuD");
        assertNotNull(usage);
        assertEquals("/jw/web/console/app/testapp/1/userview/builder/missingUserview?menuId=menuD", usage.getString("link"));
    }

    @Test
    public void testUserviewPageComponentStopsAtNearestAddressableAncestorAcrossColumns() {
        AppDefinition appDef = new AppDefinition();

        UserviewDefinition userviewDef = new UserviewDefinition();
        userviewDef.setId("myUserview3");
        userviewDef.setJson("{\"categories\":[{\"menus\":[{\"properties\":{\"id\":\"menuE\"}}]}]}");
        appDef.setUserviewDefinitionList(Arrays.asList(userviewDef));

        // mirrors a real ColumnsComponent/ColumnComponent layout: Column nodes
        // always have empty properties (no id), so the FormMenu nested inside
        // the second column can't be addressed by a [data-cbuilder-id=...]
        // chain in the userview builder - the ColumnsComponent (which does
        // have an id) is the deepest safe target.
        BuilderDefinition page = new BuilderDefinition();
        page.setId("up-menuE");
        page.setName("Page E");
        page.setType(UserviewService.TYPE);
        page.setDescription("myUserview3");
        page.setJson("{\"className\":\"org.joget.apps.userview.model.UserviewPage\","
                + "\"properties\":{\"id\":\"up-menuE\"},"
                + "\"elements\":[{"
                + "\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\","
                + "\"properties\":{\"id\":\"columnsRow1\"},"
                + "\"elements\":["
                + "{\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},"
                + "{\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{},\"elements\":["
                + "{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"formMenu3\",\"formId\":\"targetForm\"}}"
                + "]}"
                + "]"
                + "}]}");
        appDef.setBuilderDefinitionList(Arrays.asList(page));

        JSONArray usages = DependenciesUtil.getDependencies(appDef, "testapp", "1", "form", "targetForm", request);

        JSONObject usage = findUsage(usages, "userview", "up-menuE");
        assertNotNull(usage);
        assertEquals("/jw/web/console/app/testapp/1/userview/builder/myUserview3?menuId=menuE"
                + "&overview_path=categories%5B0%5D.menus%5B0%5D.referencePage.elements%5B0%5D",
                usage.getString("link"));
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
