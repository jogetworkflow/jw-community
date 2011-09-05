package org.joget.apps.form.model;

import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Base class for implementations to validate values for an element or form.
 */
public abstract class FormValidator extends ExtDefaultPlugin implements PropertyEditable {

    /**
     *  Element this validator is tied to
     */
    private Element element;

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * Return the HTML decoration for the attached element, e.g. marking a required field.
     * @return
     */
    public String getElementDecoration() {
        return null;
    }

    /**
     * Validate the submitted values for an element/form
     * @param element
     * @param data
     * @param id ID of the element
     * @param values The values to validate
     * @return true if the validation is successful
     */
    public abstract boolean validate(Element element, FormData data, String[] values);
}
