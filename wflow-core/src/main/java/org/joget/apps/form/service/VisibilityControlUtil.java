package org.joget.apps.form.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.Section;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

/**
 * Utility class for handling visibility control logic.
 * This class provides common functionality for both Section and Element visibility rules.
 */
public class VisibilityControlUtil {

    /**
     * Parse visibility control properties into a collection of rules.
     *
     * @param element The element containing visibility properties
     * @param formData The form data
     * @param controlElementsMap Map to store control element references (for value lookup)
     * @return Collection of visibility rules as maps
     */
    public static Collection<Map<String, String>> parseVisibilityRules(
            Element element,
            FormData formData,
            Map<String, Element> controlElementsMap) {

        List<Map<String, String>> rules = new ArrayList<Map<String, String>>();
        Form rootForm = FormUtil.findRootForm(element);
        boolean isSection = element instanceof Section;

        if (isSection) {
            Collection<Map<String, String>> legacyRules = parseLegacyVisibilityRules(
                    element, rootForm, formData, controlElementsMap);
            rules.addAll(legacyRules);
        }

        Object visibilityRulesObj = element.getProperty("visibility_rules");
        if (visibilityRulesObj != null && rootForm != null) {
            Collection<Map<String, String>> formRules = parseVisibilityRulesFromForm(
                    element, rootForm, formData, controlElementsMap, visibilityRulesObj);

            // If Section has both own visibility and applied rules, combine with OR
            if (!rules.isEmpty() && !formRules.isEmpty()) {
                // Wrap existing rules in parentheses
                Map<String, String> openParen = new HashMap<String, String>();
                openParen.put("field", "(");
                openParen.put("join", "");
                rules.add(0, openParen);

                Map<String, String> closeParen = new HashMap<String, String>();
                closeParen.put("field", ")");
                closeParen.put("join", "");
                rules.add(closeParen);

                // Add OR before form rules
                if (!formRules.isEmpty()) {
                    Map<String, String> firstFormRule = formRules.iterator().next();
                    firstFormRule.put("join", "or");
                }
            }

            rules.addAll(formRules);
        }

        return rules;
    }

    /**
     * Parse legacy visibility rules
     */
    private static Collection<Map<String, String>> parseLegacyVisibilityRules(
            Element element,
            Form rootForm,
            FormData formData,
            Map<String, Element> controlElementsMap) {

        Collection<Map<String, String>> rules = new ArrayList<Map<String, String>>();

        String visibilityControl = element.getPropertyString("visibilityControl");
        if (visibilityControl == null || visibilityControl.isEmpty()) {
            return rules;
        }

        String[] fields = visibilityControl.split(";", -1);
        String[] values = element.getPropertyString("visibilityValue").split(";", -1);
        String[] regex = element.getPropertyString("regex").split(";", -1);
        String[] joins = element.getPropertyString("join").split(";", -1);
        String[] reverses = element.getPropertyString("reverse").split(";", -1);

        if (fields.length == 0) {
            return rules;
        }

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEmpty()) {
                continue;
            }

            Map<String, String> rule = new HashMap<String, String>();
            rule.put("join", (joins.length > i) ? joins[i] : "");
            rule.put("reverse", (reverses.length > i) ? reverses[i] : "");
            rule.put("value", (values.length > i) ? values[i] : "");
            rule.put("regex", (regex.length > i) ? regex[i] : "");

            if (!"(".equals(fields[i]) && !")".equals(fields[i])) {
                Element controlElement = FormUtil.findElement(fields[i], rootForm, formData, false);
                if (controlElement != null) {
                    String visibilityControlParam = FormUtil.getElementParameterName(controlElement);
                    rule.put("field", visibilityControlParam);
                    if (controlElementsMap != null) {
                        controlElementsMap.put(visibilityControlParam, controlElement);
                    }
                }
            } else {
                rule.put("field", fields[i]);
            }

            if (rule.get("field") != null) {
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Parse visibility rules from Form's visibility_rules array based on element's applied rule keys.
     *
     * @param element The element with visibility_rules keys
     * @param rootForm The root form containing rule definitions
     * @param formData The form data
     * @param controlElementsMap Map to store control element references
     * @param visibilityRulesObj The element's visibility_rules property (Map of key -> true)
     * @return Collection of visibility rules
     */
    @SuppressWarnings("unchecked")
    private static Collection<Map<String, String>> parseVisibilityRulesFromForm(
            Element element,
            Form rootForm,
            FormData formData,
            Map<String, Element> controlElementsMap,
            Object visibilityRulesObj) {

        Collection<Map<String, String>> rules = new ArrayList<Map<String, String>>();

        // Get applied rule keys from element
        Map<String, Object> appliedRuleKeys = null;
        if (visibilityRulesObj instanceof Map) {
            appliedRuleKeys = (Map<String, Object>) visibilityRulesObj;
        } else if (visibilityRulesObj instanceof String) {
            // Handle JSON string format
            try {
                JSONObject json = new JSONObject((String) visibilityRulesObj);
                appliedRuleKeys = new HashMap<String, Object>();
                for (String key : json.keySet()) {
                    appliedRuleKeys.put(key, json.get(key));
                }
            } catch (Exception e) {
                return rules;
            }
        }

        if (appliedRuleKeys == null || appliedRuleKeys.isEmpty()) {
            return rules;
        }

        // Get Form's visibility_rules array
        Object formVisibilityRules = rootForm.getProperty("visibility_rules");
        if (formVisibilityRules == null) {
            return rules;
        }

        // Parse form rules and find matching ones
        Collection<Map<String, Object>> formRulesList = null;
        if (formVisibilityRules instanceof Object[]) {
            formRulesList = new ArrayList<Map<String, Object>>();
            for (Object obj : (Object[]) formVisibilityRules) {
                if (obj instanceof Map) {
                    formRulesList.add((Map<String, Object>) obj);
                }
            }
        } else if (formVisibilityRules instanceof Collection) {
            formRulesList = (Collection<Map<String, Object>>) formVisibilityRules;
        }

        if (formRulesList == null || formRulesList.isEmpty()) {
            return rules;
        }

        // Build rules from applied form rules
        boolean isFirstRule = true;
        for (Map<String, Object> formRule : formRulesList) {
            String ruleKey = (String) formRule.get("visibility_key");
            if (ruleKey == null || !isRuleApplied(appliedRuleKeys, ruleKey)) {
                continue;
            }

            String visibilityControl = getStringValue(formRule, "visibilityControl");
            if (visibilityControl == null || visibilityControl.isEmpty()) {
                continue;
            }

            String[] fields = visibilityControl.split(";", -1);
            String[] values = getStringValue(formRule, "visibilityValue").split(";", -1);
            String[] regex = getStringValue(formRule, "regex").split(";", -1);
            String[] joins = getStringValue(formRule, "join").split(";", -1);
            String[] reverses = getStringValue(formRule, "reverse").split(";", -1);

            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isEmpty()) {
                    continue;
                }

                Map<String, String> rule = new HashMap<String, String>();

                // Use OR to join different rules (except first condition of first rule)
                String joinVal = (joins.length > i) ? joins[i] : "";
                if (!isFirstRule && i == 0 && !"(".equals(fields[i]) && !")".equals(fields[i])) {
                    joinVal = "or";
                }

                rule.put("join", joinVal);
                rule.put("reverse", (reverses.length > i) ? reverses[i] : "");
                rule.put("value", (values.length > i) ? values[i] : "");
                rule.put("regex", (regex.length > i) ? regex[i] : "");

                if (!"(".equals(fields[i]) && !")".equals(fields[i])) {
                    Element controlElement = FormUtil.findElement(fields[i], rootForm, formData, false);
                    if (controlElement != null) {
                        String visibilityControlParam = FormUtil.getElementParameterName(controlElement);
                        rule.put("field", visibilityControlParam);
                        if (controlElementsMap != null) {
                            controlElementsMap.put(visibilityControlParam, controlElement);
                        }
                    }
                } else {
                    rule.put("field", fields[i]);
                }

                if (rule.get("field") != null) {
                    rules.add(rule);
                }
            }

            isFirstRule = false;
        }

        return rules;
    }

    /**
     * Check if a rule key is applied (value is true or "true").
     */
    private static boolean isRuleApplied(Map<String, Object> appliedRuleKeys, String ruleKey) {
        Object value = appliedRuleKeys.get(ruleKey);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equals(value.toString());
    }

    /**
     * Get string value from a map, handling null and different types.
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Evaluate visibility rules using JavaScript engine.
     *
     * @param rules The collection of visibility rules
     * @param controlElementsMap Map of control field names to their elements
     * @param formData The form data
     * @return true if the element should be visible, false otherwise
     */
    public static Boolean evaluateVisibilityRules(
            Collection<Map<String, String>> rules,
            Map<String, Element> controlElementsMap,
            FormData formData) {

        if (rules.isEmpty()) {
            return true;
        }

        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        Scriptable scope = cx.initStandardObjects(null);
        try {
            String ruleExpression = buildRuleExpression(rules, controlElementsMap, formData);
            return (Boolean) cx.evaluateString(scope, ruleExpression, "", 1, null);
        } catch (Exception e) {
            LogUtil.error(VisibilityControlUtil.class.getName(), e, "Visibility rules evaluation failed");
            return false;
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    /**
     * Build the JavaScript expression string from visibility rules.
     */
    private static String buildRuleExpression(
            Collection<Map<String, String>> rules,
            Map<String, Element> controlElementsMap,
            FormData formData) {

        StringBuilder rule = new StringBuilder();

        for (Map<String, String> r : rules) {
            String field = r.get("field");
            String join = r.get("join");
            String value = r.get("value");
            String regex = r.get("regex");
            String reverse = r.get("reverse");

            // Add join operator
            if (rule.length() > 0 && !rule.toString().endsWith("(") && !")".equals(field)) {
                if ("or".equals(join)) {
                    rule.append(" || ");
                } else {
                    rule.append(" && ");
                }
            }

            if (!")".equals(field)) {
                rule.append(" ");
            }

            // Add NOT operator if reversed
            if (reverse != null && !reverse.isEmpty() && !")".equals(field)) {
                rule.append("!");
            }

            // Add parentheses or value check
            if ("(".equals(field) || ")".equals(field)) {
                rule.append(field);
            } else {
                rule.append(checkControlValue(controlElementsMap, formData, field, value, regex));
            }
        }

        return rule.toString();
    }

    /**
     * Check if a control field value matches the expected value.
     */
    private static boolean checkControlValue(
            Map<String, Element> controlElementsMap,
            FormData formData,
            String field,
            String value,
            String operator) {

        Element controlElement = controlElementsMap.get(field);
        if (controlElement == null) {
            return false;
        }

        String[] paramValue = FormUtil.getElementPropertyValues(controlElement, formData);
        if (paramValue == null) {
            return false;
        }

        if (paramValue.length == 0) {
            paramValue = new String[]{""};
        }

        for (String v : paramValue) {
            if (Section.checkValue(v, operator, value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convert visibility rules to JSON string for client-side JavaScript.
     *
     * @param rules The collection of visibility rules
     * @return JSON string representation of rules
     */
    public static String toJson(Collection<Map<String, String>> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("rules", rules);
            return jsonObject.toString();
        } catch (Exception e) {
            LogUtil.error(VisibilityControlUtil.class.getName(), e, "Failed to convert visibility rules to JSON");
            return null;
        }
    }

    /**
     * Check if visibility control is defined for an element.
     * Checks both new format (visibility_rules keys) and legacy format (visibilityControl).
     *
     * @param element The element to check
     * @return true if visibility control is defined
     */
    public static boolean hasVisibilityControl(Element element) {
        // Check new format (visibility_rules keys)
        Object visibilityRulesObj = element.getProperty("visibility_rules");
        if (visibilityRulesObj != null) {
            if (visibilityRulesObj instanceof Map) {
                Map<String, Object> rules = (Map<String, Object>) visibilityRulesObj;
                if (!rules.isEmpty()) {
                    return true;
                }
            } else if (visibilityRulesObj instanceof String && !((String) visibilityRulesObj).isEmpty()) {
                return true;
            }
        }
        String visibilityControl = element.getPropertyString("visibilityControl");
        return visibilityControl != null && !visibilityControl.isEmpty();
    }
}