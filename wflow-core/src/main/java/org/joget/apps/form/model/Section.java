package org.joget.apps.form.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.lib.RulesDecisionPlugin;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

public class Section extends Element implements FormBuilderEditable, FormContainer, PluginWebSupport {
    protected Map<FormData, Boolean> continueValidations = new HashMap<FormData, Boolean>();
    private Collection<Map<String, String>> rules = null;
    private Map<String, Element> elements = new HashMap<String, Element>();

    @Override
    public String getName() {
        return "Section";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Section Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        if (((Boolean) dataModel.get("includeMetaData") == true) || !isHidden(formData)) {
            String template = "section.ftl";
            
            if (isReadonly(formData)) {
                FormUtil.setReadOnlyProperty(this, true, "true".equalsIgnoreCase(getPropertyString("readonlyLabel")));
            }
            
            if (!(dataModel.containsKey("elementMetaData") && !dataModel.get("elementMetaData").toString().isEmpty())) {
                if (!getRules(formData).isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("rules", rules);

                        String json = jsonObject.toString();
                        dataModel.put("rules", json);
                    } catch (Exception e) {
                        LogUtil.error(Section.class.getName(), e, "Not able to retrieve visibility control rules");
                    }
                }
                dataModel.put("visible", isMatch(formData));
            } else {
                dataModel.put("visible", true);
            }   

            String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
            
            if (!((Boolean) dataModel.get("includeMetaData") == true) && !html.contains("form-cell") && !html.contains("form-column-label")) {
                html = html.replaceFirst("form-section", "form-section form-section-empty");
            }
            return html;
        } else {
            return "";
        }
    }

    @Override
    public boolean continueValidation(FormData formData) {
        Boolean continueValidation = continueValidations.get(formData);
        if (continueValidation == null) {
            if (!isHidden(formData)) {
                // get the control element (where value changes the target)
                String visibilityControl = getPropertyString("visibilityControl");

                if (visibilityControl != null && !visibilityControl.isEmpty()) {
                    continueValidation = isMatch(formData);
                } else {
                    continueValidation = super.continueValidation(formData);
                }
            } else {
                continueValidation = false;
            }
            continueValidations.put(formData, continueValidation);
        }
        return continueValidation;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/section.json", null, true, "message/form/Section");
    }

    @Override
    public String getFormBuilderTemplate() {
        return "";
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        return null;
    }
    
    protected Collection<Map<String, String>> getRules(FormData formData) {
        if (rules == null) {
            rules = new ArrayList<Map<String, String>>();
            
            String[] fields = getPropertyString("visibilityControl").split(";", -1);
            String[] values = getPropertyString("visibilityValue").split(";", -1);
            String[] regex = getPropertyString("regex").split(";", -1);
            String[] joins = getPropertyString("join").split(";", -1);
            String[] reverses = getPropertyString("reverse").split(";", -1);
            
            if (fields.length > 0) {
                Form rootForm = FormUtil.findRootForm(this);
                
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].isEmpty()) {
                        continue;
                    }
                    
                    Map<String, String> rule = new HashMap<String, String>();
                    rule.put("join", joins[i]);
                    rule.put("reverse", reverses[i]);
                    rule.put("value", values[i]);
                    rule.put("regex", regex[i]);
                    if (!fields[i].equals("(") && !fields[i].equals(")")) {
                        Element controlElement = FormUtil.findElement(fields[i], rootForm, formData, false);
                        if (controlElement != null) {
                            String visibilityControlParam = FormUtil.getElementParameterName(controlElement);
                            rule.put("field", visibilityControlParam);
                            elements.put(visibilityControlParam, controlElement);
                        }
                    } else {
                        rule.put("field", fields[i]);
                    }
                    if (rule.get("field") != null) {
                        rules.add(rule);
                    }
                }
            }
        }
        return rules;
    }
    
    protected Boolean isMatch(FormData formData) {
        if (!getRules(formData).isEmpty()) {
            boolean match = false;
            
            org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
            Scriptable scope = cx.initStandardObjects(null);
            try {
                String rule = "";
                for (Map<String, String> r : getRules(formData)) {
                    String field = r.get("field");
                    String join = r.get("join");
                    String value = r.get("value");
                    String regex = r.get("regex");
                    String reverse = r.get("reverse");

                    if (!rule.isEmpty() && !rule.endsWith("(") && !")".equals(field)) {
                        if ("or".equals(join)) {
                            rule += " || ";
                        } else {
                            rule += " && ";
                        }
                    }
                    if (!")".equals(field)) {
                        rule += " ";
                    }
                    if (!reverse.isEmpty() && !")".equals(field)) {
                        rule += "!";
                    }
                    if ("(".equals(field) || ")".equals(field) ) {
                        rule += field;
                    } else {
                        rule += checkValue(formData, field, value, regex);
                    }
                }
                
                return (Boolean) cx.evaluateString(scope, rule, "", 1, null);
            } catch (Exception e) {
                LogUtil.error(Section.class.getName(), e, "rules are not valid");
            } finally {
                org.mozilla.javascript.Context.exit();
            }
            
            return match;
        } else {
            return true;
        }
    }
    
    protected boolean checkValue(FormData formData, String field, String value, String operator) {
        Element controlElement = elements.get(field);
        if (controlElement != null) {
            // check for matching values
            String[] paramValue = FormUtil.getElementPropertyValues(controlElement, formData);

            if (paramValue != null) {
                if (paramValue.length == 0) {
                    paramValue = new String[]{""};
                }
                for (String v : paramValue) {
                    if (checkValue(v, operator, value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean checkValue(String fieldValue, String operator, String value) {
        boolean result = false;
        if (fieldValue != null) {
            Double fieldValueNumber = null;
            Double valueNumber = null;
            boolean isNumeric = false;
            try {
                fieldValueNumber = Double.parseDouble(fieldValue);
                valueNumber = Double.parseDouble(value);
                isNumeric = true;
            } catch (Exception e) {
                //ignore
            }

            if (isNumeric) {
                int compare = Double.compare(fieldValueNumber, valueNumber);
                if (operator.isEmpty()) {
                    result = compare == 0;
                } else if (">".equals(operator)) {
                    result = compare > 0;
                } else if (">=".equals(operator)) {
                    result = compare >= 0;
                } else if ("<".equals(operator)) {
                    result = compare < 0;
                } else if ("<=".equals(operator)) {
                    result = compare <= 0;
                }
            } else {
                if (operator.isEmpty()) {
                    result = fieldValue.equals(value);
                } else if (">".equals(operator)) {
                    result = fieldValue.compareTo(value) > 0;
                } else if (">=".equals(operator)) {
                    result = fieldValue.compareTo(value) >= 0;
                } else if ("<".equals(operator)) {
                    result = fieldValue.compareTo(value) < 0;
                } else if ("<=".equals(operator)) {
                    result = fieldValue.compareTo(value) <= 0;
                } else if ("isTrue".equals(operator)) {
                    result = fieldValue.equalsIgnoreCase("true") || fieldValue.equals("1");
                } else if ("isFalse".equals(operator)) {
                    result = fieldValue.equalsIgnoreCase("false") || fieldValue.equals("0");
                } else if ("contains".equals(operator)) {
                    result = fieldValue.contains(value);
                } else if ("listContains".equals(operator)) {
                    String[] list = fieldValue.split(";");
                    result = ArrayUtils.contains(list, value);
                } else if ("in".equals(operator)) {
                    String[] list = value.replaceAll("__", ";").split(";");
                    result = ArrayUtils.contains(list, fieldValue);
                } else if ("true".equals(operator)) {
                    result = fieldValue.matches(StringEscapeUtils.unescapeJavaScript(value));
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, String> getElementStyles(String styleClass, Map<String, String> attrs) {
        Map<String, String> styles = super.getElementStyles(styleClass, attrs);
        
        //section header styles
        Map<String, String> sectionHeaderAttrs = AppPluginUtil.generateAttrAndStyles(getProperties(), "header-");
        if (!sectionHeaderAttrs.get("desktopStyle").isEmpty()) {
            styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title{" + sectionHeaderAttrs.get("desktopStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("tabletStyle").isEmpty()) {
            styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title{" + sectionHeaderAttrs.get("tabletStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("mobileStyle").isEmpty()) {
            styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title{" + sectionHeaderAttrs.get("mobileStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverDesktopStyle").isEmpty()) {
            styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title{" + sectionHeaderAttrs.get("hoverDesktopStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverTabletStyle").isEmpty()) {
            styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title{" + sectionHeaderAttrs.get("hoverTabletStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverMobileStyle").isEmpty()) {
            styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title{" + sectionHeaderAttrs.get("hoverMobileStyle") + "} ");
        } 
        
        return styles;
    }
    
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String script = AppUtil.readPluginResource(RulesDecisionPlugin.class.getName(), "/properties/form/sectionVisibilityEditor.js", null, false, null);
        response.getWriter().write(script);
    }
}
