package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains meta data regarding a data list column
 */
public class DataListColumn {
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
     * Flag to indicate if column is hidden
     */
    private boolean hidden;
    /**
     * Column Width
     */
    private String width;
    /**
     * Column Style
     */
    private String style;
    /**
     * Optional action for this column
     */
    private DataListAction action;
    /**
     * Optional link to a URL
     */
    /**
     * Formatters for the column
     */
    private Collection<DataListColumnFormat> formats;
    /**
     * Optional field to store the DB type e.g. VARCHAR, etc
     */
    private String type;
    /**
     * Flag to indicate whether to render the column as HTML content
     */
    private Boolean renderHtml = null;

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

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getStyle() {
        if (style == null) {
            style = "";
        }
        if (getWidth() != null && !getWidth().isEmpty()) {
            if (!style.isEmpty() && !style.endsWith(";")) {
                style += ";";
            }
            style += "width:"+ getWidth() + ";";
            width = null;
        }
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    
    public DataListAction getAction() {
        return action;
    }

    public void setAction(DataListAction action) {
        this.action = action;
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
    
    public Boolean isRenderHtml() {
        return renderHtml;
    }

    public void setRenderHtml(boolean renderHtml) {
        this.renderHtml = renderHtml;
    }
}
