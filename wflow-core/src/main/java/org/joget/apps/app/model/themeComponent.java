package org.joget.apps.app.model;

public interface themeComponent {

    /**
     * Return plugin label. This value will be used when a Resource Bundle
     * Message Key "<i>plugin.className</i>.pluginlabel" is not found by
     * getI18nLabel() method.
     *
     * @return
     */
    public String getLabel();

    /**
     * Return Class Name for the plugin.
     *
     * @return
     */
    public String getClassName();

    /**
     * Return Class Category for the plugin.
     *
     * @return
     */
    public String getCategory();

    /**
     * Return the plugin properties options in JSON format.
     *
     * @return
     */
    public String getPropertyOptions();

    /**
     * Return the html to render the element in component view
     *
     * @return
     */
    public String render();

    /**
     * Return the prefix to render the component css variable
     * style if possible
     *
     * @return
     */
    public String getPrefix();

}
