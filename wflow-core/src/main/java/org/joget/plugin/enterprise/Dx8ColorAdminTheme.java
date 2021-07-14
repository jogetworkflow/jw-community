package org.joget.plugin.enterprise;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.AjaxUniversalTheme;

public class Dx8ColorAdminTheme extends AjaxUniversalTheme {
    
    @Override
    public String getName() {
        return "DX 8 Color Admin Theme";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
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
    protected String generateLessCss() {
        String css = super.generateLessCss();
        
        css += AppUtil.readPluginResource(getClass().getName(), "/resources/css/dx8ColorAdminTheme.css", null, true, null);
        
        return css;
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/dx8ColorAdminTheme.json", null, true, null);
    }
}
