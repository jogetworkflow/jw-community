package org.joget.apps.app.dao;

import java.util.Collection;
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
     * Reads the package version and modification time for an app version directly from the
     * database, bypassing the Hibernate first-level cache and the app definition caches.
     * <p>
     * Loading the {@code AppDefinition}/{@code PackageDefinition} entity would let the persistence
     * context return the already-managed (possibly stale) instance without re-reading the committed
     * row, hiding a concurrent package save. This scalar read reflects the latest committed values,
     * so a Process Builder read can detect a save that committed while it was generating JSON.
     * </p>
     * @param appId App id
     * @param appVersion App version
     * @return {@code Object[]{Long version, java.util.Date dateModified}}, or null if the app
     *         version has no package definition
     */
    Object[] getPackageMetadata(String appId, Long appVersion);

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
    void addAppActivityForm(PackageDefinition packageDef, String appId, Long appVersion, PackageActivityForm activityForm);

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
    void addAppActivityPlugin(PackageDefinition packageDef, String appId, Long appVersion, PackageActivityPlugin activityPlugin);

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
    void addAppParticipant(PackageDefinition packageDef, String appId, Long appVersion, PackageParticipant participant);

    /**
     * Remove a participant mapping
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param participantId
     */
    void removeAppParticipant(String appId, Long appVersion, String processDefId, String participantId);
    
    /**
     * Get all package versions in used
     * @param packageId
     * @return
     */
    Collection<Long> getPackageVersions(String packageId);
    
    /**
     * Get AppDefinition by package id and version
     * @param packageId
     * @param packageVersion
     * @return
     */
    AppDefinition getAppDefinitionByPackage(String packageId, Long packageVersion);

    /**
     * Clears cached package metadata for the app definition.
     * @param appDef App definition whose package metadata was changed
     */
    void clearPackageDefinitionCaches(AppDefinition appDef);

    /**
     * Clears cached package metadata for the app definition.
     * <p>
     * When {@code clearCurrentSession} is true, the given {@link AppDefinition} is evicted from
     * the current Hibernate session so its package collection can be re-fetched. The rest of the
     * persistence context is left intact.
     * </p>
     * @param appDef App definition whose package metadata was changed
     * @param clearCurrentSession true to evict the given app definition from the current session
     */
    void clearPackageDefinitionCaches(AppDefinition appDef, boolean clearCurrentSession);

    /**
     * Merge an existing package definition
     * @param packageDef 
     */
    public void merge(PackageDefinition packageDef);    

}
