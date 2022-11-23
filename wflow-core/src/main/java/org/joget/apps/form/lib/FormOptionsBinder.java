package org.joget.apps.form.lib;

import java.util.HashSet;
import java.util.Set;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;

/**
 * Form load binder that loads the data rows of a form.
 */
public class FormOptionsBinder extends FormBinder implements FormLoadOptionsBinder, FormAjaxOptionsBinder {
    
    @Override
    public String getName() {
        return "Default Form Options Binder";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
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
        
        String useAjax = "";
        if (SecurityUtil.getDataEncryption() != null && SecurityUtil.getNonceGenerator() != null) {
            useAjax = ",{name:'useAjax',label:'@@form.defaultformoptionbinder.useAjax@@',type:'checkbox',value :'false',options :[{value :'true',label :''}]}";
        }
        
        Object[] arguments = new Object[]{formDefField,useAjax};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/formOptionsBinder.json", arguments, true, "message/form/DefaultFormOptionsBinder");
        return json;
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        setFormData(formData);
        return loadAjaxOptions(null);
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
    
    public boolean useAjax() {
        return "true".equalsIgnoreCase(getPropertyString("useAjax"));
    }

    public FormRowSet loadAjaxOptions(String[] dependencyValues) {
        FormRowSet results = new FormRowSet();
        results.setMultiRow(true);
        //Using filtered formset to ensure the returned result is clean with no unnecessary nulls
        FormRowSet filtered = new FormRowSet();
        filtered.setMultiRow(true);
        
        try {
            // get form
            String formDefId = (String) getProperty("formDefId");
            String tableName = getTableName(formDefId);
            if (tableName != null) {

                String condition = null;
                Object[] conditionParams = null;

                WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");

                FormData formData = getFormData();
                WorkflowAssignment workflowAssignment = null;
                if(formData != null && formData.getActivityId() != null) {
                    workflowAssignment = workflowManager.getAssignment(formData.getActivityId());
                }

                String extraCondition = AppUtil.processHashVariable(getPropertyString("extraCondition"), workflowAssignment, null, null);

                if (extraCondition != null && !extraCondition.trim().isEmpty()) {
                    condition = " WHERE " + extraCondition;
                }
                
                if (dependencyValues != null && getProperty("groupingColumn") != null) {
                    if (extraCondition == null || extraCondition.trim().isEmpty()) {
                        condition = " WHERE ";
                    } else {
                        condition += " AND ";
                    }
                    
                    if (dependencyValues.length > 0) {
                        condition += "e.customProperties." + getProperty("groupingColumn").toString() + " in (";
                        for (String s : dependencyValues) {
                            condition += "?,";
                        }
                        condition = condition.substring(0, condition.length()-1) + ")";
                        
                        conditionParams = dependencyValues;
                    } else {
                        condition += "e.customProperties." + getProperty("groupingColumn").toString() + " is empty";
                    }
                }

                String labelColumn = (String) getProperty("labelColumn");

                // get form data
                FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
                results = formDataDao.find(formDefId, tableName, condition, conditionParams, labelColumn, false, null, null);

                if (results != null) {
                    if ("true".equals(getPropertyString("addEmptyOption"))) {
                        FormRow emptyRow = new FormRow();
                        emptyRow.setProperty(FormUtil.PROPERTY_VALUE, "");
                        emptyRow.setProperty(FormUtil.PROPERTY_LABEL, getPropertyString("emptyLabel"));
                        filtered.add(emptyRow);
                    }

                    //Determine id column. Setting to default if not specified
                    String idColumn = (String) getProperty("idColumn");
                    idColumn = (idColumn == null || "".equals(idColumn)) ? FormUtil.PROPERTY_ID : idColumn;

                    String groupingColumn = (String) getProperty("groupingColumn");

                    // loop thru results to set value and label
                    Set<String> exists = new HashSet<String>();
                    for (FormRow row : results) {
                        FormRow newRow = new FormRow();
                        String id = row.getProperty(idColumn);
                        String label = row.getProperty(labelColumn);
                        String grouping = "";
                        if (groupingColumn != null && !groupingColumn.isEmpty() && row.containsKey(groupingColumn)) {
                            grouping = row.getProperty(groupingColumn);
                        }

                        if (!exists.contains(id+":"+label+":"+grouping) && id != null && !id.isEmpty() && label != null && !label.isEmpty()) {
                            newRow.setProperty(FormUtil.PROPERTY_VALUE, id);
                            newRow.setProperty(FormUtil.PROPERTY_LABEL, label);
                            newRow.setProperty(FormUtil.PROPERTY_GROUPING, grouping);

                            filtered.add(newRow);
                            exists.add(id+":"+label+":"+grouping);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("FormOptionsBinder", e, "");
        }
        return filtered;
    }
}
