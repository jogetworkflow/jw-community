package org.joget.apps.datalist.lib;

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

    public String getName() {
        return "Text Field Data List Filter Type";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Data List Filter Type - Text Field";
    }

    public String getLabel() {
        return "Text Field";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/textFieldDataListFilterType.json", null, true, null);
    }

    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/textFieldDataListFilterType.ftl", null);
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            String cname = datalist.getBinder().getColumnName(name);
            
            //support aggregate function
            if (cname.toLowerCase().contains("count(")
                    || cname.toLowerCase().contains("sum(")
                    || cname.toLowerCase().contains("avg(")
                    || cname.toLowerCase().contains("min(")
                    || cname.toLowerCase().contains("max(")) {
                queryObject.setQuery(cname + " = ?");
                queryObject.setValues(new String[]{value});
            } else {
                queryObject.setQuery("lower(" + cname + ") like lower(?)");
                queryObject.setValues(new String[]{'%' + value + '%'});
            }
            
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
