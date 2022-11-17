package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;

public class SimpleTensorFlowAITool extends DefaultApplicationPlugin implements PluginWebSupport {

    @Override
    public String getName() {
        return "SimpleTensorFlowAITool";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Simple TensorFlow AI";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/simpleTensorFlowAITool.json", null, true, null);
    }

    @Override
    public Object execute(Map props) {
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                
        Map tensorflow = (Map) getProperty("tensorflow");
        
        Map<String, String> variables = new HashMap<String, String>(); 
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) props.get("workflowAssignment");
        
        String id = null;
        if (workflowAssignment != null) {
            id = workflowAssignment.getProcessId();
            
            if (workflowAssignment.getProcessName() != null) {
                Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableList(id);
                for (WorkflowVariable wv : variableList) {
                    if (wv.getVal() != null) {
                        variables.put(wv.getName(), wv.getVal().toString());
                    }
                }
            }
        }
        
        Map<String, Object> tfvariables = TensorFlowUtil.getEditorResults(tensorflow, id, variables);
        TensorFlowUtil.convertTFVariables(tfvariables, variables);
        
        storeToForm(workflowAssignment, variables);
        storeToWorkflowVariable(workflowAssignment, variables);
        beanshell(workflowAssignment, (AppDefinition) getProperty("appDef"), variables);
        
        return null;
    }
    
    protected void storeToForm(WorkflowAssignment wfAssignment, Map<String, String> variables) {
        String formDefId = getPropertyString("formDefId");
        if (formDefId != null && formDefId.trim().length() > 0) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            AppDefinition appDef = (AppDefinition) getProperty("appDef");

            Object[] fieldMapping = (Object[]) getProperty("fieldMapping");

            FormRowSet rowSet = new FormRowSet();

            rowSet.add(getRow(wfAssignment, fieldMapping, variables));

            if (rowSet.size() > 0) {
                appService.storeFormData(appDef.getId(), appDef.getVersion().toString(), formDefId, rowSet, null);
            }
        }
    }
    
    protected FormRow getRow(WorkflowAssignment wfAssignment, Object[] fieldMapping, Map<String, String> variables) {
        FormRow row = new FormRow();

        for (Object o : fieldMapping) {
            Map mapping = (HashMap) o;
            String fieldName = mapping.get("field").toString();
            String outputvariable = mapping.get("outputvariable").toString();

            String value = variables.get(outputvariable);

            if (value == null) {
                value = "";
            }

            if (FormUtil.PROPERTY_ID.equals(fieldName)) {
                row.setId(value);
            } else {
                row.put(fieldName, value);
            }
        }

        if (row.getId() == null || (row.getId() != null && row.getId().trim().length() == 0)) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            row.setId(appService.getOriginProcessId(wfAssignment.getProcessId()));
        }

        Date currentDate = new Date();
        row.setDateModified(currentDate);
        row.setDateCreated(currentDate);

        return row;
    }

    protected void storeToWorkflowVariable(WorkflowAssignment wfAssignment,  Map<String, String> variables) {
        Object[] wfVariableMapping = (Object[]) getProperty("wfVariableMapping");
        if (wfVariableMapping != null && wfVariableMapping.length > 0 && wfAssignment.getActivityId() != null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");

            for (Object o : wfVariableMapping) {
                Map mapping = (HashMap) o;
                String variable = mapping.get("variable").toString();
                String outputvariable = mapping.get("outputvariable").toString();

                String value = variables.get(outputvariable);

                if (value != null) {
                    workflowManager.activityVariable(wfAssignment.getActivityId(), variable, value);
                }
            }
        }
    }
    
    protected void beanshell(WorkflowAssignment wfAssignment, AppDefinition appDef, Map<String, String> variables) {
        String script = getPropertyString("script");
        if (!script.isEmpty()) {
            Map properties = new HashMap();
            properties.put("wfAssignment", wfAssignment);
            properties.put("appDef", appDef);
            properties.put("variables", variables);

            AppPluginUtil.executeScript(script, properties);
        }
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write(TensorFlowUtil.getEditorScript(request, response));
    }
}
