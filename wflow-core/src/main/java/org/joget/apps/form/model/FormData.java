package org.joget.apps.form.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.ListOrderedMap;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Permission;

/**
 * Represents data related to a form e.g. from request, binders, errors, etc.
 */
public class FormData {

    private String primaryKeyValue;
    private String processId;
    private String activityId;
    protected Map<FormLoadBinder, FormRowSet> loadBinderMap = new HashMap<FormLoadBinder, FormRowSet>();
    protected Map<FormLoadBinder, FormRowSet> optionsBinderMap = new HashMap<FormLoadBinder, FormRowSet>();
    protected Map<String, String> previousErrorMap = new ListOrderedMap();
    protected Map<String, String> fileErrorMap = new ListOrderedMap();
    protected Map<String, String> errorMap = new ListOrderedMap();
    protected Map<String, String[]> requestParamMap = new HashMap<String, String[]>();
    protected Map<FormStoreBinder, FormRowSet> binderRowSetMap = new ListOrderedMap();
    protected Map<String, String> resultMap = new ListOrderedMap();
    protected Map<String, String> variableMap;
    protected Boolean stay = false;
    protected String permissionKey = Permission.DEFAULT;

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
     * Retrieves the value of a property from a store binder for an element.
     * @param element
     * @param property
     * @return
     */
    public String getStoreBinderDataProperty(Element element) {
        String value = null;
        if (element != null) {
            FormStoreBinder binder = FormUtil.findStoreBinder(element);
            FormRowSet rowSet = getStoreBinderData(binder);
            if (rowSet != null && !rowSet.isEmpty()) {
                FormRow firstRow = rowSet.get(0);
                value = firstRow.getProperty(element.getPropertyString(FormUtil.PROPERTY_ID));
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
     * Adds an error from previous submission
     * @param id
     * @param error
     */
    public void addPreviousFormError(String id, String error) {
        if (previousErrorMap.containsKey(id)) {
            error = previousErrorMap.get(id) + "<br/>" + error;
        }
        previousErrorMap.put(id, error);
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
     * Adds an error retrieved from file upload
     * @param json
     */
    public void addFileError(String id, String error) {
        if (fileErrorMap.containsKey(id)) {
            error = fileErrorMap.get(id) + "<br/>" + error;
        }
        fileErrorMap.put(id, error);
    }
    
    /**
     * Returns the previous submission error for a specific id.
     * @param id
     * @return null if there is no error.
     */
    public String getPreviousFormError(String id) {
        return previousErrorMap.get(id);
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
     * Returns the file upload error for a specific id.
     * @param id
     * @return null if there is no error.
     */
    public String getFileError(String id) {
        return fileErrorMap.get(id);
    }
    
    /**
     * Retrieves previous submission errors for a form
     * @return
     */
    public Map<String, String> getPreviousFormErrors() {
        return previousErrorMap;
    }

    /**
     * Retrieves errors for a form
     * @return
     */
    public Map<String, String> getFormErrors() {
        return errorMap;
    }
    
    /**
     * Retrieves file upload errors for a form
     * @return
     */
    public Map<String, String> getFileErrors() {
        return fileErrorMap;
    }

    /**
     * Clears all errors in a form
     */
    public void clearFormErrors() {
        previousErrorMap.clear();
        errorMap.clear();
        fileErrorMap.clear();
    }

    /**
     * Adds request parameter values.
     * @param parameter
     * @param values
     */
    public void addRequestParameterValues(String parameter, String[] values) {
        
        if (values.length > 1) {
            Set result = new LinkedHashSet(Arrays.asList(values));
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
            value = values[values.length-1];
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

    public String getPermissionKey() {
        return permissionKey;
    }

    public void setPermissionKey(String permissionKey) {
        this.permissionKey = permissionKey;
    }
    
    /**
     * Set a workflow variable
     * @param name
     * @param value
     */
    public void setWorkflowVariable(String name, String value) {
        variableMap.put(name, value);
    }

    /**
     * Returns the workflow variable for a specific name.
     * @param name
     * @return null if there is no result.
     */
    public String getWorkflowVariable(String name) {
        return variableMap.get(name);
    }

    /**
     * Retrieves workflow variables for a process form
     * @return
     */
    public Map<String, String> getWorkflowVariables() {
        return variableMap;
    }
    
    /**
     * Set workflow variables for a process form
     * @param variableMap
     * @return
     */
    public void setWorkflowVariables(Map<String, String> variableMap) {
        this.variableMap = variableMap;
    }
}