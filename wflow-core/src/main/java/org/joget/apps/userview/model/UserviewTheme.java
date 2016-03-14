package org.joget.apps.userview.model;

/**
 * A base abstract class to develop a Userview Theme plugin. 
 * 
 */
public abstract class UserviewTheme extends ExtElement {
    protected Userview userview;

    /**
     * Gets userview which using this theme
     * @return 
     */
    public Userview getUserview() {
        return userview;
    }

    /**
     * Sets userview which using this theme
     * @param userview 
     */
    public void setUserview(Userview userview) {
        this.userview = userview;
    }
    
    /**
     * Return css to inject in &lt;head&gt; tag
     * @return
     */
    public abstract String getCss();

    /**
     * Return javascript to inject in &lt;head&gt; tag
     * @return
     */
    public abstract String getJavascript();

    /**
     * Return HTML template to replace default header
     * @return
     */
    public abstract String getHeader();

    /**
     * Return HTML template to replace default footer
     * @return
     */
    public abstract String getFooter();

    /**
     * Return HTML template to inject before the page container
     * @return
     */
    public abstract String getPageTop();

    /**
     * Return HTML template to inject after the page container
     * @return
     */
    public abstract String getPageBottom();

    /**
     * Return HTML template to inject before content
     * @return
     */
    public abstract String getBeforeContent();
}
