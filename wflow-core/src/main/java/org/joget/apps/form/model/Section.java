package org.joget.apps.form.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.ArrayUtils;
import org.joget.apps.app.lib.RulesDecisionPlugin;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.service.VisibilityControlUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONObject;

public class Section extends Element implements FormBuilderEditable, FormContainer, PluginWebSupport {
    protected Map<FormData, Boolean> continueValidations = new HashMap<FormData, Boolean>();
    private Collection<Map<String, String>> rules = null;
    private Map<String, Element> elements = new HashMap<String, Element>();
    private String cachedRulesParameterName = null; 

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
    
    /**
     * Get visibility rules for this section.
     * @param formData
     * @return Collection of visibility rules
     */
    protected Collection<Map<String, String>> getRules(FormData formData) {
        // clear cache when custom parameter name changed 
        // e.g when section is reused in subform repeater 
        String currentParamName = getCustomParameterName();
        if (rules != null && currentParamName != null && !currentParamName.equals(cachedRulesParameterName)) {
            rules = null;
            elements.clear();
        }
        
        if (rules == null) {
            rules = VisibilityControlUtil.parseVisibilityRules(this, formData, elements);
            cachedRulesParameterName = currentParamName;
        }
        return rules;
    }

    /**
     * Check if the visibility control rules match for this section.
     *
     * @param formData
     * @return true if section should be visible, false otherwise
     */
    protected Boolean isMatch(FormData formData) {
        return VisibilityControlUtil.evaluateVisibilityRules(getRules(formData), elements, formData);
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
                    result = fieldValue.matches(value);
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, String> getElementStyles(String styleClass, Map<String, String> attrs) {
        Map<String, String> styles = super.getElementStyles(styleClass, attrs, false);
        
        //section header styles
        Map<String, String> sectionHeaderAttrs = AppPluginUtil.generateAttrAndStyles(getProperties(), "header-");
        if (!sectionHeaderAttrs.get("desktopStyle").isEmpty()) {
            styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title, ." + styleClass + " .form-section-title span, ." + styleClass + " .subform-section-title span{" + sectionHeaderAttrs.get("desktopStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("tabletStyle").isEmpty()) {
            styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title, ." + styleClass + " .form-section-title span, ." + styleClass + " .subform-section-title span{" + sectionHeaderAttrs.get("tabletStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("mobileStyle").isEmpty()) {
            styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + " .form-section-title, ." + styleClass + " .subform-section-title, ." + styleClass + " .form-section-title span, ." + styleClass + " .subform-section-title span{" + sectionHeaderAttrs.get("mobileStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverDesktopStyle").isEmpty()) {
            styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title, ." + styleClass + ":hover .form-section-title span, ." + styleClass + ":hover .subform-section-title span{" + sectionHeaderAttrs.get("hoverDesktopStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverTabletStyle").isEmpty()) {
            styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title, ." + styleClass + ":hover .form-section-title span, ." + styleClass + ":hover .subform-section-title span{" + sectionHeaderAttrs.get("hoverTabletStyle") + "} ");
        }
        if (!sectionHeaderAttrs.get("hoverMobileStyle").isEmpty()) {
            styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + ":hover .form-section-title, ." + styleClass + ":hover .subform-section-title, ." + styleClass + ":hover .form-section-title span, ." + styleClass + ":hover .subform-section-title span{" + sectionHeaderAttrs.get("hoverMobileStyle") + "} ");
        } 
        
        return styles;
    }
    
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String script = AppUtil.readPluginResource(RulesDecisionPlugin.class.getName(), "/properties/form/sectionVisibilityEditor.js", null, false, null);
        response.getWriter().write(script);
    }
}
