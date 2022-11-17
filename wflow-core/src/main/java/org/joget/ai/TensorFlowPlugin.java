package org.joget.ai;

public interface TensorFlowPlugin {
    
    public TensorFlowInput[] getInputClasses();
    
    public TensorFlowPostProcessing[] getPostProcessingClasses();
}
