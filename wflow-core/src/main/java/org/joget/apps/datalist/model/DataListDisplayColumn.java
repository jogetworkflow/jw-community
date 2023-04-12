package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;

/**
 * A plugin type to adding column to list just for display purpose.
 * 
 */
public interface DataListDisplayColumn extends PropertyEditable {
    
    /**
     * Return the label for column header
     * @return 
     */
    public String getColumnHeader();
    
    /**
     * Return the cell value for each row
     * @param row
     * @param index
     * @return 
     */
    public String getRowValue(Object row, int index);
    
    /**
     * Is the cell value need to render as HTML
     * @return 
     */
    public Boolean isRenderHtml();
    
    /**
     * Return the icon for list builder palette
     * @return 
     */
    public String getIcon();
    
    /**
     * Return the javascript builder component to initialized and override methods in List Builder
     * @return 
     */
    public String getBuilderJavaScriptComponent();
    
    /**
     * Return the html to inject into list
     * @return 
     */
    public String getInjectedHtml();
}
