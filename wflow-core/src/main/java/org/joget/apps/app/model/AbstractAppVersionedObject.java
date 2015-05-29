package org.joget.apps.app.model;

import java.io.Serializable;
import org.simpleframework.xml.Element;

/**
 * Abstract class to represent an object that is under app versioned.
 */
public class AbstractAppVersionedObject implements Serializable {

    public static final String ID_SEPARATOR = "_";
    private AppDefinition appDefinition;
    @Element(required = false)
    private String id;
    @Element(required = false)
    private String appId;
    @Element(required = false)
    private Long appVersion;

    public AppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(AppDefinition appDefinition) {
        this.appDefinition = appDefinition;
    }

    public String getAppId() {
        if (appDefinition != null) {
            return appDefinition.getId();
        } else {
            return appId;
        }
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getAppVersion() {
        if (appDefinition != null) {
            return appDefinition.getVersion();
        } else {
            return appVersion;
        }
    }

    public void setAppVersion(Long appVersion) {
        this.appVersion = appVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
