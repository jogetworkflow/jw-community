package org.joget.apps.workflow.lib;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormData;

/**
 * Form button to withdraw a workflow assignment
 */
public class AssignmentWithdrawButton extends FormButton {

    public static final String DEFAULT_ID = "assignmentWithdraw";

    @Override
    public String getName() {
        return "Assignment Withdraw Button";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Assignment Withdraw Button";
    }

    @Override
    public FormData actionPerformed(Form form, FormData formData) {
        formData.addFormResult(DEFAULT_ID, "true");
        return formData;
    }

    public String getLabel() {
        return "Assignment Withdraw Button";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
