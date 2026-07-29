package org.joget.apps.form.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class SectionVisibilityEditorJsTest {

    @Test
    public void testSectionPropertiesLoadVersionedSummaryWithoutLegacyNotice() throws Exception {
        String sectionProperties = readResource("/properties/form/section.json");

        Assert.assertTrue("Section properties should include the visibility summary editor",
                sectionProperties.contains("name : 'sectionVisibilitySummary'"));
        Assert.assertTrue("Visibility summary editor should be cache-busted by the build version",
                sectionProperties.contains("/service?version=@@build.number@@-visibility-summary-5'"));
        Assert.assertFalse("Visibility heading should not repeat the old migration notice",
                sectionProperties.contains("description : '@@form.section.visibilityNotice@@'"));
    }

    @Test
    public void testSummaryModeDoesNotValidateOrWriteData() throws Exception {
        runSectionVisibilityEditorTest(
                "editor.properties = {name: 'sectionVisibilitySummary'};\n"
                + "assertTrue(editor.isSectionVisibilitySummary(), 'summary mode should be detected');\n"
                + "assertEquals('{}', JSON.stringify(editor.getData()), 'summary mode should not write property data');\n"
                + "var errors = {};\n"
                + "editor.addOnValidation({}, errors, false);\n"
                + "assertEquals('{}', JSON.stringify(errors), 'summary mode should not add validation errors');\n");
    }

    @Test
    public void testSummaryListsLegacyAndMigratedRulesWithAffectedCount() throws Exception {
        runSectionVisibilityEditorTest(
                "var section = {\n"
                + "  className: 'org.joget.apps.form.model.Section',\n"
                + "  properties: {\n"
                + "    id: 'section1',\n"
                + "    visibilityControl: 'legacyControl',\n"
                + "    visibilityValue: 'legacyValue',\n"
                + "    visibility_rules: {rule1: true, rule2: 'true', ignored: false}\n"
                + "  },\n"
                + "  elements: []\n"
                + "};\n"
                + "CustomBuilder.data = {\n"
                + "  className: 'org.joget.apps.form.model.Form',\n"
                + "  properties: {visibility_rules: [\n"
                + "    {visibility_key: 'rule1', visibility_name: 'Rule One', visibilityControl: 'control1', visibilityValue: 'yes'},\n"
                + "    {visibility_key: 'rule2', visibility_name: 'Rule Two', visibilityControl: 'control2', visibilityValue: 'ok'},\n"
                + "    {visibility_key: 'ignored', visibility_name: 'Ignored Rule', visibilityControl: 'control3', visibilityValue: 'no'}\n"
                + "  ]},\n"
                + "  elements: [section, {className: 'org.joget.apps.form.lib.TextField', properties: {id: 'field1', visibility_rules: {rule1: true}}},\n"
                + "    {className: 'org.joget.apps.form.lib.TextField', properties: {id: 'field2', visibility_rules: {rule2: 'true'}}}]\n"
                + "};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var rules = editor.getSectionVisibilityRules();\n"
                + "assertEquals(3, rules.length, 'legacy plus two applied migrated rules should be shown');\n"
                + "assertTrue(rules[0]._pending === true, 'legacy rule should be marked pending');\n"
                + "assertEquals('pending_section1__path_0', rules[0].visibility_key, 'pending rule key should use section id and path');\n"
                + "assertEquals(2, rules[1].affectedCount, 'rule1 should count section plus field1');\n"
                + "assertEquals(2, rules[2].affectedCount, 'rule2 should count section plus field2');\n"
                + "var html = editor.renderSectionVisibilitySummaryField();\n"
                + "assertContains(html, 'Rule One', 'summary should render migrated rule names');\n"
                + "assertContains(html, '@@form.section.visibilityPendingMigration@@', 'summary should render pending badge');\n"
                + "assertContains(html, '3 rules apply to this section', 'summary should render the applied rule count');\n"
                + "assertContains(html, 'Applies to 2 elements', 'summary should render affected element count');\n"
                + "assertContains(html, 'section-visibility-open-tool', 'summary should offer access to all rules');\n");
    }

    @Test
    public void testSummaryAndAffectedCountSupportSerializedAssignments() throws Exception {
        runSectionVisibilityEditorTest(
                "var section = {className: 'org.joget.apps.form.model.Section', properties: {\n"
                + "  id: 'section1', visibility_rules: '{\"rule1\":true}'\n"
                + "}, elements: []};\n"
                + "var field = {className: 'org.joget.apps.form.lib.TextField', properties: {\n"
                + "  id: 'field1', visibility_rules: '{\"rule1\":\"true\"}'\n"
                + "}, elements: []};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: JSON.stringify([\n"
                + "  {visibility_key: 'rule1', visibility_name: 'Serialized Rule', visibilityControl: 'control1'}\n"
                + "])}, elements: [section, field]};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var rules = editor.getSectionVisibilityRules();\n"
                + "assertEquals(1, rules.length, 'serialized section assignments should appear in the summary');\n"
                + "assertEquals('rule1', rules[0].visibility_key, 'serialized assignments should resolve against serialized definitions');\n"
                + "assertEquals(2, rules[0].affectedCount, 'serialized boolean and string-boolean assignments should both be counted');\n"
                + "assertContains(editor.renderSectionVisibilitySummaryField(), 'Serialized Rule', 'the serialized assignment should render its rule card');\n");
    }

    @Test
    public void testRuleDetailsEscapeValuesAndRenderOperators() throws Exception {
        runSectionVisibilityEditorTest(
                "var html = editor.renderSectionVisibilityRuleDetails({\n"
                + "  visibilityControl: 'field1;field2;field3',\n"
                + "  visibilityValue: '<bad>;a__b;',\n"
                + "  regex: 'contains;in',\n"
                + "  join: ';or;',\n"
                + "  reverse: 'true;;'\n"
                + "});\n"
                + "assertContains(html, '&lt;bad&gt;', 'condition values should be escaped');\n"
                + "assertContains(html, '@@app.rulesdecision.contains@@', 'contains operator label should render');\n"
                + "assertContains(html, '<span class=\"visibility-condition-value\">a;b</span>', 'encoded semicolon values should be restored');\n"
                + "assertContains(html, '<span class=\"visibility-condition-reverse\">NOT</span>', 'reverse marker should render as NOT');\n"
                + "assertContains(html, '<span class=\"visibility-condition-value\">(EMPTY)</span>', 'blank comparison values should be explicit');\n"
                + "assertContains(html, 'visibility-condition-field', 'field names should have a consistent alignment hook');\n"
                + "assertContains(html, 'visibility-condition-join', 'joins should render as separators between conditions');\n");
    }

    @Test
    public void testNoRulesRendersContextualEmptyState() throws Exception {
        runSectionVisibilityEditorTest(
                "CustomBuilder.data = {properties: {}, elements: []};\n"
                + "var html = editor.renderSectionVisibilitySummaryField();\n"
                + "assertContains(html, '@@form.section.visibilityNoRules@@', 'empty state should explain that no rules apply');\n"
                + "assertContains(html, 'section-visibility-open-tool', 'empty state should link to the Visibility tool');\n"
                + "assertNotContains(html, '<div class=\"visibility-rule-card\"', 'empty state should not render a rule card');\n");
    }

    @Test
    public void testSelectedSectionIsPreferredOverMatchingPropertyId() throws Exception {
        runSectionVisibilityEditorTest(
                "var selectedSection = {className: 'org.joget.apps.form.model.Section', properties: {id: 'same', visibilityControl: 'selectedControl'}, elements: []};\n"
                + "var scannedSection = {className: 'org.joget.apps.form.model.Section', properties: {id: 'same', visibilityControl: 'scannedControl'}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [scannedSection]};\n"
                + "CustomBuilder.Builder.selectedEl = {data: {data: selectedSection}};\n"
                + "editor.options = {propertyValues: scannedSection.properties};\n"
                + "assertEquals(selectedSection, editor.getCurrentSectionData(), 'selected section should win when ids are duplicated');\n"
                + "assertEquals('selectedControl', editor.getSectionVisibilityRules()[0].visibilityControl, 'summary should use the selected section data');\n");
    }

    @Test
    public void testSelectedSectionIsResolvedToBuilderDataBeforeBuildingPendingKey() throws Exception {
        runSectionVisibilityEditorTest(
                "var first = {className: 'org.joget.apps.form.model.Section', properties: {id: 'first', visibilityControl: 'firstControl'}, elements: []};\n"
                + "var target = {className: 'org.joget.apps.form.model.Section', properties: {id: 'target', visibilityControl: 'targetControl'}, elements: []};\n"
                + "var selectedClone = {className: 'org.joget.apps.form.model.Section', properties: target.properties, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [first, target]};\n"
                + "CustomBuilder.Builder.selectedEl = {data: {data: selectedClone}};\n"
                + "editor.options = {propertyValues: target.properties};\n"
                + "assertEquals(target, editor.getCurrentSectionData(), 'selected section should resolve back to the canonical builder-data node');\n"
                + "assertEquals('pending_target__path_1', editor.getSectionVisibilityRules()[0].visibility_key, 'summary pending key should match the Visibility tool path key');\n");
    }

    @Test
    public void testNestedSectionLookupAndAffectedCount() throws Exception {
        runSectionVisibilityEditorTest(
                "var targetProps = {id: 'nestedSection', visibility_rules: {rule1: true}};\n"
                + "var target = {className: 'org.joget.apps.form.model.Section', properties: targetProps, elements: [\n"
                + "  {className: 'org.joget.apps.form.lib.TextField', properties: {visibility_rules: {rule1: 'true'}}}\n"
                + "]};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: [{visibility_key: 'rule1', visibility_name: 'Nested Rule', visibilityControl: 'control'}]}, elements: [\n"
                + "  {className: 'container', properties: {}, elements: [target, {properties: {visibility_rules: {rule1: false}}}]}\n"
                + "]};\n"
                + "editor.options = {propertyValues: {id: 'nestedSection'}};\n"
                + "assertEquals(target, editor.getCurrentSectionData(), 'section should be found recursively by id');\n"
                + "assertEquals(2, editor.countRuleAffectedElements('rule1'), 'nested true and string-true assignments should be counted');\n"
                + "assertEquals(2, editor.getSectionVisibilityRules()[0].affectedCount, 'summary should expose the recursive count');\n");
    }

    @Test
    public void testAffectedCountIsSafeOutsideCustomBuilder() throws Exception {
        runSectionVisibilityEditorTest(
                "CustomBuilder = undefined;\n"
                + "assertEquals(0, editor.countRuleAffectedElements('rule1'), 'missing builder context should return zero');\n");
    }

    @Test
    public void testDuplicateIdFallbackDoesNotGuessWrongSection() throws Exception {
        runSectionVisibilityEditorTest(
                "var first = {className: 'org.joget.apps.form.model.Section', properties: {id: 'same', visibilityControl: 'firstControl'}, elements: []};\n"
                + "var second = {className: 'org.joget.apps.form.model.Section', properties: {id: 'same', visibilityControl: 'secondControl'}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [first, second]};\n"
                + "editor.options = {propertyValues: {id: 'same', visibilityControl: 'propertyValuesControl'}};\n"
                + "assertEquals(null, editor.getCurrentSectionData(), 'ambiguous duplicate ids should not resolve to the first section');\n"
                + "assertEquals('propertyValuesControl', editor.getSectionVisibilityRules()[0].visibilityControl, 'summary should use its own property values when section lookup is ambiguous');\n");
    }

    @Test
    public void testPendingSummaryKeysMatchDuplicateAndIdLessSectionPaths() throws Exception {
        runSectionVisibilityEditorTest(
                "var duplicateA = {className: 'org.joget.apps.form.model.Section', properties: {id: 'duplicate', visibilityControl: 'firstControl'}, elements: []};\n"
                + "var duplicateB = {className: 'org.joget.apps.form.model.Section', properties: {id: 'duplicate', visibilityControl: 'secondControl'}, elements: []};\n"
                + "var blank = {className: 'org.joget.apps.form.model.Section', properties: {visibilityControl: 'blankControl'}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [duplicateA, duplicateB, blank]};\n"
                + "editor.options = {propertyValues: duplicateB.properties};\n"
                + "assertEquals('pending_duplicate__path_1', editor.getSectionVisibilityRules()[0].visibility_key, 'duplicate ids should include the section path');\n"
                + "editor.options = {propertyValues: blank.properties};\n"
                + "assertEquals('pending_section__path_2', editor.getSectionVisibilityRules()[0].visibility_key, 'blank ids should include the section path');\n");
    }

    @Test
    public void testAlreadyMigratedSectionIsNotShownAsPending() throws Exception {
        // A section marked as migrated must not be re-advertised as pending by the summary,
        // matching the Visibility tool's detection guard (visibility.js scanElementsForDetection).
        runSectionVisibilityEditorTest(
                "var section = {className: 'org.joget.apps.form.model.Section', properties: {\n"
                + "  id: 'section1', visibilityControl: 'legacyControl', _visibility_migrated: true,\n"
                + "  visibility_rules: {rule1: true}\n"
                + "}, elements: []};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: [\n"
                + "  {visibility_key: 'rule1', visibility_name: 'Migrated Rule', visibilityControl: 'control'}\n"
                + "]}, elements: [section]};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var rules = editor.getSectionVisibilityRules();\n"
                + "assertEquals(1, rules.length, 'a migrated section should only show its real rule, not a pending duplicate');\n"
                + "assertEquals('rule1', rules[0].visibility_key, 'the migrated rule should be the one rendered');\n"
                + "assertTrue(rules[0]._pending !== true, 'a migrated section must not render a pending badge');\n");
    }

    @Test
    public void testStaleAndFalseRuleAssignmentsAreIgnored() throws Exception {
        runSectionVisibilityEditorTest(
                "var section = {className: 'org.joget.apps.form.model.Section', properties: {\n"
                + "  id: 'section1', visibility_rules: {active: true, stale: true, disabled: false, disabledString: 'false'}\n"
                + "}, elements: []};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: [\n"
                + "  {visibility_key: 'active', visibility_name: 'Active Rule', visibilityControl: 'control'}\n"
                + "]}, elements: [section]};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var rules = editor.getSectionVisibilityRules();\n"
                + "assertEquals(1, rules.length, 'only an enabled assignment with a definition should render');\n"
                + "assertEquals('active', rules[0].visibility_key, 'stale and false assignments should be ignored');\n");
    }

    @Test
    public void testSummaryEscapesRuleNamesAndAttributeValues() throws Exception {
        runSectionVisibilityEditorTest(
                "var section = {className: 'org.joget.apps.form.model.Section', properties: {id: 'section1', visibility_rules: {'rule\\\" onclick=\\\"bad': true}}, elements: []};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: [{\n"
                + "  visibility_key: 'rule\\\" onclick=\\\"bad', visibility_name: '<img src=x onerror=bad>', visibilityControl: 'control'\n"
                + "}]}, elements: [section]};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var html = editor.renderSectionVisibilitySummaryField();\n"
                + "assertContains(html, '&lt;img src=x onerror=bad&gt;', 'rule name should be escaped');\n"
                + "assertContains(html, 'rule&quot; onclick=&quot;bad', 'rule key should be escaped in attributes');\n"
                + "assertNotContains(html, '<img src=x', 'raw rule-name markup must not be emitted');\n");
    }

    @Test
    public void testEscapeHtmlFallbackEscapesQuotes() throws Exception {
        runSectionVisibilityEditorTest(
                "PropertyEditor = undefined;\n"
                + "var escaped = editor.escapeHtml('<tag attr=\"bad\" data=\\'x\\'>&');\n"
                + "assertContains(escaped, '&lt;tag', 'fallback should escape opening tags');\n"
                + "assertContains(escaped, 'attr=&quot;bad&quot;', 'fallback should escape double quotes');\n"
                + "assertContains(escaped, 'data=&#39;x&#39;', 'fallback should escape single quotes');\n"
                + "assertContains(escaped, '&amp;', 'fallback should escape ampersands');\n");
    }

    @Test
    public void testSummaryUsesAccessibleActionsAndThemeTokens() throws Exception {
        runSectionVisibilityEditorTest(
                "var section = {className: 'org.joget.apps.form.model.Section', properties: {id: 'section1', visibility_rules: {rule1: true}}, elements: []};\n"
                + "CustomBuilder.data = {properties: {visibility_rules: [{visibility_key: 'rule1', visibility_name: 'Rule One', visibilityControl: 'control'}]}, elements: [section]};\n"
                + "editor.options = {propertyValues: section.properties};\n"
                + "var html = editor.renderSectionVisibilitySummaryField();\n"
                + "assertContains(html, '<button type=\"button\" class=\"visibility-action section-visibility-open-rule\"', 'edit action should use a semantic button');\n"
                + "assertContains(html, '<button type=\"button\" class=\"visibility-action section-visibility-open-tool\"', 'manage action should use a semantic button');\n"
                + "assertContains(html, 'var(--theme-primary-color-1,#fff)', 'surface should follow the active builder theme');\n"
                + "assertContains(html, 'var(--theme-label-color-2,#252f4a)', 'text should follow the active builder theme');\n"
                + "assertContains(html, '.visibility-condition-list{display:grid;grid-template-columns:minmax(72px,max-content) minmax(0,1fr)', 'condition columns should align and stay compact at normal drawer widths');\n"
                + "assertContains(html, 'visibility-condition-expression', 'operator and value should remain visually grouped');\n"
                + "assertContains(html, '@container visibility-summary (max-width:260px)', 'only exceptionally narrow drawers should stack condition details');\n"
                + "assertContains(html, 'body[builder-theme=\"dark\"]', 'dark theme should receive a readable migration badge');\n");
    }

    @Test
    public void testConditionEditorUsesLegacyLayout() throws Exception {
        runSectionVisibilityEditorTest(
                "editor.properties = {name: 'visibilityControl'};\n"
                + "var html = editor.renderField();\n"
                + "assertContains(html, '.visibilitywrapper .col_inputs{display: flex;flex-wrap: wrap;}', 'condition controls should retain the original flexible layout');\n"
                + "assertContains(html, '.visibilitywrapper .perow.condition .buttons{position:absolute;', 'condition actions should retain their original positioning');\n"
                + "assertContains(html, 'body.rtl .visibilitywrapper .perow.condition .buttons{right: unset; left:5px;}', 'condition actions should retain RTL positioning');\n");
    }

    @Test
    public void testRuleAndElementCountsUseReadableSingularAndPluralText() throws Exception {
        runSectionVisibilityEditorTest(
                "assertEquals('1 rule applies to this section', editor.getVisibilityRuleCountText(1), 'one rule should use singular text');\n"
                + "assertEquals('3 rules apply to this section', editor.getVisibilityRuleCountText(3), 'multiple rules should use plural text');\n"
                + "assertEquals('Applies to 1 element', editor.getVisibilityRuleUsageText(1), 'one affected element should use singular text');\n"
                + "assertEquals('Applies to 4 elements', editor.getVisibilityRuleUsageText(4), 'multiple affected elements should use plural text');\n");
    }

    @Test
    public void testRuleDetailsHandleNoConditionsAndAllOperators() throws Exception {
        runSectionVisibilityEditorTest(
                "var emptyHtml = editor.renderSectionVisibilityRuleDetails({visibilityControl: '(;)'});\n"
                + "assertContains(emptyHtml, '@@form.section.visibilityNoConditions@@', 'empty groups should have a readable fallback');\n"
                + "var operators = ['', '>', '>=', '<', '<=', 'isTrue', 'isFalse', 'contains', 'listContains', 'in', 'true', 'unknown'];\n"
                + "var expected = ['@@pbuilder.label.equalTo@@', '@@pbuilder.label.greaterThan@@', '@@pbuilder.label.greaterThanOrEqualTo@@',\n"
                + "  '@@pbuilder.label.lessThan@@', '@@pbuilder.label.lessThanOrEqualTo@@', '@@pbuilder.label.isTrue@@',\n"
                + "  '@@pbuilder.label.isFalse@@', '@@app.rulesdecision.contains@@', '@@app.rulesdecision.listContains@@',\n"
                + "  '@@app.rulesdecision.in@@', '@@app.rulesdecision.regex@@', '@@pbuilder.label.equalTo@@'];\n"
                + "for (var i = 0; i < operators.length; i++) {\n"
                + "  assertEquals(expected[i], editor.getVisibilityOperationLabel(operators[i]), 'operator label should remain compatible for ' + operators[i]);\n"
                + "}\n");
    }

    @Test
    public void testRuleDetailsRenderGroupsAsNestedBoxes() throws Exception {
        runSectionVisibilityEditorTest(
                "var html = editor.renderSectionVisibilityRuleDetails({\n"
                + "  visibilityControl: 'fieldA;(;fieldB;fieldC;);fieldD',\n"
                + "  visibilityValue: 'yes;;one;two;;done',\n"
                + "  regex: ';;;;;',\n"
                + "  join: ';or;;or;;and',\n"
                + "  reverse: ';true;;;;'\n"
                + "});\n"
                + "assertContains(html, 'visibility-condition-group-box', 'grouped conditions should render inside an inset box');\n"
                + "assertNotContains(html, 'visibility-condition-group open', 'opening parentheses should not be exposed in the summary');\n"
                + "assertNotContains(html, 'visibility-condition-group close', 'closing parentheses should not be exposed in the summary');\n"
                + "assertContains(html, '@@app.rulesdecision.or@@', 'group boundary join should be shown');\n"
                + "assertContains(html, '<span class=\"visibility-condition-reverse\">NOT</span>', 'group reverse marker should be shown as NOT');\n");
    }

    @Test
    public void testRuleDetailsRenderNestedAndMalformedGroupsSafely() throws Exception {
        runSectionVisibilityEditorTest(
                "var nestedHtml = editor.renderSectionVisibilityRuleDetails({\n"
                + "  visibilityControl: '(;fieldA;(;fieldB;);)',\n"
                + "  visibilityValue: ';one;;two;',\n"
                + "  regex: ';;;;',\n"
                + "  join: ';;;;',\n"
                + "  reverse: ';;;;'\n"
                + "});\n"
                + "assertEquals(2, nestedHtml.split('class=\"visibility-condition-group-box\"').length - 1, 'nested groups should render as nested boxes');\n"
                + "var malformedHtml = editor.renderSectionVisibilityRuleDetails({visibilityControl: ');fieldA;('});\n"
                + "assertContains(malformedHtml, 'fieldA', 'conditions should survive unmatched legacy group tokens');\n"
                + "assertNotContains(malformedHtml, '>(<', 'unmatched group tokens should not leak punctuation into the summary');\n");
    }

    @Test
    public void testRuleDetailsRejectExcessiveNestingWithoutRecursiveRendering() throws Exception {
        runSectionVisibilityEditorTest(
                "var controls = [];\n"
                + "for (var i = 0; i < 1000; i++) { controls.push('('); }\n"
                + "controls.push('fieldA');\n"
                + "for (var j = 0; j < 1000; j++) { controls.push(')'); }\n"
                + "var html = editor.renderSectionVisibilityRuleDetails({visibilityControl: controls.join(';')});\n"
                + "assertContains(html, '@@form.section.visibilityNoConditions@@', 'unsafe nesting should use the safe empty-state summary');\n");
    }

    @Test
    public void testOpenVisibilityRuleDelegatesAndFallsBackToToolButton() throws Exception {
        runSectionVisibilityEditorTest(
                "var openedKey = null;\n"
                + "var openedEdit = null;\n"
                + "VisibilityManager.openRule = function(key, edit) { openedKey = key; openedEdit = edit; };\n"
                + "editor.openVisibilityRule('rule1', true);\n"
                + "assertEquals('rule1', openedKey, 'rule key should be delegated to VisibilityManager');\n"
                + "assertTrue(openedEdit, 'edit mode should be delegated to VisibilityManager');\n"
                + "editor.openSectionVisibilityRule('pending_section1__path_0');\n"
                + "assertEquals('pending_section1__path_0', openedKey, 'pending rule should be delegated by key');\n"
                + "assertTrue(openedEdit === false, 'pending rule should only be highlighted, not edited');\n"
                + "editor.openSectionVisibilityRule('rule2');\n"
                + "assertEquals('rule2', openedKey, 'migrated rule should be delegated by key');\n"
                + "assertTrue(openedEdit, 'migrated rule should still open for editing');\n"
                + "delete VisibilityManager.openRule;\n"
                + "editor.openVisibilityRule(null, false);\n"
                + "assertEquals(4, propertyCloseChecks, 'opening the Visibility tool should check for unsaved property changes first');\n"
                + "assertEquals(1, enhancedToolsCount, 'fallback should reveal Advanced Tools before clicking its hidden button');\n"
                + "assertEquals(1, visibilityButtonClicks, 'fallback should still open the Visibility tool');\n");
    }

    private void runSectionVisibilityEditorTest(String testScript) throws Exception {
        String source = readResource("/properties/form/sectionVisibilityEditor.js");
        source = source.replace("@@form.section.visibilityRuleCountOne@@", "1 rule applies to this section");
        source = source.replace("@@form.section.visibilityRuleCountMany@@", "{0} rules apply to this section");
        source = source.replace("@@form.section.visibilityUsedByOne@@", "Applies to 1 element");
        source = source.replace("@@form.section.visibilityUsedByMany@@", "Applies to {0} elements");
        source = source.replace("@@form.section.visibilityNot@@", "NOT");
        source = source.replace("@@form.section.visibilityEmptyValue@@", "(EMPTY)");
        String script = getBrowserStubs()
                + "var editor = (" + source + ");\n"
                + "editor.id = 'sectionVisibilitySummary';\n"
                + "editor.properties = {name: 'sectionVisibilitySummary'};\n"
                + "editor.options = {propertyValues: {}};\n"
                + testScript;

        Context context = Context.enter();
        try {
            Scriptable scope = context.initStandardObjects();
            context.evaluateString(scope, script, "SectionVisibilityEditorJsTest", 1, null);
        } finally {
            Context.exit();
        }
    }

    private String getBrowserStubs() {
        return "function assertTrue(value, message) { if (!value) { throw new Error(message); } }\n"
                + "function assertEquals(expected, actual, message) { if (expected !== actual) { throw new Error(message + ': expected [' + expected + '] but was [' + actual + ']'); } }\n"
                + "function assertContains(value, expected, message) { if (value.indexOf(expected) < 0) { throw new Error(message + ': missing [' + expected + '] in [' + value + ']'); } }\n"
                + "function assertNotContains(value, expected, message) { if (value.indexOf(expected) >= 0) { throw new Error(message + ': found [' + expected + '] in [' + value + ']'); } }\n"
                + "if (!String.prototype.replaceAll) { String.prototype.replaceAll = function(search, replacement) { return this.split(search).join(replacement); }; }\n"
                + "var visibilityButtonClicks = 0;\n"
                + "var enhancedToolsCount = 0;\n"
                + "var propertyCloseChecks = 0;\n"
                + "var CustomBuilder = {Builder: {selectedEl: null}, data: null, enableEnhancedTools: function() { enhancedToolsCount++; }, checkChangeBeforeCloseElementProperties: function(callback) { propertyCloseChecks++; callback(); }};\n"
                + "var VisibilityManager = {getRuleElement: function() { return CustomBuilder.data.properties; }};\n"
                + "var PropertyEditor = {Util: {escapeHtmlTag: function(value) {\n"
                + "  value = value === undefined || value === null ? '' : String(value);\n"
                + "  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#39;');\n"
                + "}}};\n"
                + "function JQueryWrapper(value) { this.value = value; this.length = value ? 1 : 0; }\n"
                + "JQueryWrapper.prototype.data = function(name) { return this.value && this.value.data ? this.value.data[name] : null; };\n"
                + "JQueryWrapper.prototype.trigger = function(name) { if (this.value === '#visibility-btn' && name === 'click') { visibilityButtonClicks++; } return this; };\n"
                + "function $(value) { return new JQueryWrapper(value); }\n"
                + "$.each = function(value, callback) {\n"
                + "  if (!value) { return; }\n"
                + "  if (typeof value.length === 'number') { for (var i = 0; i < value.length; i++) { callback.call(value[i], i, value[i]); } }\n"
                + "  else { for (var key in value) { if (Object.prototype.hasOwnProperty.call(value, key)) { callback.call(value[key], key, value[key]); } } }\n"
                + "};\n"
                + "$.extend = function(target, source) { for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } return target; };\n";
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
