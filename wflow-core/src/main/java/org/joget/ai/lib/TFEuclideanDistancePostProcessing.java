package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowPostProcessing;
import org.joget.ai.TensorFlowUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class TFEuclideanDistancePostProcessing implements TensorFlowPostProcessing {

    @Override
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String name = params.get("name").toString();
        String variable = params.get("variable").toString();
        String variable2 = params.get("variable2").toString();
        
        float[] values = (float[]) tfVariables.get(variable);
        float[] values2 = (float[]) tfVariables.get(variable2);
        float distance = TensorFlowUtil.getEuclideanDistance(values, values2);
        tfVariables.put(name, distance);
        
        TensorFlowUtil.debug("Post processing output ("+ name +") : ", distance);
    }

    @Override
    public String getName() {
        return "euclideanDistance";
    }

    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.euclideanDistance");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String variableNameLabel = ResourceBundleUtil.getMessage("app.simpletfai.variableName");
        
        String html = "<div><select name=\"variable\" class=\"post_variable half required\"><option value=\"\">"+variableNameLabel+"</option></select><span class=\"label\">"+variableNameLabel+"</span><select name=\"variable2\" class=\"post_variable half required\"><option value=\"\">"+variableNameLabel+"</option></select><span class=\"label\">"+variableNameLabel+"</span></div>";
        return html;
    }

    @Override
    public String getInitScript() {
        return "";
    }
    
}
