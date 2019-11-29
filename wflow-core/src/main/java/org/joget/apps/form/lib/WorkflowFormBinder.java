package org.joget.apps.form.lib;

import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormDataDeletableBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreElementBinder;
import org.joget.apps.form.service.FormUtil;

/**
 * Data binder that loads/stores data from the form database and also workflow variables.
 */
public class WorkflowFormBinder extends DefaultFormBinder implements FormLoadElementBinder, FormStoreElementBinder, FormDataDeletableBinder {

    @Override
    public String getName() {
        return "Workflow Form Binder";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Workflow Form Binder";
    }

    @Override
    public String getLabel() {
        return "Workflow Form Binder";
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        setProperty("autoHandleWorkflowVariable", "true");
        
        // load form data from DB
        FormRowSet rows = super.load(element, primaryKey, formData);
        return rows;
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        setProperty("autoHandleWorkflowVariable", "true");
        
        FormRowSet result = rows;
        if (rows != null && !rows.isEmpty()) {
            // store form data to DB
            result = super.store(element, rows, formData);
        }
        return result;
    }

    public String getFormId() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_ID);
    }

    public String getTableName() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
    }
}