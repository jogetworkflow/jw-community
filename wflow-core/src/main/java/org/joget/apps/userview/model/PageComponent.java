package org.joget.apps.userview.model;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.property.service.PropertyUtil;

public abstract class PageComponent extends ExtElement {

    private Collection<PageComponent> children = new ArrayList<PageComponent>();
    private PageComponent parent;

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

    public String render() {
        if (getPropertyString("hidden").equalsIgnoreCase("true")) {
            return "";
        }
        
        String id = "pc-" + getPropertyString("id");
        if (!getPropertyString("customId").isEmpty()) {
            id = getPropertyString("customId");
        }

        String desktopStyle = "";
        String tabletStyle = "";
        String mobileStyle = "";
        String hoverDesktopStyle = "";
        String hoverTabletStyle = "";
        String hoverMobileStyle = "";
        String cssClass = "";
        String attr = ""; 
        
        cssClass += " " + getName().replaceAll(" ", "_");
        
        for (String key : getProperties().keySet()) {
            if ((key.startsWith("css-") 
                        || key.startsWith("attr-")
                        || key.startsWith("style-hover-mobile-")
                        || key.startsWith("style-hover-tablet-")
                        || key.startsWith("style-hover-")
                        || key.startsWith("style-mobile-")
                        || key.startsWith("style-tablet-")
                        || key.startsWith("style-"))
                     && !getPropertyString(key).isEmpty()) {
                    
                String value = "";
                if (!(getProperty(key) instanceof String)) {
                    try {
                        Gson gson = new Gson();
                        value = gson.toJson(getProperty(key));
                    } catch (Exception e) {
                        LogUtil.error(getClassName(), e, "");
                    }
                    if (value.equals("[]") || value.equals("{}")) {
                        continue;
                    }
                } else {
                    value =  getPropertyString(key);
                }
                if (key.contains("style") && key.endsWith("-background-image")) {
                    value = "url('"+value+"')";
                }
                
                if (key.startsWith("css-")) {
                    cssClass += " " + value;
                } else if (key.startsWith("attr-")) {
                    attr += " " + key.replace("attr-", "") + "=\"" + StringUtil.escapeString(value, StringUtil.TYPE_HTML, null) + "\"";
                } else if (key.startsWith("style-hover-mobile-")) {
                    hoverMobileStyle += key.replace("style-hover-mobile-", "") + ":" + value + " !important;";
                } else if (key.startsWith("style-hover-tablet-")) {
                    hoverTabletStyle += key.replace("style-hover-tablet-", "") + ":" + value + " !important;";
                } else if (key.startsWith("style-hover-")) {
                    hoverDesktopStyle += key.replace("style-hover-", "") + ":" + value + " !important;";
                } else if (key.startsWith("style-mobile-")) {
                    mobileStyle += key.replace("style-mobile-", "") + ":" + value + " !important;";
                } else if (key.startsWith("style-tablet-")) {
                    tabletStyle += key.replace("style-tablet-", "") + ":" + value + " !important;";
                } else if (key.startsWith("style-")) {
                    desktopStyle += key.replace("style-", "") + ":" + value + " !important;";
                }
            }
        }
        
        String builderStyles = "";
        
        if (!desktopStyle.isEmpty() || !tabletStyle.isEmpty() || !mobileStyle.isEmpty()) {
            String styleClass = "builder-style-"+UuidGenerator.getInstance().getUuid();
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
            builderStyles += "</style>";
        }
        
        boolean isBuilder = "true".equalsIgnoreCase(getRequestParameterString("isPreview"));
        if (isBuilder) {
            attr += " data-cbuilder-classname=\"" + getClassName() + "\" data-cbuilder-id=\"" + getPropertyString("id") + "\" data-cbuilder-label=\"" + getI18nLabel() + "\"";
        }

        return render(id, cssClass, builderStyles, attr, isBuilder);
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

    public abstract String render(String id, String cssClass, String style, String attributes, boolean isBuilder);

    public abstract String getBuilderJavaScriptTemplate();
}
