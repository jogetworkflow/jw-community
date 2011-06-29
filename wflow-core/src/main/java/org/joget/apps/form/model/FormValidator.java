package org.joget.apps.form.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;

/**
 * Base class for implementations to validate values for an element or form.
 */
public abstract class FormValidator implements Plugin {

    private String className;
    private Map<String, Object> properties;

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }
    /**
     *  Element this validator is tied to
     */
    private Element element;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * Convenience method to get a property value
     * @param property
     * @return
     */
    public Object getProperty(String property) {
        Object value = (properties != null) ? properties.get(property) : null;
        return value;
    }

    /**
     * Convenience method to set a property value
     * @param property
     * @param value
     */
    public void setProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
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
