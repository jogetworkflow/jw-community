package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;

public interface DatalistDefinitionDao extends AppVersionedObjectDao<DatalistDefinition> {

    public Collection<DatalistDefinition> getDatalistDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    public Long getDatalistDefinitionListCount(String filterString, AppDefinition appDefinition);
}
