package org.joget.apps.datalist.model;

/**
 * A table/list containing rows of data
 */
public class DataList {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAXIMUM_PAGE_SIZE = 1000000;
    /**
     * Unique identifier
     */
    private String id;
    /**
     * Descriptive name
     */
    private String name;
    /**
     * Configured columns
     */
    private DataListColumn[] columns;
    /**
     * Configured list-level actions (buttons at top/bottom of the list)
     */
    private DataListAction[] actions;
    /**
     * Configured row-level actions (links at each row in the list)
     */
    private DataListAction[] rowActions;
    /**
     * Configured filters
     */
    private DataListFilter[] filters;
    /**
     * Number of rows per page
     */
    private int pageSize = DEFAULT_PAGE_SIZE;
    /**
     * Configured binder
     */
    private DataListBinder binder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataListColumn[] getColumns() {
        return columns;
    }

    public void setColumns(DataListColumn[] columns) {
        this.columns = columns;
    }

    public DataListBinder getBinder() {
        return binder;
    }

    public void setBinder(DataListBinder binder) {
        this.binder = binder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public DataListAction[] getActions() {
        return actions;
    }

    public void setActions(DataListAction[] actions) {
        this.actions = actions;
    }

    public DataListAction[] getRowActions() {
        return rowActions;
    }

    public void setRowActions(DataListAction[] rowActions) {
        this.rowActions = rowActions;
    }

    public DataListFilter[] getFilters() {
        return filters;
    }

    public void setFilters(DataListFilter[] filters) {
        this.filters = filters;
    }
}
