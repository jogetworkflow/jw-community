package org.joget.apps.userview.model;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.service.UserviewCache;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UserviewPage {
    private PluginManager pluginManager;
    protected UserviewMenu menu;
    
    public UserviewPage(UserviewMenu menu) {
        menu.setProperty("css-main-component", "true");
        this.menu = menu;
    }
    
    public String render() {
        //try to cache the page content of all components (including info tiles) if it is not builder request
        boolean isBuilder = "true".equalsIgnoreCase(menu.getRequestParameterString("isBuilder"));
        if (!isBuilder) {
            String content = UserviewCache.getCachedContent(menu, UserviewCache.CACHE_TYPE_PAGE);
            if (content != null) {
                return content;
            }
        }
        
        String html = "";
        Collection<PageComponent> components = getPageComponents();
        
        if (components != null) {
            for (PageComponent c : components) {
                html += c.render();
                
                if (c instanceof UserviewMenu && c != menu) {
                    UserviewMenu t = (UserviewMenu) c;
                    if (!t.getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY).isEmpty() 
                            && (!menu.getProperties().containsKey(UserviewMenu.ALERT_MESSAGE_PROPERTY) 
                                || menu.getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY).isEmpty())) {
                        menu.setProperty(UserviewMenu.ALERT_MESSAGE_PROPERTY, t.getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY));
                    }
                    if (!t.getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY).isEmpty()
                            && (!menu.getProperties().containsKey(UserviewMenu.REDIRECT_URL_PROPERTY) 
                                || menu.getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY).isEmpty())) {
                        menu.setProperty(UserviewMenu.REDIRECT_URL_PROPERTY, t.getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY));
                        menu.setProperty(UserviewMenu.REDIRECT_PARENT_PROPERTY, t.getPropertyString(UserviewMenu.REDIRECT_PARENT_PROPERTY));
                    }
                }
            }
        } else {
            html = menu.render();
        }
        
        if (!isBuilder) {
            UserviewCache.setCachedContent(menu, UserviewCache.CACHE_TYPE_PAGE, html);
        }
        
        return html;
    }
    
    public PageComponent findComponent(String id) {
        Collection<PageComponent> components = getPageComponents();
        if (components == null) {
            components = new ArrayList<PageComponent>();
            components.add(menu);
        }
        PageComponent p = findComponent(id, components);
        return p;
    }
    
    protected PageComponent findComponent(String id, Collection<PageComponent> components) {
        if (components != null) {
            for (PageComponent c : components) {
                if (id.equals("pc-" + c.getPropertyString("id")) || id.equals(c.getPropertyString("id")) || id.equals(c.getPropertyString("customId"))) {
                    return c;
                } else if (c.getChildren() != null && !c.getChildren().isEmpty()) {
                    PageComponent temp = findComponent(id, c.getChildren());
                    if (temp != null) {
                        return temp;
                    }
                }
            }
        }
        return null;
    }
    
    public String renderComponent(String id, Collection<PageComponent> components) {
        String html = "";
        
        if (components == null) {
            components = getPageComponents();
            if (components == null) {
                components = new ArrayList<PageComponent>();
                components.add(menu);
            }
        }
        
        if (components != null) {
            for (PageComponent c : components) {
                if (id.equals("pc-" + c.getPropertyString("id")) || id.equals(c.getPropertyString("id")) || id.equals(c.getPropertyString("customId"))) {
                    html += c.render();
                    
                    if (c instanceof UserviewMenu) {
                        UserviewMenu t = (UserviewMenu) c;
                        menu.setProperty(UserviewMenu.ALERT_MESSAGE_PROPERTY, t.getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY));
                        menu.setProperty(UserviewMenu.REDIRECT_URL_PROPERTY, t.getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY));
                        menu.setProperty(UserviewMenu.REDIRECT_PARENT_PROPERTY, t.getPropertyString(UserviewMenu.REDIRECT_PARENT_PROPERTY));
                    }
                    
                    break;
                } else if (c.getChildren() != null && !c.getChildren().isEmpty()) {
                    html += renderComponent(id, c.getChildren());
                    
                    if (!html.isEmpty()) {
                        break;
                    }
                }
            }
        }
        
        return html;
    }
    
    private Collection<PageComponent> getPageComponents() {
        if (menu.getProperties().containsKey("REFERENCE_PAGE")) {
            try {
                return getPageComponents(null, (JSONObject) menu.getProperty("REFERENCE_PAGE"));
            } catch(Exception e) {
                LogUtil.error(UserviewPage.class.getName(), e, "");
            }
        } else {
            BuilderDefinitionDao dao = (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
            BuilderDefinition page = dao.loadById("up-"+menu.getPropertyString("id"), AppUtil.getCurrentAppDefinition());

            try {
                if (page != null) {
                    return getPageComponents(null, new JSONObject(page.getJson()));
                }
            } catch (Exception e) {
                LogUtil.error(UserviewPage.class.getName(), e, "");
            }
        }
        
        return null;
    }
    
    private Collection<PageComponent> getPageComponents(PageComponent parent, JSONObject jsonObj) throws JSONException {
        Collection<PageComponent> components = new ArrayList<PageComponent>();
        
        if (jsonObj.has("elements")) {
            JSONArray elements = jsonObj.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                PageComponent pc = getPageComponent(elements.getJSONObject(i));
                if (pc != null) {
                    pc.setParent(parent);
                    
                    if (pc.getProperties().containsKey("id")) {
                        pc.setProperty("attr-data-pc-id", pc.getProperty("id"));
                    }
                    
                    components.add(pc);
                }
            }
        }
        
        return components;
    }
    
    public PageComponent getPageComponent(JSONObject jsonObj) throws JSONException {
        PageComponent component;
        if ("menu-component".equalsIgnoreCase(jsonObj.getString("className"))) {
            component = menu;
        } else {
            component = (PageComponent) getPluginManager().getPlugin(jsonObj.getString("className"));
            if (component != null) {
                if (component instanceof ExtElement) {
                    ((ExtElement) component).setRequestParameters(menu.getRequestParameters());
                }
                if (component instanceof UserviewMenu) {
                    ((UserviewMenu) component).setKey(menu.getKey());
                    ((UserviewMenu) component).setUrl(menu.getUrl());
                }
                component.setProperties(PropertyUtil.getProperties(jsonObj.getJSONObject("properties")));
                component.setChildren(getPageComponents(component, jsonObj));
                component.setUserview(menu.getUserview());
            }
        }
        
        //check is userview menu and make sure it a CachedUserviewMenu
        component = PageComponent.makeCacheable(component, menu);
        return component;
    }
    
    private PluginManager getPluginManager() {
        if (pluginManager == null) {
            pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        }
        return pluginManager;
    }
}
