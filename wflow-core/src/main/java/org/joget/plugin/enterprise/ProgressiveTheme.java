package org.joget.plugin.enterprise;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;

public class ProgressiveTheme extends UniversalTheme {

    public enum Color {
        RED("#F44336", "#D32F2F", ""),
        PINK("#E91E63", "#C2185B", ""),
        PURPLE("#9C27B0", "#7B1FA2", ""),
        DEEP_PURPLE("#673AB7", "#512DA8", ""),
        INDIGO("#3F51B5", "", ""),
        BLUE("#0D6EFD", "", ""),
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
        GREY("#6c757D", "#616161", ""),
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
    protected String getDefaultColor(String defaultColor) {
        if (defaultColor.equals("primary")) {
            defaultColor = "INDIGO";
        }
        else if (defaultColor.equals("accent")) {
            defaultColor = "#0D6EFD";
        }
        else if (defaultColor.equals("button")) {
            defaultColor = "#6c757D";
        }
        else if (defaultColor.equals("buttonText")) {
            defaultColor = "#FFFFFF";
        }
        else if (defaultColor.equals("menuFont")) {
            defaultColor = "#000000";
        }
        else if (defaultColor.equals("font")) {
            defaultColor = "#FFFFFF";
        }
        return defaultColor;
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
    
    @Override
    public String getMenus(Map<String, Object> data) {
        if ("true".equals(getPropertyString("displayCategoryLabel"))) {
            data.put("combine_single_menu_category", false);
        } else {
            data.put("combine_single_menu_category", true);
        }
        data.put("categories_container_before", getSidebarUserMenu(data));
        return super.getMenus(data);
    }
}
