package org.joget.apps.datalist.lib;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.datalist.model.DataListBinder;

/**
 * This interface should be implemented for a List Binder that intends to have its non-critical queries be cached.
 *
 * <p>Non-critical queries can be classified as queries that are secondary information such as list count,
 * and not involved in the presentation of primary data such as row data.
 */
public interface StaleCacheDataListBinder {

    /**
     * Return an array of additional parameters that will be hashed and used for retrieving non-critical query results.
     * Please note that the Objects returned by this method should implement {@link Object#hashCode}. Otherwise, results
     * are not guaranteed to be consistent and will be considered undefined behaviour.
     *
     * <p>This method is intended to be used as a last resort to convey any custom conditions that are hardcoded into
     * all its queries. As this method will be called <b>every time, on execution of all non-critical queries</b>, and
     * the parameters returned by this method should only include hardcoded information that cannot be conveyed in any
     * other way.
     *
     * <p>A simple way to convey information (stateless or stateful), such as parameters that should only be used when a
     * particular query is executed would be to add a new property entry into the binder's properties, see
     * {@link org.joget.plugin.property.model.PropertyEditable}. Implementation is up to developer's own discretion.
     *
     * <p>The objects returned should be the criteria/conditions and values that will be used in the query.
     * This ensures that the cached result will be unique to the query being executed and is retrievable in subsequent
     * executions.
     *
     * <p>Typical parameters that should be hashed include:
     * <ul>
     *     <li>Table name</li>
     *     <li>Criteria/conditions: WHERE, GROUP BY, HAVING, etc.</li>
     *     <li>Prepared statement values: the values for {@code ?} in prepared statements</li>
     * </ul>
     *
     * <p>This key may be used as-is or be re-hashed with other parameters in the caller method.
     * See {@link org.joget.apps.datalist.service.DataListUtil#getStaleCacheKey}
     *
     * @return an array of Objects that implement {@link Object#hashCode}
     */
    Object[] getAdditionalStaleCacheKeyParams();

    /**
     * Get whether to cache the row count
     * @return {@code true} if should cache; {@code false} otherwise
     */
    boolean shouldCacheRowCount();

    /**
     * Get the amount of time the stale data should be available for.
     * @return duration in seconds
     */
    int getCacheTtl();
}
