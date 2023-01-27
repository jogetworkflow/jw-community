package org.joget.ai;

import java.io.IOException;
import java.util.Map;
import org.tensorflow.Tensor;

public interface TensorFlowInput extends TensorFlowElement {
    
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException;
}
