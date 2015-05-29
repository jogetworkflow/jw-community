package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AbstractVersionedObject;

public interface VersionedObjectDao<T extends AbstractVersionedObject> {

    /**
     * Total row count by specific version
     * @param id
     * @param appId
     * @param version
     * @param name
     * @return
     */
    Long countByVersion(String id, String appId, Long version, String name);

    /**
     * Count the latest versions of matching objects
     * @param id Optional ID to filter
     * @param appId Optional package ID to filter
     * @param name Optional name to filter
     * @return
     */
    Long countLatestVersions(String id, String appId, String name);

    /**
     * Total number of versions for a specific ID.
     * @param id
     * @return
     */
    Long countVersions(String id);

    /**
     * Delete an object by primary key
     * @param obj
     */
    void delete(T obj);

    /**
     * Delete all versions of an object
     * @param appId
     */
    void deleteAllVersions(String id);

    /**
     * Delete a specific object version
     * @param id
     * @param version
     */
    void deleteVersion(String id, Long version);

    /**
     * Find unique forms by specific version
     * @param id
     * @param appId
     * @param version
     * @param name
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    Collection<T> findByVersion(String id, String appId, Long version, String name, final String sort, final Boolean desc, final Integer start, final Integer rows);

    /**
     * Find the latest versions of matching objects
     * @param id Optional ID to filter
     * @param appId Optional package ID to filter
     * @param name Optional name to filter
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    Collection<T> findLatestVersions(String id, String appId, String name, final String sort, final Boolean desc, final Integer start, final Integer rows);

    /**
     * Find the versions for a specific ID.
     * @param id
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    Collection<T> findVersions(String id, final String sort, final Boolean desc, final Integer start, final Integer rows);

    /**
     * Hibernate entity name for the object.
     * @return
     */
    String getEntityName();

    /**
     * Gets the latest version for an object
     * @param id
     * @return
     */
    Long getLatestVersion(final String id);

    /**
     * Find the latest version of an object by ID
     * @param id
     * @return
     */
    T loadById(String id);

    /**
     * Loads an object by unique ID (primary key)
     * @param uid
     * @return
     */
    T loadByUid(T uid);

    /**
     * Find a specific version of an object by ID
     * @param id
     * @param version
     * @return
     */
    T loadVersion(String id, Long version);

    /**
     * Save/update an object
     * @param object
     */
    void saveOrUpdate(T object);
}
