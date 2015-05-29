package org.joget.apps.form.model;

import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Base class for form load and store binders
 * @author julian
 */
public abstract class FormBinder extends ExtDefaultPlugin implements PropertyEditable {

    public static final String FORM_LOAD_BINDER = "loadBinder";
    public static final String FORM_OPTIONS_BINDER = "optionsBinder";
    public static final String FORM_STORE_BINDER = "storeBinder";
    private Element element;
    private Map<String, Object> properties;

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
