package org.kecak.apps.form.model;

import java.util.Map;

/**
 * @author aristo
 *
 * Indicator for Grid Element
 */
@Deprecated
public interface GridElement {

    /**
     * Value formatting when displaying to grid element
     *
     * @param columnName
     * @param properties property for current columnName
     * @param recordId
     * @param value
     * @param appId
     * @param appVersion
     * @param contextPath
     * @return
     */
    String formatColumn(String columnName, Map<String, String> properties, String recordId, String value, String appId, Long appVersion, String contextPath);

    /**
     * Get properties of grid columns
     *
     * @return
     */
    Map<String, String>[] getColumnProperties();

    /**
     * Extract field name / id from column property
     *
     * @param columnProperty
     * @return
     */
    String getField(Map<String, String> columnProperty);
}
