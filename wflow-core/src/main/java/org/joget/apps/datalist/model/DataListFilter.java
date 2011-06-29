package org.joget.apps.datalist.model;

/**
 * Represents a filter option for a data list
 */
public class DataListFilter {

    /**
     * Used to represent an optgroup in a selectbox
     */
    public static final String FILTER_HEADER = "FILTERHEADER";
    /**
     * Parameter name for the filter option containing both the name and value delimited by FILTER_OPTION_DELIMITER
     */
    public static final String PARAMETER_FILTER_OPTION = "fo";
    /**
     * Used to separate filter name and value in a String
     */
    public static final String FILTER_OPTION_DELIMITER = "|";
    /**
     * Parameter name for a chosen filter
     */
    public static final String PARAMETER_FILTER_NAME = "fn";
    /**
     * Parameter name for the filter value
     */
    public static final String PARAMETER_FILTER_VALUE = "fv";
    /**
     * Identifier for the filter
     */
    private String name;
    /**
     * Descriptive name for the filter
     */
    private String label;
    /**
     * Available values for the filter, represented as a 2 dimensional array of key=label. May be null or empty.
     */
    private String[][] options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[][] getOptions() {
        return options;
    }

    public void setOptions(String[][] options) {
        this.options = options;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
