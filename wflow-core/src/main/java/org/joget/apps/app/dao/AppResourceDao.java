package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;

public interface AppResourceDao extends AppVersionedObjectDao<AppResource> {
    Collection<AppResource> getResources(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);
    
    Long getResourcesCount(String filterString, AppDefinition appDefinition);
}
