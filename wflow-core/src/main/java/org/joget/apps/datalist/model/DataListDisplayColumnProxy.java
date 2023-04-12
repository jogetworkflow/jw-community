package org.joget.apps.datalist.model;

import java.util.Map;

/**
 * A proxy class to use DataListDisplayColumn plugin as DataListColumn
 */
public class DataListDisplayColumnProxy extends DataListColumn {
    private DataListDisplayColumn displayColumn;
    private DataList datalist;

    public DataListDisplayColumnProxy(DataListDisplayColumn displayColumn) {
        this.properties = displayColumn.getProperties();
        this.displayColumn = displayColumn;
    }
    
    public DataListDisplayColumn getDisplayColumn() {
        return displayColumn;
    }
    
    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
        if (displayColumn instanceof DataListDisplayColumnDefault) {
            ((DataListDisplayColumnDefault) displayColumn).setDatalist(datalist);
        }
    }
    
    @Override
    public void setProperties(Map<String, Object> properties) {
        if (displayColumn != null) {
            displayColumn.setProperties(properties);
        }
    }
    
    @Override
    public Object getProperty(String property) {
        return (displayColumn != null)?displayColumn.getProperty(property):null;
    }
    
    @Override
    public String getPropertyString(String property) {
        return (displayColumn != null)?displayColumn.getPropertyString(property):"";
    }
    
    @Override
    public void setProperty(String property, Object value) {
        if (displayColumn != null) {
            displayColumn.setProperty(property, value);
        }
    }
    
    @Override
    public String getName() {
        //always return the primary key of binder to prevent error
        return (displayColumn != null)?getDatalist().getBinder().getPrimaryKeyColumnName():"id";
    }
    
    @Override
    public String getLabel() {
        return (displayColumn != null)?displayColumn.getColumnHeader():"";
    }
    
    public String getRowValue(Object row, int index) {
        return (displayColumn != null)?displayColumn.getRowValue(row, index):"";
    }
    
    @Override
    public boolean isSortable() {
        return false;
    }
    
    @Override
    public Boolean isRenderHtml() {
        return (displayColumn != null)?displayColumn.isRenderHtml():false;
    }
}
