package org.joget.apps.form.lib;

import java.util.Collection;
import org.joget.apps.app.service.AppService;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.service.FormUtil;

/**
 *
 */
public class DefaultFormBinder extends FormBinder implements FormLoadBinder, FormStoreBinder {

    @Override
    public String getName() {
        return "Default Form Binder";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Default Form Binder";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Default Form Binder";
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        
        FormRowSet results = null;
        if (primaryKey != null && primaryKey.trim().length() > 0) {
            AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
            Form form = FormUtil.findRootForm(element);
            form = findFormForLoadBinder(form);
            if (form == null) {
                form = FormUtil.findRootForm(element);
            }
            if (form != null) {
                //Load from Form Data if found
                String tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
                if (!formData.getLoadBinderMap().isEmpty()) {
                    for (FormRowSet set : formData.getLoadBinderMap().values()) {
                        if (tableName.equals(set.getReferenceTable()) && primaryKey.equals(set.getReferenceKey())) {
                            results = set;
                            break;
                        }
                    }
                }
                
                if (results == null) {
                    results = appService.loadFormDataWithoutTransaction(form, primaryKey);
                    results.setReferenceTable(tableName);
                    results.setReferenceKey(primaryKey);
                }
            }
        }
        return results;
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        if (rows != null && !rows.isEmpty()) {
            // find root form
            Form form = FormUtil.findRootForm(element);
            form = findFormForStoreBinder(form);
            if (form == null) {
                form = FormUtil.findRootForm(element);
            }
            if (form == null) {
                return rows;
            }

            // store form data
            AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
            String primaryKeyValue = form.getPrimaryKeyValue(formData);
            rows = appService.storeFormData(form, rows, primaryKeyValue);
        }
        return rows;
    }

    /**
     * Returns the Form that is tied to this binder.
     * @param element
     * @return 
     */
    protected Form findFormForLoadBinder(Element element) {
        Form form = null;
        if (element != null) {
            if (element.getLoadBinder() == this) {
                if (element instanceof AbstractSubForm) {
                    Collection<Element> children = element.getChildren();
                    if (!children.isEmpty()) {
                        form = (Form) children.iterator().next();
                    }
                } else if (element instanceof Form) {
                    form = (Form) element;
                }
            } else {
                for (Element child : element.getChildren()) {
                    form = findFormForLoadBinder(child);
                    if (form != null) {
                        break;
                    }
                }
            }
        }
        return form;
    }

    /**
     * Returns the Form that is tied to this binder.
     */
    protected Form findFormForStoreBinder(Element element) {
        Form form = null;
        if (element != null) {
            if (element.getStoreBinder() == this) {
                if (element instanceof AbstractSubForm) {
                    Collection<Element> children = element.getChildren();
                    if (!children.isEmpty()) {
                        form = (Form) children.iterator().next();
                    }
                } else if (element instanceof Form) {
                    form = (Form) element;
                }
            } else {
                for (Element child : element.getChildren()) {
                    form = findFormForStoreBinder(child);
                    if (form != null) {
                        break;
                    }
                }
            }
        }
        return form;
    }
}
