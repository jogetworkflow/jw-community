package org.joget.apps.datalist.model;

import org.joget.apps.util.DefaultPropertyValuesCache;
import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DataListDisplayColumnDefault extends ExtDefaultPlugin implements DataListDisplayColumn {
    private DataList datalist;

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    public String getDefaultPropertyValues(){
        return DefaultPropertyValuesCache.getDefaultPropertyValues(this);
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
