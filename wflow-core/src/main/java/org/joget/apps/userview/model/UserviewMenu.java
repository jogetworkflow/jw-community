package org.joget.apps.userview.model;

public abstract class UserviewMenu extends ExtElement{

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
}
