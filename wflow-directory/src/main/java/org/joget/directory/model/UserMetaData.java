package org.joget.directory.model;

import java.io.Serializable;
import org.joget.commons.spring.model.Auditable;

public class UserMetaData implements Serializable, Auditable {
    private String username;
    private String key;
    private String value;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAuditTrailId() {
        return username + "_" + key;
    }
}
