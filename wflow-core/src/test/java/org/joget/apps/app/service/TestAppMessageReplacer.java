package org.joget.apps.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.io.IOUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:testAppsApplicationContext.xml"})
public class TestAppMessageReplacer {

    @Autowired
    private AppService appService;
    private AppDefinition appDef;

    private static final JsonMapper mapper = new JsonMapper();
    private static final String EXPECTED_PENCILS = "<i class=\"fas fa-pencil-alt\"></i><i class=\"fas fa-pencil-alt\"></i><i class=\"fas fa-pencil-alt\"></i> i18n_pencils";

    @Before
    public void importApp() throws IOException {
        // import app
        if (appDef == null) {
            try (InputStream in = TestAppMessageReplacer.class.getClassLoader().getResourceAsStream("appMessageReplacer/APP_testReplaceAppMessages-1.jwa")) {
                assertNotNull("Test app to import is not found! (appMessageReplacer/APP_testReplaceAppMessages-1.jwa)", in);
                appDef = appService.importApp(IOUtils.toByteArray(in));
                AppUtil.setCurrentAppDefinition(appDef);
            }
        }
    }

    @After
    public void removeApp() {
        // clean up
        if (appDef != null) {
            appService.deleteAllAppDefinitionVersions(appDef.getAppId());
            AppUtil.clearRequest();
        }
    }

    @Test
    public void streamingJsonReplacement_f1_shouldTranslateProperly() {
        testReplacementMethod(AppMessageReplacer::streamingJsonReplacement);
    }

    @Test
    public void regexJsonReplacement_f1_shouldTranslateProperly() {
        testReplacementMethod(AppMessageReplacer::regexJsonReplacement);
    }

    @Test
    public void replaceAppMessage_i18nHashVariableKeyOnly_shouldTranslate() {
        String translated = AppUtil.replaceAppMessage("pencils");
        assertEquals(EXPECTED_PENCILS, translated);
    }

    @Test
    public void replaceAppMessage_i18nHashVariableKeyOnlyWithAppendedI18n_shouldTranslate() {
        String translated = AppUtil.replaceAppMessage("appended_pencils");
        assertEquals("i18n_appended_pencils", translated);
    }

    @Test
    public void replaceAppMessage_i18nHashVariable_shouldTranslate() {
        String translated = AppUtil.replaceAppMessage("#i18n.pencils#");
        assertEquals(EXPECTED_PENCILS, translated);
    }

    @Test
    public void replaceAppMessage_noTranslationExist_shouldReturnInput() {
        String translated = AppUtil.replaceAppMessage("NOTHING");
        assertEquals("NOTHING", translated);
    }

    private void testReplacementMethod(AppMessageReplacementMethod func) {
        try {
            // get JSON from FormDefinition
            FormDefinition formDef = appDef.getFormDefinitionList().iterator().next();
            String formJson = formDef.getJson();

            // execute test
            Map<String, String> appMessages = AppUtil.getAppMessageFromStore();
            String out = func.apply(formJson, "json", appMessages);

            // compare with expected json
            try (InputStream in = TestAppMessageReplacer.class.getClassLoader().getResourceAsStream("appMessageReplacer/APP_testReplaceAppMessages-1_f1-translated_en.json")) {
                assertNotNull("Expected JSON to test is not found! (appMessageReplacer/APP_testReplaceAppMessages-1_f1-translated_en.json)", in);
                String expectedJson = IOUtils.toString(in, StandardCharsets.UTF_8);
                JsonNode expected = mapper.readTree(expectedJson);
                JsonNode actual = mapper.readTree(out);
                assertEquals(expected, actual);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private interface AppMessageReplacementMethod {
        String apply(String content, String escapeType, Map<String, String> appMessages) throws Exception;
    }
}
