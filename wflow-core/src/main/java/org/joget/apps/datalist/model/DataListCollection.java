package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.List;
import org.displaytag.properties.SortOrderEnum;

/**
 * Collection to represent a paged list of data list rows
 */
public class DataListCollection<E> extends ArrayList<E> {

    private int pageNumber;
    private int objectsPerPage;
    private int fullListSize;
    private String sortCriterion;
    private SortOrderEnum sortDirection;
    private String searchId;

    public List getList() {
        return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    public void setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
    }

    public int getFullListSize() {
        return fullListSize;
    }

    public void setFullListSize(int fullListSize) {
        this.fullListSize = fullListSize;
    }

    public String getSortCriterion() {
        return sortCriterion;
    }

    public void setSortCriterion(String sortCriterion) {
        this.sortCriterion = sortCriterion;
    }

    public SortOrderEnum getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortOrderEnum sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }
}
