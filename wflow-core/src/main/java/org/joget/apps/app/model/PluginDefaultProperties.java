package org.joget.apps.app.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class PluginDefaultProperties extends AbstractAppVersionedObject {

    @Element(required = false)
    private String pluginName;
    @Element(required = false)
    private String pluginDescription;
    @Element(required = false)
    private String pluginProperties;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public void setPluginDescription(String pluginDescription) {
        this.pluginDescription = pluginDescription;
    }

    public String getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(String pluginProperties) {
        this.pluginProperties = pluginProperties;
    }
}
