package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFTextInput implements TensorFlowInput {

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        return TensorFlowUtil.textInput(AppPluginUtil.getVariable(params.get("text").toString(), variables), 
                TensorFlowUtil.getInputStream(AppPluginUtil.getVariable(params.get("dict").toString(), variables), null, null), 
                ((AppPluginUtil.getVariable(params.get("dict").toString(), variables).endsWith("json"))?"json":"csv"), 
                Integer.parseInt(AppPluginUtil.getVariable(params.get("maxlength").toString(), variables)), 
                params.get("datatype").toString(), "true".equalsIgnoreCase(params.get("fillback").toString()));
    }

    @Override
    public String getName() {
        return "text";
    }
    
    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.text");
    }
    
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String label = ResourceBundleUtil.getMessage("app.simpletfai.textvalue");
        String dictionaryLabel = ResourceBundleUtil.getMessage("app.simpletfai.dictionary");
        String chooseFileLabel = ResourceBundleUtil.getMessage("peditor.chooseFile");
        String clearFileLabel = ResourceBundleUtil.getMessage("peditor.clear");
        String maxlengthLabel = ResourceBundleUtil.getMessage("app.simpletfai.maxlength");
        String fillBackLabel = ResourceBundleUtil.getMessage("app.simpletfai.fillBack");
        
        String html = "<select name=\"datatype\" class=\"input_datatype\"></select>";
        html += "<div><input name=\"text\" class=\"input_text full required\" placeholder=\""+label+"\"/></div>";
        html += "<div><input name=\"dict\" class=\"input_dict half required\" placeholder=\""+dictionaryLabel+"\"/> <a class=\"choosefile btn button small\">"+chooseFileLabel+"</a> <a class=\"clearfile btn button small\">"+clearFileLabel+"</a></div>";
        html += "<div><input name=\"maxlength\" class=\"input_maxlength half required\" placeholder=\""+maxlengthLabel+"\"/><label><input name=\"fillback\" class=\"input_fillback truefalse\" type=\"checkbox\" value=\"true\"/> "+fillBackLabel+"<label></div>";
        
        return html;
    }

    @Override
    public String getInitScript() {
        return "";
    }
}