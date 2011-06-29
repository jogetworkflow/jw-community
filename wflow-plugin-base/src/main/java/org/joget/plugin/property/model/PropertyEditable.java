package org.joget.plugin.property.model;

public interface PropertyEditable {
    /**
     * Label to be display
     * @return
     */
    public String getLabel();

    /**
     * Class name for the element.
     * @return
     */
    public abstract String getClassName();

    /**
     * JSON property options
     * @return
     */
    public String getPropertyOptions();

    /**
     * Default JSON property values
     * @return
     */
    public String getDefaultPropertyValues();
}
