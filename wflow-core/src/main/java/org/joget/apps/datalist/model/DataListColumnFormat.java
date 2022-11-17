package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;
import org.kecak.apps.datalist.DataJsonControllerDataListColumnHandler;

import javax.annotation.Nonnull;

/**
 * Interface of Datalist Column Formatter plugin
 */
public interface DataListColumnFormat extends PropertyEditable, DataJsonControllerDataListColumnHandler {

    /**
     * Format column value 
     * 
     * @param dataList
     * @param column
     * @param row
     * @param value
     * @return
     */
    String format(DataList dataList, DataListColumn column, Object row, Object value);


    /**
     * Get Sort As
     *
     * Manipulate how the column will be sort when being displayed on dataList.
     *
     * When using {@link org.joget.apps.form.dao.FormDataDao} for example binder {@link org.joget.apps.datalist.lib.FormRowDataListBinder},
     * if the return contains question mark character ('?') the binder will consider it as a function
     * that will be implemented using "ORDER BY [sortAs]" pattern, otherwise
     * default "ORDER BY CAST([sort] AS [sortAs])" pattern will be used.
     *
     * For other custom dataList binder, you should implement your own handling mechanism.
     *
     * @param dataList
     * @param column
     * @return
     */
    String getSortAs(@Nonnull DataList dataList, @Nonnull DataListColumn column);
}
