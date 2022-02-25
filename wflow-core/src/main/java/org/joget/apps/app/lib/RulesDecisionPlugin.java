package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.DecisionPluginDefault;
import org.joget.workflow.model.DecisionResult;
import org.joget.workflow.util.WorkflowUtil;

public class RulesDecisionPlugin extends DecisionPluginDefault implements PluginWebSupport {

    @Override
    public String getName() {
        return "RulesDecisionPlugin";
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
        return "Simple Rules";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        
        String processId = "";
        String actId = "";
        if (request.getRequestURL().indexOf("/plugin/configure") != -1) {
            String[] parts = request.getRequestURL().toString().split("/");
            processId = parts[10];
            actId = parts[12];
        }
        
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/rulesDecisionPlugin.json", new String[]{processId, actId}, true, null);
    }

    public DecisionResult getDecision(String processDefId, String processId, String routeId, Map<String, String> variables) {
        DecisionResult result = null;
        
        Map rules = (Map) getProperty("rules");
        
        if (rules != null) {
            boolean ruleMatch = false;
            
            Object[] ifrules = (Object[]) rules.get("ifrules");
            if (ifrules != null && ifrules.length > 0) {
                for (Object ruleGroupObj : ifrules) {
                    Map ruleGroup = (Map) ruleGroupObj;
                    if (checkConditions((Map) ruleGroup, variables)) {
                        Object[] actions = (Object[]) ruleGroup.get("actions");
                        result = getResult(actions, variables);
                        ruleMatch = true;
                        break;
                    }
                }
            }
            
            if (!ruleMatch) {
                Object[] elseActions = (Object[]) rules.get("else");
                result = getResult(elseActions, variables);
            }
        }
        
        return result;
    }
    
    public boolean checkConditions(Map ruleGroup, Map<String, String> variables) {
        boolean result = true;
        if ("or".equals(ruleGroup.get("andOr"))) {
            result = false;
        }
        
        Object[] conditions = (Object[]) ruleGroup.get("conditions");
        if (conditions != null && conditions.length > 0) {
            boolean temp = true;
            for (Object conditionObj : conditions) {
                Map condition = (Map) conditionObj;
                if (condition.containsKey("conditions")) {
                    temp = checkConditions(condition, variables);
                } else {
                    temp = checkCondition(condition, variables);
                }
                
                if ("or".equals(ruleGroup.get("andOr"))) {
                    result = result || temp;
                    
                    if (result) {
                        break;
                    }
                } else {
                    result = result && temp;
                    if (!result) {
                        break;
                    }
                }
            }
        }
        
        if ("true".equalsIgnoreCase(ruleGroup.get("revert").toString())) {
            return !result;
        } else {
            return result;
        }
    }
    
    public boolean checkCondition(Map rule, Map<String, String> variables) {
        String variable = (String) rule.get("variable");
        String operation = (String) rule.get("operation");
        String value = (String) rule.get("value");
        boolean result = false;
        
        variable = AppPluginUtil.getVariable(variable, variables);
        
        if (variable != null) {
            Double variableNumber = null;
            Double valueNumber = null;
            boolean isNumeric = false;
            if (!variable.isEmpty() && value != null && !value.isEmpty()) {
                try {
                    variableNumber = Double.parseDouble(variable);
                    valueNumber = Double.parseDouble(value);
                    isNumeric = true;
                } catch (Exception e) {
                    //ignore
                }
            }
        
            if (isNumeric) {
                int compare = Double.compare(variableNumber, valueNumber);
                if ("==".equals(operation)) {
                    result = compare == 0;
                } else if (">".equals(operation)) {
                    result = compare > 0;
                } else if (">=".equals(operation)) {
                    result = compare >= 0;
                } else if ("<".equals(operation)) {
                    result = compare < 0;
                } else if ("<=".equals(operation)) {
                    result = compare <= 0;
                }
            } else {
                if ("==".equals(operation)) {
                    result = variable.equals(value);
                } else if ("true".equals(operation)) {
                    result = variable.equalsIgnoreCase("true") || variable.equals("1");
                } else if ("false".equals(operation)) {
                    result = variable.equalsIgnoreCase("false") || variable.equals("0");
                } else if ("contains".equals(operation)) {
                    result = variable.contains(value);
                } else if ("listContains".equals(operation)) {
                    String[] list = variable.split(";");
                    result = ArrayUtils.contains(list, value);
                } else if ("in".equals(operation)) {
                    String[] list = value.split(";");
                    result = ArrayUtils.contains(list, variable);
                } else if ("regex".equals(operation)) {
                    result = variable.matches(StringEscapeUtils.unescapeJavaScript(value));
                }
            }
        }
        
        return ("true".equalsIgnoreCase(rule.get("revert").toString()))?!result:result;
    }
    
    public DecisionResult getResult(Object[] actions, Map<String, String> variables) {
        DecisionResult result = null;
        if (actions != null && actions.length > 0) {
            result = new DecisionResult();
            String name = null;
            String value = null;
            for (Object actionObj : actions) {
                Map action = (Map) actionObj;
                name = (String) action.get("name");
                if ("transition".equals(action.get("type"))) {
                    if (name != null && !name.isEmpty()) {
                        result.addTransition(name);
                    }
                } else {
                    value = (String) action.get("value");
                    value = AppPluginUtil.getVariable(value, variables);
                    
                    if (value == null) {
                        value = "";
                    }
                    
                    if (name != null && !name.isEmpty()) {
                        result.setVariable(name, value);
                    }
                }
            }
            
            if (result.getTransitions().size() > 1) {
                result.setIsAndSplit(true);
            }
        }
        
        return result;
    }
    
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write(AppPluginUtil.getRuleEditorScript(request, response));
    }
}
