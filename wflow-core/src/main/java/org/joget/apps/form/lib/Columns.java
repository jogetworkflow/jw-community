package org.joget.apps.form.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.service.VisibilityControlUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

/**
 * Columns layout container for grouping child fields into configurable columns.
 * Uses calc() for automatic space distribution like legacy columns.
 */
public class Columns extends Element implements FormBuilderPaletteElement, FormContainer {
    private Collection<Map<String, String>> rules = null;
    private Map<String, Element> elements = new HashMap<String, Element>();

    @Override
    public String getName() {
        return "Columns";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Columns Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        if (((Boolean) dataModel.get("includeMetaData") == true) || !isHidden(formData)) {
            String template = "columns.ftl";

            if (!(dataModel.containsKey("elementMetaData") && !dataModel.get("elementMetaData").toString().isEmpty())) {
                if (!getRules(formData).isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("rules", rules);
                        String json = jsonObject.toString();
                        dataModel.put("rules", json);
                    } catch (Exception e) {
                        LogUtil.error(Columns.class.getName(), e, "Not able to retrieve visibility control rules");
                    }
                }
                dataModel.put("visible", isMatch(formData));
            } else {
                dataModel.put("visible", true);
            }

            String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
            return html;
        } else {
            return "";
        }
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

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/columns_template.json", null, true, null);
    }

    @Override
    public String getLabel() {
        return "Columns";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/columns.json", null, true, "message/form/Columns");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 1200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"las la-columns\"></i>";
    }

    /**
     * Get visibility rules for this columns element.
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
     * Check if the visibility control rules match for this columns element.
     * @param formData
     * @return true if element should be visible, false otherwise
     */
    protected Boolean isMatch(FormData formData) {
        return VisibilityControlUtil.evaluateVisibilityRules(getRules(formData), elements, formData);
    }
}