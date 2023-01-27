package org.kecak.apps.archive.model;

import java.io.Serializable;
import java.util.Date;

public class ArchiveXPDL implements Serializable{
	private static long serialVersionUID = 8126768131764084425L;

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }
    private String id;
	private Date dateCreated;
	private String XPDLId;
	private String XPDLVersion;
	private String XPDLClassVersion;
	private String XPDLUploadTime;
	private String oid;
	private String version;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getXPDLId() {
		return XPDLId;
	}
	public void setXPDLId(String xPDLId) {
		XPDLId = xPDLId;
	}
	public String getXPDLVersion() {
		return XPDLVersion;
	}
	public void setXPDLVersion(String xPDLVersion) {
		XPDLVersion = xPDLVersion;
	}
	public String getXPDLClassVersion() {
		return XPDLClassVersion;
	}
	public void setXPDLClassVersion(String xPDLClassVersion) {
		XPDLClassVersion = xPDLClassVersion;
	}
	public String getXPDLUploadTime() {
		return XPDLUploadTime;
	}
	public void setXPDLUploadTime(String xPDLUploadTime) {
		XPDLUploadTime = xPDLUploadTime;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
