package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormDataDeletableBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreElementBinder;
import org.joget.apps.form.service.FormUtil;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

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
        // load form data from DB
        FormRowSet rows = super.load(element, primaryKey, formData);
        if (rows != null) {
            // handle workflow variables
            String activityId = formData.getActivityId();
            String processId = formData.getProcessId();
            WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
            Collection<WorkflowVariable> variableList = null;
            if (activityId != null && !activityId.isEmpty()) {
                variableList = workflowManager.getActivityVariableList(activityId);
            } else if (processId != null && !processId.isEmpty()) {
                variableList = workflowManager.getProcessVariableList(processId); 
            } else {
                variableList = new ArrayList<WorkflowVariable>();
            }
            
            if (variableList != null && !variableList.isEmpty()) {
                FormRow row = null;
                if (rows.isEmpty()) {
                    row = new FormRow();
                    rows.add(row);
                } else {
                    row = rows.iterator().next();
                }
                
                Map<String, String> variableMap = new HashMap<String, String>();
                for (WorkflowVariable variable : variableList) {
                    Object val = variable.getVal();
                    if (val != null) {
                        variableMap.put(variable.getId(), val.toString());
                    }
                }
                loadWorkflowVariables(element, row, variableMap);
            }
        }
        return rows;
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        FormRowSet result = rows;
        if (rows != null && !rows.isEmpty()) {
            // store form data to DB
            result = super.store(element, rows, formData);

            // handle workflow variables
            if (!rows.isMultiRow()) {
                String activityId = formData.getActivityId();
                String processId = formData.getProcessId();
                if (activityId != null || processId != null) {
                    WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");

                    // recursively find element(s) mapped to workflow variable
                    FormRow row = rows.iterator().next();
                    Map<String, String> variableMap = new HashMap<String, String>();
                    variableMap = storeWorkflowVariables(element, row, variableMap);

                    if (activityId != null) {
                        workflowManager.activityVariables(activityId, variableMap);
                    } else {
                        workflowManager.processVariables(processId, variableMap);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Recursive into elements to set workflow variable values to be loaded.
     * @param element
     * @param row The current row of data to be loaded
     * @param variableMap The variable name=value pairs.
     * @return
     */
    protected Map<String, String> loadWorkflowVariables(Element element, FormRow row, Map<String, String> variableMap) {
        String variableName = element.getPropertyString(AppUtil.PROPERTY_WORKFLOW_VARIABLE);
        if (variableName != null && !variableName.trim().isEmpty()) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            String variableValue = variableMap.get(variableName);
            if (variableValue != null) {
                row.put(id, variableValue);
            }
        }
        for (Iterator<Element> i = element.getChildren().iterator(); i.hasNext();) {
            Element child = i.next();
            loadWorkflowVariables(child, row, variableMap);
        }
        return variableMap;
    }

    /**
     * Recursive into elements to retrieve workflow variable values to be stored.
     * @param element
     * @param row The current row of data
     * @param variableMap The variable name=value pairs to be stored.
     * @return
     */
    protected Map<String, String> storeWorkflowVariables(Element element, FormRow row, Map<String, String> variableMap) {
        String variableName = element.getPropertyString(AppUtil.PROPERTY_WORKFLOW_VARIABLE);
        if (variableName != null && !variableName.trim().isEmpty()) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            String value = (String) row.get(id);
            if (value != null) {
                variableMap.put(variableName, value);
            }
        }
        for (Iterator<Element> i = element.getChildren().iterator(); i.hasNext();) {
            Element child = i.next();
            storeWorkflowVariables(child, row, variableMap);
        }
        return variableMap;
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