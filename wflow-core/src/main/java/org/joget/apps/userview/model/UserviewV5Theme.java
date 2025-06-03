package org.joget.apps.userview.model;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     * HTML template to handle AJAX menus count
     * 
     * @param data
     * @return 
     */
    public String getAjaxMenusCount(Map<String, Object> data) {
        return UserviewUtil.getTemplate(this, data, "/templates/userview/ajax_menus_count.ftl");
    }

    /**
     * HTML template for putting javascript and css link for getHead() template
     * 
     * @param data
     * @return 
     */
    public String getJsCssLib(Map<String, Object> data) {
        if(getPropertyString("customLogin").equalsIgnoreCase("true") && !getPropertyString("template").isEmpty() && (Boolean) data.get("is_login_page")){
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            UserviewV5Theme dx8Theme = (UserviewV5Theme) pluginManager.getPlugin("org.joget.apps.userview.lib.AjaxUniversalTheme");
            dx8Theme.setProperties(getProperties());
            dx8Theme.setUserview(getUserview());
            dx8Theme.setRequestParameters(getRequestParameters());
            return dx8Theme.getJsCssLib(data);
        }
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
        boolean isCustomLogin = false;
        if (getPropertyString("customLogin").equalsIgnoreCase("true") && !getPropertyString("template").isEmpty()){
            //reuse info tile in enterprise for this 
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            SimplePageComponent infoTile = (SimplePageComponent) pluginManager.getPlugin("org.joget.plugin.enterprise.InformationTileComponent");
            
            if (infoTile != null) {
                isCustomLogin = true;
                
                String html = "<style>\n" +
                "body#login #loginForm {display: none;}\n" +
                ".img100 {height: 100% !important;}\n" +
                "body#login header, body#login footer {display: none;}\n" +
                "</style>";
                
                infoTile.setProperties(getProperties());
                infoTile.setUserview(getUserview());
                infoTile.setRequestParameters(getRequestParameters());
                
                //preset value
                if (infoTile.getPropertyString("customUsername").isEmpty()) {
                    infoTile.setProperty("customUsername", ResourceBundleUtil.getMessage("ubuilder.login.username"));
                }
                if (infoTile.getPropertyString("customPassword").isEmpty()) {
                    infoTile.setProperty("customPassword", ResourceBundleUtil.getMessage("ubuilder.login.password"));
                }
                if (infoTile.getPropertyString("btnText").isEmpty()) {
                    infoTile.setProperty("btnText", ResourceBundleUtil.getMessage("ubuilder.login"));
                }
                
                String style = "<style>#custom_login {height: 100vh !important;}</style>";
                
                html += infoTile.render("custom_login", "", style, "", false);
                
                //Handle login footer to inject before the form end tag
                String footerContent = (String) data.get("login_form_footer");
                html = html.replaceFirst("</form>", StringUtil.escapeRegex(footerContent+ "</form>"));
                data.put("login_form_footer", ""); //empty it to prevent double insert

                html += UserviewUtil.getTemplate(this, data, "/templates/userview/customLoginScript.ftl");;
                
                String bodyClasses = (String) data.get("body_classes");
                data.put("body_classes", bodyClasses.replace("rtl", ""));
                data.put("right_to_left", false);
                
                data.put("login_form_before", html);
            }
        }
        
        if (!isCustomLogin) {
            if (!data.containsKey("loginBackground") && !getPropertyString("loginBackground").isEmpty()) {
                data.put("loginBackground", "<style>#login{background-size:cover; background-image:url('"+getPropertyString("loginBackground")+"');}</style>");
            }
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
     * HTML template for menu. Return a temporary placeholder for menu count to improve performance.
     * The actual count will retrieve in separated AJAX call.
     * 
     * @param category
     * @param menu
     * @return 
     */
    public String decorateMenu(UserviewCategory category, UserviewMenu menu) {
        if (menu == null && category.getProperties().containsKey("enableMegaMenu") && Boolean.parseBoolean(category.getPropertyString("enableMegaMenu"))) {
            Map megaMenuProps = (Map) category.getProperty("megaMenuConfiguration");

            String megaMenuTemplate = "<div id='mega-menu-{id}' class='mega-menu-container container-fluid' style='display:none;background-color:{background-color};padding-top: {padding-top}; padding-right: {padding-right}; padding-bottom: {padding-bottom}; padding-left: {padding-left}; height: {min-height}'> <style>div#mega-menu-{id} > .row > [class^=\"col\"]:last-child {padding-right: 0px !important;} div#mega-menu-{id} > .row > [class^=\"col\"] {padding-right: {col-gutter} !important; padding-left: 0px !important;} div#mega-menu-{id} > .row + .row {margin-top: {row-gutter} !important;}</style>";
            megaMenuTemplate = megaMenuTemplate.replaceAll("\\{id\\}", category.getPropertyString("id"));

            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            SimplePageComponent infoTile = (SimplePageComponent) pluginManager.getPlugin("org.joget.plugin.enterprise.InformationTileComponent");

            infoTile.setUserview(getUserview());
            infoTile.setRequestParameters(getRequestParameters());

            int defaultCol = 1; 
            int defaultRow = 1;

            int col = defaultCol;
            int row = defaultRow;
            String rowGutter = "";

            int menuIndex = 0; 
            List<UserviewMenu> menus = (List<UserviewMenu>) category.getMenus(); 
            Map<String, String> generalLayoutProps = (Map<String, String>) megaMenuProps.get("general-layout");
            if (generalLayoutProps != null) {
                col = generalLayoutProps.containsKey("col") ? Integer.parseInt(generalLayoutProps.get("col")) : defaultCol;
                row = generalLayoutProps.containsKey("row")
                ? ((col != 1) ? (int) Math.ceil((double) menus.size() / col) : col)
                : col;
                String displayInFullHeight = generalLayoutProps.containsKey("display-full-height") ? generalLayoutProps.get("display-full-height") : "false";
                rowGutter = generalLayoutProps.containsKey("row-gutter") ? generalLayoutProps.get("row-gutter") : "";
                String colGutter = generalLayoutProps.containsKey("col-gutter") ? generalLayoutProps.get("col-gutter") : "";

                for (Map.Entry<String, String> entry : generalLayoutProps.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value instanceof String) {
                        megaMenuTemplate = megaMenuTemplate.replace('{' + key + '}', (String) value);
                    }                
                }

                if (displayInFullHeight.equals("true")) {
                    megaMenuTemplate = megaMenuTemplate.replace("{min-height}", "100%");
                }else if (displayInFullHeight.isEmpty() && generalLayoutProps.containsKey("containerHeight") && !generalLayoutProps.get("containerHeight").isEmpty()) {
                    megaMenuTemplate = megaMenuTemplate.replace("{min-height}", generalLayoutProps.get("containerHeight") + "; height: "+ generalLayoutProps.get("containerHeight") + ";");
                }else {
                    megaMenuTemplate = megaMenuTemplate.replace("{min-height}", "; height: fit-content;");
                }

                if (!rowGutter.isEmpty()) {
                    megaMenuTemplate = megaMenuTemplate.replaceAll("\\{row-gutter\\}", rowGutter);
                }else {
                    megaMenuTemplate = megaMenuTemplate.replaceAll("\\{row-gutter\\}", "");
                }

                if (!colGutter.isEmpty()) {
                    megaMenuTemplate = megaMenuTemplate.replaceAll("\\{col-gutter\\}", colGutter);
                }else {
                    megaMenuTemplate = megaMenuTemplate.replaceAll("\\{col-gutter\\}", "");
                }
            }
            int repeatCounter = 0;
            boolean isColumnAuto = (col == 0) ? true : false;
            for (int r = 0; r < row; r++) {
                if (menuIndex == menus.size()) {
                    break;
                }

                megaMenuTemplate += "<div class='row" + "' id='rowNo" + r + "'>";

                if (col == 0) {
                    col = menus.size();
                }
                // Column & Menu Templates
                for (int c = 0; c < col; c++) {
                    if (menuIndex == menus.size()) {
                        break;
                    }

                    if(isColumnAuto) {
                        megaMenuTemplate += "<div class='col-auto' style='margin-bottom:" + rowGutter + "' id='colNo-" + c + "'>";
                    }else {
                        megaMenuTemplate += "<div class='col' id='colNo-" + c + "'>";
                    }

                    // Menu Templates
                    UserviewMenu m = menus.get(menuIndex);
                    String menuId = m.getPropertyString("id");
                    String menuLabel = m.getPropertyString("label");

                    if (menuLabel != null) {
                        menuLabel = StringUtil.stripHtmlRelaxed(menuLabel);
                    }

                    String icon = menuLabel.matches(".*<i.*?</i>.*") ? menuLabel.replaceAll(".*(<i.*?</i>).*", "$1") : "<i class='zmdi zmdi-menu'></i> ";
                    String text = menuLabel.replaceAll("<i.*?</i>", "").trim();

                    //Set properties
                    infoTile.setProperties((Map<String, Object>) megaMenuProps.get("mega-menu-" + menuId));

                    if (infoTile.getPropertyString("template") == null || infoTile.getPropertyString("template").isEmpty()) {
                        if (generalLayoutProps.containsKey("useGlobalValues") && generalLayoutProps.get("useGlobalValues").equals("true")) {
                            infoTile.setProperty("template", generalLayoutProps.get("template"));
            
                            // Fetch repeat values and handle the repeat logic
                            Object generalLayout = megaMenuProps.get("general-layout");
                            if (generalLayout instanceof Map) {
                                Map<String, Object> generalLayoutMap = (Map<String, Object>) generalLayout;
                                
                                // Get the "repeat" object from the map
                                Object repeatObj = generalLayoutMap.get("repeat");
                                
                                // Check if repeatObj is a List of Maps
                                if (repeatObj instanceof List) {
                                    List<Map<String, Object>> repeatValues = (List<Map<String, Object>>) repeatObj;

                                    if (repeatCounter >= 0 && repeatCounter < repeatValues.size()) {
                                        Map<String, Object> repeatEntry = repeatValues.get(repeatCounter);
                                        
                                        for (Map.Entry<String, Object> entry : repeatEntry.entrySet()) {
                                            String key = entry.getKey();
                                            Object value = entry.getValue();

                                            if (value instanceof String) {
                                                infoTile.setProperty(key, (String) value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (infoTile.getProperty("template") != null && infoTile.getPropertyString("template").contains("{{icon||")) {
                        infoTile.setProperty("title", text);
                        if (!icon.isEmpty()) {
                            infoTile.setProperty("icon", icon);
                        }
                    } else {
                        infoTile.setProperty("title", menuLabel);
                    }

                    if (infoTile.getPropertyString("template") != null && infoTile.getPropertyString("width").isEmpty()) {
                        String imgWidth = "150px";
                        Pattern pattern = Pattern.compile("\\{\\{width\\|\\|([^}]*)\\}\\}");
                        Matcher matcher = pattern.matcher(infoTile.getPropertyString("template"));
                        
                        if (matcher.find()) {
                            imgWidth = matcher.group(1); 
                        }
                        infoTile.setProperty("width", imgWidth);
                    }

                    if (infoTile.getPropertyString("template") != null && infoTile.getPropertyString("description").isEmpty()) {
                        String defaultDesc = "";
                        Pattern pattern = Pattern.compile("\\{\\{description\\|\\|([^}]*)\\}\\}");
                        Matcher matcher = pattern.matcher(infoTile.getPropertyString("template"));
                        
                        if (matcher.find()) {
                            defaultDesc = matcher.group(1); 
                        }
                        infoTile.setProperty("description", defaultDesc);
                    }
                    
                    Object megaMenu = megaMenuProps.get("mega-menu-" + menuId);
                    if (megaMenu != null) {
                        Object repeatObj = ((Map) megaMenu).get("repeat");
                        if (repeatObj != null) {
                            Object[] repeatArray = (Object[]) repeatObj;
                               
                            Pattern pattern = Pattern.compile("\\{\\{repeat\\|\\|(\\[.*?\\])\\}\\}", Pattern.DOTALL);
                            Matcher matcher = pattern.matcher(infoTile.getPropertyString("template"));
                            
                            if (repeatArray.length == 0 && matcher.find()) {
                                try {  
                                    JSONArray jsonArray = new JSONArray(matcher.group(1));
                                    
                                    Map<String, Object>[] maps = new Map[jsonArray.length()];

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        Map<String, Object> map = jsonObject.toMap();

                                        maps[i] = map;
                                    }
                                    
                                    infoTile.setProperty("repeat", maps);
                                } catch (JSONException e) {}
                            }
                        }
                    }
                    
                    //preset value
                    if (infoTile.getPropertyString("menu_link").isEmpty()) {
                        infoTile.setProperty("menu_link", m.getUrl());
                    }
                    
                    String temp = "";

                    infoTile.setProperty("template", infoTile.getPropertyString("template").replaceAll("\\{menu_id\\}", "menu-id-" + menuId));
                    temp = infoTile.render("mega-menu-" + menuId, "", "", "", false);

                    megaMenuTemplate += temp;    
                    
                    megaMenuTemplate += "</div>";

                    menuIndex += 1;
                }

                megaMenuTemplate += "</div>";
            }

            if (generalLayoutProps.containsKey("useCustomFooter") && generalLayoutProps.get("useCustomFooter").equals("true")) {
                String customFooter = generalLayoutProps.containsKey("footerText") ? generalLayoutProps.get("footerText") : "";
                String customFooterLink = generalLayoutProps.containsKey("footerTextLink") ? generalLayoutProps.get("footerTextLink") : "";
                String customFooterColor = generalLayoutProps.containsKey("footerTextColor") ? generalLayoutProps.get("footerTextColor") : "";
                String openNewTab = generalLayoutProps.containsKey("openNewTab") ? "target=\"_blank\"" : "";

                megaMenuTemplate += "<div class='megaMenuCustomFooter row justify-content-center'><a href='" + customFooterLink + "' style='margin-top: 3rem !important;color:" + customFooterColor + ";' " + openNewTab + ">" + customFooter + "</a></div>";
            }

            megaMenuTemplate += "</div>";

            return megaMenuTemplate;
        }
        else if (menu.getProperties().containsKey("rowCount") && Boolean.parseBoolean(menu.getPropertyString("rowCount"))) {
            // sanitize label
            String label = menu.getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }
            return "<a href='" + menu.getUrl() + "' class='menu-link default'><span>" + label + "</span> <span class='pull-right badge rowCount' data-ajaxmenucount=\""+menu.getPropertyString("id")+"\">...</span></a>";
        } else {
            return menu.getMenu();
        }
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
    
    /**
     * To get custom content
     * @param data
     * @return 
     */
    public String getCustomContent(Map<String, Object> data) {
        return null;
    }
}
