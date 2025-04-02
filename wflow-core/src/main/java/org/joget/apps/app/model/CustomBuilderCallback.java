package org.joget.apps.app.model;

/**
 * Interface to provide additional builder to perform extra processing in some events
 * 
 */
public interface CustomBuilderCallback extends AppImportExportAwarePlugin {
    
    /**
     * To do some post processing after a definition is added
     * 
     * @param object
     */
    public void addDefinition(BuilderDefinition object);
    
    /**
     * To do some post processing after a definition is updated
     * 
     * @param object
     */
    public void updateDefinition(BuilderDefinition object);
    
    /**
     * To do some post processing after a definition is deleted
     * 
     * @param object
     */
    public void deleteDefinition(BuilderDefinition object);
}
