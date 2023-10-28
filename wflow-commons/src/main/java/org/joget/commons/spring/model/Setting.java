package org.joget.commons.spring.model;

import java.io.Serializable;

public class Setting implements Serializable {

    private String id;
    private String property;
    private String value;
    private Integer ordering;
    private String originalValue = null; //used to keep track value changes

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        //keep track original value
        if (id != null && originalValue == null) {
            originalValue = value;
        }
        
        this.value = value;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
