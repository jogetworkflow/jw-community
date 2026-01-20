package org.joget.apps.datalist.lib;

import java.util.Map;

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
    
    public Map<String, String> getStyles() {
        Map<String, String> styles = super.getStyles();
        styles.put("STYLE", styles.get("STYLE") + " .dataList#dataList_"+getDatalist().getId()+" .table-wrapper{overflow-x: initial;} .ph_selector.stretched-link{cursor:pointer;}" + " .dataList#dataList_"+getDatalist().getId() + " input:checked + .data-row{box-shadow: 2px 4px 11px 0px #00000066 !important; outline: 2px solid #1877FF  !important}");
        
        return styles;
    }
}
