package org.joget.apps.form.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.joget.apps.form.lib.TextField;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.Section;
import org.junit.Assert;
import org.junit.Test;

public class VisibilityControlUtilTest {

    @Test
    public void testMigratedRuleWithDuplicateNestedControlDoesNotRecurse() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField nestedControl = textField("control");
        Section controlledSection = section("section1", nestedControl);
        controlledSection.setProperty("visibility_rules", appliedRules("rule1"));

        Section otherSection = section("section2", textField("control"));

        Form form = form("form1", controlledSection, otherSection);
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Migrated Section Rule", "control", "show", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                controlledSection, formData, controlElements);

        Assert.assertEquals("The migrated rule should still be parsed after the re-entrancy guard returns.", 1, rules.size());
        Assert.assertTrue("The duplicated control field should be resolved for runtime evaluation.",
                controlElements.containsKey("control"));
        Assert.assertTrue("The migrated visibility rule should evaluate normally.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testMultipleAppliedMigratedRulesAreCombinedWithOr() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control1", new String[]{"no"});
        formData.addRequestParameterValues("control2", new String[]{"yes"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1", "rule2"));

        Form form = form("form1", section("section1", textField("control1"), textField("control2"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "First Rule", "control1", "yes", "", "", ""),
                rule("rule2", "Second Rule", "control2", "yes", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);
        List<Map<String, String>> ruleList = new ArrayList<Map<String, String>>(rules);

        Assert.assertEquals(2, ruleList.size());
        Assert.assertEquals("", ruleList.get(0).get("join"));
        Assert.assertEquals("Different migrated rules applied to one element should be OR-ed.", "or",
                ruleList.get(1).get("join"));
        Assert.assertTrue("Second rule match should make the target visible even when the first rule is false.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testGroupedAppliedRulesAreCombinedWithOrAtOpeningGroup() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control1", new String[]{"no"});
        formData.addRequestParameterValues("control2", new String[]{"yes"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1", "rule2"));

        Form form = form("form1", section("section1", textField("control1"), textField("control2"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "First Rule", "control1", "yes", "", "", ""),
                rule("rule2", "Grouped Rule", "(;control2;)", ";yes;", ";;", ";;", ";;")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);
        List<Map<String, String>> ruleList = new ArrayList<Map<String, String>>(rules);

        Assert.assertEquals(4, ruleList.size());
        Assert.assertEquals("(", ruleList.get(1).get("field"));
        Assert.assertEquals("A grouped rule must be OR-ed at its opening parenthesis.", "or",
                ruleList.get(1).get("join"));
        Assert.assertTrue("The matching grouped rule should make the target visible.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testSectionLegacyAndMigratedRulesAreCombinedWithOr() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("legacyControl", new String[]{"hide"});
        formData.addRequestParameterValues("migratedControl", new String[]{"show"});

        Section section = section("section1", textField("legacyControl"), textField("migratedControl"));
        section.setProperty("visibilityControl", "legacyControl");
        section.setProperty("visibilityValue", "show");
        section.setProperty("regex", "");
        section.setProperty("join", "");
        section.setProperty("reverse", "");
        section.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section);
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Migrated Rule", "migratedControl", "show", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                section, formData, controlElements);
        List<Map<String, String>> ruleList = new ArrayList<Map<String, String>>(rules);

        Assert.assertEquals("Legacy section rules should be grouped before migrated rules.", "(", ruleList.get(0).get("field"));
        Assert.assertEquals("Migrated section rules should be OR-ed with the legacy section rule group.", "or",
                ruleList.get(3).get("join"));
        Assert.assertTrue("A matching migrated rule should show the section even when its legacy rule is false.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testAppliedRuleJsonStringIsSupported() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", "{\"rule1\":true}");

        Form form = form("form1", section("section1", textField("control"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Migrated Rule", "control", "show", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertEquals(1, rules.size());
        Assert.assertTrue(VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testMalformedAppliedRuleJsonIsIgnoredAndParsingGuardIsReleased() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", "{not-json");

        Form form = form("form1", section("section1", textField("control"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Migrated Rule", "control", "show", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> malformedRules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertTrue("Malformed applied-rule JSON should not break form rendering.", malformedRules.isEmpty());
        Assert.assertTrue("An ignored rule set should leave the element visible.",
                VisibilityControlUtil.evaluateVisibilityRules(malformedRules, controlElements, formData));

        target.setProperty("visibility_rules", "{\"rule1\":true}");
        Collection<Map<String, String>> validRules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertEquals("Parsing must work again after an earlier malformed value.", 1, validRules.size());
        Assert.assertTrue(VisibilityControlUtil.evaluateVisibilityRules(validRules, controlElements, formData));
    }

    @Test
    public void testFalseAndUnknownAppliedRuleKeysAreIgnored() {
        FormData formData = new FormData();
        TextField target = textField("target");
        Map<String, Object> applied = appliedRules("unknown");
        applied.put("rule1", false);
        applied.put("rule2", "false");
        target.setProperty("visibility_rules", applied);

        Form form = form("form1", section("section1", textField("control"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "First Rule", "control", "show", "", "", ""),
                rule("rule2", "Second Rule", "control", "show", "", "", "")));

        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, new HashMap<String, Element>());

        Assert.assertTrue("False and deleted rule references must not create conditions.", rules.isEmpty());
    }

    @Test
    public void testFormRuleObjectArrayIsSupported() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section("section1", textField("control"), target));
        form.setProperty("visibility_rules", new Object[]{
            "invalid entry",
            rule("rule1", "Migrated Rule", "control", "show", "", "", "")
        });

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertEquals("Object-array form properties should retain valid map entries.", 1, rules.size());
        Assert.assertTrue(VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testFormRuleCollectionIgnoresInvalidEntries() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section("section1", textField("control"), target));
        List<Object> storedRules = new ArrayList<Object>();
        storedRules.add(null);
        storedRules.add("invalid entry");
        storedRules.add(rule("rule1", "Migrated Rule", "control", "show", "", "", ""));
        form.setProperty("visibility_rules", storedRules);

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertEquals("Collection-backed form properties should skip malformed entries.", 1, rules.size());
        Assert.assertTrue(VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testMigratedGroupedConditionsPreserveJoinsAndReverse() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control1", new String[]{"show"});
        formData.addRequestParameterValues("control2", new String[]{"safe"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section("section1",
                textField("control1"), textField("control2"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Grouped Rule",
                        "(;control1;control2;)",
                        ";show;blocked;",
                        ";;contains;",
                        ";;or;",
                        ";;true;")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);
        List<Map<String, String>> ruleList = new ArrayList<Map<String, String>>(rules);

        Assert.assertEquals(4, ruleList.size());
        Assert.assertEquals("(", ruleList.get(0).get("field"));
        Assert.assertEquals("or", ruleList.get(2).get("join"));
        Assert.assertEquals("true", ruleList.get(2).get("reverse"));
        Assert.assertEquals(")", ruleList.get(3).get("field"));
        Assert.assertTrue("show OR NOT contains(blocked) should match.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));

        formData.addRequestParameterValues("control1", new String[]{"hide"});
        formData.addRequestParameterValues("control2", new String[]{"blocked value"});
        Assert.assertFalse("hide OR NOT contains(blocked) should not match.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testMissingControlFieldDoesNotFailEvaluation() {
        FormData formData = new FormData();
        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section("section1", target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Broken Rule", "deletedControl", "show", "", "", "")));

        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);

        Assert.assertTrue("A deleted control field should be skipped instead of producing invalid JavaScript.",
                rules.isEmpty());
        Assert.assertTrue(VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testMalformedRuleExpressionFailsClosed() {
        Collection<Map<String, String>> malformedRules = Arrays.asList(
                condition("(", "", "", "", ""));

        Assert.assertFalse("A malformed expression should hide the element.",
                VisibilityControlUtil.evaluateVisibilityRules(malformedRules,
                        new HashMap<String, Element>(), new FormData()));
    }

    @Test
    public void testEmptyGroupFailsClosedAndIsSentToTheClient() {
        Collection<Map<String, String>> emptyGroupRules = Arrays.asList(
                condition("(", "", "", "", ""),
                condition(")", "", "", "", ""));

        Assert.assertFalse("An empty group left after a control field is deleted must fail closed.",
                VisibilityControlUtil.evaluateVisibilityRules(emptyGroupRules,
                        new HashMap<String, Element>(), new FormData()));
        Assert.assertNotNull("The same invalid expression must be sent to the client monitor.",
                VisibilityControlUtil.toJson(emptyGroupRules));
    }

    @Test
    public void testExcessivelyNestedRuleExpressionFailsClosedBeforeRhinoParsing() {
        List<Map<String, String>> deeplyNestedRules = new ArrayList<Map<String, String>>();
        for (int i = 0; i < 1000; i++) {
            deeplyNestedRules.add(condition("(", "", "", "", ""));
        }
        deeplyNestedRules.add(condition("missingControl", "", "", "", ""));
        for (int i = 0; i < 1000; i++) {
            deeplyNestedRules.add(condition(")", "", "", "", ""));
        }

        Assert.assertFalse("Unsafe nesting should be rejected without overflowing Rhino's parser stack.",
                VisibilityControlUtil.evaluateVisibilityRules(
                        deeplyNestedRules, new HashMap<String, Element>(), new FormData()));
    }

    @Test
    public void testVisibilityMetadataHelpersSupportLegacyAndMigratedFormats() {
        TextField element = textField("target");
        Assert.assertFalse(VisibilityControlUtil.hasVisibilityControl(element));
        Assert.assertNull(VisibilityControlUtil.toJson(new ArrayList<Map<String, String>>()));

        element.setProperty("visibilityControl", "legacyControl");
        Assert.assertTrue(VisibilityControlUtil.hasVisibilityControl(element));

        element.setProperty("visibilityControl", "");
        element.setProperty("visibility_rules", appliedRules("rule1"));
        Assert.assertTrue(VisibilityControlUtil.hasVisibilityControl(element));

        List<Map<String, String>> rules = Arrays.asList(
                condition("control", "show", "", "", ""));
        String json = VisibilityControlUtil.toJson(rules);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"rules\""));
        Assert.assertTrue(json.contains("\"field\":\"control\""));
    }

    @Test
    public void testParsingGuardBlocksReentrantCallWithoutCorruptingOuterMarker() {
        FormData formData = new FormData();
        formData.addRequestParameterValues("control", new String[]{"show"});

        TextField target = textField("target");
        target.setProperty("visibility_rules", appliedRules("rule1"));

        Form form = form("form1", section("section1", textField("control"), target));
        form.setProperty("visibility_rules", Arrays.asList(
                rule("rule1", "Migrated Rule", "control", "show", "", "", "")));

        // Simulate the outer parse having already marked this element as in progress.
        Assert.assertTrue("The outer parse owns the in-progress marker.",
                formData.addVisibilityRulesParsingInProgress(target));

        Collection<Map<String, String>> reentrant = VisibilityControlUtil.parseVisibilityRules(
                target, formData, new HashMap<String, Element>());
        Assert.assertTrue("A re-entrant parse of the same element must break the cycle with an empty result.",
                reentrant.isEmpty());

        // The blocked re-entrant call must NOT release the marker owned by the outer parse.
        Assert.assertFalse("Re-entrant guard must leave the outer parse's marker in place.",
                formData.addVisibilityRulesParsingInProgress(target));

        // Once the outer parse releases the marker, a fresh parse works normally.
        formData.removeVisibilityRulesParsingInProgress(target);
        Map<String, Element> controlElements = new HashMap<String, Element>();
        Collection<Map<String, String>> rules = VisibilityControlUtil.parseVisibilityRules(
                target, formData, controlElements);
        Assert.assertEquals("After the guard is released the element parses normally.", 1, rules.size());
        Assert.assertTrue("The released element evaluates its rule normally.",
                VisibilityControlUtil.evaluateVisibilityRules(rules, controlElements, formData));
    }

    @Test
    public void testSectionPropertyOptionsKeepVisibilitySummaryHook() throws Exception {
        String sectionJson = readResource("/properties/form/section.json");
        String sectionVisibilityEditor = readResource("/properties/form/sectionVisibilityEditor.js");

        Assert.assertTrue(sectionJson.contains("sectionVisibilitySummary"));
        Assert.assertTrue(sectionJson.contains("/web/json/app[APP_PATH]/plugin/org.joget.apps.form.model.Section/service"));
        Assert.assertTrue(sectionVisibilityEditor.contains("isSectionVisibilitySummary"));
        Assert.assertTrue(sectionVisibilityEditor.contains("renderSectionVisibilitySummaryField"));
        Assert.assertTrue(sectionVisibilityEditor.contains("openVisibilityRule"));
    }

    private Form form(String id, Element... children) {
        Form form = new Form();
        form.setProperty(FormUtil.PROPERTY_ID, id);
        form.setChildren(Arrays.asList(children));
        return form;
    }

    private Section section(String id, Element... children) {
        Section section = new Section();
        section.setProperty(FormUtil.PROPERTY_ID, id);
        section.setChildren(Arrays.asList(children));
        return section;
    }

    private TextField textField(String id) {
        TextField textField = new TextField();
        textField.setProperty(FormUtil.PROPERTY_ID, id);
        return textField;
    }

    private Map<String, Object> appliedRules(String... keys) {
        Map<String, Object> appliedRules = new HashMap<String, Object>();
        for (String key : keys) {
            appliedRules.put(key, true);
        }
        return appliedRules;
    }

    private Map<String, Object> rule(String key, String name, String control, String value,
            String regex, String join, String reverse) {
        Map<String, Object> rule = new HashMap<String, Object>();
        rule.put("visibility_key", key);
        rule.put("visibility_name", name);
        rule.put("visibilityControl", control);
        rule.put("visibilityValue", value);
        rule.put("regex", regex);
        rule.put("join", join);
        rule.put("reverse", reverse);
        return rule;
    }

    private Map<String, String> condition(String field, String value, String regex,
            String join, String reverse) {
        Map<String, String> condition = new HashMap<String, String>();
        condition.put("field", field);
        condition.put("value", value);
        condition.put("regex", regex);
        condition.put("join", join);
        condition.put("reverse", reverse);
        return condition;
    }

    private String readResource(String path) throws Exception {
        InputStream input = getClass().getResourceAsStream(path);
        Assert.assertNotNull("Missing test resource " + path, input);
        try {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
