package org.joget.plugin.enterprise;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.service.AppUtil;

public class ProgressiveTheme extends UniversalTheme {

    public enum Color {
        RED("#F44336", "#D32F2F", ""),
        PINK("#E91E63", "#C2185B", ""),
        PURPLE("#9C27B0", "#7B1FA2", ""),
        DEEP_PURPLE("#673AB7", "#512DA8", ""),
        INDIGO("#3F51B5", "", ""),
        BLUE("#1976D2", "", ""),
        LIGHT_BLUE("#03A9F4", "#0288D1", ""),
        CYAN("#00BCD4", "#0097A7", ""),
        TEAL("#009688", "#00796B", ""),
        GREEN("#4CAF50", "#388E3C", ""),
        LIGHT_GREEN("#8BC34A", "#689F38", ""),
        LIME("#CDDC39", "#AFB42B", ""),
        YELLOW("#FFEB3B", "#FBC02D", ""),
        AMBER("#FFC107", "#FFA000", ""),
        ORANGE("#FF9800", "#F57C00", ""),
        DEEP_ORANGE("#FF5722", "#E64A19", ""),
        BROWN("#795548", "#795548", ""),
        GREY("#9E9E9E", "#616161", ""),
        BLUE_GREY("#607D8B", "#455A64", ""),
        DEEP_GREY("#2B343A", "#1E262B", "#222c32"),
        LAVENDERBLUSH("#FFF0F5", "", ""),
        THISTLE("#D8BFD8", "", ""),
        PLUM("#DDA0DD", "", ""),
        LAVENDER("#E6E6FA", "", ""),
        GHOSTWHITE("#F8F8FF", "", ""),
        DARKROYALBLUE("#3b5998", "", ""),
        ROYALBLUE("#4169E1", "", ""),
        CORNFLOWERBLUE("#6495ED", "", ""),
        ALICEBLUE("#F0F8FF", "", ""),
        LIGHTSTEELBLUE("#B0C4DE", "", ""),
        STEELBLUE("#4682B4", "", ""),
        LIGHTSKYBLUE("#87CEFA", "", ""),
        SKYBLUE("#87CEEB", "", ""),
        DEEPSKYBLUE("#00BFFF", "", ""),
        AZURE("#F0FFFF", "", ""),
        LIGHTCYAN("#E1FFFF", "", ""),
        IVORY("#FFFFF0", "", ""),
        LEMONCHIFFON("#FFFACD", "", ""),
        WHEAT("#F5DEB3", "", ""),
        LIGHTGREY("#D3D3D3", "", ""),
        SILVER("#C0C0C0", "", ""),
        BLACK("#000000", "#222222", ""),
        WHITE("#FFFFFF", "", "#DDDDDD");
        
        private final String color;  
        private final String dark; 
        private final String light;
        Color(String color, String dark, String light) {
            this.color = color;
            this.dark = dark;
            this.light = light;
        }
    }
    
    @Override
    protected String getPrimaryColor() {
        String primary = "#3F51B5";
        if ("custom".equals(getPropertyString("primaryColor"))) {
            primary = getPropertyString("customPrimary");
        } else if (!getPropertyString("primaryColor").isEmpty()) {
            Color p = Color.valueOf(getPropertyString("primaryColor"));
            if (p != null) {
                primary = p.color;
            }
        }
        return primary;
    }
    
    @Override
    protected String generateLessCss() {
        String css = "";
        String lessVariables = "";
        String primary = "";
        String dark = "darken(@primary , 10%)";
        String light = "lighten(@primary , 5%)";
        String accent = "#1976D2";
        String lightAccent = "lighten(@accent , 10%)";
        String button = "#FF9800";
        String buttonText = "#FFFFFF";
        String font = "#FFFFFF";
        
        if ("custom".equals(getPropertyString("primaryColor"))) {
            primary = getPropertyString("customPrimary");
            if (!getPropertyString("customPrimaryDark").isEmpty()) {
                dark = getPropertyString("customPrimaryDark");
            }
            if (!getPropertyString("customPrimaryLight").isEmpty()) {
                light = getPropertyString("customPrimaryLight");
            }
        } else {
            Color p = Color.INDIGO;
            if (!getPropertyString("primaryColor").isEmpty()){
                p = Color.valueOf(getPropertyString("primaryColor")); 
            }
            if (p != null) {
                primary = p.color;
                dark = (p.dark.isEmpty())?dark:p.dark;
                if ("light".equals(getPropertyString("themeScheme"))) {
                    light = "screen(@primary, #eeeeee)";
                } else {
                    light = (p.light.isEmpty())?light:p.light;
                }
            }
        }
        
        if ("custom".equals(getPropertyString("accentColor"))) {
            accent = getPropertyString("customAccent");
            if (!getPropertyString("customAccentLight").isEmpty()) {
                lightAccent = getPropertyString("customAccentLight");
            }
        }  else if (!getPropertyString("accentColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("accentColor"));
            if (a != null) {
                accent = a.color;
                lightAccent = (a.light.isEmpty())?lightAccent:a.light;
            }
        }
        
        if ("custom".equals(getPropertyString("buttonColor"))) {
            button = getPropertyString("customButton");
        } else if (!getPropertyString("buttonColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("buttonColor"));
            if (a != null) {
                button = a.color;
            }
        }
        
        if ("custom".equals(getPropertyString("buttonTextColor"))) {
            buttonText = getPropertyString("customButtonText");
        } else if (!getPropertyString("buttonTextColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("buttonTextColor"));
            if (a != null) {
                buttonText = a.color;
            }
        }
        
        if ("custom".equals(getPropertyString("fontColor"))) {
            font = getPropertyString("customFontColor");
        } else if (!getPropertyString("fontColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("fontColor"));
            if (a != null) {
                font = a.color;
            }
        }
        
        if ("light".equals(getPropertyString("themeScheme"))) {
            String menuFont = "#000000";
            if ("custom".equals(getPropertyString("menuFontColor"))) {
                menuFont = getPropertyString("customMenuFontColor");
            } else if (!getPropertyString("menuFontColor").isEmpty()) {
                Color a = Color.valueOf(getPropertyString("menuFontColor"));
                if (a != null) {
                    menuFont = a.color;
                }
            }
            
            lessVariables += "@primary: " + primary + "; @darkPrimary: " + dark + "; @lightPrimary: " + light + "; @accent: " + accent + "; @lightAccent: " + lightAccent + "; @menuFont: " + menuFont + "; @button: " + button + "; @buttonText: " + buttonText + "; @defaultFontColor : " + font + ";";
        } else {
            lessVariables += "@primary: " + primary + "; @darkPrimary: " + dark + "; @lightPrimary: " + light + "; @accent: " + accent + "; @lightAccent: " + lightAccent + "; @button: " + button + "; @buttonText: " + buttonText + "; @defaultFontColor : " + font + ";";
        }
        
        // process LESS
        String less = AppUtil.readPluginResource(getClass().getName(), "resources/themes/" + getPathName() + "/" + getPropertyString("themeScheme") + ".less");
        less = lessVariables + "\n" + less;
        // read CSS from cache
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("cssCache");
        if (cache != null) {
            Element element = cache.get(less);
            if (element != null) {
                css = (String) element.getObjectValue();
            }
        }
        if (css == null || css.isEmpty()) {
            // not available in cache, compile LESS
            css = compileLess(less);
            // store CSS in cache
            if (cache != null) {
                Element element = new Element(less, css);
                cache.put(element);
            }
        }
        return css;
    }
    
    @Override
    public String getName() {
        return "DX Progressive Theme";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "A Progressive Web App Userview Theme based on Material Design";
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPathName() {
        return "progressive";
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/progressiveTheme.json", null, true, null);
    }
    
}
