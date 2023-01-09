package org.joget.apps.workflow.lib;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormData;

/**
 * Form button to complete a workflow assignment
 */
public class AssignmentCompleteButton extends FormButton {

    public static final String DEFAULT_ID = "assignmentComplete";

    @Override
    public String getName() {
        return "Assignment Complete Button";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Assignment Complete Button";
    }

    @Override
    public FormData actionPerformed(Form form, FormData formData) {
        formData.addFormResult(DEFAULT_ID, "true");
        return formData;
    }

    public String getLabel() {
        return "Assignment Complete Button";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
