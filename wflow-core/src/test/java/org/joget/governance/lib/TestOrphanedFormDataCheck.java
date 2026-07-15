package org.joget.governance.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestOrphanedFormDataCheck {

    public final static String quotedKeyword = "quotedTN";
    public final static String hashKeywordNested = "hashNestedTN";
    public final static String hashKeywordEscape = "hashEscapeTN";
    public final static String hashKeywordDot = "hashDotTN";
    public final static String hashKeywordParam = "hashParamTN";
    public final static String hashKeyword = "hashTN";
    public final static String beanShellKeyword = "beanshellTN";
    public final static String tableKeyword = "tableTN";
    public final static String formHashKeyword = "formHashTN";
    public final static String notExistKeyword = "notExistTN";

    // "keyword"
    @Test
    public void testQuotedKeyword() {
        assertTrue(checkKeywordExist("{\"value\":\"" + quotedKeyword + "\"}", quotedKeyword));
    }

    // .keyword}
    @Test
    public void testHashKeywordNested() {
        assertTrue(checkKeywordExist("#sample.{table." + hashKeywordNested + "}#", hashKeywordNested));
    }

    // .keyword?
    @Test
    public void testHashKeywordEscape() {
        assertTrue(checkKeywordExist("#sample." + hashKeywordEscape + "?sql#", hashKeywordEscape));
    }

    // .keyword.
    @Test
    public void testHashKeywordDot() {
        assertTrue(checkKeywordExist("#sample." + hashKeywordDot + ".field#", hashKeywordDot));
    }

    // .keyword[
    @Test
    public void testHashKeywordParam() {
        assertTrue(checkKeywordExist("#sample." + hashKeywordParam + "[key=1]#", hashKeywordParam));
    }

    // .keyword#
    @Test
    public void testHashKeyword() {
        assertTrue(checkKeywordExist("#sample." + hashKeyword + "#", hashKeyword));
    }

    // "keyword\"
    @Test
    public void testBeanShellKeyword() {
        assertTrue(checkKeywordExist("{\"script\":\"String tableName = \\\"" + beanShellKeyword + "\\\";\"}", beanShellKeyword));
    }

    // app_fd_keyword
    @Test
    public void testTableKeyword() {
        assertTrue(checkKeywordExist("{\"tableName\":\"app_fd_" + tableKeyword + "\"}", tableKeyword));
    }

    // form.keyword.
    @Test
    public void testFormHashKeyword() {
        assertTrue(checkKeywordExist("#form." + formHashKeyword + ".field1#", formHashKeyword));
    }

    // not exist keyword
    @Test
    public void testNotExistKeyword() {
        assertFalse(checkKeywordExist("{\"value\":\"otherTable\"}", notExistKeyword));
    }

    @Test
    public void testDefinitionUsages() {
        Set<String> tables = new HashSet<String>();
        tables.add(quotedKeyword);
        tables.add(hashKeywordNested);
        tables.add(hashKeywordEscape);
        tables.add(hashKeywordDot);
        tables.add(hashKeywordParam);
        tables.add(hashKeyword);
        tables.add(beanShellKeyword);
        tables.add(tableKeyword);
        tables.add(formHashKeyword);
        tables.add(notExistKeyword);

        OrphanedFormDataCheck.checkDefinitionUsages(createAppDefinitionWithUsages(), tables);

        assertTrue(tables.contains(notExistKeyword));
        assertFalse(tables.contains(quotedKeyword));
        assertFalse(tables.contains(hashKeywordNested));
        assertFalse(tables.contains(hashKeywordEscape));
        assertFalse(tables.contains(hashKeywordDot));
        assertFalse(tables.contains(hashKeywordParam));
        assertFalse(tables.contains(hashKeyword));
        assertFalse(tables.contains(beanShellKeyword));
        assertFalse(tables.contains(tableKeyword));
        assertFalse(tables.contains(formHashKeyword));
    }

    public boolean checkKeywordExist(String text, String keyword) {
        Set<String> tables = new HashSet<String>();
        tables.add(keyword);

        OrphanedFormDataCheck.checkDefinitionUsages(createAppDefinitionWithJson(text), tables);

        return !tables.contains(keyword); //tables not contains the keyword meaning it found in the definition
    }

    private AppDefinition createAppDefinitionWithJson(String json) {
        AppDefinition appDef = new AppDefinition();
        FormDefinition formDef = new FormDefinition();
        formDef.setJson(json);
        appDef.setFormDefinitionList(Arrays.asList(formDef));
        return appDef;
    }

    private AppDefinition createAppDefinitionWithUsages() {
        AppDefinition appDef = new AppDefinition();
        appDef.setFormDefinitionList(Arrays.asList(createFormDefinition()));
        appDef.setDatalistDefinitionList(Arrays.asList(createDatalistDefinition()));
        appDef.setUserviewDefinitionList(Arrays.asList(createUserviewDefinition()));
        appDef.setBuilderDefinitionList(Arrays.asList(createBuilderDefinition()));
        appDef.setPluginDefaultPropertiesList(Arrays.asList(createPluginDefaultProperties()));
        appDef.setPackageDefinitionList(Arrays.asList(createPackageDefinition()));
        return appDef;
    }

    private FormDefinition createFormDefinition() {
        FormDefinition formDef = new FormDefinition();
        formDef.setJson("{\"tableName\":\"" + quotedKeyword + "\",\"value\":\"#sample.{table." + hashKeywordNested + "}# #sample." + hashKeywordEscape + "?sql#\"}");
        return formDef;
    }

    private DatalistDefinition createDatalistDefinition() {
        DatalistDefinition datalistDef = new DatalistDefinition();
        datalistDef.setJson("{\"binder\":{\"properties\":{\"tableName\":\"app_fd_" + tableKeyword + "\"}}}");
        return datalistDef;
    }

    private UserviewDefinition createUserviewDefinition() {
        UserviewDefinition userviewDef = new UserviewDefinition();
        userviewDef.setJson("{\"content\":\"#form." + formHashKeyword + ".field1# #sample." + hashKeywordDot + ".field#\"}");
        return userviewDef;
    }

    private BuilderDefinition createBuilderDefinition() {
        BuilderDefinition builderDef = new BuilderDefinition();
        builderDef.setJson("{\"value\":\"#sample." + hashKeywordParam + "[key=1]#\"}");
        return builderDef;
    }

    private PluginDefaultProperties createPluginDefaultProperties() {
        PluginDefaultProperties pluginDefaultProperties = new PluginDefaultProperties();
        pluginDefaultProperties.setPluginProperties("{\"value\":\"#sample." + hashKeyword + "#\"}");
        return pluginDefaultProperties;
    }

    private PackageDefinition createPackageDefinition() {
        PackageDefinition packageDef = new PackageDefinition();
        PackageActivityPlugin activityPlugin = new PackageActivityPlugin();
        activityPlugin.setPluginProperties("{\"script\":\"String tableName = \\\"" + beanShellKeyword + "\\\";\"}");
        Map<String, PackageActivityPlugin> activityPluginMap = new HashMap<String, PackageActivityPlugin>();
        activityPluginMap.put("process1::tool1", activityPlugin);
        packageDef.setPackageActivityPluginMap(activityPluginMap);

        PackageParticipant participant = new PackageParticipant();
        participant.setPluginProperties("");
        Map<String, PackageParticipant> participantMap = new HashMap<String, PackageParticipant>();
        participantMap.put("process1::participant1", participant);
        packageDef.setPackageParticipantMap(participantMap);
        return packageDef;
    }
}
