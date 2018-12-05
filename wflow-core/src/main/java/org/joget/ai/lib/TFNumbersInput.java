package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFNumbersInput implements TensorFlowInput {

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        return TensorFlowUtil.numbersInput(AppPluginUtil.getVariable(params.get("numbers").toString(), variables), params.get("datatype").toString());
    }

    @Override
    public String getName() {
        return "numbers";
    }
    
    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.numbers");
    }
    
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String label = ResourceBundleUtil.getMessage("app.simpletfai.numbervalues");
        return "<select name=\"datatype\" class=\"input_datatype\"></select><div><input name=\"numbers\" class=\"input_numbers full required\" placeholder=\""+label+"\"/></div>";
    }

    @Override
    public String getInitScript() {
        return "";
    }
}