package org.joget.apps.userview.model;

import org.joget.commons.util.StringUtil;

/**
 * A base abstract class to develop a Userview Menu plugin. 
 * 
 */
public abstract class UserviewMenu extends ExtElement{

    public static final String REDIRECT_URL_PROPERTY = "userviewRedirectUrl";
    public static final String REDIRECT_PARENT_PROPERTY = "userviewRedirectParent";
    public static final String ALERT_MESSAGE_PROPERTY = "userviewAlertMessage";
    
    private String url;
    private String key;
    private String readyJspPage;
    private String readyRenderPage;
    private Userview userview;

    /**
     * Gets URL of this menu
     * 
     * @return 
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets URL of this menu
     * 
     * @param url 
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets userview key of this menu
     * 
     * @return 
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets userview key of this menu
     * 
     * @return 
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Category to be displayed in Userview Builder palette 
     * @return
     */
    public abstract String getCategory();

    /**
     * Icon path to be displayed in Userview Builder palette 
     * @return
     */
    public abstract String getIcon();

    /**
     * Get render HTML template for UI
     * @return
     */
    public abstract String getRenderPage();

    /**
     * Used to determine this menu item can used as home page or not.
     * @return
     */
    public abstract boolean isHomePageSupported();

    /**
     * Get Decorated menu HTML for rendering
     * @return
     */
    public abstract String getDecoratedMenu();

    /**
     * Get menu html for rendering. It will call getDecoratedMenu method 
     * to retrieve the menu HTML. If empty value is return, a default menu 
     * HTML will be generated based on getURL method and "label" property.
     * @return
     */
    public String getMenu() {
        // sanitize output if not decorated. Otherwise need to sanitize in individial plugins
        String decoratedMenu = getDecoratedMenu();
        if (decoratedMenu == null || (decoratedMenu != null && decoratedMenu.trim().length() == 0)) {
            // sanitize label
            String label = getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }
            return "<a href='" + getUrl() + "' class='menu-link default'><span>" + label + "</span></a>";
        } else {
            return decoratedMenu;
        }
    }

    /**
     * Get path of JSP file to render the HTML template. 
     * 
     * If this value is not NULL, value returned by getRenderPage will be ignored.
     * It is used to use the system predefined template for rendering. 
     * Options are as following:
     *    - userview/plugin/datalist.jsp
     *    - userview/plugin/form.jsp
     *    - userview/plugin/runProcess.jsp
     *    - userview/plugin/unauthorized.jsp
     * @return
     */
    public String getJspPage() {
        return null;
    }

    /**
     * Used by the system to retrieve the JSP file page to avoid the logic to run again.
     * It will called the getJspPage method once to initial the value.
     * 
     * @return 
     */
    public String getReadyJspPage() {
        if (readyJspPage == null) {
            readyJspPage = getJspPage();
        }
        return readyJspPage;
    }

    /**
     * Used by the system to retrieve the HTML template to avoid the logic to run again.
     * It will called the getRenderPage method once to initial the value.
     * 
     * @return 
     */
    public String getReadyRenderPage() {
        if (readyRenderPage == null) {
            readyRenderPage = getRenderPage();
        }
        return readyRenderPage;
    }

    /**
     * Gets the userview which this menu is belongs to.
     * @return 
     */
    public Userview getUserview() {
        return userview;
    }

    /**
     * Sets the userview which this menu is belongs to.
     * @param userview 
     */
    public void setUserview(Userview userview) {
        this.userview = userview;
    }
    
    /**
     * Set this property to force the userview to redirect to a specific URL.
     * @param redirectUrl 
     */
    public void setRedirectUrl(String redirectUrl) {
        setRedirectUrl(redirectUrl, false);
    }
    
    /**
     * Set this property to force the userview to redirect to a specific URL 
     * with option to redirect in the parent window
     * 
     * @param redirectUrl 
     * @param redirectToParent set true to force redirection in parent frame.
     */
    public void setRedirectUrl(String redirectUrl, boolean redirectToParent) {
        if (redirectToParent && !redirectUrl.startsWith("/") && !redirectUrl.startsWith("http") && !redirectUrl.contains("embed=")) {
            if (!redirectUrl.startsWith("javascript")) {
                if (redirectUrl.contains("?")) {
                    redirectUrl += "&embed=false";
                } else {
                    redirectUrl += "?embed=false";
                }
            }
        }
        
        setProperty(REDIRECT_URL_PROPERTY, redirectUrl);
        setProperty(REDIRECT_PARENT_PROPERTY, Boolean.valueOf(redirectToParent).toString());
    }
    
    /**
     * Set this property to display an alert message/prompt.
     * @param message
     */
    public void setAlertMessage(String message) {
        setProperty(ALERT_MESSAGE_PROPERTY, message);
    }
}
