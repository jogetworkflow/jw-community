package org.joget.apps.app.model;

import java.io.Serializable;

/**
 * Key for a PackageParticipant
 */
public class PackageParticipantKey implements Serializable {
    
    private String packageId;
    private Long packageVersion;
    private String processDefId;
    private String participantId;    

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public Long getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(Long packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String activityDefId) {
        this.participantId = activityDefId;
    }
    
    @Override
    public String toString() {
        return "{type=PackageParticipantKey," + "packageId=" + packageId + ", packageVersion=" + packageVersion + ", processDefId=" + processDefId + ", participantId=" + participantId + "'}'";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    
}
