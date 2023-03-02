package org.joget.apps.userview.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.property.service.PropertyUtil;

public abstract class PageComponent extends ExtElement {

    private Collection<PageComponent> children = new ArrayList<PageComponent>();
    private PageComponent parent;
    private Userview userview;

    public PageComponent getParent() {
        return parent;
    }

    public void setParent(PageComponent parent) {
        this.parent = parent;
    }

    public Collection<PageComponent> getChildren() {
        return children;
    }

    public void setChildren(Collection<PageComponent> children) {
        this.children = children;
    }

    public Userview getUserview() {
        return userview;
    }

    public void setUserview(Userview userview) {
        this.userview = userview;
    }
    
    public boolean isPermissionHidden() {
        if ("true".equals(userview.getParamString("isBuilder"))) {
            return false;
        }
        if (Permission.DEFAULT.equals(userview.getPermissionKey())) {
            return getPropertyString("hidden").equalsIgnoreCase("true");
        } else if (getProperties().containsKey("permission_rules")) {
            Map<String, Object> permissionRules = (Map<String, Object>) getProperty("permission_rules");
            if (permissionRules != null && permissionRules.containsKey(userview.getPermissionKey())) {
                Map<String, Object> ruleObj = (Map<String, Object>)permissionRules.get(userview.getPermissionKey());
                return "true".equalsIgnoreCase((String) ruleObj.get("hidden"));
            }
        }
        return false;
    }
    
    public String render() {
        if (isPermissionHidden()) {
            return "";
        }
        
        String id = "pc-" + getPropertyString("id");
        if (!getPropertyString("customId").isEmpty()) {
            id = getPropertyString("customId");
        }
        
        Map<String, String> styles = AppPluginUtil.generateAttrAndStyles(getProperties(), "");
        
        String desktopStyle = styles.get("desktopStyle");
        String tabletStyle = styles.get("tabletStyle");
        String mobileStyle = styles.get("mobileStyle");
        String hoverDesktopStyle = styles.get("hoverDesktopStyle");
        String hoverTabletStyle = styles.get("hoverTabletStyle");
        String hoverMobileStyle = styles.get("hoverMobileStyle");
        String cssClass = styles.get("cssClass");
        String attr = styles.get("attr"); 
        
        cssClass += " " + getName().replaceAll(" ", "_");
        
        String styleClass = "builder-style-"+UuidGenerator.getInstance().getUuid();
        String additionalStyle = getAdditionalStyle(styleClass);
        String builderStyles = "";
        
        if (!desktopStyle.isEmpty() || !tabletStyle.isEmpty() || !mobileStyle.isEmpty() || !additionalStyle.isEmpty()) {
            cssClass += " " + styleClass;

            builderStyles = "<style id=\""+styleClass+"\">";
            if (!desktopStyle.isEmpty()) {
                builderStyles += "." + styleClass + "{" + desktopStyle + "} ";
            }
            if (!tabletStyle.isEmpty()) {
                builderStyles += "@media (max-width: 991px) {." + styleClass + "{" + tabletStyle + "}} ";
            }
            if (!mobileStyle.isEmpty()) {
                builderStyles += "@media (max-width: 767px) {." + styleClass + "{" + mobileStyle + "}} ";
            }
            if (!hoverDesktopStyle.isEmpty()) {
                builderStyles += "." + styleClass + ":hover{" + hoverDesktopStyle + "} ";
            }
            if (!hoverTabletStyle.isEmpty()) {
                builderStyles += "@media (max-width: 991px) {." + styleClass + ":hover{" + hoverTabletStyle + "}} ";
            }
            if (!hoverMobileStyle.isEmpty()) {
                builderStyles += "@media (max-width: 767px) {." + styleClass + ":hover{" + hoverMobileStyle + "}} ";
            }
            
            builderStyles += additionalStyle;
            
            builderStyles += "</style>";
        }
        
        boolean isBuilder = "true".equalsIgnoreCase(getRequestParameterString("isBuilder"));
        if (isBuilder) {
            attr += " data-cbuilder-classname=\"" + StringUtil.escapeString(getClassName(), StringUtil.TYPE_HTML) + "\" data-cbuilder-id=\"" + StringUtil.escapeString(getPropertyString("id"), StringUtil.TYPE_HTML) + "\" data-cbuilder-label=\"" + StringUtil.escapeString(getI18nLabel(), StringUtil.TYPE_HTML) + "\"";
        }

        return render(id, cssClass, builderStyles, attr, isBuilder);
    }
    
    public String getAdditionalStyle(String styleClass) {
        return "";
    }
    
    public String getAdditionalStyle(String styleClass, String selector, String prefix) {
        Map<String, String> styles = AppPluginUtil.generateAttrAndStyles(getProperties(), prefix);
        
        String desktopStyle = styles.get("desktopStyle");
        String tabletStyle = styles.get("tabletStyle");
        String mobileStyle = styles.get("mobileStyle");
        String hoverDesktopStyle = styles.get("hoverDesktopStyle");
        String hoverTabletStyle = styles.get("hoverTabletStyle");
        String hoverMobileStyle = styles.get("hoverMobileStyle");
        
        String builderStyles = "";
        
        if (!desktopStyle.isEmpty() || !tabletStyle.isEmpty() || !mobileStyle.isEmpty()) {
            if (!desktopStyle.isEmpty()) {
                builderStyles += "." + styleClass + " " + selector + "{" + desktopStyle + "} ";
            }
            if (!tabletStyle.isEmpty()) {
                builderStyles += "@media (max-width: 991px) {." + styleClass + " " + selector + "{" + tabletStyle + "}} ";
            }
            if (!mobileStyle.isEmpty()) {
                builderStyles += "@media (max-width: 767px) {." + styleClass + " " + selector + "{" + mobileStyle + "}} ";
            }
            if (!hoverDesktopStyle.isEmpty()) {
                builderStyles += "." + styleClass + " " + selector + ":hover{" + hoverDesktopStyle + "} ";
            }
            if (!hoverTabletStyle.isEmpty()) {
                builderStyles += "@media (max-width: 991px) {." + styleClass + " " + selector + ":hover{" + hoverTabletStyle + "}} ";
            }
            if (!hoverMobileStyle.isEmpty()) {
                builderStyles += "@media (max-width: 767px) {." + styleClass + " " + selector + ":hover{" + hoverMobileStyle + "}} ";
            }
        }
        
        return builderStyles;
    }
    
    public String renderChildren() {
        String html = "";
        
        if (getChildren() != null) {
            for (PageComponent p : getChildren()) {
                String temp = p.render();
                if (temp != null) {
                    html += temp;
                }
            }
        }
        html += getPropertyString("textContent");
        
        return html;
    }
    
    public String getDefaultPropertyValues() {
        String options = getPropertyOptions();
        if (options != null && !options.isEmpty()) {
            return PropertyUtil.getDefaultPropertyValues(options);
        }
        return "";
    }
    
    public boolean isUiMenu() {
        return this instanceof UserviewMenu;
    }
    
    /**
     * Category to be displayed in Userview Builder palette 
     * @return
     */
    public abstract String getCategory();
    
    @Override
    public String getPluginIcon() {
        return getIcon();
    }

    /**
     * Icon path to be displayed in Userview Builder palette 
     * @return
     */
    public abstract String getIcon();

    public abstract String render(String id, String cssClass, String style, String attributes, boolean isBuilder);

    public abstract String getBuilderJavaScriptTemplate();
}
