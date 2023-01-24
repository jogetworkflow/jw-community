package org.joget.apps.userview.model;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.joget.plugin.base.PluginManager;
import org.springframework.context.MessageSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import org.xhtmlrenderer.util.LoggerUtil;

/**
 * A base abstract class to develop a Userview Theme plugin for version v5.0 onward.
 * 
 */
public abstract class UserviewV5Theme extends UserviewTheme {
    
    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getCss() {
        //is not using anymore
        return null;
    } 
    
    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getJavascript() {
        //is not using anymore
        return null;
    } 

    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getHeader() {
        //is not using anymore
        return null;
    } 

    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getFooter() {
        //is not using anymore
        return null;
    } 
    
    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getPageTop() {
        //is not using anymore
        return null;
    } 

    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getPageBottom() {
        //is not using anymore
        return null;
    } 

    /**
     * @Deprecated not use for UserviewV5Theme
     * 
     * @return 
     */
    public String getBeforeContent() {
        //is not using anymore
        return null;
    } 

    /**
     * HTML template to handle error when retrieving userview content
     * 
     * @param e
     * @param data
     * @return 
     */
    public String handleContentError(Exception e, Map<String, Object> data) {
        LogUtil.error(getClassName(), e, "Error rendering content.");
        data.put("exception", e);
        data.put("date", AppUtil.processHashVariable("#date.d MMM yyyy HH:mm:ss#", null, null, null));
        return UserviewUtil.getTemplate(this, data, "/templates/userview/error.ftl");
    }

    /**
     * HTML template to handle page not found.
     * 
     * @param data
     * @return 
     */
    public String handlePageNotFound(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/pageNotFound.ftl");
    }

    /**
     * HTML template to handle theme layout
     * 
     * @param data
     * @return 
     */
    public String getLayout(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/layout.ftl");
    }

    /**
     * HTML template to handle page header
     * 
     * @param data
     * @return 
     */
    public String getHeader(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/header.ftl");
    }

    /**
     * HTML template to handle page footer
     * 
     * @param data
     * @return 
     */
    public String getFooter(Map<String, Object> data) {
        MessageSource messageSource = (MessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = LocaleContextHolder.getLocale();

        String footer = "&copy;Copyright Kecak Workflow " + messageSource.getMessage("build.year", null, "", locale) + " - Build " + messageSource.getMessage("build.number", null, "", locale);
        LogUtil.info(this.getClassName(), "year : " + messageSource.getMessage("build.year", null, "", locale));
        LogUtil.info(this.getClassName(), "build number : " + messageSource.getMessage("build.number", null, "", locale));
        LogUtil.info(this.getClassName(), "footerMessage : " + data);
        data.put("buildNumber", messageSource.getMessage("build.number", null, "", locale));
        data.put("footerMessage", footer);

        return UserviewUtil.getTemplate(this, data, "/templates/userview/footer.ftl");
    }

    /**
     * HTML template to handle userview menu content
     * 
     * @param data
     * @return 
     */
    public String getContentContainer(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/contentContainer.ftl");
    }

    /**
     * HTML template to handle menus
     * 
     * @param data
     * @return 
     */
    public String getMenus(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/menus.ftl");
    }

    /**
     * HTML template for putting javascript and css link for getHead() template
     * 
     * @param data
     * @return 
     */
    public String getJsCssLib(Map<String, Object> data) {
        return "<link href=\"" + data.get("context_path") + "/css/empty_userview.css?build=" + data.get("build_number") + "\" rel=\"stylesheet\" type=\"text/css\" />";
    }

    /**
     * Gets dynamic generated CSS for getHead() template
     * 
     * @param data
     * @return 
     */
    public String getCss(Map<String, Object> data) {
        return "";
    }

    /**
     * Gets dynamic generated javascript for getHead() template 
     * 
     * @param data
     * @return 
     */
    public String getJs(Map<String, Object> data) {
        return "";
    }

    /**
     * Gets dynamic generated meta data for getHead() template 
     * 
     * @param data
     * @return 
     */
    public String getMetas(Map<String, Object> data) {
        return "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>\n"
                + "<meta charset=\"utf-8\" />";
    }

    /**
     * HTML template to handle for &lt;head&gt; tag
     * 
     * @param data
     * @return 
     */
    public String getHead(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/head.ftl");
    }

    /**
     * Gets the fav icon relative path for getHead() template 
     * 
     * @param data
     * @return 
     */
    public String getFavIconLink(Map<String, Object> data) {
        return data.get("context_path") + "/images/favicon_uv.ico";
    }

    /**
     * HTML template for login form
     * 
     * @param data
     * @return 
     */
    public String getLoginForm(Map<String, Object> data) {
        if (!data.containsKey("login_form_before")) {
            if (getProperties().containsKey("loginPageTop")) {
                data.put("login_form_before", getPropertyString("loginPageTop"));
            } else {
                data.put("login_form_before", this.userview.getSetting().getPropertyString("loginPageTop"));
            }
        }
        if (!data.containsKey("login_form_after")) {
            if (getProperties().containsKey("loginPageBottom")) {
                data.put("login_form_after", getPropertyString("loginPageBottom"));
            } else {
                data.put("login_form_after", this.userview.getSetting().getPropertyString("loginPageBottom"));
            }
        }
        return UserviewUtil.getTemplate(this, data, "/templates/userview/login.ftl");
    }

    /**
     * HTML template for menu category label
     * 
     * @param category
     * @return 
     */
    public String decorateCategoryLabel(UserviewCategory category) {
        return "<span>" + StringUtil.stripHtmlRelaxed(category.getPropertyString("label")) + "</span>";
    }
    
    /**
     * HTML template for menu 
     * 
     * @param category
     * @param menu
     * @return 
     */
    public String decorateMenu(UserviewCategory category, UserviewMenu menu) {
        return menu.getMenu();
    }
    
    /**
     * Return theme defined menus id
     * 
     * @return 
     */
    public String[] themeDefinedMenusId() {
        return null;
    }
    
    /**
     * To handle special redirection case
     * @return 
     */
    public String handleRedirection() {
        return null;
    }
    
    /**
     * To get custom homepage
     * @return 
     */
    public String getCustomHomepage() {
        return null;
    }
}
