package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface of Datalist Filter Type Plugin
 * 
 */
public interface DataListFilterType extends PropertyEditable {

    /**
     * HTML template of the filter
     * 
     * @param datalist
     * @param name
     * @param label
     * @return 
     */
    public String getTemplate(DataList datalist, String name, String label);

    /**
     * Condition and parameters to construct query
     * 
     * @param datalist
     * @param name
     * @return 
     */
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name);
}
