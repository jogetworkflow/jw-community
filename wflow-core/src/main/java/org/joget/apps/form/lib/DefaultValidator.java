package org.joget.apps.form.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;

public class DefaultValidator extends FormValidator {

    @Override
    public String getName() {
        return "Default Validator";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
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

        if ("true".equals(mandatory)) {
            result = validateMandatory(data, id, label, values);
        }
        if (type != null) {
            type = ";" + type;
            if (type.indexOf(";mandatory") >= 0) {
                result = validateMandatory(data, id, label, values);
            }
            if (type.indexOf(";alphanumeric") >= 0) {
                result = validateAlphaNumeric(data, id, label, values);
            }
            if (type.indexOf(";alphabet") >= 0) {
                result = validateAlphabet(data, id, label, values);
            }
            if (type.indexOf(";numeric") >= 0) {
                result = validateNumeric(data, id, label, values);
            }
            if (type.indexOf(";email") >= 0) {
                result = validateEmail(data, id, label, values);
            }
        }
        return result;
    }

    protected boolean validateMandatory(FormData data, String id, String label, String[] values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
            if (id != null) {
                data.addFormError(id, "Missing required value");
            }
        } else {
            for (String val : values) {
                if (val == null || val.trim().length() == 0) {
                    result = false;
                    data.addFormError(id, "Missing required value");
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateAlphaNumeric(FormData data, String id, String label, String[] values) {
        boolean result = true;
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^[a-zA-Z0-9]*$")) {
                    result = false;
                    data.addFormError(id, "Only alphanumeric allowed");
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateAlphabet(FormData data, String id, String label, String[] values) {
        boolean result = true;
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^[a-zA-Z]*$")) {
                    result = false;
                    data.addFormError(id, "Only alphabets allowed");
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateNumeric(FormData data, String id, String label, String[] values) {
        boolean result = true;
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^[0-9.]*$")) {
                    result = false;
                    data.addFormError(id, "Only numbers allowed");
                    break;
                }
            }
        }
        return result;
    }

    protected boolean validateEmail(FormData data, String id, String label, String[] values) {
        boolean result = true;
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.isEmpty()) {
                    String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
                    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(val);
                    if (!matcher.matches()) {
                        result = false;
                        data.addFormError(id, "Only emails allowed");
                        break;
                    }
                }
            }
        }
        return result;
    }
}
