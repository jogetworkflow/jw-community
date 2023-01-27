package org.joget.commons.spring.model;

import java.io.Serializable;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

public class ResourceBundleMessage implements Serializable{

    private String id;
    @NotBlank
    private String key;
    @NotBlank
    private String locale;
    @NotBlank
    private String message;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
