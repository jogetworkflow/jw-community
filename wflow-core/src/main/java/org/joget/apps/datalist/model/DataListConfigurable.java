package org.joget.apps.datalist.model;

import java.util.Properties;

/**
 * Interface for objects that are dynamically created and configured
 */
public interface DataListConfigurable {

    /**
     * Identifier for the object
     * @return
     */
    String getName();

    /**
     * Fully qualified class name for the object, to be used for instantiation
     * @return
     */
    String getClassName();

    /**
     * Configured properties for the object
     * @return
     */
    Properties getProperties();

    /**
     * Set configured properties to the object
     * @param properties
     */
    void setProperties(Properties properties);
}
