package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFBooleanInput implements TensorFlowInput {

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        return TensorFlowUtil.booleanInput(AppPluginUtil.getVariable(params.get("boolean").toString(), variables));
    }

    @Override
    public String getName() {
        return "boolean";
    }
    
    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.boolean");
    }
    
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String trueLabel = ResourceBundleUtil.getMessage("app.simpletfai.true");
        String falseLabel = ResourceBundleUtil.getMessage("app.simpletfai.false");
        String valueLabel = ResourceBundleUtil.getMessage("app.simpletfai.value");
        return "<select name=\"boolean\" class=\"input_boolean small\"><option value=\"true\">"+trueLabel+"</option><option value=\"false\">"+falseLabel+"</option></select><span class=\"label\">"+valueLabel+"</span>";
    }

    @Override
    public String getInitScript() {
        return "";
    }
}
