package org.joget.apps.workflow.lib;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

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
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Assignment Withdraw Button";
    }

    @Override
    public FormData actionPerformed(Form form, FormData formData) {
        Logger.getLogger(AssignmentWithdrawButton.class.getName()).log(Level.INFO, " -- AssignmentWithdrawButton actionPerformed " + FormUtil.getElementParameterName(this));
        formData.addFormResult(DEFAULT_ID, "true");
        return formData;
    }
}
