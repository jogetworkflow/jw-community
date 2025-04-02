package org.joget.apps.app.model;

import java.util.zip.ZipOutputStream;

public interface AppImportExportAwarePlugin {
    
    /**
     * Used to inject html into the import app configuration page
     * @return 
     */
    public String importAppConfigHtml();
    
    /**
     * To do some post processing after import based on the import app definition and the zip file
     * 
     * @param appDef
     * @param zip 
     */
    public void importAppPostProcessing(AppDefinition appDef, byte[] zip);
    
    /**
     * Used to inject html into the export app configuration page
     * @return 
     */
    public String exportAppConfigHtml();
    
    /**
     * To do some post processing to the exported zip based on the app definition
     * 
     * @param appDef
     * @param zip 
     */
    public void exportAppPostProcessing(AppDefinition appDef, ZipOutputStream zip);
}
