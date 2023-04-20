package org.joget.apps.datalist.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.service.PropertyUtil;

public abstract class DataListDisplayColumnDefault extends ExtDefaultPlugin implements DataListDisplayColumn {
    private DataList datalist;
    private static Map<String, String> defaultPropertyValues = new HashMap<String, String>();

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    public String getDefaultPropertyValues(){
        if (!DataListDisplayColumnDefault.defaultPropertyValues.containsKey(getClassName())) {
            DataListDisplayColumnDefault.defaultPropertyValues.put(getClassName(), PropertyUtil.getDefaultPropertyValues(getPropertyOptions()));
        }
        return DataListDisplayColumnDefault.defaultPropertyValues.get(getClassName());
    }
    
    @Override
    public String getBuilderJavaScriptComponent() {
        return "";
    }
    
    @Override
    public String getInjectedHtml() {
        return "";
    }
}
