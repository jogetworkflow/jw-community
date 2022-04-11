package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.workflow.model.DecisionResult;

public class SimpleTensorFlowAIDecisionPlugin extends RulesDecisionPlugin {
    @Override
    public String getName() {
        return "SimpleTensorFlowAIDecisionPlugin";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/simpleTensorFlowAIDecisionPlugin.json", null, true, null);
    }
    
    @Override
    public DecisionResult getDecision(String processDefId, String processId, String routeId, Map<String, String> variables) {
        Map tensorflow = (Map) getProperty("tensorflow");
        
        Map<String, Object> tfvariables = TensorFlowUtil.getEditorResults(tensorflow, processId, variables);
        
        TensorFlowUtil.convertTFVariables(tfvariables, variables);
        
        DecisionResult decision = super.getDecision(processDefId, processId, routeId, variables);
        
        return decision;
    }
    
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mode = SecurityUtil.validateStringInput(request.getParameter("mode"));
        if ("tensorflow".equals(mode)) {
            response.getWriter().write(TensorFlowUtil.getEditorScript(request, response));
        } else {
            response.getWriter().write(AppPluginUtil.getRuleEditorScript(request, response));
        }
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
    
    @Override
    public String getPluginIcon() {
        return "<i class=\"fas fa-robot\"></i>";
    }
}
