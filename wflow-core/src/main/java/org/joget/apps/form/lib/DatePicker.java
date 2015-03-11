package org.joget.apps.form.lib;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.DateUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.web.servlet.LocaleResolver;

public class DatePicker extends Element implements FormBuilderPaletteElement {
    
    @Override
    public String getName() {
        return "Date Picker";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Date Picker Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "datePicker.ftl";
        
        String displayFormat = getJavaDateFormat(getPropertyString("format"));
        
        // set value
        String value = FormUtil.getElementPropertyValue(this, formData);
        value = formattedValue(value, displayFormat, formData);
        
        dataModel.put("displayFormat", displayFormat.toUpperCase());
        
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }
    
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (getPropertyString("dataFormat") != null && !getPropertyString("dataFormat").isEmpty()) {
                try {
                    String displayFormat = getJavaDateFormat(getPropertyString("format"));
                    if (!displayFormat.equals(getPropertyString("dataFormat"))) {
                        SimpleDateFormat data = new SimpleDateFormat(getPropertyString("dataFormat"));
                        SimpleDateFormat display = new SimpleDateFormat(displayFormat);
                        Date date = display.parse(value);
                        value = data.format(date);
                    }
                } catch (Exception e) {}
            }
            if (value != null) {
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, value);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>Date Picker</label><input type='text' />";
    }

    @Override
    public String getLabel() {
        return "Date Picker";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/datePicker.json", null, true, "message/form/DatePicker");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 500;
    }

    @Override
    public String getFormBuilderIcon() {
        return "/plugin/org.joget.apps.form.lib.TextField/images/textField_icon.gif";
    }
    
    protected String getJavaDateFormat(String format) {
        if (format == null || format.isEmpty()) {
            LocaleResolver localeResolver = (LocaleResolver) AppUtil.getApplicationContext().getBean("localeResolver");  
            Locale locale = localeResolver.resolveLocale(WorkflowUtil.getHttpServletRequest());
            if (locale != null && locale.toString().startsWith("zh")) {
                return "yyyy-MM-dd";
            } else {
                return "MM/dd/yyyy";
            }
        }
        
        if (format.contains("DD")) {
            format = format.replaceAll("DD", "EEEE");
        } else {
            format = format.replaceAll("D", "EEE");
        }
        
        if (format.contains("MM")) {
            format = format.replaceAll("MM", "MMMMM");
        } else {
            format = format.replaceAll("M", "MMM");
        }
        
        if (format.contains("mm")) {
            format = format.replaceAll("mm", "MM");
        } else {
            format = format.replaceAll("m", "M");
        }
        
        if (format.contains("yy")) {
            format = format.replaceAll("yy", "yyyy");
        } else {
            format = format.replaceAll("y", "yy");
        }
        
        return format;
    }
    
    @Override
    public Boolean selfValidate(FormData formData) {
        Boolean valid = true;
        String id = FormUtil.getElementParameterName(this);
        String value = FormUtil.getElementPropertyValue(this, formData);
               
        if (value != null && !value.isEmpty()) {
            String displayFormat = getJavaDateFormat(getPropertyString("format"));
            String formattedValue = formattedValue(value, displayFormat, formData);
            
            valid = DateUtil.validateDateFormat(formattedValue, displayFormat);
            
            if (!valid) {
                formData.addFormError(id, ResourceBundleUtil.getMessage("form.datepicker.error.invalidFormat"));
            }
            
            Form form = null;
            if (!getPropertyString("startDateFieldId").isEmpty() ||
                !getPropertyString("endDateFieldId").isEmpty()) {
                form = FormUtil.findRootForm(this);
            }
            
            String startDate = "";
            String endDate = "";
            
            if (!getPropertyString("startDateFieldId").isEmpty()) {
                Element e = FormUtil.findElement(getPropertyString("startDateFieldId"), form, formData);
                if (e != null) {
                    String compareValue = FormUtil.getElementPropertyValue(e, formData);
                    if (compareValue != null && !compareValue.isEmpty()) {
                        String formattedCompare = compareValue;
                        if (e instanceof DatePicker) {
                            formattedCompare = formatCompareValue(compareValue, displayFormat);
                        }
                        if (!DateUtil.compare(formattedCompare, formattedValue, displayFormat) && !formattedCompare.equals(value)) {
                            valid = false;
                            startDate = formattedCompare;
                        }
                    }
                }
            }
            
            if (!getPropertyString("endDateFieldId").isEmpty()) {
                Element e = FormUtil.findElement(getPropertyString("endDateFieldId"), form, formData);
                if (e != null) {
                    String compareValue = FormUtil.getElementPropertyValue(e, formData);
                    if (compareValue != null && !compareValue.isEmpty()) {
                        String formattedCompare = compareValue;
                        if (e instanceof DatePicker) {
                            formattedCompare = formatCompareValue(compareValue, displayFormat);
                        }
                        if (!DateUtil.compare(formattedValue, formattedCompare , displayFormat) && !formattedCompare.equals(value)) {
                            valid = false;
                            endDate = formattedCompare;
                        }
                    }
                }
            }
            
            String type = getPropertyString("currentDateAs");
            if (!type.isEmpty()) {
                SimpleDateFormat display = new SimpleDateFormat(displayFormat);
                String formattedCompare = display.format(new Date());
                String start, end;
                if ("minDate".equals(type)) {
                    start = formattedCompare;
                    end = formattedValue;
                } else {
                    start = formattedValue;
                    end = formattedCompare;
                }
                
                if (!DateUtil.compare(start, end , displayFormat) && !formattedCompare.equals(formattedValue)) {
                    valid = false;
                    
                    if ("minDate".equals(type)) {
                        if (startDate.isEmpty() || !DateUtil.compare(formattedCompare, startDate, displayFormat)) {
                            startDate = formattedCompare;
                        }
                    } else {
                        if (endDate.isEmpty() || !DateUtil.compare(endDate, formattedCompare, displayFormat)) {
                            endDate = formattedCompare;
                        }
                    }
                }
            }
                
            if (!startDate.isEmpty()) {
                formData.addFormError(id, ResourceBundleUtil.getMessage("form.datepicker.error.minDate", new String[]{startDate}));
            }

            if (!endDate.isEmpty()) {
                formData.addFormError(id, ResourceBundleUtil.getMessage("form.datepicker.error.maxDate", new String[]{endDate}));
            }
        }
        
        return valid;
    }
    
    private String formatCompareValue(String value, String displayFormat) {
        String dataFormat = getPropertyString("dataFormat");
        if (dataFormat != null && !dataFormat.isEmpty() && !displayFormat.equals(dataFormat)) {
            try {
                SimpleDateFormat data = new SimpleDateFormat(dataFormat);
                SimpleDateFormat display = new SimpleDateFormat(displayFormat);
                Date date = data.parse(value);
                value = display.format(date);
            } catch (Exception e) {}
        }
        return value;
    }
    
    private String formattedValue(String value, String displayFormat, FormData formData) {
        if (!FormUtil.isFormSubmitted(this, formData) && getPropertyString("dataFormat") != null && !getPropertyString("dataFormat").isEmpty()) {
            try {
                if (!displayFormat.equals(getPropertyString("dataFormat"))) {
                    SimpleDateFormat data = new SimpleDateFormat(getPropertyString("dataFormat"));
                    SimpleDateFormat display = new SimpleDateFormat(displayFormat);
                    Date date = data.parse(value);
                    value = display.format(date);
                }
            } catch (Exception e) {
            }
        }
        return value;
    }
}
