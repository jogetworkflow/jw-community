package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;

public interface UserviewDefinitionDao extends AppVersionedObjectDao<UserviewDefinition> {

    public Collection<UserviewDefinition> getUserviewDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    public Long getUserviewDefinitionListCount(String filterString, AppDefinition appDefinition);
}
