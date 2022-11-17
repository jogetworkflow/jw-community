package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowPostProcessing;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class TFLabelsPostProcessing implements TensorFlowPostProcessing {

    @Override
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String name = params.get("name").toString();
        String variable = params.get("variable").toString();
        
        float[] values = (float[]) tfVariables.get(variable);
        Map<Float, String> resultMap = TensorFlowUtil.getSortedLabelResultMap(
                TensorFlowUtil.getInputStream(AppPluginUtil.getVariable(params.get("labels").toString(), variables), null, null), 
                values, Float.parseFloat(AppPluginUtil.getVariable(params.get("threshold").toString(), variables)));
        
        if (!resultMap.isEmpty()) {
            String value = "";
            for (Float probability: resultMap.keySet()) {
                if (!value.isEmpty()) {
                    value += ";";
                }
                value += resultMap.get(probability);

                if (params.get("toplabel") != null && "true".equalsIgnoreCase(params.get("toplabel").toString())) {
                    break;
                }
            }
            tfVariables.put(name, value);
            TensorFlowUtil.debug("Post processing output ("+ name +") : ", value);
        } else {
            tfVariables.put(name, "");
            TensorFlowUtil.debug("Post processing output ("+ name +") : ", null);
        }
    }

    @Override
    public String getName() {
        return "labels";
    }

    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.labels");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String thresholdLabel = ResourceBundleUtil.getMessage("app.simpletfai.threshold");
        String toplabelLabel = ResourceBundleUtil.getMessage("app.simpletfai.toplabel");
        String labelsFileLabel = ResourceBundleUtil.getMessage("app.simpletfai.labels_file");
        String chooseFileLabel = ResourceBundleUtil.getMessage("peditor.chooseFile");
        String clearFileLabel = ResourceBundleUtil.getMessage("peditor.clear");
        String variableNameLabel = ResourceBundleUtil.getMessage("app.simpletfai.variableName");
        
        String html = "<input name=\"threshold\" class=\"post_threshold small required\" placeholder=\""+thresholdLabel+"\"/><span class=\"label\">"+thresholdLabel+"</span>";
        html += "<label><input name=\"toplabel\" class=\"post_toplabel truefalse\" type=\"checkbox\" value=\"true\"/> "+toplabelLabel+"</label>";
        html += "<div><input name=\"labels\" class=\"post_labels half required\" placeholder=\""+labelsFileLabel+"\"/><span class=\"label\">"+labelsFileLabel+"</span> <a class=\"choosefile btn button small\">"+chooseFileLabel+"</a> <a class=\"clearfile btn button small\">"+clearFileLabel+"</a></div>";
        html += "<div><select name=\"variable\" class=\"post_variable half required\"><option value=\"\">"+variableNameLabel+"</option></select><span class=\"label\">"+variableNameLabel+"</span></div>";
        
        return html;
    }

    @Override
    public String getInitScript() {
        return "";
    }
    
}
