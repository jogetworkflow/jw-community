package org.joget.apps.app.model;

import org.simpleframework.xml.Element;

public class AppResource extends AbstractAppVersionedObject {
    @Element(required = false)
    private Long filesize;
    @Element(required = false)
    private String permissionClass;
    @Element(required = false)
    private String permissionProperties;

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getPermissionClass() {
        return permissionClass;
    }

    public void setPermissionClass(String permissionClass) {
        this.permissionClass = permissionClass;
    }
    
    public String getPermissionProperties() {
        return permissionProperties;
    }

    public void setPermissionProperties(String permissionProperties) {
        this.permissionProperties = permissionProperties;
    }
}
