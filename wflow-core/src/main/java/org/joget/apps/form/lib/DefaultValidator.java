package org.joget.apps.form.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;

public class DefaultValidator extends FormValidator {

    @Override
    public String getName() {
        return "Default Validator";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Default Form Validator";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/defaultValidator.json", null, true, "message/form/DefaultValidator");
    }

    @Override
    public String getElementDecoration() {
        String decoration = "";
        String type = (String) getProperty("type");
        String mandatory = (String) getProperty("mandatory");
        if ("true".equals(mandatory)) {
            decoration += " * ";
        }
        if (decoration.trim().length() > 0) {
            decoration = decoration.trim();
        }
        return decoration;
    }

    @Override
    public boolean validate(Element element, FormData data, String[] values) {
        // temporarily just check for empty values
        boolean result = true;
        String id = FormUtil.getElementParameterName(element);
        String label = element.getPropertyString("label");
        String mandatory = (String) getProperty("mandatory");
        String type = (String) getProperty("type");
        String message = (String) getProperty("message");

        if ("true".equals(mandatory)) {
            result = validateMandatory(data, id, label, values, message);
        }
        if (type != null && result) {
            type = ";" + type;
            if (type.indexOf(";mandatory") >= 0) {
                result = validateMandatory(data, id, label, values, message);
            }
            if (type.indexOf(";alphanumeric") >= 0) {
                result = validateAlphaNumeric(data, id, label, values, message);
            }
            if (type.indexOf(";alphabet") >= 0) {
                result = validateAlphabet(data, id, label, values, message);
            }
            if (type.indexOf(";numeric") >= 0) {
                result = validateNumeric(data, id, label, values, message);
            }
            if (type.indexOf(";email") >= 0) {
                result = validateEmail(data, id, label, values, message);
            }
            if (type.indexOf(";custom") >= 0) {
                String regex = (String) getProperty("custom-regex");
                result = validateCustom(data, id, label, values, regex, message);
            }
        }
        return result;
    }

    protected boolean validateMandatory(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.missingValue");
        }
        
        if (values == null || values.length == 0) {
            result = false;
            if (id != null) {
                data.addFormError(id, message);
            }
        } else {
            for (String val : values) {
                if (val == null || val.trim().length() == 0) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateAlphaNumeric(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.alphanumeric");
        }
        
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^[a-zA-Z0-9]*$")) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateAlphabet(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.alphabets");
        }
        
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^[a-zA-Z]*$")) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateNumeric(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.numbers");
        }
        
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.isEmpty() && !val.matches("^[-]?\\d+([.,]\\d+)?$")) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateEmail(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.email");
        }

        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.isEmpty()) {
                    if (!StringUtil.validateEmail(val, true)) {
                        result = false;
                        data.addFormError(id, message);
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    protected boolean validateCustom(FormData data, String id, String label, String[] values, String regex, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.invalid");
        }
        
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.isEmpty()) {
                    String expression = regex;
                    int flags = Pattern.CASE_INSENSITIVE;
                    if ("true".equalsIgnoreCase(getPropertyString("custom-regex-casesensitive"))) {
                        flags = 0;
                    }
                    Pattern pattern = Pattern.compile(expression, flags);
                    Matcher matcher = pattern.matcher(val);
                    if (!matcher.matches()) {
                        result = false;
                        data.addFormError(id, message);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
