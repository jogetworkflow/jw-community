package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains meta data regarding a data list column
 */
public class DataListColumn {

    public DataListColumn() {
    }

    public DataListColumn(String name, String label, boolean sortable) {
        this.name = name;
        this.label = label;
        this.sortable = sortable;
    }
    /**
     * Identifier for the column
     */
    private String name;
    /**
     * Descriptive label for the column
     */
    private String label;
    /**
     * Flag to indicate if column is sortable
     */
    private boolean sortable;
    /**
     * Flag to indicate if column is filterable
     */
    private boolean filterable;
    /**
     * Optional filter for this column
     */
    private DataListFilter filter;
    /**
     * Optional action for this column
     */
    private DataListAction action;
    /**
     * Optional link to a URL
     */
    private String href;
    /**
     * Optional target to a URL
     */
    private String target;
    /**
     * Name of the parameter if linked, defaults to the current column name if not specified.
     */
    private String hrefParam;
    /**
     * Name of the column to use for the param value, defaults to the current column name if not specified.
     */
    private String hrefColumn;
    /**
     * Formatters for the column
     */
    private Collection<DataListColumnFormat> formats;
    /**
     * Optional field to store the DB type e.g. VARCHAR, etc
     */
    private String type;

    /**
     * Convenience method to add a format to this column
     * @param format
     * @return
     */
    public DataListColumn addFormat(DataListColumnFormat format) {
        if (formats == null) {
            formats = new ArrayList<DataListColumnFormat>();
        }
        formats.add(format);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public DataListFilter getFilter() {
        return filter;
    }

    public void setFilter(DataListFilter filter) {
        this.filter = filter;
    }

    public DataListAction getAction() {
        return action;
    }

    public void setAction(DataListAction action) {
        this.action = action;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getHrefParam() {
        return hrefParam;
    }

    public void setHrefParam(String hrefParam) {
        this.hrefParam = hrefParam;
    }

    public String getHrefColumn() {
        return hrefColumn;
    }

    public void setHrefColumn(String hrefColumn) {
        this.hrefColumn = hrefColumn;
    }

    public Collection<DataListColumnFormat> getFormats() {
        return formats;
    }

    public void setFormats(Collection<DataListColumnFormat> formats) {
        this.formats = formats;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
