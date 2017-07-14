package org.joget.apps.app.model;

import java.text.DecimalFormat;
import org.simpleframework.xml.Element;

public class AppResource extends AbstractAppVersionedObject {
    @Element(required = false)
    private Long filesize;
    @Element(required = false)
    private String permissionClass;
    @Element(required = false)
    private String permissionProperties;

    public String getFilesizeString() {
        if(filesize <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(filesize)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(filesize/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
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
