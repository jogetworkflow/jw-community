package org.kecak.apps.form.dao;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Kecak implementation of FormDataDao
 */
public interface FormDataDao {
    /**
     * Loads a data row for a form based on the primary key
     * @param form
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow load(Form form, String primaryKey, Boolean loadDeleted);

    /**
     * Loads a data row for a form based on the primary key
     * @param formDefId
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow load(String formDefId, String tableName, String primaryKey, Boolean loadDeleted);

    /**
     * Loads a data row for a form based on the primary key.
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param form
     * @param primaryKey
     * @return
     */
    public FormRow loadWithoutTransaction(Form form, String primaryKey, Boolean loadDeleted);

    /**
     * Loads a data row for a form based on the primary key.
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param formDefID
     * @param tableName
     * @param primaryKey
     * @return
     */
    public FormRow loadWithoutTransaction(String formDefID, String tableName, String primaryKey, Boolean loadDeleted);

    /**
     * Loads a data row for a table based on the primary key
     * @param tableName
     * @param columnName is not used
     * @param primaryKey
     * @return null if the row does not exist
     */
    public FormRow loadByTableNameAndColumnName(String tableName, String columnName, String primaryKey, Boolean loadDeleted);

    /**
     * Query to find find primary key based on a field name and it's value.
     * @param form
     * @param fieldName
     * @param value
     * @return
     */
    public String findPrimaryKey(Form form, final String fieldName, final String value, final Boolean loadDeleted);


    /**
     * Query to find find primary key based on a field name and it's value.
     * @param formDefId
     * @param tableName
     * @param fieldName
     * @param value
     * @return
     */
    public String findPrimaryKey(String formDefId, String tableName, final String fieldName, final String value, final Boolean loadDeleted);

    /**
     * Query to find a list of matching form rows.
     * @param form
     * @param condition
     * @param params
     * @param sort
     * @param sortAs
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    FormRowSet find(Form form, final String condition, final Object[] params, final String sort, final String sortAs, final Boolean desc, final Integer start, final Integer rows, final Boolean loadDeleted);

    /**
     * Query to find a list of matching form rows.
     * @param formDefId
     * @param tableName
     * @param condition
     * @param params
     * @param sort
     * @param sortAs
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    FormRowSet find(String formDefId, String tableName, final String condition, final Object[] params, final String sort, final String sortAs, final Boolean desc, final Integer start, final Integer rows, final Boolean loadDeleted);

    /**
     * Query total row count for a form.
     * @param form
     * @param condition
     * @param params
     * @return
     */
    public Long count(Form form, final String condition, final Object[] params, final Boolean loadSoftDeleted);

    /**
     * Query total row count for a form.
     * @param formDefId
     * @param tableName
     * @param condition
     * @param params
     * @return
     */
    public Long count(String formDefId, String tableName, final String condition, final Object[] params, final Boolean loadSoftDeleted);

    /**
     * Delete form data by primary keys
     * @param form
     * @param primaryKeyValues
     */
    void delete(Form form, String[] primaryKeyValues, boolean isHardDelete);

    /**
     * Delete form data by primary keys
     * @param formDefId
     * @param tableName
     * @param primaryKeyValues
     * @param isHardDelete
     */
    void delete(String formDefId, String tableName, String[] primaryKeyValues, boolean isHardDelete);

    /**
     * Delete form data by rows
     * @param formDefId
     * @param tableName
     * @param rows
     * @param isHardDelete
     */
    void delete(String formDefId, String tableName, FormRowSet rows, boolean isHardDelete);
}
