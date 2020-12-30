package org.joget.apps.datalist.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;

public class DataListFilter {

    public static final String OPERATOR_OR = "OR";
    public static final String OPERATOR_AND = "AND";
    private String name;
    private String label;
    private DataListFilterType type;
    private String operator;
    private boolean hidden = false;
    
    private Map<String, Object> properties;
    
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public Object getProperty(String property) {
        Object value = (properties != null) ? properties.get(property) : null;
        return value;
    }
    
    public String getPropertyString(String property) {
        String value = (properties != null && properties.get(property) != null) ? (String) properties.get(property) : "";
        return value;
    }
    
    public void setProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
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

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
