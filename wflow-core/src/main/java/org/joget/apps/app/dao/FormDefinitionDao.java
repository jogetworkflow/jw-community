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

    /**
     * A cheap change-detection manifest for every definition mapped to a table: one row of
     * {@code [appId, appVersion, formId, dateModified, jsonLength]} per definition, WITHOUT the
     * {@code json} payload.
     */
    Collection<Object[]> getFormDefinitionManifestByTableName(String tableName);

    /** Loads the full definitions for specific form ids mapped to a table — the changed ones. */
    Collection<FormDefinition> loadFormDefinitionByTableNameAndIds(String tableName, Collection<String> ids);
}
