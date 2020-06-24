package org.joget.apps.form.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;

public class FormColumnsData {
    
    private Long lastUpdate;
    private Set<String> keys = new LinkedHashSet<String>();
    private Set<String> columns = new LinkedHashSet<String>();
    private Map<String, Map<String, String>> fieldsWithLabel = new LinkedHashMap<String, Map<String, String>>();
    private Map<String, Set<String>> fieldsWithoutLabel = new LinkedHashMap<String, Set<String>>();
    private boolean hasChange = false;
    
    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    public Map<String, Map<String, String>> getFieldsWithLabel() {
        return fieldsWithLabel;
    }

    public void setFieldsWithLabel(Map<String, Map<String, String>> fieldsWithLabel) {
        this.fieldsWithLabel = fieldsWithLabel;
    }

    public Map<String, Set<String>> getFieldsWithoutLabel() {
        return fieldsWithoutLabel;
    }

    public void setFieldsWithoutLabel(Map<String, Set<String>> fieldsWithoutLabel) {
        this.fieldsWithoutLabel = fieldsWithoutLabel;
    }
    
    public Set<String> getColumnNames(Map<String, String> checkDuplicateMap) {
        Set<String> columnNames = new HashSet<String>();
        
        if (hasChange) {
            if (checkDuplicateMap == null) {
                checkDuplicateMap = new HashMap<String, String>();
            }

            for (String key : keys) {
                Map<String, String> labelFields = fieldsWithLabel.get(key);
                if (labelFields != null && !labelFields.isEmpty()) {
                    addColumnName(key, labelFields.keySet(), columnNames, checkDuplicateMap);
                }
                Set<String> fields = fieldsWithoutLabel.get(key);
                if (fields != null && !fields.isEmpty()) {
                    addColumnName(key, fields, columnNames, checkDuplicateMap);
                }
            }
        } else if (columns != null && !columns.isEmpty()) {
            columnNames.addAll(columns);
            for (String c : columns) {
                checkDuplicateMap.put(c.toLowerCase(), c);
            }
        }
        
        return columnNames;
    }
    
    protected void addColumnName(String key, Set<String> fields, Set<String> columnNames, Map<String, String> checkDuplicateMap) {
        for (String c : fields) {
            if (!c.isEmpty()) {
                String exist = checkDuplicateMap.get(c.toLowerCase());
                if (exist != null && !exist.equals(c)) {
                    LogUtil.warn(FormColumnsData.class.getName(), "Detected duplicated column in Form [" + key + "]: \"" + exist + "\" and \"" + c + "\". Removed \"" + exist + "\" and replaced with \"" + c + "\".");
                    columnNames.remove(exist);
                }
                checkDuplicateMap.put(c.toLowerCase(), c);
                columnNames.add(c);
            }
        }
    }
    
    public void remove(String key) {
        fieldsWithLabel.remove(key);
        fieldsWithoutLabel.remove(key);
    }
    
    public boolean needUpdate(String key, Date dateModified) {
        return lastUpdate == null || (dateModified != null && lastUpdate != null && lastUpdate.compareTo(dateModified.getTime()) < 0);
    }
    
    public void processFormDefinitions(Collection<FormDefinition> list) {
        hasChange = false;
        if (list != null && !list.isEmpty()) {
            Date newUpdateTime = new Date();
            Set<String> toRemove = new HashSet<String>();
            toRemove.addAll(keys);
            keys.clear();
            
            FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
            
            for (FormDefinition formDef : list) {
                String key = formDef.getAppId() + ":" + formDef.getAppVersion() + ":" + formDef.getId();
                keys.add(key);
                if (!toRemove.contains(key) || needUpdate(key, formDef.getDateModified())) {
                    hasChange = true;
                    LogUtil.debug(FormColumnsData.class.getName(), "form["+key+"] is updated.");
                    
                    remove(key);
                    String json = formDef.getJson();
                    if (json != null) {
                        try {
                            Form form = (Form) formService.createElementFromJson(json, false);
                            processElement(key, form);
                        } catch (Exception e) {
                            LogUtil.debug(FormColumnsData.class.getName(), "JSON definition of form["+key+"] is either protected or corrupted.");
                        }
                    }
                }
                toRemove.remove(key);
            }
            
            for (String key : toRemove) {
                hasChange = true;
                remove(key);
                LogUtil.debug(FormColumnsData.class.getName(), "form["+key+"] is removed.");
            }
            
            if (hasChange) {
                lastUpdate = newUpdateTime.getTime();
            }
        }
    }
    
    public boolean isChanged() {
        return hasChange;
    }
    
    protected void processElement(String key, Element element) {
        Collection<String> fieldNames = element.getDynamicFieldNames();
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String field : fieldNames) {
                addField(key, field, null);
            }
        }
        if (!(element instanceof FormContainer) && element.getProperties() != null) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (id != null && !id.isEmpty()) {
                addField(key, id, element.getPropertyString(FormUtil.PROPERTY_LABEL));
            }
        }
        if (!(element instanceof AbstractSubForm)) { // do not recurse into subforms
            Collection<Element> children = element.getChildren();
            if (children != null) {
                for (Element child : children) {
                    processElement(key, child);
                }
            }
        }
    }
    
    protected void addField(String key, String fieldId, String fieldLabel) {
        if (fieldLabel != null && !fieldLabel.isEmpty()) {
            Map<String, String> fields = fieldsWithLabel.get(key);
            if (fields == null) {
                fields = new LinkedHashMap<String, String>();
                fieldsWithLabel.put(key, fields);
            }
            fields.put(fieldId, fieldLabel);
        } else {
            Set<String> fields = fieldsWithoutLabel.get(key);
            if (fields == null) {
                fields = new LinkedHashSet<String>();
                fieldsWithoutLabel.put(key, fields);
            }
            fields.add(fieldId);
        }
    }
    
    public static Set<String> getFormColumnNames(String tableName, Map<String, String> checkDuplicateMap) {
        Set<String> columns = new HashSet<String>();
        
        FormColumnsData formColumnsData = null;
        try {
            String columnsJson = FileUtils.readFileToString(new File(FormColumnsData.getFormMappingPath(), FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName + ".json"), "UTF-8");
            if (columnsJson != null && !columnsJson.isEmpty()) {
                Gson gson = new Gson();
                formColumnsData = gson.fromJson(columnsJson, new TypeToken<FormColumnsData>(){}.getType());
            }
        } catch (Exception e) {
            LogUtil.debug(FormColumnsData.class.getName(), "Error reading "+FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName+".json : " + e.getMessage());
        }
        if (formColumnsData == null) {
            formColumnsData = new FormColumnsData();
        }
        
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
        Collection<FormDefinition> formList = formDefinitionDao.loadFormDefinitionByTableName(tableName);
        formColumnsData.processFormDefinitions(formList);
        columns.addAll(formColumnsData.getColumnNames(checkDuplicateMap));
        
        if (formColumnsData.isChanged()) {
            LogUtil.debug(FormColumnsData.class.getName(), "table ["+tableName+"] is changed.");
            try {
                Gson gson = new Gson();
                String columnsJson = gson.toJson(formColumnsData);
                FileUtils.writeStringToFile(new File(getFormMappingPath(), FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName + ".json"), columnsJson, "UTF-8");
            } catch (Exception e) {
                LogUtil.debug(FormColumnsData.class.getName(), "Error writing to "+FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName+".json : " + e.getMessage());
            } 
        }
        
        return columns;
    }
    
    protected static String getFormMappingPath() {
        // determine path to mapping directory
        String path = SetupManager.getBaseDirectory();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += FormDataDaoImpl.FORM_MAPPING_DIRECTORY;
        new File(path).mkdirs();
        return path;
    }
}
