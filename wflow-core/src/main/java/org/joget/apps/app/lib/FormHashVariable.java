package org.joget.apps.app.lib;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormRow;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class FormHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        String temp[] = variableKey.split("\\.");

        String tableName = temp[0];
        String columnName = "";
        String primaryKey = "";
        if (temp.length > 2) {
            primaryKey = temp[1];
            columnName = temp[2];
        } else {
            columnName = temp[1];
        }
        try {
            if (tableName != null && tableName.length() != 0) {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                FormDataDao formDataDao = (FormDataDao) appContext.getBean("formDataDao");

                WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
                if (wfAssignment != null) {

                    WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
                    WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(wfAssignment.getProcessId());

                    if (link != null) {
                        primaryKey = link.getOriginProcessId();
                    } else if (primaryKey.isEmpty()) {
                        primaryKey = wfAssignment.getProcessId();
                    }
                }

                FormRow row = formDataDao.loadByTableNameAndColumnName(tableName, columnName, primaryKey);

                if (row != null && row.getCustomProperties() != null) {
                    Object val = row.getCustomProperties().get(columnName);
                    if (val != null) {
                        return val.toString();
                    } else {
                        LogUtil.info(FormHashVariable.class.getName(), "#form." + variableKey + "# is NULL");
                        return "";
                    }
                }
            }
        } catch (Exception ex) {}
        return null;
    }

    public String getName() {
        return "Form Data Hash Variable";
    }

    public String getPrefix() {
        return "form";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Form Data Hash Variable";
    }

    public String getClassName() {
       return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
