package org.joget.apps.userview.model;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.StringUtil;

public abstract class UserviewV5Theme extends UserviewTheme {
    
    public String getCss() {
        //is not using anymore
        return null;
    } 
    
    public String getJavascript() {
        //is not using anymore
        return null;
    } 

    public String getHeader() {
        //is not using anymore
        return null;
    } 

    public String getFooter() {
        //is not using anymore
        return null;
    } 
    
    public String getPageTop() {
        //is not using anymore
        return null;
    } 

    public String getPageBottom() {
        //is not using anymore
        return null;
    } 

    public String getBeforeContent() {
        //is not using anymore
        return null;
    } 

    public String handleContentError(Exception e, Map<String, Object> data) {
        return StringEscapeUtils.escapeHtml(e.getMessage());
    }

    public String handlePageNotFound(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/pageNotFound.ftl");
    }

    public String getLayout(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/layout.ftl");
    }

    public String getHeader(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/header.ftl");
    }

    public String getFooter(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/footer.ftl");
    }

    public String getContentContainer(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/contentContainer.ftl");
    }

    public String getMenus(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/menus.ftl");
    }

    public String getJsCssLib(Map<String, Object> data) {
        return "<link href=\"" + data.get("context_path") + "/css/empty_userview.css?build=" + data.get("build_number") + "\" rel=\"stylesheet\" type=\"text/css\" />";
    }

    public String getCss(Map<String, Object> data) {
        return "";
    }

    public String getJs(Map<String, Object> data) {
        return "";
    }

    public String getMetas(Map<String, Object> data) {
        return "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>\n"
                + "<meta charset=\"utf-8\" />";
    }

    public String getHead(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/head.ftl");
    }

    public String getFavIconLink(Map<String, Object> data) {
        return data.get("context_path") + "/images/favicon_uv.ico";
    }

    public String getLoginForm(Map<String, Object> data) {
        if (!data.containsKey("login_form_before")) {
            data.put("login_form_before", this.userview.getSetting().getPropertyString("loginPageTop"));
        }
        if (!data.containsKey("login_form_after")) {
            data.put("login_form_after", this.userview.getSetting().getPropertyString("loginPageBottom"));
        }
        return UserviewUtil.getTemplate(this, data, "/templates/userview/login.ftl");
    }

    public String decorateCategoryLabel(UserviewCategory category) {
        return StringUtil.stripHtmlRelaxed(category.getPropertyString("label"));
    }
}
