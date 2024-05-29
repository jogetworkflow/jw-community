package org.joget.apps.app.dao;

import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.model.AppDefinition;

public interface AppDefinitionDao extends VersionedObjectDao<AppDefinition> {
    
    public AppDefCache getCache();

    public Long getPublishedVersion(String appId);
    
    public AppDefinition getPublishedAppDefinition(String appId);

    public Collection<AppDefinition> findPublishedApps(final String sort, final Boolean desc, final Integer start, final Integer rows);
    
    public AppDefinition syncAppDefinition(String appId, Long version);

    public void saveOrUpdate(String appId, Long version, boolean includeDependencies);
 
    public void merge(AppDefinition appDef);    
    
    public void updateDateModified(AppDefinition appDef);
    
    public void updateDateModified(AppDefinition appDef, Date dateModified);
}
