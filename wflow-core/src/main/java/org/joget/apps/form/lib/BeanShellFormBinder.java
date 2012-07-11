package org.joget.apps.form.lib;

import bsh.Interpreter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.FormStoreElementBinder;

public class BeanShellFormBinder extends FormBinder implements FormLoadBinder, FormStoreBinder, FormLoadElementBinder, FormStoreElementBinder, FormLoadOptionsBinder {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public String getName() {
        return "Bean Shell Form Binder";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "Executes standard Java syntax";
    }

    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        Map properties = new HashMap();
        properties.putAll(getProperties());
        properties.put("element", element);
        properties.put("primaryKey", primaryKey);
        properties.put("formData", formData);
        return executeScript(getPropertyString("script"), properties);
    }

    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        Map properties = new HashMap();
        properties.putAll(getProperties());
        properties.put("element", element);
        properties.put("rows", rows);
        properties.put("formData", formData);
        return executeScript(getPropertyString("script"), properties);
    }

    public String getLabel() {
        return "Bean Shell Form Binder";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/beanShellFormBinder.json", null, true, "message/form/beanShellFormBinder");
    }
    
    protected FormRowSet executeScript(String script, Map properties) {
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            for (Object key : properties.keySet()) {
                interpreter.set(key.toString(), properties.get(key));
            }
            Logger.getLogger(getClass().getName()).log(Level.FINE, "Executing script " + script);
            result = interpreter.eval(script);
            return (FormRowSet) result;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing script", e);
            return null;
        }
    }
}
