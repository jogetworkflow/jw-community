package org.joget.commons.util;

import java.util.List;
import java.util.ArrayList;

public class PagedList<T> extends ArrayList<T> {

    private Integer total;
    private Integer start;
    private Integer rows;
    private String sort;
    private Boolean desc;

    public PagedList() {
        super();
    }

    public PagedList(List list, String sort, Boolean desc, Integer start, Integer rows, Integer total) {
        if (list != null) {
            addAll(list);
        }
        setTotal(total);
    }

    public PagedList(boolean sortAndPage, List list, String sort, Boolean desc, Integer start, Integer rows, Integer total) {
        if (list != null) {
            if (sortAndPage) {
                list = PagingUtils.sortAndPage(list, sort, desc, start, rows);
            }
            addAll(list);
        }
        setTotal(total);
    }

    public Integer getTotal() {
        if (total != null) {
            return total;
        } else {
            return super.size();
        }
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Boolean getDesc() {
        return desc;
    }

    public void setDesc(Boolean desc) {
        this.desc = desc;
    }
}
