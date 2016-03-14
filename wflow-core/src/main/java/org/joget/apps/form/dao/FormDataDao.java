package org.joget.apps.form.dao;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import java.util.Collection;

/**
 * Service method used to manage form data
 * 
 */
public interface FormDataDao {

    /**
     * clear cache for a form
     * @param form
     */
    public void clearFormCache(Form form);
    
    /**
     * Loads a data row for a form based on the primary key
     * @param form
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow load(Form form, String primaryKey);
    
    /**
     * Loads a data row for a form based on the primary key
     * @param formDefId
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow load(String formDefId, String tableName, String primaryKey);

    /**
     * Loads a data row for a form based on the primary key. 
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param form
     * @param primaryKey
     * @return 
     */
    public FormRow loadWithoutTransaction(Form form, String primaryKey);

    /**
     * Loads a data row for a form based on the primary key. 
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param formDefID
     * @param tableName
     * @param primaryKey
     * @return 
     */
    public FormRow loadWithoutTransaction(String formDefID, String tableName, String primaryKey);

    /**
     * Loads a data row for a table based on the primary key
     * @param tableName
     * @param columnName is not used
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow loadByTableNameAndColumnName(String tableName, String columnName, String primaryKey);

    /**
     * Query to find a list of matching form rows.
     * @param form
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public FormRowSet find(Form form, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows);

    /**
     * Query to find a list of matching form rows.
     * @param formDefId
     * @param tableName
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public FormRowSet find(String formDefId, String tableName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows);

    /**
     * Query total row count for a form.
     * @param form
     * @param condition
     * @param params
     * @return
     */
    public Long count(Form form, final String condition, final Object[] params);

    /**
     * Query total row count for a form.
     * @param form
     * @param condition
     * @param params
     * @return
     */
    public Long count(String formDefId, String tableName, final String condition, final Object[] params);

    /**
     * Query to find find primary key based on a field name and it's value.
     * @param form
     * @param fieldName
     * @param value
     * @return
     */
    public String findPrimaryKey(Form form, final String fieldName, final String value);

    
    /**
     * Query to find find primary key based on a field name and it's value.
     * @param formDefId
     * @param tableName
     * @param fieldName
     * @param value
     * @return
     */
    public String findPrimaryKey(String formDefId, String tableName, final String fieldName, final String value);

    /**
     * Saves (creates or updates) form data
     * @param form
     */
    public void saveOrUpdate(Form form, FormRowSet rowSet);

    /**
     * Saves (creates or updates) form data
     * @param form
     */
    public void saveOrUpdate(String formDefId, String tableName, FormRowSet rowSet);

    /**
     * Call Hibernate to update DB schema
     * @param form
     * @param rowSet
     */
    public void updateSchema(Form form, FormRowSet rowSet);
    
    /**
     * Call Hibernate to update DB schema
     * @param formDefId
     * @param tableName
     * @param rowSet
     */
    public void updateSchema(String formDefId, String tableName, FormRowSet rowSet);
    
    /**
     * Delete form data by primary keys
     * @param form
     * @param primaryKeyValues 
     */
    public void delete(Form form, String[] primaryKeyValues);

    /**
     * Delete form data by primary keys
     * @param form
     * @param primaryKeyValues 
     */
    public void delete(String formDefId, String tableName, String[] primaryKeyValues);
    
    /**
     * Delete form data by rows
     * @param form
     * @param primaryKeyValues 
     */
    public void delete(String formDefId, String tableName, FormRowSet rows);

    /**
     * Gets the generated hibernate entity name for the form
     * @param form
     * @return
     */
    public String getFormEntityName(Form form);
    
    /**
     * Gets the generated hibernate entity name for the form
     * @param form
     * @return
     */
    public String getFormEntityName(String formDefId);

    /**
     * Gets the defined table name for the form
     * @param form
     */
    public String getFormTableName(Form form);
    
    /**
     * Gets the defined table name for the form
     * @param form
     */
    public String getFormTableName(String formDefId, String tableName);

    /**
     * Returns collection of all column names to be saved
     * @param form
     * @param rowSet
     * @return
     */
    public Collection<String> getFormRowColumnNames(FormRowSet rowSet);

    /**
     * Returns collection of all columns from forms mapped to a table
     * @param tableName
     * @return
     */
    public Collection<String> getFormDefinitionColumnNames(String tableName);

    /**
     * Returns EntityName of form mapped to a table & column
     * @param tableName
     * @param columnName
     * @return
     */
    public String getEntityName(String tableName, String columnName);

}