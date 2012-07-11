package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;

public interface FormDefinitionDao extends AppVersionedObjectDao<FormDefinition> {

    public FormDefinition loadById(String id, AppDefinition appDefinition);

    public boolean add(FormDefinition object);

    public boolean update(FormDefinition object);
    
    public Collection<FormDefinition> getFormDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    public Long getFormDefinitionListCount(String filterString, AppDefinition appDefinition);
    
    public Collection<String> getTableNameList(AppDefinition appDefinition);

    /**
     * Retrieves FormDefinitions mapped to a table name.
     * @param tableName
     * @return
     */
    Collection<FormDefinition> loadFormDefinitionByTableName(String tableName);
}
