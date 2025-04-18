package org.joget.apps.datalist.model;

import java.util.Arrays;
import java.util.Objects;

public class DataListFilterQueryObject {

    private String operator;
    private String query;
    private String[] values;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataListFilterQueryObject that = (DataListFilterQueryObject) o;
        return Objects.equals(operator, that.operator) && Objects.equals(query, that.query) && Objects.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, query, Arrays.hashCode(values));
    }

    @Override
    public String toString() {
        return "DataListFilterQueryObject{" + "operator='" + operator + '\'' +
                ", query='" + query + '\'' +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
