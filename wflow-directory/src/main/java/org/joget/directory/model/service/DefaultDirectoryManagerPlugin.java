package org.joget.directory.model.service;

import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * A base abstract class to develop a Directory Manager plugin
 * 
 */
public abstract class DefaultDirectoryManagerPlugin extends ExtDefaultPlugin implements DirectoryManagerPlugin, PropertyEditable, ExtDirectoryManager {
    
    /**
     * Used by System to retrieve a directory manager implementation
     * 
     * @param properties
     * @return 
     */
    public DirectoryManager getDirectoryManagerImpl(Map properties) {
        setProperties(properties);
        return (DirectoryManager) this;
    }
    
    
}
