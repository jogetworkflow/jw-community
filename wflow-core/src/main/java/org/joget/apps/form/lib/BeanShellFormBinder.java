package org.joget.apps.form.lib;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormLoadMultiRowElementBinder;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.FormStoreElementBinder;
import org.joget.apps.form.model.FormStoreMultiRowElementBinder;
import org.joget.commons.util.SecurityUtil;

public class BeanShellFormBinder extends FormBinder implements FormLoadBinder, FormStoreBinder, FormLoadElementBinder, FormStoreElementBinder, FormLoadOptionsBinder, FormLoadMultiRowElementBinder, FormStoreMultiRowElementBinder, FormAjaxOptionsBinder {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public String getName() {
        return "Bean Shell Form Binder";
    }

    public String getVersion() {
        return "5.0.0";
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
        properties.put("values", new String[]{});
        return executeScript(getPropertyString("script"), properties, false);
    }

    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        Map properties = new HashMap();
        properties.putAll(getProperties());
        properties.put("element", element);
        properties.put("rows", rows);
        properties.put("formData", formData);
        return executeScript(getPropertyString("script"), properties, true);
    }

    public String getLabel() {
        return "Bean Shell Form Binder";
    }

    public String getPropertyOptions() {
        String useAjax = "";
        if (SecurityUtil.getDataEncryption() != null && SecurityUtil.getNonceGenerator() != null) {
            useAjax = ",{name:'useAjax',label:'@@form.beanshellformbinder.useAjax@@',description:'@@form.beanshellformbinder.useAjax.desc@@',type:'checkbox',value :'false',options :[{value :'true',label :''}]}";
        }
        
        Object[] arguments = new Object[]{useAjax};
        
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/beanShellFormBinder.json", arguments, true, "message/form/beanShellFormBinder");
    }
    
    protected FormRowSet executeScript(String script, Map properties, boolean throwException) throws RuntimeException {
        return (FormRowSet) AppPluginUtil.executeScript(script, properties, throwException);
    }

    public boolean useAjax() {
        return "true".equalsIgnoreCase(getPropertyString("useAjax"));
    }

    public FormRowSet loadAjaxOptions(String[] dependencyValues) {
        Map properties = new HashMap();
        properties.putAll(getProperties());
        properties.put("values", (dependencyValues == null)? new String[]{}: dependencyValues);
        return executeScript(getPropertyString("script"), properties, false);
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
