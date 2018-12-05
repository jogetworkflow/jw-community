package org.joget.ai;

import java.io.IOException;
import java.util.Map;

public interface TensorFlowPostProcessing extends TensorFlowElement {
    
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException;
}
