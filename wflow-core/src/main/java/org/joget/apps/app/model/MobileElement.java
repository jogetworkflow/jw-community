package org.joget.apps.app.model;

/**
 * Marker interface to indicate whether an element (form, userview, etc) supports mobile view.
 */
public interface MobileElement {

    /**
     * Verifies that mobile view is supported.
     * @return 
     */
    boolean isMobileSupported();
    
}
