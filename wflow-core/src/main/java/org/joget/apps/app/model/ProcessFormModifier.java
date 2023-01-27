package org.joget.apps.app.model;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowAssignment;

public interface ProcessFormModifier extends PropertyEditable {
    
    /**
     * Modify the form element to add process assignment related element
     * 
     * @param form
     * @param formData
     * @param assignment 
     */
    public void modify(Form form, FormData formData, WorkflowAssignment assignment);
    
    /**
     * Custom assignment form submission handling after form data saved. 
     * Return false to complete assignment as usual.
     * 
     * @param form
     * @param formData
     * @param assignment
     * @return 
     */
    public boolean customSubmissionHandling(Form form, FormData formData, WorkflowAssignment assignment);
}
