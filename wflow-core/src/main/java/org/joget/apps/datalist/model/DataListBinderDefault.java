package org.joget.apps.datalist.model;

import java.util.Map;
import java.util.Properties;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;

/**
 * Convenient abstract base class for binders to inherit
 */
public abstract class DataListBinderDefault extends DefaultPlugin implements DataListBinder {
    public static String USERVIEW_KEY_SYNTAX = "#userviewKey#";

    Properties properties;

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

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }
}
