package org.joget.apps.datalist.model;

import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

import javax.annotation.Nonnull;

/**
 * A base abstract class to develop a Datalist Column Formatter
 */
public abstract class DataListColumnFormatDefault extends ExtDefaultPlugin implements DataListColumnFormat {
    private DataList datalist;

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }

    /**
     *
     * @param dataList
     * @param column
     * @return
     */
    public String getSortAs(@Nonnull DataList dataList, @Nonnull DataListColumn column) {
        final String sort = column.getName();
        return FormUtil.PROPERTY_DATE_CREATED.equals(sort) || FormUtil.PROPERTY_DATE_MODIFIED.equals(sort) ? "timestamp" : "string";
    }
}
