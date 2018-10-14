package org.joget.apps.app.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.model.DecisionResult;
import org.joget.workflow.util.WorkflowUtil;
import org.tensorflow.Tensor;

public class SimpleTensorFlowAIDecisionPlugin  extends RulesDecisionPlugin {
    
    protected AppService appService = null;
    protected Map<String, FormRowSet> formDatas = new HashMap<String, FormRowSet>();

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
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        
        String processId = "";
        String actId = "";
        if (request.getRequestURL().indexOf("/plugin/configure") != -1) {
            String[] parts = request.getRequestURL().toString().split("/");
            processId = parts[10];
            actId = parts[12];
        }
        
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/simpleTensorFlowAIDecisionPlugin.json", new String[]{processId, actId, processId, actId}, true, null);
    }

    public AppService getAppService() {
        if (appService == null) {
            appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        }
        return appService;
    }
    
    @Override
    public DecisionResult getDecision(String processDefId, String processId, String routeId, Map<String, String> variables) {
        Map tensorflow = (Map) getProperty("tensorflow");
        
        Map<String, Object> tfvariables = new HashMap<String, Object>();
        
        if (tensorflow != null) {
            try {
                Object[] sessions = (Object[]) tensorflow.get("sessions");
                if (sessions != null && sessions.length > 0) {
                    for (Object sessionObj : sessions) {
                        Map session = (Map) sessionObj;
                        runSession(session, tfvariables, variables, processId);
                    }
                }

                Object[] postProcessingList = (Object[]) tensorflow.get("postProcessing");
                if (postProcessingList != null && postProcessingList.length > 0) {
                    for (Object postProcessingObj : postProcessingList) {
                        Map postProcessing = (Map) postProcessingObj;
                        runPostProcessing(postProcessing, tfvariables, variables);
                    }
                }
            } catch (Exception e) {
                //catch exception to let the rules checking still run if error
            }
        }
        
        convertTFVariables(tfvariables, variables);
        
        DecisionResult decision = super.getDecision(processDefId, processId, routeId, variables);
        
        return decision;
    }
    
    protected boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
    
    protected InputStream getInputStream(String filename, String formId, String recordId) {
        try {
            if (formId != null && !formId.isEmpty()) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                File file = FileUtil.getFile(filename, getAppService().getFormTableName(appDef, formId), recordId);
                return new FileInputStream(file);
            } else if (isValidURL(filename)) {
                return new URL(filename).openStream();
            } else {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                return new FileInputStream(AppResourceUtil.getFile(appDef.getAppId(), appDef.getVersion().toString(), filename));
            }
        } catch (Exception e) {
            LogUtil.debug(getClassName(), "Fail to open " + filename);
        }
        return null;
    }
    
    protected Map<String, Tensor> getInputs(Object[] inputs, String processId, Map<String, String> variables) throws IOException {
        Map<String, Tensor> inputMap = new HashMap<String, Tensor>();
        
        if (inputs != null && inputs.length > 0) {
            String filename = "";
            String form = "";
            String recordId = "";
            String type = "";
            FormRowSet rows = null;
            for (Object inputObj : inputs) {
                Map input = (Map) inputObj;
                if ("image".equalsIgnoreCase(input.get("type").toString())) {
                    filename = getVariable(input.get("image").toString(), variables);
                    form = getVariable(input.get("form").toString(), variables);
                    if (!form.isEmpty()) {
                        recordId = getAppService().getOriginProcessId(processId);
                        if (!formDatas.containsKey(form+"::"+recordId)) {
                            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                            rows = getAppService().loadFormData(appDef.getAppId(), appDef.getVersion().toString(), form, recordId);
                            if (rows != null) {
                                formDatas.put(form+"::"+recordId, rows);
                            }
                        }
                        rows = formDatas.get(form+"::"+recordId);
                        if (rows != null && !rows.isEmpty()) {
                            filename = rows.get(0).getProperty(filename);
                        }
                    }
                    type = FilenameUtils.getExtension(filename);
                    inputMap.put(getVariable(input.get("name").toString(), variables), TensorFlowUtil.imageInput(getInputStream(filename, form, processId), type, Integer.parseInt(getVariable(input.get("height").toString(), variables)), Integer.parseInt(getVariable(input.get("width").toString(), variables)), Float.parseFloat(getVariable(input.get("mean").toString(), variables)), Float.parseFloat(getVariable(input.get("scale").toString(), variables)), input.get("datatype").toString()));
                } else if ("text".equalsIgnoreCase(input.get("type").toString())) {
                    inputMap.put(getVariable(input.get("name").toString(), variables), TensorFlowUtil.textInput(getVariable(input.get("text").toString(), variables), getInputStream(getVariable(input.get("dict").toString(), variables), null, null), ((getVariable(input.get("dict").toString(), variables).endsWith("json"))?"json":"csv"), Integer.parseInt(getVariable(input.get("maxlength").toString(), variables)), input.get("datatype").toString(), "true".equalsIgnoreCase(input.get("fillback").toString())));
                } else if ("numbers".equalsIgnoreCase(input.get("type").toString())) {
                    inputMap.put(getVariable(input.get("name").toString(), variables), TensorFlowUtil.numbersInput(getVariable(input.get("numbers").toString(), variables), input.get("datatype").toString()));
                } else if ("boolean".equalsIgnoreCase(input.get("type").toString())) {
                    inputMap.put(getVariable(input.get("name").toString(), variables), TensorFlowUtil.booleanInput(getVariable(input.get("boolean").toString(), variables)));
                }
            }
        }
        
        return inputMap;
    }
    
    protected String[] getOutputNames(Object[] outputs) {
        Collection<String> names = new ArrayList<String>();
        
        if (outputs != null && outputs.length > 0) {
            for (Object outputObj : outputs) {
                Map output = (Map) outputObj;
                names.add(output.get("name").toString());
            }
        }
        
        return names.toArray(new String[0]);
    }
    
    protected void outputToTfVariables(Object[] outputs, Map<String, float[]> outputVariables, Map<String, Object> tfvariables) {
        if (outputs != null && outputs.length > 0) {
            for (Object outputObj : outputs) {
                Map output = (Map) outputObj;
                tfvariables.put(output.get("variable").toString(), outputVariables.get(output.get("name").toString()));
            }
        }
    }
    
    protected void runSession(Map session, Map<String, Object> tfvariables, Map<String, String> variables, String processId) {
        InputStream model = getInputStream(getVariable(session.get("model").toString(), variables), null, null);
        try {
            Map<String, Tensor> inputMap = getInputs((Object[]) session.get("inputs"), processId, variables);
            Map<String, float[]> outputVariables = TensorFlowUtil.executeSimpleTensorFlowModel(model, inputMap, getOutputNames((Object[]) session.get("outputs")));
            outputToTfVariables((Object[]) session.get("outputs"), outputVariables, tfvariables);
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        } finally {
            if (model != null) {
                try {
                    model.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    protected void runPostProcessing(Map postProcessing, Map<String, Object> tfvariables, Map<String, String> variables) {
        String name = getVariable(postProcessing.get("name").toString(), variables);
        String type = postProcessing.get("type").toString();
        String variable = postProcessing.get("variable").toString();
        try {
            float[] values = (float[]) tfvariables.get(variable);
            if ("labels".equalsIgnoreCase(type)) {
                Map<Float, String> resultMap = TensorFlowUtil.getSortedLabelResultMap(getInputStream(getVariable(postProcessing.get("labels").toString(), variables), null, null), values, Float.parseFloat(getVariable(postProcessing.get("threshold").toString(), variables)));
                if (!resultMap.isEmpty()) {
                    String value = "";
                    for (Float probability: resultMap.keySet()) {
                        if (!value.isEmpty()) {
                            value += ";";
                        }
                        value += resultMap.get(probability);
                        
                        if (postProcessing.get("toplabel") != null && "true".equalsIgnoreCase(postProcessing.get("toplabel").toString())) {
                            break;
                        }
                    }
                    tfvariables.put(name, value);
                    debug("Post processing output ("+ name +") : ", value);
                } else {
                    tfvariables.put(name, "");
                    debug("Post processing output ("+ name +") : ", null);
                }
            } else if ("euclideanDistance".equalsIgnoreCase(type)) {
                String variable2 = postProcessing.get("variable2").toString();
                float[] values2 = (float[]) tfvariables.get(variable2);
                float distance = TensorFlowUtil.getEuclideanDistance(values, values2);
                tfvariables.put(name, distance);
                debug("Post processing output ("+ name +") : ", distance);
            } else if ("valuelabel".equalsIgnoreCase(type)) {
                String variable2 = postProcessing.get("variable2").toString();
                Integer number = null;
                
                if (!variable2.isEmpty()) {
                    float[] values2 = (float[]) tfvariables.get(variable2);
                    number = (int) values2[0];
                }
                Boolean unique = postProcessing.get("unique") != null && "true".equalsIgnoreCase(postProcessing.get("unique").toString());
                
                List<String> labels = TensorFlowUtil.getValueToLabelList(getInputStream(getVariable(postProcessing.get("labels").toString(), variables), null, null), values, number, unique);
                String labelsStr = String.join(";", labels);
                tfvariables.put(name, labelsStr);
                debug("Post processing output ("+ name +") : ", labelsStr);
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Fail to run post processing : " + name);
        }
    }
    
    protected void convertTFVariables(Map<String, Object> tfvariables, Map<String, String> variables) {
        for (String name : tfvariables.keySet()) {
            if (tfvariables.get(name) instanceof String || tfvariables.get(name) instanceof Float) {
                variables.put(name, tfvariables.get(name).toString());
            } else if (tfvariables.get(name) instanceof float[]) {
                String value = "";
                DecimalFormat df = new DecimalFormat("0.00");
                for (Float f : (float[]) tfvariables.get(name)) {
                    if (!value.isEmpty()) {
                        value += ";";
                    }
                    value += df.format(f);
                }
                variables.put(name, value);
            }
        }
        debug("Variables past to rules", variables);
    }
    
    protected void debug(String message, Object obj) {
        if (LogUtil.isDebugEnabled(getClassName())) {
            if (obj != null) {
                message += " : " + TensorFlowUtil.outputToJson(obj);
            }
            LogUtil.debug(getClassName(), message);
        }
    }
    
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mode = SecurityUtil.validateStringInput(request.getParameter("mode"));
        if ("tensorflow".equals(mode)) {
            String forms = "{\"value\":\"\", \"label\":\"" + StringUtil.escapeString(ResourceBundleUtil.getMessage("app.simpletfai.retrieveFromUrl"), StringUtil.TYPE_JSON, null) + "\"}";
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
            Collection<FormDefinition> formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, null, null, null, null);
            if (formDefinitionList != null && !formDefinitionList.isEmpty()) {
                for (FormDefinition f : formDefinitionList) {
                    forms += ", {\"value\":\"" + f.getId() + "\", \"label\":\"" + StringUtil.escapeString(f.getName(), StringUtil.TYPE_JSON, null) + ResourceBundleUtil.getMessage("app.simpletfai.form") + "\"}";
                }
            }
            
            response.getWriter().write(AppUtil.readPluginResource(getClass().getName(), "/properties/app/tensorflowEditor.js", new String[]{forms}, false, null));
        } else {
            response.getWriter().write(AppPluginUtil.getRuleEditorScript(this, request, response));
        }
    }
}
