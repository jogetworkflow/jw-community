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
        styles.put("STYLE", styles.get("STYLE") + " .dataList#dataList_"+getDatalist().getId()+" .table-wrapper{overflow-x: initial;} .ph_selector.stretched-link{cursor:pointer;} input:checked + .data-row{box-shadow: 0px 0px 8px 0 rgb(0 67 255 / 70%) !important}");
        
        return styles;
    }
}
