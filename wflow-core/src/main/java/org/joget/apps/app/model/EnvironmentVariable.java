package org.joget.apps.app.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class EnvironmentVariable extends AbstractAppVersionedObject {

    @Element(required = false)
    private String value;
    @Element(required = false)
    private String remarks;

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
