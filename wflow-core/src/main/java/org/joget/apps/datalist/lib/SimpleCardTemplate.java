package org.joget.apps.datalist.lib;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListTemplate;

public class SimpleCardTemplate extends DataListTemplate {

    @Override
    public String getName() {
        return "SimpleCardTemplate";
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
        return "Card - Simple";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/simpleCardTemplate.json", null, true, null);
    }
    
    @Override
    public String getTemplate() {
        return getTemplate(null, "/templates/simpleCardTemplate.ftl", null);
    }
}
