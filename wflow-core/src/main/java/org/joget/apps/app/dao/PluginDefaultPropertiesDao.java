package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;

public interface PluginDefaultPropertiesDao extends AppVersionedObjectDao<PluginDefaultProperties> {

    public Collection<PluginDefaultProperties> getPluginDefaultPropertiesList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    public Long getPluginDefaultPropertiesListCount(String filterString, AppDefinition appDefinition);
    
    /**
     * Delete multiple plugin default properties by ids
     * 
     * @param ids
     * @param appDef
     * @return 
     */
    public boolean delete(String[] ids, AppDefinition appDef);
}
