package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;

public interface AppDefinitionDao extends VersionedObjectDao<AppDefinition> {

    public Long getPublishedVersion(String appId);

    public Collection<AppDefinition> findPublishedApps(final String sort, final Boolean desc, final Integer start, final Integer rows);
}
