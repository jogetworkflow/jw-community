package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.ElementArray;
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
        String paramName = FormUtil.getElementParameterName(this);
        
        if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
            formData.addRequestParameterValues(paramName, new String[]{""});
        } else if (paramValues != null && FormUtil.isFormSubmitted(this, formData)) {
            //check & remove invalid data from values
            Collection<String> newValues = new ArrayList<String>();
            Set<String> allValues = new HashSet<String>();
            if (FormUtil.isAjaxOptionsSupported(this, formData)) {
                FormAjaxOptionsBinder ab = (FormAjaxOptionsBinder) getOptionsBinder();
                
                List<String> controlValues = new ArrayList<String>();
                Element controlElement = getControlElement(formData);
                if (controlElement instanceof ElementArray) {
                    for (Element e : controlElement.getChildren()) {
                        controlValues.addAll(Arrays.asList(FormUtil.getRequestParameterValues(e, formData)));
                    }
                } else {
                    controlValues.addAll(Arrays.asList(FormUtil.getRequestParameterValues(controlElement, formData)));
                }
                
                if (controlValues.size() == 1 && controlValues.get(0).contains(";")) {
                    controlValues.addAll(Arrays.asList(controlValues.get(0).split(";"))); //to consistent the behaviour with FormUtil.getAjaxOptionsBinderData line 2032
                    controlValues.remove(0);
                }
                
                FormRowSet rowSet = ab.loadAjaxOptions(controlValues.toArray(new String[0]));
                if (rowSet != null) {
                    formData.setOptionsBinderData(getOptionsBinder(), rowSet);
                    for (FormRow r : rowSet) {
                        allValues.add(r.getProperty(FormUtil.PROPERTY_VALUE));
                    }
                }
            } else {
                Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);
                
                //for other child implementation which does not using options binder & option grid, do nothing
                if (optionMap == null || optionMap.isEmpty()) {
                    return formData;
                }
                for (Map option : optionMap) {
                    if (option.containsKey(FormUtil.PROPERTY_VALUE)) {
                        allValues.add(option.get(FormUtil.PROPERTY_VALUE).toString());
                    }
                }
            }
            for (String pv : paramValues) {
                if (allValues.contains(pv)) {
                    newValues.add(pv);
                }
            }
            
            if (newValues.isEmpty()) {
                newValues.add("");
            }
            
            formData.addRequestParameterValues(paramName, newValues.toArray(new String[0]));
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
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.SelectBox.pluginLabel") + "</label><select><option>" + ResourceBundleUtil.getMessage("form.checkbox.template.options") + "</option></select>";
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
            if (getControlElement(formData) instanceof ElementArray) {
                List<String> names = new ArrayList<String>();
                for (Element e : getControlElement(formData).getChildren()) {
                    names.add(FormUtil.getElementParameterName(e));
                }
                setProperty("controlFieldParamName", StringUtils.join(names, ";"));
            } else {
                setProperty("controlFieldParamName", FormUtil.getElementParameterName(getControlElement(formData)));
            }
            
            FormUtil.setAjaxOptionsElementProperties(this, formData);
        }
    }

    @Override
    public Element getControlElement(FormData formData) {
        if (controlElement == null) {
            if (getPropertyString("controlField") != null && !getPropertyString("controlField").isEmpty()) {
                Form form = FormUtil.findRootForm(this);
                
                if (getPropertyString("controlField").contains(";")) {
                    String[] cf = getPropertyString("controlField").split(";");
                    Collection<Element> elements = new ArrayList<Element>();
                    for (String c : cf) {
                        Element e = FormUtil.findElement(c.trim(), form, formData);
                        if (e != null) {
                            elements.add(e);
                        }
                    }
                    if (elements.size() > 1) {
                        controlElement = new ElementArray();
                        controlElement.setChildren(elements);
                    } else if (elements.size() == 1) {
                        controlElement = elements.iterator().next();
                    }
                } else {
                    controlElement = FormUtil.findElement(getPropertyString("controlField"), form, formData);
                }
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

