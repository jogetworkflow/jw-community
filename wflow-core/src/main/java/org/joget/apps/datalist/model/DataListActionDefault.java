package org.joget.apps.datalist.model;

import java.util.Map;
import java.util.Properties;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;

/**
 * Base class for a data list action
 */
public abstract class DataListActionDefault extends DefaultPlugin implements DataListAction {

    private Properties properties = new Properties();

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        return null;
    }

    /**
     * Convenience method to get a property value
     * @param property
     * @return
     */
    public String getProperty(String property) {
        String value = (properties != null) ? properties.getProperty(property) : null;
        return value;
    }

    /**
     * Convenience method to set a property value
     * @param property
     * @param value
     */
    public void setProperty(String property, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.put(property, value);
    }
}
