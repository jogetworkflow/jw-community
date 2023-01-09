package org.joget.apps.app.lib;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FormHashVariable extends DefaultHashVariablePlugin {
    Map<String, FormRow> formDataCache = new HashMap<String, FormRow>();

    @Override
    public String processHashVariable(String variableKey) {
        String[] primaryKeys = null;
        if (variableKey.contains("[") && variableKey.contains("]")) {
            primaryKeys = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]")).split(";");
            variableKey = variableKey.substring(0, variableKey.indexOf("["));

            if (primaryKeys.length == 0) {
                LogUtil.debug(FormHashVariable.class.getName(), "#form." + variableKey + "# is NULL");
                return "";
            }
        }

        //get from request parameter if exist
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (primaryKeys == null && request != null) {
            if(request.getParameter("id") != null && !request.getParameter("id").isEmpty()) {
                primaryKeys = new String[] { request.getParameter("id") };
            } else if(request.getParameter("primaryKey") != null && !request.getParameter("primaryKey").isEmpty()) {
                primaryKeys = new String[] { request.getParameter("primaryKey") };
            }
        }

        String temp[] = variableKey.split("\\.");

        String tableName = temp[0];
        String columnName = temp[1];

        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if(wfAssignment == null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            String assignmentId = request.getParameter("assignmentId");
            String activityId = request.getParameter("activityId");
            if(assignmentId != null && !assignmentId.isEmpty()) {
                wfAssignment = workflowManager.getAssignment(assignmentId);
            } else if(activityId != null && !activityId.isEmpty()) {
                wfAssignment = workflowManager.getAssignment(activityId);
            }
        }

        if (primaryKeys != null || wfAssignment != null) {
            try {
                if (tableName != null && tableName.length() != 0) {
                    ApplicationContext appContext = AppUtil.getApplicationContext();
                    FormDataDao formDataDao = (FormDataDao) appContext.getBean("formDataDao");

                    if (primaryKeys == null) {

                        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
                        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(wfAssignment.getProcessId());

                        if (link != null) {
                            primaryKeys = new String[] { link.getOriginProcessId() };
                        } else {
                            primaryKeys = new String[] { wfAssignment.getProcessId() };
                        }
                    }

                    FormRowSet rows = Arrays.stream(primaryKeys)
                            .map(new Function<String, FormRow>() {
                                @Override
                                public FormRow apply(String primaryKey) {
                                    String cacheKey = tableName + "##" + String.join("", primaryKey);

                                    if (formDataCache.containsKey(cacheKey)) {
                                        return formDataCache.get(cacheKey);
                                    }

                                    FormRow row = formDataDao.loadByTableNameAndColumnName(tableName, columnName, primaryKey);
                                    formDataCache.put(cacheKey, row);

                                    return row;
                                }
                            }).collect(Collectors.toCollection(FormRowSet::new));

                    String val = rows.stream()
                            .filter(Objects::nonNull)
                            .map(FormRow::getCustomProperties)
                            .filter(Objects::nonNull)
                            .map(new Function<Map, Object>() {
                                @Override
                                public Object apply(Map m) {
                                    return m.get(columnName);
                                }
                            })
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .collect(Collectors.joining(";"));

                    if (!val.isEmpty()) {
                        return AppUtil.escapeHashVariable(val);
                    } else {
                        LogUtil.debug(FormHashVariable.class.getName(), "#form." + variableKey + "# is NULL");
                        return "";
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(FormHashVariable.class.getName(), ex, ex.getMessage());
            }
        }
        return "";
    }

    public String getName() {
        return "Form Data Hash Variable";
    }

    public String getPrefix() {
        return "form";
    }

    public String getVersion() {
        return "5.0.0";
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
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("form.TABLE.COLUMN");
        syntax.add("form.TABLE.COLUMN[PRIMARY_KEY]");
        
        return syntax;
    }
    
    @Override
    public String escapeHashVariableValue(String value) {
        return AppUtil.escapeHashVariable(value);
    }
}
