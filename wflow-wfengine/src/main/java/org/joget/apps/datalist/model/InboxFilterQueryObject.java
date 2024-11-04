package org.joget.apps.datalist.model;

public interface InboxFilterQueryObject {
    
    public String getOperator();

    public void setOperator(String operator);

    public String getQuery();

    public void setQuery(String query);

    public String[] getValues();

    public void setValues(String[] values);
}
