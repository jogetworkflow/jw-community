package org.joget.apps.datalist.model;

import org.joget.apps.datalist.lib.TextFieldDataListFilterType;

public class DataListFilter {

    public static final String OPERATOR_OR = "OR";
    public static final String OPERATOR_AND = "AND";
    private String name;
    private String label;
    private DataListFilterType type;
    private String operator;

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

    public String getOperator() {
        if (operator == null) {
            return OPERATOR_AND;
        }
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public DataListFilterType getType() {
        //hardcode default type
        if (type == null) {
            type = new TextFieldDataListFilterType();
        }

        return type;
    }

    public void setType(DataListFilterType type) {
        this.type = type;
    }
}
