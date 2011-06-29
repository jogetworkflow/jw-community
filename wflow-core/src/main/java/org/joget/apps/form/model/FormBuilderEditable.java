package org.joget.apps.form.model;

import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface that describes meta information used for configuring in the Form Builder.
 */
public interface FormBuilderEditable extends PropertyEditable {

    /**
     * HTML template used for display in the Form Builder
     * @return
     */
    public String getFormBuilderTemplate();
}
