package org.joget.apps.app.model;

import java.util.zip.ZipOutputStream;

/**
 * Interface to provide additional builder to perform extra processing in some events
 * 
 */
public interface CustomBuilderCallback {
    
    /**
     * To do some post processing after import based on the import app definition and the zip file
     * 
     * @param appDef
     * @param zip 
     */
    public void importAppPostProcessing(AppDefinition appDef, byte[] zip);
    
    /**
     * To do some post processing to the exported zip based on the app definition
     * 
     * @param appDef
     * @param zip 
     */
    public void exportAppPostProcessing(AppDefinition appDef, ZipOutputStream zip);
    
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
