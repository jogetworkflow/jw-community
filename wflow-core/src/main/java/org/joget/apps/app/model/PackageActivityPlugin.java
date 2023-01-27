package org.joget.apps.app.model;

import java.io.Serializable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Represents a mapping for a workflow activity tool to a plugin.
 */
@Root
public class PackageActivityPlugin implements Serializable {

    private PackageDefinition packageDefinition;
    @Element(required = false)
    private String processDefId;
    @Element(required = false)
    private String activityDefId;
    @Element(required = false)
    private String pluginName;
    @Element(required = false)
    private String pluginProperties;

    /**
     * Unique ID (primary key) for the object, which consists of the process def ID and activity def ID separated by ::.
     * @return
     */
    public String getUid() {
        String key = getProcessDefId() + PackageDefinition.UID_SEPARATOR + getActivityDefId();
        return key;
    }

    public void setUid(String uid) {
    }

    public PackageDefinition getPackageDefinition() {
        return packageDefinition;
    }

    public void setPackageDefinition(PackageDefinition packageDefinition) {
        this.packageDefinition = packageDefinition;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getActivityDefId() {
        return activityDefId;
    }

    public void setActivityDefId(String activityDefId) {
        this.activityDefId = activityDefId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(String pluginProperties) {
        this.pluginProperties = pluginProperties;
    }
}
