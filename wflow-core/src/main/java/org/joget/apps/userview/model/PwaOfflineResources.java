package org.joget.apps.userview.model;

import java.util.Set;

public interface PwaOfflineResources {
    
    /** 
     * Return a list of static resources to make an element at least readable in offline mode.
     * 
     * @return 
     */
    public Set<String> getOfflineStaticResources();
}
