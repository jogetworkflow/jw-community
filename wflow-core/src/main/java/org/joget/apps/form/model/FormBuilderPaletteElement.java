package org.joget.apps.form.model;

/**
 * Interface that describes meta information used for adding an element into the Form Builder palette.
 */
public interface FormBuilderPaletteElement extends FormBuilderEditable {

    /**
     * Category for the element in the Form Builder palette
     * @return
     */
    public String getFormBuilderCategory();

    /**
     * Ordering position. Palette to display based on the position value in ascending order for a category.
     * @return
     */
    public int getFormBuilderPosition();

    /**
     * Path to icon for the element in the Form Builder palette. This path is relative to the context path.
     * @return null to use the default icon.
     */
    public String getFormBuilderIcon();
}
