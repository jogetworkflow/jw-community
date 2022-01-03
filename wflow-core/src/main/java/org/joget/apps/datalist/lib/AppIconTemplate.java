package org.joget.apps.datalist.lib;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListTemplate;

public class AppIconTemplate extends DataListTemplate {

    @Override
    public String getName() {
        return "AppIconTemplate";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public String getLabel() {
        return "Card - App Icon";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/appIconTemplate.json", null, true, null);
    }
    
    @Override
    public String getTemplate() {
        return getTemplate(null, "/templates/appIconTemplate.ftl", null);
    }
}