package org.joget.apps.datalist.model;

import java.util.Map;
import java.util.Properties;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Default implementation to format Object to String
 */
public abstract class DataListColumnFormatDefault extends DefaultPlugin implements DataListColumnFormat, PropertyEditable {

    private Properties properties;

    @Override
    public String getName() {
        return "Default String column format";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Object getProperty(String property) {
        Object value = (properties != null) ? properties.get(property) : null;
        return value;
    }

    public String getPropertyString(String property) {
        String value = (properties != null) ? (String) properties.get(property) : "";
        return value;
    }

    public void setProperty(String property, Object value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.put(property, value);
    }

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        String text = (value != null) ? value.toString() : null;
        return text;
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }
}
