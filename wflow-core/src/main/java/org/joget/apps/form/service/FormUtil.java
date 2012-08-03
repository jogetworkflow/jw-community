package org.joget.apps.form.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAction;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.FormValidator;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Utility methods for the Form module.
 */
@Service("appsFormUtil")
public class FormUtil implements ApplicationContextAware {

    public static final String PROPERTY_ELEMENT_UNIQUE_KEY = "elementUniqueKey";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_OPTIONS = "options";
    public static final String PROPERTY_SELECTED = "selected";
    public static final String PROPERTY_GROUPING = "grouping";
    public static final String PROPERTY_OPTIONS_DELIMITER = ";";
    public static final String PROPERTY_CLASS_NAME = "className";
    public static final String PROPERTY_ELEMENTS = "elements";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_VALIDATOR = "validator";
    public static final String PROPERTY_READONLY = "readonly";
    public static final String PROPERTY_READONLY_LABEL = "readonlyLabel";
    public static final String PROPERTY_DATE_CREATED = "dateCreated";
    public static final String PROPERTY_DATE_MODIFIED = "dateModified";
    public static final String PROPERTY_CUSTOM_PROPERTIES = "customProperties";
    public static final String PROPERTY_TABLE_NAME = "tableName";
    public static final String PROPERTY_TEMP_FILE_PATH = "_tempFilePathMap";
    public static final String FORM_META_ORIGINAL_ID = "_FORM_META_ORIGINAL_ID";
    static ApplicationContext appContext;
    
    public static Long runningNumber = 0L;

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public static Element parseElementFromJson(String json) throws Exception {
        // create json object
        JSONObject obj = new JSONObject(json);

        // parse json object
        Element element = FormUtil.parseElementFromJsonObject(obj);

        return element;
    }

    public static Element parseElementFromJsonObject(JSONObject obj) throws Exception {
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        // instantiate element
        String className = obj.getString(FormUtil.PROPERTY_CLASS_NAME);
        Element element = (Element) pluginManager.getPlugin(className);
        if (element != null) {
            // set element properties
            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(obj);
            element.setProperties(properties);
            element.setProperty(FormUtil.PROPERTY_ELEMENT_UNIQUE_KEY, FormUtil.getUniqueKey());

            // recurse into child elements
            Collection<Element> childElements = FormUtil.parseChildElementsFromJsonObject(obj);
            if (childElements == null) {
                childElements = new ArrayList<Element>();
            }
            element.setChildren(childElements);

            // set binders and properties
            FormLoadBinder loadBinder = (FormLoadBinder) FormUtil.parseBinderFromJsonObject(obj, FormBinder.FORM_LOAD_BINDER);
            element.setLoadBinder(loadBinder);
            FormLoadBinder optionsBinder = (FormLoadBinder) FormUtil.parseBinderFromJsonObject(obj, FormBinder.FORM_OPTIONS_BINDER);
            element.setOptionsBinder(optionsBinder);
            FormStoreBinder storeBinder = (FormStoreBinder) FormUtil.parseBinderFromJsonObject(obj, FormBinder.FORM_STORE_BINDER);
            element.setStoreBinder(storeBinder);

            // set validator
            FormValidator validator = FormUtil.parseValidatorFromJsonObject(obj);
            if (validator != null) {
                validator.setElement(element);
                element.setValidator(validator);
            }
        }

        return element;
    }

    /**
     * Parses the properties attribute from a JSON object into a Map
     * @param obj
     * @return
     * @throws JSONException
     */
    public static Map<String, Object> parsePropertyFromJsonObject(JSONObject obj) throws JSONException {
        Map<String, Object> property = new HashMap<String, Object>();

        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            property = PropertyUtil.getPropertiesValueFromJson(objProperty.toString());

            if (property.containsKey(FormUtil.PROPERTY_OPTIONS)) {
                FormRowSet options = new FormRowSet();
                Object[] objs = (Object[]) property.get(FormUtil.PROPERTY_OPTIONS);

                for (Object o : objs) {
                    Map temp = (HashMap) o;
                    FormRow option = new FormRow();
                    for (String key : (Set<String>) temp.keySet()) {
                        option.setProperty(key, (String) temp.get(key));
                    }
                    options.add(option);
                }
                property.put(FormUtil.PROPERTY_OPTIONS, options);
            }
        }

        return property;
    }

    /**
     * Parse child elements
     * @param obj
     * @param parentFormId
     * @param loadBinder
     * @param storeBinder
     * @param root
     * @return
     * @throws Exception
     */
    public static Collection<Element> parseChildElementsFromJsonObject(JSONObject obj) throws Exception {
        Collection<Element> childElements = new ArrayList<Element>();
        if (!obj.isNull(FormUtil.PROPERTY_ELEMENTS)) {
            JSONArray elements = obj.getJSONArray(FormUtil.PROPERTY_ELEMENTS);
            if (elements != null && elements.length() > 0) {
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject childObj = (JSONObject) elements.get(i);

                    // create child element
                    Element childElement = FormUtil.parseElementFromJsonObject(childObj);
                    if (childElement == null) {
                        continue;
                    }

                    // recurse into children
                    Collection<Element> grandChildElements = FormUtil.parseChildElementsFromJsonObject(childObj);
                    if (grandChildElements == null) {
                        grandChildElements = new ArrayList<Element>();
                    }
                    childElement.setChildren(grandChildElements);
                    childElements.add(childElement);
                }
            }
        }

        return childElements;
    }

    /**
     * Parse binder object
     * @param obj
     * @param binderType The JSON property for the binder.
     * @return
     * @throws Exception
     */
    public static FormBinder parseBinderFromJsonObject(JSONObject obj, String binderType) throws Exception {
        FormBinder binder = null;
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            if (!objProperty.isNull(binderType)) {
                String binderStr = objProperty.getString(binderType);
                JSONObject binderObj = new JSONObject(binderStr);

                // create binder object
                if (!binderObj.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                    String className = binderObj.getString(FormUtil.PROPERTY_CLASS_NAME);
                    if (className != null && className.trim().length() > 0) {
                        binder = (FormBinder) pluginManager.getPlugin(className);
                        if (binder != null) {
                            // set child properties
                            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(binderObj);
                            binder.setProperties(properties);
                        }
                    }
                }
            }
        }
        return binder;
    }

    /**
     * Parse validator object
     * @param obj
     * @return
     * @throws Exception
     */
    public static FormValidator parseValidatorFromJsonObject(JSONObject obj) throws Exception {
        FormValidator validator = null;
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            if (!objProperty.isNull(FormUtil.PROPERTY_VALIDATOR)) {
                String validatorStr = objProperty.getString(FormUtil.PROPERTY_VALIDATOR);
                JSONObject validatorObj = new JSONObject(validatorStr);

                // create validator object
                if (!validatorObj.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                    String className = validatorObj.getString(FormUtil.PROPERTY_CLASS_NAME);
                    if (className != null && className.trim().length() > 0) {
                        validator = (FormValidator) pluginManager.getPlugin(className);
                        if (validator != null) {
                            // set child properties
                            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(validatorObj);
                            validator.setProperties(properties);
                        }
                    }
                }
            }
        }
        return validator;
    }

    /**
     * Utility method to recursively find and invoke option binders starting from an element.
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeOptionBinders(Element element, FormData formData) {
        if (formData == null) {
            formData = new FormData();
        }
        FormLoadBinder binder = (FormLoadBinder) element.getOptionsBinder();
        String primaryKeyValue = (formData != null) ? element.getPrimaryKeyValue(formData) : null;
        if (binder != null) {
            FormRowSet data = binder.load(element, primaryKeyValue, formData);
            if (data != null) {
                formData.setOptionsBinderData(binder, data);
            }
        }
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                FormUtil.executeOptionBinders(child, formData);
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively traverse and invoke load binders starting from an element.
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeLoadBinders(Element element, FormData formData) {
        if (formData == null) {
            formData = new FormData();
        }
        FormLoadBinder binder = (FormLoadBinder) element.getLoadBinder();
        String primaryKeyValue = (formData != null) ? element.getPrimaryKeyValue(formData) : null;
        if (!(element instanceof AbstractSubForm) && binder != null) {
            FormRowSet data = binder.load(element, primaryKeyValue, formData);
            if (data != null) {
                formData.setLoadBinderData(binder, data);
            }
        }
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                FormUtil.executeLoadBinders(child, formData);
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively find and invoke validators starting from an element.
     * @param element
     * @param formData
     * @return true if all validators are successful.
     */
    public static boolean executeValidators(Element element, FormData formData) {
        boolean result = true;
        if (element.continueValidation(formData)) {
            FormValidator validator = (FormValidator) element.getValidator();
            if (validator != null) {
                String[] values = FormUtil.getElementPropertyValues(element, formData);
                result = validator.validate(element, formData, values) && result;
            }
            result = element.selfValidate(formData) && result;
            
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    result = FormUtil.executeValidators(child, formData) && result;
                }
            }
        }
        return result;
    }

    /**
     * Utility method to recursively find and invoke the formatData method starting from an element.
     * @param element
     * @param formData
     * @param binderRowSetMap
     * @return A Map mapping a binder to FormRowSets containing formatted values from all elements.
     */
    public static FormData executeElementFormatData(Element element, FormData formData) {
        // get store binder and rowset for element
        FormStoreBinder binder = FormUtil.findStoreBinder(element);
        if (binder != null) {
            FormRowSet rowSet = formData.getStoreBinderData(binder);
            if (rowSet == null) {
                rowSet = new FormRowSet();
                formData.setStoreBinderData(binder, rowSet);
            }

            // get element formatted data
            FormRowSet elementResult = element.formatData(formData);
            if (elementResult != null) {
                if (!elementResult.isMultiRow()) {
                    // get single row
                    FormRow elementRow = elementResult.get(0);

                    // append to consolidated row set
                    if (rowSet.isEmpty()) {
                        rowSet.add(elementRow);
                    } else {
                        FormRow currentRow = rowSet.get(0);
                        currentRow.putAll(elementRow);
                    }
                } else {
                    //if the store binder of this element is null, store as single row in json format
                    if (element.getStoreBinder() == null) {
                        try {
                            // create json object
                            JSONArray jsonArray = new JSONArray();
                            for (FormRow row : elementResult) {
                                JSONObject jsonObject = new JSONObject();
                                for (Map.Entry entry : row.entrySet()) {
                                    String key = (String) entry.getKey();
                                    String value = (String) entry.getValue();
                                    jsonObject.put(key, value);
                                }
                                
                                //File upload is not support when no binder is set.
                                
                                jsonArray.put(jsonObject);
                            }

                            // convert into json string
                            String json = jsonArray.toString();

                            // store in single row FormRowSet
                            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
                            FormRow elementRow = new FormRow();
                            elementRow.put(id, json);

                            // append to consolidated row set
                            if (rowSet.isEmpty()) {
                                rowSet.add(elementRow);
                            } else {
                                FormRow currentRow = rowSet.get(0);
                                currentRow.putAll(elementRow);
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(FormUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        // multiple row result, append all to rowset
                        rowSet.addAll(elementResult);
                        rowSet.setMultiRow(true);
                    }
                }
            }
        }

        if (element.continueValidation(formData)) {
            // recurse into children
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    FormUtil.executeElementFormatData(child, formData);
                }
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively find and invoke the formatDataForValidation method starting from an element.
     * @param element
     * @param formData
     * @return the formatted data.
     */
    public static FormData executeElementFormatDataForValidation(Element element, FormData formData) {
        // recurse into children
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                formData = FormUtil.executeElementFormatDataForValidation(child, formData);
            }
        }
        formData = element.formatDataForValidation(formData);
        return formData;
    }

    /**
     * Utility method to recursively find and invoke actions starting from an element.
     * @param form
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeActions(Form form, Element element, FormData formData) {
        FormData updatedFormData = formData;
        if (element == null) {
            element = form;
        }
        if (element instanceof FormAction) {
            FormAction action = (FormAction) element;
            if (action != null) {
                boolean isActive = action.isActive(form, formData);
                if (isActive) {
                    updatedFormData = action.actionPerformed(form, formData);
                }
            }
        }
        // recurse into children
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                updatedFormData = FormUtil.executeActions(form, child, formData);
            }
        }
        return updatedFormData;
    }

    /**
     * Utility method to recursively find the nearest ancestor load binder for an element.
     * @param element
     * @return
     */
    public static FormLoadBinder findLoadBinder(Element element) {
        FormLoadBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getLoadBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the nearest ancestor options binder for an element.
     * @param element
     * @return
     */
    public static FormLoadBinder findOptionsBinder(Element element) {
        FormLoadBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getOptionsBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the nearest ancestor store binder for an element.
     * @param element
     * @return
     */
    public static FormStoreBinder findStoreBinder(Element element) {
        FormStoreBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getStoreBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the parent Form for an element.
     * @param element
     * @return
     */
    public static Form findRootForm(Element element) {
        Form form = null;
        Element el = element;
        while (el != null && form == null) {
            if (el instanceof Form) {
                form = (Form) el;
                break;
            }
            el = el.getParent();
        }
        return form;
    }

    /**
     * Utility method to recursively find an element by ID.
     * @param id
     * @param rootElement
     * @param formData
     * @return
     */
    public static Element findElement(String id, Element rootElement, FormData formData) {
        return findElement(id, rootElement, formData, false);
    }

    /**
     * Utility method to recursively find an element by ID.
     * @param id
     * @param rootElement
     * @param formData
     * @return
     */
    public static Element findElement(String id, Element rootElement, FormData formData, Boolean includeSubForm) {
        if (rootElement == null) {
            return null;
        }
        Element result = null;
        String elementId = rootElement.getPropertyString(FormUtil.PROPERTY_ID);
        if (elementId != null && elementId.equals(id)) {
            result = rootElement;
            return result;
        } else if (!(rootElement instanceof AbstractSubForm) || ((rootElement instanceof AbstractSubForm) && includeSubForm)) {
            Collection<Element> children = rootElement.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    result = FormUtil.findElement(id, child, formData, includeSubForm);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the parameter name for the element.
     * @param element
     * @return
     */
    public static String getElementParameterName(Element element) {
        String paramName = element.getCustomParameterName();
        if (paramName == null || paramName.trim().length() == 0) {
            paramName = element.getPropertyString(FormUtil.PROPERTY_ID);
        }
        return paramName;
    }

    /**
     * Returns the request parameter value for an element
     * @param element
     * @return
     */
    public static String getRequestParameter(Element element, FormData formData) {
        String value = null;
        String paramName = FormUtil.getElementParameterName(element);
        value = formData.getRequestParameter(paramName);
        return value;
    }

    /**
     * Returns the request parameter value for an element
     * @param element
     * @return
     */
    public static String[] getRequestParameterValues(Element element, FormData formData) {
        String[] values = null;
        String paramName = FormUtil.getElementParameterName(element);
        values = formData.getRequestParameterValues(paramName);
        return values;
    }

    /**
     * Retrieves the property value for an element, first from the element's load binder.
     * If no binder is available, the default value is used.
     * @param element
     * @param formData
     * @param property
     * @return
     */
    public static String getElementPropertyValue(Element element, FormData formData) {
        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);
        String value = element.getPropertyString(FormUtil.PROPERTY_VALUE);
        
        if (formData != null) { // handle default value from options binder
            FormRowSet rowSet = formData.getOptionsBinderData(element, id);
            if (rowSet != null) {
                
                for (FormRow row : rowSet) {
                    Iterator<String> it = row.stringPropertyNames().iterator();
                    // get the key based on the "value" property
                    String optionValue = row.getProperty(PROPERTY_VALUE);
                    if (optionValue == null) {
                        // no "value" property, use first property instead
                        String key = it.next();
                        optionValue = row.getProperty(key);
                    }
                    
                    if(row.getProperty(PROPERTY_SELECTED) != null && (row.getProperty(PROPERTY_SELECTED).equalsIgnoreCase("true"))){
                        value = optionValue;
                        break;
                    }
                }
            }
        }

        // read from request if available, TODO: handle null values e.g. no options selected in a checkbox
        String paramValue = FormUtil.getRequestParameter(element, formData);
        if (paramValue != null) {
            value = paramValue;
        } else {
            // load from binder if available
            if (formData != null) {
                String binderValue = formData.getLoadBinderDataProperty(element, id);
                if (binderValue != null) {
                    value = binderValue;
                }
            }
        }

        return value;
    }

    /**
     * Retrieves the property value for an element, first from the element's load binder.
     * If no binder is available, the default value is used.
     * This method supports multiple values for a property.
     * @param element
     * @param formData
     * @param property
     * @param nullToEmpty Set to true to default value to "" when there the request parameter value is null
     * @return
     */
    public static String[] getElementPropertyValues(Element element, FormData formData) {
        List<String> values = new ArrayList<String>();

        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);
        String value = element.getPropertyString(FormUtil.PROPERTY_VALUE);

        // handle multiple values
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value, FormUtil.PROPERTY_OPTIONS_DELIMITER);
            while (st.hasMoreTokens()) {
                String val = st.nextToken();
                values.add(val);
            }
        }
        
        if (formData != null) { // handle default value from options binder
            FormRowSet rowSet = formData.getOptionsBinderData(element, id);
            if (rowSet != null) {
                
                for (FormRow row : rowSet) {
                    Iterator<String> it = row.stringPropertyNames().iterator();
                    // get the key based on the "value" property
                    String optionValue = row.getProperty(PROPERTY_VALUE);
                    if (optionValue == null) {
                        // no "value" property, use first property instead
                        String key = it.next();
                        optionValue = row.getProperty(key);
                    }
                    
                    if(row.getProperty(PROPERTY_SELECTED) != null && (row.getProperty(PROPERTY_SELECTED).equalsIgnoreCase("true"))){
                        values.add(optionValue);
                    }
                }
            }
        }
        
        // read from request if available, TODO: handle null values e.g. checkbox
        if (id != null) {
            String[] paramValues = FormUtil.getRequestParameterValues(element, formData);
            if (paramValues != null) {
                values = Arrays.asList(paramValues);
            } else {
                // load from binder if available
                if (formData != null) {
                    FormRowSet rowSet = formData.getLoadBinderData(element);
                    if (rowSet != null) {
                        values = new ArrayList<String>();
                        for (FormRow row : rowSet) {
                            String propValue = row.getProperty(id);
                            if (propValue != null) {
                                StringTokenizer st = new StringTokenizer(propValue, FormUtil.PROPERTY_OPTIONS_DELIMITER);
                                while (st.hasMoreTokens()) {
                                    String val = st.nextToken();
                                    values.add(val);
                                }
                            }
                        }
                    }
                }
            }
        }

        String[] result = (String[]) values.toArray(new String[0]);
        return result;
    }

    public static boolean isElementPropertyValuesChanges(Element element, FormData formData, String[] updatedValues) {
        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);

        String primaryKeyValue = formData.getPrimaryKeyValue();
        if (primaryKeyValue != null && !primaryKeyValue.equals(formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID))) {
            return true;
        }

        List<String> values = new ArrayList<String>();

        String value = element.getPropertyString(FormUtil.PROPERTY_VALUE);

        // load from binder if available
        if (formData != null) {
            String binderValue = formData.getLoadBinderDataProperty(element, id);
            if (binderValue != null) {
                StringTokenizer st = new StringTokenizer(binderValue, FormUtil.PROPERTY_OPTIONS_DELIMITER);
                while (st.hasMoreTokens()) {
                    String val = st.nextToken();
                    values.add(val);
                }
            }
        }
        String[] loadedValues = (String[]) values.toArray(new String[0]);

        if (loadedValues != null && updatedValues != null && loadedValues.length == updatedValues.length) {
            return !Arrays.equals(loadedValues, updatedValues);
        }

        return true;
    }

    /**
     * Retrieves the property options for an element, first from the element's options binder.
     * If no binder is available, the default options are used.
     * @param element
     * @param formData
     * @return
     */
    public static Collection<Map> getElementPropertyOptionsMap(Element element, FormData formData) {
        Collection<Map> optionsMap = new ArrayList<Map>();

        // load from "options" property
        Object optionProperty = element.getProperty(FormUtil.PROPERTY_OPTIONS);
        if (optionProperty != null && optionProperty instanceof Collection) {
            for (Map opt : (FormRowSet) optionProperty) {
                optionsMap.add(opt);
            }
        }

        // load from binder if available
        if (formData != null) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            FormRowSet rowSet = formData.getOptionsBinderData(element, id);
            if (rowSet != null) {
                optionsMap = new ArrayList<Map>();
                for (Map row : rowSet) {
                    optionsMap.add(row);
                }
            }
        }

        return optionsMap;
    }

    /**
     * Generates a delimited string from an array of Strings.
     * @param values
     * @return
     */
    public static String generateElementPropertyValues(String[] values) {
        String result = null;
        if (values != null && values.length > 0) {
            for (String val : values) {
                // TODO: replace delimiter?
                if (result == null) {
                    result = "";
                } else {
                    result += FormUtil.PROPERTY_OPTIONS_DELIMITER;
                }
                result += val;
            }
        }
        return result;
    }

    /**
     * Retrieve the error attached to the elemenet
     * @param element
     * @param formData
     * @return null if there is no error.
     */
    public static String getElementError(Element element, FormData formData) {
        String id = FormUtil.getElementParameterName(element);
        String error = formData.getFormError(id);
        return error;
    }

    /**
     * Retrieve a decoration on an element by any attached validator, e.g. marking a required field.
     * @param element
     * @param formData
     * @return
     */
    public static String getElementValidatorDecoration(Element element, FormData formData) {
        String decoration = "";
        FormValidator validator = element.getValidator();
        if (validator != null) {
            decoration = validator.getElementDecoration();
            if (decoration == null) {
                decoration = "";
            }
        }
        return decoration;
    }

    /**
     * Generates JSON representing an element.
     * @param element
     * @return
     */
    public static String generateElementJson(Element element) throws Exception {
        JSONObject jsonObject = FormUtil.generateElementJsonObject(element);
        String json = jsonObject.toString();
        return json;
    }

    /**
     * Generates JSON representing the properties of an element.
     * @param element
     * @return
     */
    public static String generateElementPropertyJson(Element element) {
        String json = null;
        try {
            Map<String, Object> properties = element.getProperties();
            JSONObject jsonObject = generatePropertyJsonObject(properties);
            json = jsonObject.toString();
        } catch (Exception ex) {
            Logger.getLogger(FormUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    /**
     * Generates a JSONObject to represent the properties of an element
     * @return
     */
    public static JSONObject generatePropertyJsonObject(Map<String, Object> properties) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Object objValue = entry.getValue();
                if (objValue != null && objValue instanceof FormRowSet) {
                    JSONArray jsonArray = new JSONArray();
                    for (FormRow row : (FormRowSet) objValue) {
                        Set<String> props = row.stringPropertyNames();
                        JSONObject jo = new JSONObject();
                        for (String key : props) {
                            String val = row.getProperty(key);
                            jo.accumulate(key, val);
                        }
                        jsonArray.put(jo);
                    }
                    jsonObject.put(propertyName, jsonArray);
                } else if (objValue != null && objValue instanceof Object[]) { 
                    Object[] mapArray = (Object[]) objValue;
                    JSONArray jsonArray = new JSONArray();
                    for (Object row :  mapArray) {
                        Map m = (Map) row;
                        JSONObject jo = new JSONObject(m);
                        jsonArray.put(jo);
                    }
                    jsonObject.put(propertyName, jsonArray);
                } else if (objValue != null && objValue instanceof Map) {
                    jsonObject.put(propertyName, (Map) objValue);
                } else {
                    String value = (objValue != null) ? objValue.toString() : "";
                    jsonObject.accumulate(propertyName, value);
                }
            }
        }
        return jsonObject;
    }

    /**
     * Generates a JSONObject to represent an element
     * @param element
     * @return
     * @throws Exception
     */
    public static JSONObject generateElementJsonObject(Element element) throws Exception {
        JSONObject jsonObj = new JSONObject();

        // set class name
        jsonObj.put(FormUtil.PROPERTY_CLASS_NAME, element.getClassName());

        // set properties
        JSONObject jsonProps = FormUtil.generatePropertyJsonObject(element.getProperties());
        jsonObj.put(FormUtil.PROPERTY_PROPERTIES, jsonProps);

        // set validator
        FormValidator validator = element.getValidator();
        if (validator != null) {
            JSONObject jsonValidatorProps = FormUtil.generatePropertyJsonObject(validator.getProperties());
            JSONObject jsonValidator = new JSONObject();
            jsonValidator.put(FormUtil.PROPERTY_CLASS_NAME, validator.getClassName());
            jsonValidator.put(FormUtil.PROPERTY_PROPERTIES, jsonValidatorProps);
            jsonProps.put(FormUtil.PROPERTY_VALIDATOR, jsonValidator);
        }

        // set load binder
        FormBinder loadBinder = (FormBinder) element.getLoadBinder();
        if (loadBinder != null) {
            JSONObject jsonLoadBinderProps = FormUtil.generatePropertyJsonObject(loadBinder.getProperties());
            JSONObject jsonLoadBinder = new JSONObject();
            jsonLoadBinder.put(FormUtil.PROPERTY_CLASS_NAME, loadBinder.getClassName());
            jsonLoadBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonLoadBinderProps);
            jsonProps.put(FormBinder.FORM_LOAD_BINDER, jsonLoadBinder);
        }

        // set store binder
        FormBinder storeBinder = (FormBinder) element.getStoreBinder();
        if (storeBinder != null) {
            JSONObject jsonStoreBinderProps = FormUtil.generatePropertyJsonObject(storeBinder.getProperties());
            JSONObject jsonStoreBinder = new JSONObject();
            jsonStoreBinder.put(FormUtil.PROPERTY_CLASS_NAME, storeBinder.getClassName());
            jsonStoreBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonStoreBinderProps);
            jsonProps.put(FormBinder.FORM_STORE_BINDER, jsonStoreBinder);
        }

        // set options binder
        FormBinder optionsBinder = (FormBinder) element.getOptionsBinder();
        if (optionsBinder != null) {
            JSONObject jsonOptionsBinderProps = FormUtil.generatePropertyJsonObject(optionsBinder.getProperties());
            JSONObject jsonOptionsBinder = new JSONObject();
            jsonOptionsBinder.put(FormUtil.PROPERTY_CLASS_NAME, optionsBinder.getClassName());
            jsonOptionsBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonOptionsBinderProps);
            jsonProps.put(FormBinder.FORM_OPTIONS_BINDER, jsonOptionsBinder);
        }


        // set child elements
        JSONArray jsonChildren = new JSONArray();
        Collection<Element> children = element.getChildren();
        if (children != null) {
            for (Element child : children) {
                JSONObject childJson = FormUtil.generateElementJsonObject(child);
                jsonChildren.put(childJson);
            }
        }
        jsonObj.put(FormUtil.PROPERTY_ELEMENTS, jsonChildren);

        return jsonObj;
    }

    /**
     * Generates the HTML tag meta data for the element that is used by the form builder.
     * @param element
     * @return
     */
    public static String generateElementMetaData(Element element) {
        String properties = FormUtil.generateElementPropertyJson(element);
        String escaped = StringEscapeUtils.escapeHtml(properties);
        String elementMetaData = " element-class=\"" + element.getClass().getName() + "\" element-property=\"" + escaped + "\" ";
        return elementMetaData;
    }

    /**
     * Generates HTML output using a FreeMarker template.
     * @param templatePath
     * @param dataModel
     * @return
     */
    public static String generateElementHtml(final Element element, final FormData formData, final String templatePath, Map dataModel) {
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, element.getClassName(), "/templates/" + templatePath, "message/form/" + element.getName().replace(" ", ""));
    }

    /**
     * Generates a standard map of data to be used within an element template.
     * @param element
     * @param formData
     * @return
     */
    public static Map generateDefaultTemplateDataModel(Element element, FormData formData) {
        Map dataModel = new HashMap();

        // set element and form data
        dataModel.put("element", element);
        dataModel.put("formData", formData);

        // set param name
        String paramName = FormUtil.getElementParameterName(element);
        dataModel.put("elementParamName", paramName);

        // set validator decoration
        String decoration = FormUtil.getElementValidatorDecoration(element, formData);
        dataModel.put("decoration", decoration);

        // set error, if any
        String error = FormUtil.getElementError(element, formData);
        dataModel.put("error", error);

        // set metadata flag
        dataModel.put("includeMetaData", Boolean.FALSE);
        dataModel.put("elementMetaData", "");

        // add request into data model
        if (!dataModel.containsKey("request")) {
            try {
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null) {
                    dataModel.put("request", request);
                }
            } catch (NoClassDefFoundError e) {
                // ignore if servlet request is not available
            }
        }
        return dataModel;
    }

    /**
     * Recursively set the readonly property for all descendent elements.
     * @param element
     */
    public static void setReadOnlyProperty(Element element) {
        setReadOnlyProperty(element, true, null);
    }
    
    /**
     * Recursively set the readonly property for all descendent elements.
     * @param element
     */
    public static void setReadOnlyProperty(Element element, Boolean readonly, Boolean label) {
        if (readonly != null && readonly) {
            element.setProperty(FormUtil.PROPERTY_READONLY, "true");
        }
        if (label != null && label) {
            element.setProperty(FormUtil.PROPERTY_READONLY_LABEL, "true");
        }
        Collection<Element> children = element.getChildren();
        for (Element child : children) {
            setReadOnlyProperty(child, readonly, label);
        }
    }

    /**
     * Check a form is submitted or not
     * @param formData
     */
    public static boolean isFormSubmitted(Element element, FormData formData) {
        Form form = findRootForm(element);
        if (form != null) {
            String paramName = FormUtil.getElementParameterName(form);
            if (formData.getRequestParameter(paramName+"_SUBMITTED") != null) {
                return true;
            }
        }
        return false;
    }
    
    public static String getUniqueKey() {
        if (runningNumber == Long.MAX_VALUE) {
            runningNumber = 0L;
        }
        runningNumber++;
        return Long.toString(runningNumber);
    }
}
