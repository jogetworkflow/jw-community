package org.joget.apps.userview.model;

public abstract class UserviewMenu extends ExtElement{

    public static final String REDIRECT_URL_PROPERTY = "userviewRedirectUrl";
    public static final String REDIRECT_PARENT_PROPERTY = "userviewRedirectParent";
    public static final String ALERT_MESSAGE_PROPERTY = "userviewAlertMessage";
    
    private String url;
    private String key;
    private String readyJspPage;
    private String readyRenderPage;
    private Userview userview;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Category to be displayed
     * @return
     */
    public abstract String getCategory();

    /**
     * Icon path to be displayed
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
     * Get Decorated menu html for rendering
     * @return
     */
    public abstract String getDecoratedMenu();

    /**
     * Get menu html for rendering
     * @return
     */
    public String getMenu() {
        String decoratedMenu = getDecoratedMenu();
        if (decoratedMenu == null || (decoratedMenu != null && decoratedMenu.trim().length() == 0)) {
            return "<a href='" + getUrl() + "' class='menu-link default'><span>" + getPropertyString("label") + "</span></a>";
        } else {
            return decoratedMenu;
        }
    }

    /**
     * Get render HTML template for UI in jsp file
     * @return
     */
    public String getJspPage() {
        return null;
    }

    public String getReadyJspPage() {
        if (readyJspPage == null) {
            readyJspPage = getJspPage();
        }
        return readyJspPage;
    }

    public String getReadyRenderPage() {
        if (readyRenderPage == null) {
            readyRenderPage = getRenderPage();
        }
        return readyRenderPage;
    }

    public Userview getUserview() {
        return userview;
    }

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
     * Set this property to force the userview to redirect to a specific URL.
     * @param redirectUrl 
     * @param redirectToParent set true to force redirection in parent frame.
     */
    public void setRedirectUrl(String redirectUrl, boolean redirectToParent) {
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
