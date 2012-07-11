package org.joget.apps.app.dao;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;

/**
 * DAO to load/store PackageDefinition objects
 */
public interface PackageDefinitionDao extends VersionedObjectDao<PackageDefinition> {

    /**
     * Loads the package definition for a specific app version
     * @param appId
     * @param appVersion
     * @return
     */
    PackageDefinition loadAppPackageDefinition(String appId, Long appVersion);

    /**
     * Loads the package definition based on a process definition ID
     * @param packageId
     * @param packageVersion
     * @return
     */
    PackageDefinition loadPackageDefinition(String packageId, Long packageVersion);

    /**
     * Loads the package definition based on a process definition ID
     * @param packageVersion
     * @param processDefId
     * @return
     */
    PackageDefinition loadPackageDefinitionByProcess(String packageId, Long packageVersion, String processDefId);

    /**
     * Create package definition for an app
     * @param appDef
     * @param packageVersion
     * @return
     */
    PackageDefinition createPackageDefinition(AppDefinition appDef, Long packageVersion);

    /**
     * Update the package version for a package definition
     * @param packageDef
     * @param packageVersion
     * @return
     */
    PackageDefinition updatePackageDefinitionVersion(PackageDefinition packageDef, Long packageVersion);

    /**
     * Add a form/URL mapping to an activity
     * @param appId
     * @param appVersion
     * @param activityForm
     */
    void addAppActivityForm(String appId, Long appVersion, PackageActivityForm activityForm);

    /**
     * Remove a form/URL mapping from an activity
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param activityDefId
     */
    void removeAppActivityForm(String appId, Long appVersion, String processDefId, String activityDefId);

    /**
     * Add a plugin mapping to an activity
     * @param appId
     * @param appVersion
     * @param activityPlugin
     */
    void addAppActivityPlugin(String appId, Long appVersion, PackageActivityPlugin activityPlugin);

    /**
     * Remove a plugin mapping from an activity
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param activityDefId
     */
    void removeAppActivityPlugin(String appId, Long appVersion, String processDefId, String activityDefId);

    /**
     * Add a participant mapping
     * @param appId
     * @param appVersion
     * @param participant
     */
    void addAppParticipant(String appId, Long appVersion, PackageParticipant participant);

    /**
     * Remove a participant mapping
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param participantId
     */
    void removeAppParticipant(String appId, Long appVersion, String processDefId, String participantId);
}
