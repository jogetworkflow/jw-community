package org.joget.apps.form.lib;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

/**
 * Form load binder that loads the data rows of a form.
 */
public class FormOptionsBinder extends FormBinder implements FormLoadOptionsBinder {
    
    @Override
    public String getName() {
        return "Default Form Options Binder";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Default Form Options Binder";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Default Form Options Binder";
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@form.defaultformoptionbinder.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "',required : 'True'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@form.defaultformoptionbinder.formId@@',type:'textfield',required : 'True'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/formOptionsBinder.json", arguments, true, "message/form/DefaultFormOptionsBinder");
        return json;
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        FormRowSet results = new FormRowSet();
        results.setMultiRow(true);
        //Using filtered formset to ensure the returned result is clean with no unnecessary nulls
        FormRowSet filtered = new FormRowSet();
        filtered.setMultiRow(true);
        // get form
        String formDefId = (String) getProperty("formDefId");
        String tableName = getTableName(formDefId);
        if (tableName != null) {

            String condition = null;
            String extraCondition = (String) getProperty("extraCondition");
            if (extraCondition != null && !extraCondition.trim().isEmpty()) {
                condition = " WHERE " + extraCondition;
            }

            // get form data
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            results = formDataDao.find(formDefId, tableName, condition, null, null, null, null, null);

            if (results != null) {
                if ("true".equals(getPropertyString("addEmptyOption"))) {
                    FormRow emptyRow = new FormRow();
                    emptyRow.setProperty(FormUtil.PROPERTY_VALUE, "");
                    emptyRow.setProperty(FormUtil.PROPERTY_LABEL, "");
                    filtered.add(emptyRow);
                }
                
                String labelColumn = (String) getProperty("labelColumn");
                //Determine id column. Setting to default if not specified
                String idColumn = (String) getProperty("idColumn");
                idColumn = (idColumn == null || "".equals(idColumn)) ? FormUtil.PROPERTY_ID : idColumn;
                // loop thru results to set value and label
                for (FormRow row : results) {
                    String id = row.getProperty(idColumn);
                    String label = row.getProperty(labelColumn);
                    if (id != null && !id.isEmpty() && label != null && !label.isEmpty()) {
                        row.setProperty(FormUtil.PROPERTY_VALUE, id);
                        row.setProperty(FormUtil.PROPERTY_LABEL, label);
                        filtered.add(row);
                    }
                }
            }
        }
        return filtered;
    }

    /**
     * Retrieves table name for a specific form ID.
     * @param formDefId
     * @return 
     */
    protected String getTableName(String formDefId) {
        String tableName = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null && formDefId != null) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            tableName = appService.getFormTableName(appDef, formDefId);
        }
        return tableName;
    }
}
