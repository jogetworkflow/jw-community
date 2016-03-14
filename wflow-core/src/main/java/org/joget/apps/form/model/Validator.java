package org.joget.apps.form.model;

import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Base class for implementations to validate values for an element.
 */
public abstract class Validator extends ExtDefaultPlugin implements PropertyEditable {

    private Element element;

    /**
     *  Gets Element that this validator is tied to.
     */
    public Element getElement() {
        return element;
    }

    /**
     *  Sets Element that this validator is tied to.
     */
    public void setElement(Element element) {
        this.element = element;
    }
    
    /**
     * Return the HTML decoration for the attached element, e.g. marking a required field.
     * @return
     */
    public String getElementDecoration(Element element, FormData formData) {
        return getElementDecoration();
    }

    /**
     * Return the HTML decoration for the attached element, e.g. marking a required field.
     * @return
     */
    public String getElementDecoration() {
        return null;
    }

    /**
     * Validate the submitted values for an element
     * @param element
     * @param data
     * @param values The values to validate
     * @return true if the validation is successful
     */
    public abstract boolean validate(Element element, FormData data, String[] values);
}
