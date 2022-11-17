package org.joget.apps.app.model;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowProcessResult;

public interface StartProcessFormModifier extends PropertyEditable {
    
    /**
     * Modify the form element to add start process related element
     * 
     * @param form
     * @param formData
     * @param processDefId
     */
    public void modify(Form form, FormData formData, String processDefId);
    
    /**
     * Custom start process form submission handling after form data saved.Return null to start process as usual.
     * 
     * @param form
     * @param formData
     * @param result
     * @return 
     */
    public WorkflowProcessResult customSubmissionHandling(Form form, FormData formData, WorkflowProcessResult result);
}
