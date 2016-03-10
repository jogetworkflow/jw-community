package org.joget.apps.form.lib;

import bsh.Interpreter;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.commons.util.LogUtil;

public class BeanShellValidator extends FormValidator {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public String getName() {
        return "Bean Shell Validator";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Executes standard Java syntax";
    }

    public String getLabel() {
        return "Bean Shell Validator";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/beanShellValidator.json", null, true, null);
    }
    
    protected boolean executeScript(String script, Map properties, boolean throwException) throws RuntimeException {
        Boolean result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            for (Object key : properties.keySet()) {
                interpreter.set(key.toString(), properties.get(key));
            }
            LogUtil.debug(getClass().getName(), "Executing script " + script);
            result = (Boolean) interpreter.eval(script);
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing script");
            if (throwException) {
                throw new RuntimeException("Error executing script");
            }
        }
        return (result != null)?result:false;
    }
    
    @Override
    public String getElementDecoration() {
        return getPropertyString("decoration");
    }

    @Override
    public boolean validate(Element element, FormData data, String[] values) {
        Map properties = new HashMap();
        properties.putAll(getProperties());
        properties.put("element", element);
        properties.put("values", values);
        properties.put("formData", data);
        return executeScript(getPropertyString("script"), properties, false);
    }
}
