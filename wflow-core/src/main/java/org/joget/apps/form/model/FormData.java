package org.joget.apps.form.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.ListOrderedMap;
import org.joget.apps.form.service.FormUtil;

/**
 * Represents data related to a form e.g. from request, binders, errors, etc.
 */
public class FormData {

    private String primaryKeyValue;
    private String processId;
    private String activityId;
    protected Map<FormLoadBinder, FormRowSet> loadBinderMap = new HashMap<FormLoadBinder, FormRowSet>();
    protected Map<FormLoadBinder, FormRowSet> optionsBinderMap = new HashMap<FormLoadBinder, FormRowSet>();
    protected Map<String, String> errorMap = new ListOrderedMap();
    protected Map<String, String[]> requestParamMap = new HashMap<String, String[]>();
    protected Map<FormStoreBinder, FormRowSet> binderRowSetMap = new ListOrderedMap();
    protected Map<String, String> resultMap = new ListOrderedMap();
    protected Boolean stay = false;

    public Boolean getStay() {
        return stay;
    }

    public void setStay(Boolean stay) {
        this.stay = stay;
    }

    public String getPrimaryKeyValue() {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String primaryKeyValue) {
        this.primaryKeyValue = primaryKeyValue;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Map<FormLoadBinder, FormRowSet> getLoadBinderMap() {
        return loadBinderMap;
    }

    /**
     * Adds data from a load binder
     * @param binder
     * @param data
     */
    public void setLoadBinderData(FormLoadBinder binder, FormRowSet data) {
        loadBinderMap.put(binder, data);
    }

    /**
     * Retrieves the rows from a load binder for an element.
     * @param element
     * @param property
     * @return
     */
    public FormRowSet getLoadBinderData(Element element) {
        FormRowSet rowSet = null;

        // lookup binder for the element
        FormLoadBinder binder = FormUtil.findLoadBinder(element);

        // get form row set
        if (binder != null) {
            rowSet = loadBinderMap.get(binder);
        }
        return rowSet;
    }

    /**
     * Retrieves the value of a property from a load binder for an element.
     * @param element
     * @param property
     * @return
     */
    public String getLoadBinderDataProperty(Element element, String property) {
        String value = null;
        if (property != null) {
            FormRowSet rowSet = getLoadBinderData(element);
            if (rowSet != null && !rowSet.isEmpty()) {
                FormRow firstRow = rowSet.get(0);
                value = firstRow.getProperty(property);
            }
        }
        return value;
    }

    /**
     * Adds data from an options binder
     * @param binder
     * @param data
     */
    public void setOptionsBinderData(FormLoadBinder binder, FormRowSet data) {
        optionsBinderMap.put(binder, data);
    }

    /**
     * Retrieves the value of a property from an options binder for an element.
     * @param element
     * @param property
     * @return
     */
    public FormRowSet getOptionsBinderData(Element element, String property) {
        FormRowSet rowSet = null;

        // lookup binder for the element
        FormLoadBinder binder = FormUtil.findOptionsBinder(element);

        // get form row set
        if (binder != null) {
            rowSet = optionsBinderMap.get(binder);
        }
        return rowSet;
    }

    /**
     * Adds an error
     * @param id
     * @param error
     */
    public void addFormError(String id, String error) {
        if (errorMap.containsKey(id)) {
            error = errorMap.get(id) + "<br/>" + error;
        }
        errorMap.put(id, error);
    }

    /**
     * Returns the error for a specific id.
     * @param id
     * @return null if there is no error.
     */
    public String getFormError(String id) {
        return errorMap.get(id);
    }

    /**
     * Retrieves errors for a form
     * @return
     */
    public Map<String, String> getFormErrors() {
        return errorMap;
    }

    /**
     * Clears all errors in a form
     */
    public void clearFormErrors() {
        errorMap.clear();
    }

    /**
     * Adds request parameter values.
     * @param parameter
     * @param values
     */
    public void addRequestParameterValues(String parameter, String[] values) {
        
        if (values.length > 1) {
            Set result = new HashSet(Arrays.asList(values));
            values = (String[]) result.toArray(new String[0]);
        }
        
        requestParamMap.put(parameter, values);
    }

    /**
     * Returns the parameter value from a HTTP request
     * @param paramName
     * @return
     */
    public String getRequestParameter(String paramName) {
        String value = null;
        String[] values = getRequestParameterValues(paramName);
        if (values != null && values.length > 0) {
            value = values[0];
        }
        return value;
    }

    /**
     * Returns the parameter values from a HTTP request
     * @param paramName
     * @return
     */
    public String[] getRequestParameterValues(String paramName) {
        return requestParamMap.get(paramName);
    }

    /**
     * Retrieves request params for a form
     * @return
     */
    public Map<String, String[]> getRequestParams() {
        return requestParamMap;
    }

    /**
     * Sets data to be stored by store binders
     * @param binder
     * @param data
     */
    public void setStoreBinderData(FormStoreBinder binder, FormRowSet data) {
        binderRowSetMap.put(binder, data);
    }

    /**
     * Retrieves the value of an element property to be passed to a store binder for storing.
     * @param binder
     * @param property
     * @return
     */
    public FormRowSet getStoreBinderData(FormStoreBinder binder) {
        return binderRowSetMap.get(binder);
    }

    /**
     * Returns the collection of store binders contained in this FormData object.
     * @return
     */
    public Collection<FormStoreBinder> getStoreBinders() {
        Collection<FormStoreBinder> binders = binderRowSetMap.keySet();
        return binders;
    }

    /**
     * Adds an result
     * @param id
     * @param result
     */
    public void addFormResult(String id, String result) {
        resultMap.put(id, result);
    }

    /**
     * Returns the result for a specific id.
     * @param id
     * @return null if there is no result.
     */
    public String getFormResult(String id) {
        return resultMap.get(id);
    }

    /**
     * Retrieves results for a form
     * @return
     */
    public Map<String, String> getFormResults() {
        return resultMap;
    }

    /**
     * Clears all results in a form
     */
    public void clearFormResults() {
        resultMap.clear();
    }
}