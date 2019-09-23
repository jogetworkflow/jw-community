package org.joget.apps.datalist.model;

/**
 * Used by datalist action & formatter plugin to inject css/js/HTML to UI
 */
public interface DataListPluginExtend {
    
    /**
     * Retrieve the HTML to inject to the UI
     * 
     * @param dataList
     * @return 
     */
    public String getHTML(DataList dataList);
}
