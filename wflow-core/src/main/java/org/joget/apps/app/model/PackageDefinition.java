package org.joget.apps.app.model;

import java.util.HashMap;
import java.util.Map;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Metadata for a Workflow Package.
 */
@Root
public class PackageDefinition extends AbstractVersionedObject {

    public static final String UID_SEPARATOR = "::";
    @Element(required = false)
    private String uid;
    private AppDefinition appDefinition;
    
    @Path("packageActivityFormMap")
    @ElementMap(required = false,inline=true)
    private Map<String, PackageActivityForm> packageActivityFormMap;
    
    @Path("packageActivityPluginMap")
    @ElementMap(required = false,inline=true)
    private Map<String, PackageActivityPlugin> packageActivityPluginMap;
    
    @Path("packageParticipantMap")
    @ElementMap(required = false,inline=true)
    private Map<String, PackageParticipant> packageParticipantMap;

    public String getPackageUid(String processDefId, String id) {
        String ouid = processDefId + UID_SEPARATOR + id;
        return ouid;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    public AppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(AppDefinition appDefinition) {
        if (appDefinition != null) {
            setAppId(appDefinition.getAppId());
        }
        this.appDefinition = appDefinition;
    }

    public Map<String, PackageActivityForm> getPackageActivityFormMap() {
        return packageActivityFormMap;
    }

    public void setPackageActivityFormMap(Map<String, PackageActivityForm> packageActivityFormMap) {
        this.packageActivityFormMap = packageActivityFormMap;
    }

    public PackageActivityForm getPackageActivityForm(String processDefId, String activityDefId) {
        PackageActivityForm result = null;
        if (this.packageActivityFormMap != null) {
            String ouid = getPackageUid(processDefId, activityDefId);
            result = packageActivityFormMap.get(ouid);
        }
        return result;
    }

    public void addPackageActivityForm(PackageActivityForm packageActivityForm) {
        if (this.packageActivityFormMap == null) {
            this.packageActivityFormMap = new HashMap<String, PackageActivityForm>();
        }
        String ouid = packageActivityForm.getUid();
        this.packageActivityFormMap.put(ouid, packageActivityForm);
        packageActivityForm.setPackageDefinition(this);
    }

    public void removePackageActivityForm(String processDefId, String activityDefId) {
        if (this.packageActivityFormMap == null) {
            this.packageActivityFormMap = new HashMap<String, PackageActivityForm>();
        }
        String ouid = getPackageUid(processDefId, activityDefId);
        this.packageActivityFormMap.remove(ouid);
    }

    public Map<String, PackageActivityPlugin> getPackageActivityPluginMap() {
        return packageActivityPluginMap;
    }

    public void setPackageActivityPluginMap(Map<String, PackageActivityPlugin> packageActivityPluginMap) {
        this.packageActivityPluginMap = packageActivityPluginMap;
    }

    public PackageActivityPlugin getPackageActivityPlugin(String processDefId, String activityDefId) {
        PackageActivityPlugin result = null;
        if (this.packageActivityPluginMap != null) {
            String ouid = getPackageUid(processDefId, activityDefId);
            result = packageActivityPluginMap.get(ouid);
        }
        return result;
    }

    public void addPackageActivityPlugin(PackageActivityPlugin packageActivityPlugin) {
        if (this.packageActivityPluginMap == null) {
            this.packageActivityPluginMap = new HashMap<String, PackageActivityPlugin>();
        }
        String ouid = packageActivityPlugin.getUid();
        this.packageActivityPluginMap.put(ouid, packageActivityPlugin);
        packageActivityPlugin.setPackageDefinition(this);
    }

    public void removePackageActivityPlugin(String processDefId, String activityDefId) {
        if (this.packageActivityPluginMap == null) {
            this.packageActivityPluginMap = new HashMap<String, PackageActivityPlugin>();
        }
        String ouid = getPackageUid(processDefId, activityDefId);
        this.packageActivityPluginMap.remove(ouid);
    }

    public Map<String, PackageParticipant> getPackageParticipantMap() {
        return packageParticipantMap;
    }

    public void setPackageParticipantMap(Map<String, PackageParticipant> packageParticipantMap) {
        this.packageParticipantMap = packageParticipantMap;
    }

    public PackageParticipant getPackageParticipant(String processDefId, String participantId) {
        PackageParticipant result = null;
        if (this.packageParticipantMap != null) {
            String ouid = getPackageUid(processDefId, participantId);
            result = packageParticipantMap.get(ouid);
        }
        return result;
    }

    public void addPackageParticipant(PackageParticipant packageParticipant) {
        if (this.packageParticipantMap == null) {
            this.packageParticipantMap = new HashMap<String, PackageParticipant>();
        }
        String ouid = packageParticipant.getUid();
        this.packageParticipantMap.put(ouid, packageParticipant);
        packageParticipant.setPackageDefinition(this);
    }

    public void removePackageParticipant(String processDefId, String participantId) {
        if (this.packageParticipantMap == null) {
            this.packageParticipantMap = new HashMap<String, PackageParticipant>();
        }
        String ouid = getPackageUid(processDefId, participantId);
        this.packageParticipantMap.remove(ouid);
    }
}
