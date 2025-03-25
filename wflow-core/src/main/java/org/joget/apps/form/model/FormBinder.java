package org.joget.apps.form.model;

import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * A base abstract class to develop a Form Load Binder, Form Options Binder or Form Store Binder plugin. 
 * @author julian
 */
public abstract class FormBinder extends ExtDefaultPlugin implements PropertyEditable {

    public static final String FORM_LOAD_BINDER = "loadBinder";
    public static final String FORM_OPTIONS_BINDER = "optionsBinder";
    public static final String FORM_STORE_BINDER = "storeBinder";
    private Element element; 

    /**
     * Gets the form field element which owns this binder
     * @return 
     */
    public Element getElement() {
        return element;
    }

    /**
     * Sets the form field element which owns this binder
     * @param element 
     */
    public void setElement(Element element) {
        this.element = element;
    }
}
