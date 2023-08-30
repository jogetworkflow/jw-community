package org.joget.apps.app.model;

import java.io.Serializable;
import java.util.Date;
import org.simpleframework.xml.Element;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

/**
 * Abstract class to represent an object that is under app versioned.
 */
public class AbstractAppVersionedObject implements Serializable {

    public static final String ID_SEPARATOR = "_";
    private AppDefinition appDefinition;
    @Element(required = false)
    @RegExp(value = "^[ \\.0-9a-zA-Z_-]+$")
    private String id;
    @Element(required = false)
    private String appId;
    @Element(required = false)
    private Long appVersion;

    private String name;
    private String description;
    private String json;
    private Date dateCreated;
    private Date dateModified;

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

    public String getName() {
        return (name == null)?"":name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Override
    public String toString() {
        return "{" + "id:\"" + id + "\", appId:\"" + getAppId() + "\", appVersion:\"" + getAppVersion() + "\", name:\"" + getName() + "\", dateCreated:\"" + getDateCreated() + "\", dateModified:\"" + getDateModified() + "\"}";
    }
}
