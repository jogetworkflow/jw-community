package org.joget.apps.form.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormAjaxOptionsElement;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;

public class SelectBox extends Element implements FormBuilderPaletteElement, FormAjaxOptionsElement, PwaOfflineValidation {
    private Element controlElement;
    
    @Override
    public String getName() {
        return "Select Box";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Select Box Element";
    }

    /**
     * Returns the option key=value pairs for this select box.
     * @param formData
     * @return
     */
    public Collection<Map> getOptionMap(FormData formData) {
        Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);
        return optionMap;
    }
    
    @Override
    public FormData formatDataForValidation(FormData formData) {
        String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
        if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
            String paramName = FormUtil.getElementParameterName(this);
            formData.addRequestParameterValues(paramName, new String[]{""});
        }
        return formData;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
                if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "selectBox.ftl";
        
        dynamicOptions(formData);

        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set options
        Collection<Map> optionMap = getOptionMap(formData);
        dataModel.put("options", optionMap);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>Select Box</label><select><option>Option</option></select>";
    }

    @Override
    public String getLabel() {
        return "Select Box";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/selectBox.json", null, true, "message/form/SelectBox");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 300;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-caret-square-down\"></i>";
    }
    
    protected void dynamicOptions(FormData formData) {
        if (getControlElement(formData) != null) {
            setProperty("controlFieldParamName", FormUtil.getElementParameterName(getControlElement(formData)));
            
            FormUtil.setAjaxOptionsElementProperties(this, formData);
        }
    }

    public Element getControlElement(FormData formData) {
        if (controlElement == null) {
            if (getPropertyString("controlField") != null && !getPropertyString("controlField").isEmpty()) {
                Form form = FormUtil.findRootForm(this);
                controlElement = FormUtil.findElement(getPropertyString("controlField"), form, formData);
            }
        }
        return controlElement;
    }

    @Override
    public Map<WARNING_TYPE, String[]> validation() {
        Object binderData = getProperty(FormBinder.FORM_OPTIONS_BINDER);
        if (binderData != null && binderData instanceof Map) {
            Map bdMap = (Map) binderData;
            if (bdMap != null && bdMap.containsKey("className") && !bdMap.get("className").toString().isEmpty()) {
                PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                FormLoadBinder binder = (FormLoadBinder) pluginManager.getPlugin(bdMap.get("className").toString());
                
                if (binder != null) {
                    Map bdProps = (Map) bdMap.get("properties");
                    ((PropertyEditable) binder).setProperties(bdProps);
                
                    if (binder instanceof FormAjaxOptionsBinder && ((FormAjaxOptionsBinder) binder).useAjax()) {
                        Map<WARNING_TYPE, String[]> warning = new HashMap<WARNING_TYPE, String[]>();
                        warning.put(WARNING_TYPE.NOT_SUPPORTED, new String[]{ResourceBundleUtil.getMessage("pwa.AjaxOptionsNotSupported")});
                        return warning;
                    }
                }
            }
        }
        return null;
    }
}

