package org.joget.apps.userview.model;

import java.util.Map;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class UserviewTheme extends ExtElement {

    /**
     * Return css in string
     * @return
     */
    public abstract String getCss();

    /**
     * Return javascript in string
     * @return
     */
    public abstract String getJavascript();

    /**
     * Return header in string
     * @return
     */
    public abstract String getHeader();

    /**
     * Return footer in string
     * @return
     */
    public abstract String getFooter();

    /**
     * Return html on page top in string
     * @return
     */
    public abstract String getPageTop();

    /**
     * Return html on page bottom in string
     * @return
     */
    public abstract String getPageBottom();

    /**
     * Return html on before content in string
     * @return
     */
    public abstract String getBeforeContent();
}
