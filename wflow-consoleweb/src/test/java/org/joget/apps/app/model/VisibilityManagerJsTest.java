package org.joget.apps.app.model;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class VisibilityManagerJsTest {

    @Test
    public void testConditionEditorScriptUrlIsCacheBusted() throws Exception {
        String source = readVisibilityJs();
        Assert.assertTrue("Visibility rule condition editor should use the runtime build number.",
                source.contains("/service?version=' + CustomBuilder.buildNumber + '-visibility-summary-5"));
        Assert.assertFalse("Static JavaScript must not ship an unprocessed build-number token.",
                source.contains("@@build.number@@"));
    }

    @Test
    public void testDynamicLabelsAndRuleTooltipsUseTextNodes() throws Exception {
        String source = readVisibilityJs();
        Assert.assertTrue(source.contains("$('<span class=\"element-label\"></span>').text(label).appendTo($container)"));
        Assert.assertTrue(source.contains("$('<span class=\"element-id\"></span>').text(props[\"id\"]).appendTo($container)"));
        Assert.assertTrue(source.contains("$('<li></li>').attr('data-rule-key', rule.key).text(rule.name).appendTo($list)"));
        Assert.assertFalse("Rule names must not be concatenated into tooltip HTML.",
                source.contains("$list.append('<li data-rule-key=\"' + rule.key + '\">' + rule.name + '</li>')"));
    }

    @Test
    public void testPostMigrationApplyBypassesPendingMigrationExactlyOnce() throws Exception {
        String source = readVisibilityJs();
        Assert.assertTrue("The delegated Apply handler should accept the one-shot migration bypass.",
                source.contains("function(event, skipPendingMigration)"));
        Assert.assertTrue("Pending migration should be skipped only when the explicit bypass is set.",
                source.contains("if (!skipPendingMigration && self.pendingLegacy && self.pendingLegacy.length > 0)"));
        Assert.assertTrue("The post-migration apply should explicitly request the bypass.",
                source.contains("self.applyRuleToElementRow(elementData, desired, true)"));
        Assert.assertTrue("The synthetic click should forward the bypass into the delegated handler.",
                source.contains("$btn.trigger(\"click\", [skipPendingMigration === true])"));
    }

    @Test
    public void testOpenRuleFocusesAndEditsMatchingRuleCard() throws Exception {
        runVisibilityManagerTest(
                "var rule1 = ruleCard('rule1', 10);\n"
                + "var rule2 = ruleCard('rule2', 60);\n"
                + "VisibilityManager.container = container([rule1, rule2]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('rule2', true);\n"
                + "assertEquals(1, clickCount, 'opening a rule should click the Visibility tool button');\n"
                + "flushTimers(1);\n"
                + "assertEquals('rule2', activeKey, 'matching rule should become active');\n"
                + "assertEquals('rule2', editedKey, 'matching rule should open in the property editor');\n"
                + "assertEquals(70, animateScrollTop, 'rules pane should scroll the matching card into view');\n");
    }

    @Test
    public void testOpenRuleRetriesUntilRuleCardIsRendered() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('lateRule', true);\n"
                + "flushTimers(1);\n"
                + "assertEquals(null, activeKey, 'missing rule should not be focused before retry');\n"
                + "VisibilityManager.container.rules.push(ruleCard('lateRule', 35));\n"
                + "flushTimers(1);\n"
                + "assertEquals('lateRule', activeKey, 'rule should be focused after it appears');\n"
                + "assertEquals('lateRule', editedKey, 'rule should be edited after it appears');\n");
    }

    @Test
    public void testOpenRuleWithoutKeyOnlyOpensTool() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([ruleCard('rule1', 10)]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule(null, false);\n"
                + "assertEquals(1, enhancedToolsCount, 'opening should reveal Advanced Tools before clicking its hidden Visibility button');\n"
                + "assertEquals(1, clickCount, 'opening without a key should click the Visibility tool button');\n"
                + "flushTimers(1);\n"
                + "assertEquals(null, activeKey, 'no rule should be focused without a key');\n"
                + "assertEquals(null, editedKey, 'no rule should be edited without a key');\n");
    }

    @Test
    public void testOpenRuleCanFocusWithoutEditing() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([ruleCard('rule1', 10)]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('rule1', false);\n"
                + "flushTimers(1);\n"
                + "assertEquals('rule1', activeKey, 'matching rule should become active');\n"
                + "assertEquals(null, editedKey, 'focus-only mode should not open the property editor');\n");
    }

    @Test
    public void testOpenRuleCanResolveUniquePendingBaseKey() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([ruleCard('pending_sectionA__path_0', 25)]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('pending_sectionA', true);\n"
                + "flushTimers(1);\n"
                + "assertEquals('pending_sectionA__path_0', activeKey, 'summary base key should focus a unique path-qualified pending card');\n"
                + "assertEquals('pending_sectionA__path_0', editedKey, 'summary base key should edit a unique path-qualified pending card');\n");
    }

    @Test
    public void testOpenRuleDoesNotResolveAmbiguousPendingBaseKey() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([ruleCard('pending_sectionA__path_0', 25), ruleCard('pending_sectionA__path_1', 55)]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.editRule = function($rule) { editedKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('pending_sectionA', true);\n"
                + "flushTimers(25);\n"
                + "assertEquals(null, activeKey, 'ambiguous pending base keys should not focus the wrong card');\n"
                + "assertEquals(null, editedKey, 'ambiguous pending base keys should not edit the wrong card');\n"
                + "assertEquals('message:adv.visibility.ruleNotFound', messageShown, 'ambiguous keys should produce a visible fallback');\n");
    }

    @Test
    public void testOpenRuleStopsRetryingWhenRuleDoesNotExist() throws Exception {
        runVisibilityManagerTest(
                "VisibilityManager.container = container([]);\n"
                + "VisibilityManager.setActiveRule = function($rule) { activeKey = $rule.data('key'); };\n"
                + "VisibilityManager.openRule('deletedRule', true);\n"
                + "flushTimers(25);\n"
                + "assertEquals(0, timers.length, 'retry queue should stop after its bounded attempts');\n"
                + "assertEquals(null, activeKey, 'a deleted rule should never be focused');\n");
    }

    @Test
    public void testDetectLegacyRulesIsRecursiveAndDoesNotMutateForm() throws Exception {
        runVisibilityManagerTest(
                "var legacyProps = {\n"
                + "  id: 'sectionA', visibilityControl: 'field1;field2', visibilityValue: 'yes;ok',\n"
                + "  regex: ';contains', join: ';or', reverse: 'true;'\n"
                + "};\n"
                + "var legacy = {className: 'org.joget.apps.form.model.Section', properties: legacyProps, elements: []};\n"
                + "var migrated = {className: 'org.joget.apps.form.model.Section', properties: {id: 'done', visibilityControl: 'field3', _visibility_migrated: true}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [{className: 'container', properties: {}, elements: [legacy, migrated]}]};\n"
                + "var before = JSON.stringify(CustomBuilder.data);\n"
                + "VisibilityManager.detectLegacyRules();\n"
                + "assertEquals(1, VisibilityManager.pendingLegacy.length, 'only unmigrated legacy sections should be detected');\n"
                + "assertEquals('pending_sectionA__path_0_0', VisibilityManager.pendingLegacy[0].visibility_key, 'pending key should identify its section and path');\n"
                + "assertEquals('field1;field2', VisibilityManager.pendingLegacy[0].visibilityControl, 'pending preview should preserve conditions');\n"
                + "assertEquals(legacy, VisibilityManager.pendingLegacy[0]._sectionRef, 'pending rule should retain its source section');\n"
                + "assertEquals(before, JSON.stringify(CustomBuilder.data), 'detection/opening must not migrate or dirty the form');\n");
    }

    @Test
    public void testLegacySectionWithoutIdUsesDeterministicPendingKey() throws Exception {
        // The pending key must be derived deterministically (not from a random guid) so the Section
        // property summary can reopen the matching pending card. Must match the fallback used by
        // sectionVisibilityEditor.js getSectionVisibilityRules().
        runVisibilityManagerTest(
                "var legacy = {className: 'org.joget.apps.form.model.Section', properties: {visibilityControl: 'field1'}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [legacy]};\n"
                + "VisibilityManager.generateGuid = function() { throw new Error('id-less sections must not depend on a random guid'); };\n"
                + "VisibilityManager.detectLegacyRules();\n"
                + "assertEquals(1, VisibilityManager.pendingLegacy.length, 'the id-less legacy section should still be detected');\n"
                + "assertEquals('pending_section__path_0', VisibilityManager.pendingLegacy[0].visibility_key, 'id-less sections should use a deterministic path key');\n");
    }

    @Test
    public void testDuplicateAndIdLessLegacySectionsReceiveDistinctPendingKeys() throws Exception {
        runVisibilityManagerTest(
                "var firstDuplicate = {className: 'org.joget.apps.form.model.Section', properties: {id: 'duplicate', visibilityControl: 'field1'}, elements: []};\n"
                + "var secondDuplicate = {className: 'org.joget.apps.form.model.Section', properties: {id: 'duplicate', visibilityControl: 'field2'}, elements: []};\n"
                + "var firstBlank = {className: 'org.joget.apps.form.model.Section', properties: {visibilityControl: 'field3'}, elements: []};\n"
                + "var secondBlank = {className: 'org.joget.apps.form.model.Section', properties: {visibilityControl: 'field4'}, elements: []};\n"
                + "CustomBuilder.data = {properties: {}, elements: [firstDuplicate, secondDuplicate, firstBlank, secondBlank]};\n"
                + "VisibilityManager.detectLegacyRules();\n"
                + "assertEquals(4, VisibilityManager.pendingLegacy.length, 'all legacy sections should be detected');\n"
                + "assertEquals('pending_duplicate__path_0', VisibilityManager.pendingLegacy[0].visibility_key, 'first duplicate id should include its path');\n"
                + "assertEquals('pending_duplicate__path_1', VisibilityManager.pendingLegacy[1].visibility_key, 'second duplicate id should include its path');\n"
                + "assertEquals('pending_section__path_2', VisibilityManager.pendingLegacy[2].visibility_key, 'first blank id should include its path');\n"
                + "assertEquals('pending_section__path_3', VisibilityManager.pendingLegacy[3].visibility_key, 'second blank id should include its path');\n");
    }

    @Test
    public void testMigrateLegacyRulesPreservesDataAndIsIdempotent() throws Exception {
        runVisibilityManagerTest(
                "var existingRule = {visibility_key: 'existingRule', visibility_name: 'Existing'};\n"
                + "var legacyProps = {\n"
                + "  id: 'sectionA', visibilityControl: '(;field1;field2;)', visibilityValue: ';yes;ok;',\n"
                + "  regex: ';;contains;', join: ';;or;', reverse: ';;true;',\n"
                + "  visibility_rules: {existingRule: true}, unrelated: 'keep-me'\n"
                + "};\n"
                + "var legacy = {className: 'org.joget.apps.form.model.Section', properties: legacyProps, elements: []};\n"
                + "var rootProps = {visibility_rules: [existingRule]};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [{className: 'container', properties: {}, elements: [legacy]}]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "VisibilityManager.generateGuid = function() { return 'newRule'; };\n"
                + "VisibilityManager.migrateLegacyRules();\n"
                + "assertEquals(1, updateCount, 'a completed migration should dirty the builder once');\n"
                + "assertEquals(2, rootProps.visibility_rules.length, 'existing form rules should be preserved');\n"
                + "var migratedRule = rootProps.visibility_rules[1];\n"
                + "assertEquals('newRule', migratedRule.visibility_key, 'migration should use the generated rule key');\n"
                + "assertEquals('sectionA [migrated]', migratedRule.visibility_name, 'default section id should remain recognizable');\n"
                + "assertEquals('(;field1;field2;)', migratedRule.visibilityControl, 'group structure should be copied exactly');\n"
                + "assertEquals(';yes;ok;', migratedRule.visibilityValue, 'condition values should be copied exactly');\n"
                + "assertEquals(';;contains;', migratedRule.regex, 'operators should be copied exactly');\n"
                + "assertEquals(';;or;', migratedRule.join, 'joins should be copied exactly');\n"
                + "assertEquals(';;true;', migratedRule.reverse, 'reverse flags should be copied exactly');\n"
                + "assertTrue(legacyProps.visibility_rules.existingRule, 'existing section assignments should remain');\n"
                + "assertTrue(legacyProps.visibility_rules.newRule, 'new rule should apply to its source section');\n"
                + "assertEquals(undefined, legacyProps.visibilityControl, 'legacy controls should be cleared after migration');\n"
                + "assertTrue(legacyProps._visibility_migrated, 'section should be marked as migrated');\n"
                + "assertEquals('keep-me', legacyProps.unrelated, 'unrelated section properties should remain untouched');\n"
                + "VisibilityManager.migrateLegacyRules();\n"
                + "assertEquals(1, updateCount, 'repeated migration should not dirty the form again');\n"
                + "assertEquals(2, rootProps.visibility_rules.length, 'repeated migration should not duplicate rules');\n");
    }

    @Test
    public void testMigrateLegacyRulesNormalizesSerializedAndNullAssignments() throws Exception {
        runVisibilityManagerTest(
                "var existingRule = {visibility_key: 'existingRule', visibility_name: 'Existing'};\n"
                + "var stringProps = {id: 'stringSection', visibilityControl: 'field1', visibility_rules: '{\"existingRule\":true}'};\n"
                + "var nullProps = {id: 'nullSection', visibilityControl: 'field2', visibility_rules: null};\n"
                + "var stringSection = {className: 'org.joget.apps.form.model.Section', properties: stringProps, elements: []};\n"
                + "var nullSection = {className: 'org.joget.apps.form.model.Section', properties: nullProps, elements: []};\n"
                + "var rootProps = {visibility_rules: JSON.stringify([existingRule])};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [stringSection, nullSection]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "var guidIndex = 0;\n"
                + "VisibilityManager.generateGuid = function() { guidIndex++; return 'newRule' + guidIndex; };\n"
                + "var migratedCount = VisibilityManager.migrateLegacyRules();\n"
                + "assertEquals(2, migratedCount, 'both supported persisted formats should migrate');\n"
                + "assertEquals(1, updateCount, 'the builder should be dirtied once');\n"
                + "assertEquals(3, rootProps.visibility_rules.length, 'serialized form rules should be preserved and extended');\n"
                + "assertTrue(stringProps.visibility_rules.existingRule, 'serialized existing assignments should be preserved');\n"
                + "assertTrue(stringProps.visibility_rules.newRule1, 'the first migrated rule should be assigned');\n"
                + "assertTrue(nullProps.visibility_rules.newRule2, 'a null mapping should normalize to an empty mapping');\n");
    }

    @Test
    public void testMalformedSerializedAssignmentsArePreservedForRecovery() throws Exception {
        runVisibilityManagerTest(
                "var legacyProps = {id: 'sectionA', visibilityControl: 'field1', visibility_rules: '{not-json'};\n"
                + "var legacy = {className: 'org.joget.apps.form.model.Section', properties: legacyProps, elements: []};\n"
                + "var rootProps = {visibility_rules: []};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [legacy]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "assertEquals(0, VisibilityManager.migrateLegacyRules(), 'malformed assignments should not be partially migrated');\n"
                + "assertEquals(0, updateCount, 'a skipped migration should not dirty the builder');\n"
                + "assertEquals('{not-json', legacyProps.visibility_rules, 'malformed persisted data should not be overwritten');\n"
                + "assertEquals('field1', legacyProps.visibilityControl, 'legacy controls should remain available for recovery');\n"
                + "assertEquals(0, rootProps.visibility_rules.length, 'no duplicate global rule should be created');\n");
    }

    @Test
    public void testDoMigrationMapsPendingRuleToCreatedRule() throws Exception {
        runVisibilityManagerTest(
                "var legacyProps = {id: 'sectionA', visibilityControl: 'field1', visibility_rules: '{\"existingRule\":true}'};\n"
                + "var legacy = {className: 'org.joget.apps.form.model.Section', properties: legacyProps, elements: []};\n"
                + "var rootProps = {visibility_rules: [{visibility_key: 'existingRule'}]};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [legacy]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "VisibilityManager.generateGuid = function() { return 'createdRule'; };\n"
                + "VisibilityManager.container = container([]);\n"
                + "VisibilityManager.renderMigrationNotice = function() {};\n"
                + "VisibilityManager.renderRules = function() {};\n"
                + "VisibilityManager.updateTotalRuleCount = function() {};\n"
                + "VisibilityManager.hideEmptyState = function() {};\n"
                + "VisibilityManager.renderEmptyState = function() {};\n"
                + "VisibilityManager.detectLegacyRules();\n"
                + "var pendingKey = VisibilityManager.pendingLegacy[0].visibility_key;\n"
                + "var completedMapping = null;\n"
                + "VisibilityManager.doMigration(function(mapping) { completedMapping = mapping; });\n"
                + "assertEquals('createdRule', completedMapping[pendingKey], 'pending interactions should retarget the created rule');\n"
                + "assertEquals(0, VisibilityManager.pendingLegacy.length, 'successfully migrated previews should clear');\n"
                + "assertEquals(1, updateCount, 'orchestrated migration should dirty the builder once');\n"
                + "assertEquals('message:adv.visibility.migrationDone', messageShown, 'successful migration should be reported');\n");
    }

    @Test
    public void testDoMigrationKeepsUnmigratedPreviewWhenPersistedDataIsMalformed() throws Exception {
        runVisibilityManagerTest(
                "var legacyProps = {id: 'sectionA', visibilityControl: 'field1', visibility_rules: '{not-json'};\n"
                + "var legacy = {className: 'org.joget.apps.form.model.Section', properties: legacyProps, elements: []};\n"
                + "var rootProps = {visibility_rules: []};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [legacy]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "VisibilityManager.container = container([]);\n"
                + "VisibilityManager.renderMigrationNotice = function() {};\n"
                + "VisibilityManager.renderRules = function() {};\n"
                + "VisibilityManager.updateTotalRuleCount = function() {};\n"
                + "VisibilityManager.hideEmptyState = function() {};\n"
                + "VisibilityManager.renderEmptyState = function() {};\n"
                + "VisibilityManager.detectLegacyRules();\n"
                + "var pendingKey = VisibilityManager.pendingLegacy[0].visibility_key;\n"
                + "var completedMapping = null;\n"
                + "VisibilityManager.doMigration(function(mapping) { completedMapping = mapping; });\n"
                + "assertEquals(null, completedMapping[pendingKey], 'a skipped migration must not map to an unrelated rule');\n"
                + "assertEquals(1, VisibilityManager.pendingLegacy.length, 'the recovery warning should remain visible');\n"
                + "assertEquals(0, updateCount, 'a skipped migration should not dirty the builder');\n"
                + "assertEquals(null, messageShown, 'a skipped migration must not claim success');\n");
    }

    @Test
    public void testNormalizationDoesNotMutateRenderedProperties() throws Exception {
        runVisibilityManagerTest(
                "var persisted = {rule1: true};\n"
                + "var normalized = VisibilityManager.normalizeVisibilityAssignments(persisted);\n"
                + "normalized.rule2 = true;\n"
                + "assertEquals(undefined, persisted.rule2, 'read-only normalization should not mutate live element properties');\n"
                + "assertTrue(normalized.rule1, 'existing assignments should be retained');\n");
    }

    @Test
    public void testMigrateLegacyRulesWithNoPendingDataDoesNotDirtyBuilder() throws Exception {
        runVisibilityManagerTest(
                "var rootProps = {visibility_rules: []};\n"
                + "CustomBuilder.data = {properties: rootProps, elements: [\n"
                + "  {className: 'org.joget.apps.form.model.Section', properties: {id: 'plain'}, elements: []},\n"
                + "  {className: 'org.joget.apps.form.model.Section', properties: {id: 'done', visibilityControl: 'field1', _visibility_migrated: true}, elements: []}\n"
                + "]};\n"
                + "VisibilityManager.getRuleElement = function() { return rootProps; };\n"
                + "VisibilityManager.migrateLegacyRules();\n"
                + "assertEquals(0, updateCount, 'no-op migration should not dirty the builder');\n"
                + "assertEquals(0, rootProps.visibility_rules.length, 'no-op migration should not create rules');\n");
    }

    private void runVisibilityManagerTest(String testScript) throws Exception {
        String source = readVisibilityJs();
        String script = getBrowserStubs() + source + "\n" + testScript;

        Context context = Context.enter();
        try {
            Scriptable scope = context.initStandardObjects();
            context.evaluateString(scope, script, "VisibilityManagerJsTest", 1, null);
        } finally {
            Context.exit();
        }
    }

    private String getBrowserStubs() {
        return "function assertTrue(value, message) { if (!value) { throw new Error(message); } }\n"
                + "function assertEquals(expected, actual, message) { if (expected !== actual) { throw new Error(message + ': expected [' + expected + '] but was [' + actual + ']'); } }\n"
                + "var timers = [];\n"
                + "var clickCount = 0;\n"
                + "var enhancedToolsCount = 0;\n"
                + "var updateCount = 0;\n"
                + "var activeKey = null;\n"
                + "var editedKey = null;\n"
                + "var animateScrollTop = null;\n"
                + "var messageShown = null;\n"
                + "var CustomBuilder = {data: null, buildNumber: 'test-build', update: function() { updateCount++; }, showMessage: function(message) { messageShown = message; }, enableEnhancedTools: function() { enhancedToolsCount++; }};\n"
                + "function get_advtool_msg(key) { return 'message:' + key; }\n"
                + "function setTimeout(callback, delay) { timers.push(callback); return timers.length; }\n"
                + "function flushTimers(max) { while (timers.length > 0 && max > 0) { max--; timers.shift()(); } }\n"
                + "function ruleCard(key, top) { return {type: 'rule', key: key, top: top}; }\n"
                + "function container(rules) { return {type: 'container', rules: rules, pane: {type: 'pane', top: 10, scroll: 20}}; }\n"
                + "function JQueryWrapper(elements) {\n"
                + "  this.elements = elements || [];\n"
                + "  this.length = this.elements.length;\n"
                + "  for (var i = 0; i < this.elements.length; i++) { this[i] = this.elements[i]; }\n"
                + "}\n"
                + "JQueryWrapper.prototype.trigger = function(name) { if (this.elements[0] && this.elements[0].type === 'visibilityButton' && name === 'click') { clickCount++; } return this; };\n"
                + "JQueryWrapper.prototype.find = function(selector) {\n"
                + "  var element = this.elements[0];\n"
                + "  if (!element) { return new JQueryWrapper([]); }\n"
                + "  if (selector === '.visibility_rule') { return new JQueryWrapper(element.rules || []); }\n"
                + "  if (selector === '.visibility_rules') { return new JQueryWrapper([element.pane]); }\n"
                + "  return new JQueryWrapper([]);\n"
                + "};\n"
                + "JQueryWrapper.prototype.filter = function(callback) {\n"
                + "  var matches = [];\n"
                + "  for (var i = 0; i < this.elements.length; i++) { if (callback.call(this.elements[i], i, this.elements[i])) { matches.push(this.elements[i]); } }\n"
                + "  return new JQueryWrapper(matches);\n"
                + "};\n"
                + "JQueryWrapper.prototype.data = function(name) { return this.elements[0] && name === 'key' ? this.elements[0].key : null; };\n"
                + "JQueryWrapper.prototype.position = function() { return {top: this.elements[0] ? this.elements[0].top : 0}; };\n"
                + "JQueryWrapper.prototype.scrollTop = function() { return this.elements[0] ? this.elements[0].scroll : 0; };\n"
                + "JQueryWrapper.prototype.animate = function(values, duration) { animateScrollTop = values.scrollTop; return this; };\n"
                + "JQueryWrapper.prototype.empty = function() { this.elements = []; this.length = 0; return this; };\n"
                + "function $(selector) {\n"
                + "  if (selector === '#visibility-btn') { return new JQueryWrapper([{type: 'visibilityButton'}]); }\n"
                + "  if (selector && selector.type) { return new JQueryWrapper([selector]); }\n"
                + "  return new JQueryWrapper([]);\n"
                + "}\n"
                + "$.each = function(value, callback) {\n"
                + "  if (!value) { return; }\n"
                + "  if (typeof value.length === 'number') { for (var i = 0; i < value.length; i++) { callback.call(value[i], i, value[i]); } }\n"
                + "  else { for (var key in value) { if (Object.prototype.hasOwnProperty.call(value, key)) { callback.call(value[key], key, value[key]); } } }\n"
                + "};\n"
                + "$.extend = function(target, source) { for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } return target; };\n";
    }

    private String readVisibilityJs() throws Exception {
        Path path = Paths.get("src/main/webapp/js/at/visibility.js");
        if (!Files.exists(path)) {
            path = Paths.get("wflow-consoleweb/src/main/webapp/js/at/visibility.js");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
