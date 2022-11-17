package org.kecak.apps.form.dao;

import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRowSet;

/**
 * Kecak implementation of FormDataDao
 */
public interface FormDataDao {
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
    FormRowSet find(Form form, final String condition, final Object[] params, final String sort, final String sortAs, final Boolean desc, final Integer start, final Integer rows);

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
    FormRowSet find(String formDefId, String tableName, final String condition, final Object[] params, final String sort, final String sortAs, final Boolean desc, final Integer start, final Integer rows);

}
