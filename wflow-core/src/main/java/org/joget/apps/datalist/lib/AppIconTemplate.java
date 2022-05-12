package org.joget.apps.datalist.lib;

import java.util.Map;
import java.util.TreeMap;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.service.DataListService;

public class AppIconTemplate extends SimpleCardTemplate {

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
    
    @Override
    public String fillRows(DataListCollection rows, String childtemplate) {
        String splitColumn = getPropertyString("splitListByColumn");
        if (splitColumn.isEmpty()) {
            return super.fillRows(rows, childtemplate);
        } else {
            String value = "";
            Map<String, DataListCollection> splittedList= new TreeMap<String, DataListCollection>();
            
            for (Object o : rows) {
                String s = (String) DataListService.evaluateColumnValueFromRow(o, splitColumn);
                if (s == null) {
                    s = "";
                }
                DataListCollection splittedRows = splittedList.get(s);
                if (splittedRows == null) {
                    splittedRows = new DataListCollection();
                    splittedList.put(s, splittedRows);
                }
                splittedRows.add(o);
            }
            
            for (String key : splittedList.keySet()) {
                if (!key.isEmpty()) {
                    String style = "style=\"margin-top:25px\"";
                    if (value.isEmpty()) {
                        style = "";
                    }
                    value += "<h4 class=\"col-12\" "+style+">" + key + "</h4>";
                }
                value += super.fillRows(splittedList.get(key), childtemplate);
            }
            
            return value;
        }
    }
}