package org.joget.apps.form.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderEditable;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.service.VisibilityControlUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

/**
 * Represents a single column in the Columns layout.
 * Acts as a structural container inside Form Builder.
 */
public class ColumnContainer extends Element implements FormBuilderEditable, FormContainer {
    private Collection<Map<String, String>> rules = null;
    private Map<String, Element> elements = new HashMap<String, Element>();

    @Override
    public String getName() {
        return "ColumnsChild";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<div class='col' data-cbuilder-subelement data-cbuilder-elements></div>";
    }

    @Override
    public String getDescription() {
        return "Columns Child Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        Boolean includeMetaData = (Boolean) dataModel.get("includeMetaData");

        if ((includeMetaData != null && includeMetaData) || !isHidden(formData)) {
            String readonlyValue = getPropertyString(FormUtil.PROPERTY_READONLY);
            boolean columnReadonly = "readonly".equalsIgnoreCase(readonlyValue) || "true".equalsIgnoreCase(readonlyValue);
            int version = "readonly".equalsIgnoreCase(readonlyValue) ? 2 : 1;

            StringBuilder style = new StringBuilder();
            String flex = getPropertyString("flex");
            if (flex.isEmpty()) {
                flex = getPropertyString("style-flex");
            }
            if (!flex.isEmpty()) {
                style.append("flex:").append(flex).append(";");
            }

            String width = getPropertyString("width");
            if (width.isEmpty()) {
                width = getPropertyString("style-max-width");
            }
            if (!width.isEmpty()) {
                style.append("max-width:").append(width).append(";");
            }

            String meta = "";
            if (includeMetaData != null && includeMetaData) {
                meta = (String) dataModel.get("elementMetaData");
                meta += " data-cbuilder-subelement data-cbuilder-elements";
            }

            // Handle visibility rules
            String rulesJson = "";
            boolean visible = true;
            String uniqueKey = getPropertyString("elementUniqueKey");
            if (!(dataModel.containsKey("elementMetaData") && !dataModel.get("elementMetaData").toString().isEmpty())) {
                if (!getRules(formData).isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("rules", rules);
                        rulesJson = jsonObject.toString();
                    } catch (Exception e) {
                        LogUtil.error(ColumnContainer.class.getName(), e, "Not able to retrieve visibility control rules");
                    }
                }
                visible = isMatch(formData);
            }

            StringBuilder html = new StringBuilder();
            html.append("<div class=\"col column_container_").append(uniqueKey);
            if (!visible) {
                html.append(" visibility-hidden");
            }
            html.append("\" style=\"").append(style);
            if (!visible) {
                html.append("display:none;");
            }
            html.append("\"");
            if (!rulesJson.isEmpty()) {
                html.append(" data-visibility=\"").append(rulesJson.replace("\"", "&quot;")).append("\"");
            }
            html.append(" ").append(meta).append(">");

            Collection<Element> children = getChildren();
            if (children != null) {
                for (Element child : children) {
                    if (columnReadonly) {
                        FormUtil.setReadOnlyProperty(child, true, false, version);
                    }
                    html.append(child.render(formData, includeMetaData));
                }
            }

            html.append("</div>");

            // Add VisibilityMonitor script
            if (!rulesJson.isEmpty() && (includeMetaData == null || !includeMetaData)) {
                html.append("<script type=\"text/javascript\">");
                html.append("$(document).ready(function() {");
                html.append("new VisibilityMonitor($('.column_container_").append(uniqueKey).append("'), ").append(rulesJson).append(").init();");
                html.append("});");
                html.append("</script>");
            }

            return html.toString();
        } else {
            return "";
        }
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Column";
    }

    @Override
    public String getPropertyOptions() {
        return "[]";
    }

    @Override
    public boolean continueValidation(FormData formData) {
        if (!isHidden(formData)) {
            if (VisibilityControlUtil.hasVisibilityControl(this)) {
                return isMatch(formData);
            }
            return super.continueValidation(formData);
        }
        return false;
    }

    /**
     * Get visibility rules for this column container element.
     * @param formData
     * @return Collection of visibility rules
     */
    protected Collection<Map<String, String>> getRules(FormData formData) {
        if (rules == null) {
            rules = VisibilityControlUtil.parseVisibilityRules(this, formData, elements);
        }
        return rules;
    }

    /**
     * Check if the visibility control rules match for this column container element.
     * @param formData
     * @return true if element should be visible, false otherwise
     */
    protected Boolean isMatch(FormData formData) {
        return VisibilityControlUtil.evaluateVisibilityRules(getRules(formData), elements, formData);
    }
}