package org.joget.apps.datalist.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.apps.userview.model.PwaOfflineResources;

public class TextFieldDataListFilterType extends DataListFilterTypeDefault implements PwaOfflineResources {

    @Override
    public String getName() {
        return "Text Field Data List Filter Type";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Data List Filter Type - Text Field";
    }

    @Override
    public String getLabel() {
        return "Text Field";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/textFieldDataListFilterType.json", null, true, null);
    }

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        
        String jsTimeoutName = datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name);
        jsTimeoutName = jsTimeoutName.replace(".", "");
        jsTimeoutName = jsTimeoutName.replace("-", "");
        jsTimeoutName = jsTimeoutName.replace("_", "");
        dataModel.put("jsTimeoutName", jsTimeoutName);
        
        String filterSelection = getPropertyString("filterSelection");
        if(filterSelection.contains("String")){
            dataModel.put("filterSelectionString", "true");
        }
        if(filterSelection.contains("Number")){
            dataModel.put("filterSelectionNumeric", "true");
        }
        dataModel.put("searchType", getValue(datalist, name + "_searchType", "any"));
        
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/textFieldDataListFilterType.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        String searchType = getValue(datalist, name + "_searchType", "any");
        
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            Set<String> searchField = new HashSet<String>();
            searchField.add(name);
            
            if (!getPropertyString("searchFields").isEmpty()) {
                String[] fields = getPropertyString("searchFields").split(";");
                searchField.addAll(Arrays.asList(fields));
            }
            
            String query = "";
            Collection<String> values = new ArrayList<String>();
            
            for (String f : searchField) {
                if (!query.isEmpty()) {
                    query += " OR ";
                }
                
                String cname = datalist.getBinder().getColumnName(f);
                boolean isAggregate = (cname.toLowerCase().contains("count(")
                    || cname.toLowerCase().contains("sum(")
                    || cname.toLowerCase().contains("avg(")
                    || cname.toLowerCase().contains("min(")
                    || cname.toLowerCase().contains("max("));
                
                if(searchType.equalsIgnoreCase("largerThan") || searchType.equalsIgnoreCase("smallerThan") || isAggregate){
                    if (isAggregate) {
                        query += cname;
                    } else {
                        query += "cast(" + cname + " as big_decimal)";
                    }
                    
                    if (searchType.equalsIgnoreCase("largerThan")) {
                        query += " >= "; 
                    } else if (searchType.equalsIgnoreCase("smallerThan")) {
                        query += " <= "; 
                    } else {
                        query += " = "; 
                    }
                    
                    query += "cast(? as big_decimal)";
                    values.add(value);
                } else if (searchType.equalsIgnoreCase("startsWith") || searchType.equalsIgnoreCase("endsWith") || searchType.equalsIgnoreCase("any")) {
                    query += "lower(" + cname + ") like lower(?)";
                    
                    if (searchType.equalsIgnoreCase("startsWith")) {
                        values.add(value + "%");
                    } else if (searchType.equalsIgnoreCase("endsWith")) {
                        values.add("%" + value);
                    } else {
                        values.add("%" + value + "%");
                    }
                } else if (searchType.equalsIgnoreCase("exact")) {
                    query += cname + " = ?";
                    values.add(value);
                }
            }

            queryObject.setQuery("(" + query + ")");
            queryObject.setValues(values.toArray(new String[0]));
            
            return queryObject;
        }
        return null;
    }

    @Override
    public Set<String> getOfflineStaticResources() {
        Set<String> urls = new HashSet<String>();
        String contextPath = AppUtil.getRequestContextPath();
        urls.add(contextPath + "/plugin/org.joget.apps.datalist.lib.TextFieldDataListFilterType/js/jquery.placeholder.min.js");
        
        return urls;
    }
}
