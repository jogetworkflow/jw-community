package org.joget.apps.app.model;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class VisibilityMessageLocalizationTest {

    private static final List<String> LOCALE_RESOURCES = Arrays.asList(
            "/plugin_ar.properties",
            "/plugin_hu.properties",
            "/plugin_ko.properties",
            "/plugin_ms.properties",
            "/plugin_th.properties",
            "/plugin_zh_CN.properties",
            "/plugin_zh_TW.properties"
    );

    private static final List<String> ADVTOOL_LOCALE_RESOURCES = Arrays.asList(
            "/advtool_ar.properties",
            "/advtool_zh_CN.properties",
            "/advtool_zh_TW.properties"
    );

    private static final List<String> VISIBILITY_KEYS = Arrays.asList(
            "form.section.visibilityNoRules",
            "form.section.visibilityOpenTool",
            "form.section.visibilityManageRules",
            "form.section.visibilityEditRule",
            "form.section.visibilityLegacyRule",
            "form.section.visibilityPendingMigration",
            "form.section.visibilityRuleCountOne",
            "form.section.visibilityRuleCountMany",
            "form.section.visibilityUsedByOne",
            "form.section.visibilityUsedByMany",
            "form.section.visibilityNoConditions",
            "form.section.visibilityNot",
            "form.section.visibilityEmptyValue",
            "form.section.visibilityNotice"
    );

    private static final List<String> ADVTOOL_VISIBILITY_KEYS = Arrays.asList(
            "adv.visibility.migrationNotice",
            "adv.visibility.migrateNow",
            "adv.visibility.pending",
            "adv.visibility.migrationDone",
            "adv.visibility.ruleNotFound"
    );

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\d+\\}");

    @Test
    public void testEveryLocaleDefinesTranslatedVisibilityMessages() throws Exception {
        Properties english = loadProperties("/plugin.properties");

        for (String resource : LOCALE_RESOURCES) {
            Properties localized = loadProperties(resource);
            for (String key : VISIBILITY_KEYS) {
                String englishValue = english.getProperty(key);
                String localizedValue = localized.getProperty(key);

                Assert.assertNotNull("Missing English message " + key, englishValue);
                Assert.assertNotNull("Missing " + key + " in " + resource, localizedValue);
                Assert.assertFalse("Blank " + key + " in " + resource, localizedValue.trim().isEmpty());
                Assert.assertNotEquals("Untranslated " + key + " in " + resource,
                        englishValue, localizedValue);
                Assert.assertEquals("Placeholder mismatch for " + key + " in " + resource,
                        findPlaceholders(englishValue), findPlaceholders(localizedValue));
            }
        }
    }

    @Test
    public void testEveryAdvtoolLocaleDefinesTranslatedVisibilityMigrationMessages() throws Exception {
        Properties english = loadProperties("/advtool.properties");

        for (String resource : ADVTOOL_LOCALE_RESOURCES) {
            Properties localized = loadProperties(resource);
            for (String key : ADVTOOL_VISIBILITY_KEYS) {
                String englishValue = english.getProperty(key);
                String localizedValue = localized.getProperty(key);

                Assert.assertNotNull("Missing English message " + key, englishValue);
                Assert.assertNotNull("Missing " + key + " in " + resource, localizedValue);
                Assert.assertFalse("Blank " + key + " in " + resource, localizedValue.trim().isEmpty());
                Assert.assertNotEquals("Untranslated " + key + " in " + resource,
                        englishValue, localizedValue);
                Assert.assertEquals("Placeholder mismatch for " + key + " in " + resource,
                        findPlaceholders(englishValue), findPlaceholders(localizedValue));
            }
        }
    }

    private Properties loadProperties(String resource) throws Exception {
        InputStream input = getClass().getResourceAsStream(resource);
        Assert.assertNotNull("Missing resource " + resource, input);
        try {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } finally {
            input.close();
        }
    }

    private Set<String> findPlaceholders(String value) {
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        while (matcher.find()) {
            placeholders.add(matcher.group());
        }
        return placeholders;
    }
}
