package org.joget.apps.app.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

@Root
public class Message extends AbstractAppVersionedObject {

    @NotBlank
    @Element(required = false)
    private String messageKey;
    @NotBlank
    @Element(required = false)
    private String locale;
    @Element(required = false)
    private String message;

    @Override
    public String getId() {
        if (messageKey != null && locale != null) {
            return messageKey + ID_SEPARATOR + locale;
        } else {
            return super.getId();
        }
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
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
