package org.joget.ai.lib;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.joget.ai.TensorFlowPostProcessing;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class TFValueLabelPostProcessing implements TensorFlowPostProcessing {

    @Override
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String name = (String) params.get("name");
        String variable = (String) params.get("variable");
        
        float[] values = (float[]) tfVariables.get(variable);
        String variable2 = (String) params.get("variable2");
        Integer number = null;
        
        if (variable2 != null && !variable2.isEmpty()) {
            float[] values2 = (float[]) tfVariables.get(variable2);
            number = (int) values2[0];
        }
        Boolean unique = params.get("unique") != null && "true".equalsIgnoreCase((String) params.get("unique"));
        
        Float threshold = null;
        try {
            threshold = Float.parseFloat(AppPluginUtil.getVariable((String) params.get("threshold"), variables));
        } catch (Exception e) {}
        
        float[] scores = null;
        String variable3 = (String) params.get("variable3");
        if (variable3 != null && !variable3.isEmpty()) {
            scores = (float[]) tfVariables.get(variable3);
        }
        
        List<String> labels = TensorFlowUtil.getValueToLabelList(TensorFlowUtil.getInputStream(AppPluginUtil.getVariable((String) params.get("labels"), variables), null, null), values, number, unique, threshold, scores);
        String labelsStr = String.join(";", labels);
        tfVariables.put(name, labelsStr);
        TensorFlowUtil.debug("Post processing output ("+ name +") : ", labelsStr);
    }

    @Override
    public String getName() {
        return "valuelabel";
    }

    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.valuelabel");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String uniqueLabel = ResourceBundleUtil.getMessage("app.simpletfai.unique");
        String labelsFileLabel = ResourceBundleUtil.getMessage("app.simpletfai.labels_file");
        String chooseFileLabel = ResourceBundleUtil.getMessage("peditor.chooseFile");
        String clearFileLabel = ResourceBundleUtil.getMessage("peditor.clear");
        String variableNameLabel = ResourceBundleUtil.getMessage("app.simpletfai.detectedClasses");
        String numberLabel = ResourceBundleUtil.getMessage("app.simpletfai.numberOfValues");
        String scoreLabel = ResourceBundleUtil.getMessage("app.simpletfai.score");
        String thresholdLabel = ResourceBundleUtil.getMessage("app.simpletfai.scoreThreshold");
        
        String html = "<label><input name=\"unique\" class=\"post_unique truefalse\" type=\"checkbox\" value=\"true\"/> "+uniqueLabel+"</label>";
        html += "<div><input name=\"labels\" class=\"post_labels half required\" placeholder=\""+labelsFileLabel+"\"/><span class=\"label\">"+labelsFileLabel+"</span> <a class=\"choosefile btn button small\">"+chooseFileLabel+"</a> <a class=\"clearfile btn button small\">"+clearFileLabel+"</a></div>";
        html += "<div><select name=\"variable\" class=\"post_variable half required\"><option value=\"\">"+variableNameLabel+"</option></select><span class=\"label\">"+variableNameLabel+"</span><select name=\"variable2\" class=\"post_variable half\"><option value=\"\">"+numberLabel+"</option></select><span class=\"label\">"+numberLabel+"</span></div>";
        html += "<div><select name=\"variable3\" class=\"post_variable half\"><option value=\"\">"+scoreLabel+"</option></select><span class=\"label\">"+scoreLabel+"</span><input name=\"threshold\" class=\"post_threshold falf\" placeholder=\""+thresholdLabel+"\"/><span class=\"label\">"+thresholdLabel+"</span></div>";
        return html;
    }

    @Override
    public String getInitScript() {
        return "";
    }
}
