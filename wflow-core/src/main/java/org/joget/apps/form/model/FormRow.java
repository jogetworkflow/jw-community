package org.joget.apps.form.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.TimeZoneUtil;

/**
 * Represents a row of form data
 */
public class FormRow extends Properties {
    Map<String, String[]> tempFilePathMap;
    Map<String, String[]> deleteFilePathMap;

    public FormRow() {
        super();
    }

    public FormRow(Properties defaults) {
        super(defaults);
    }

    /**
     * Used for storing/loading data via hibernate
     * @return
     */
    public Map getCustomProperties() {
        return this;
    }

    public void setCustomProperties(Map customProperties) {
        if (customProperties != null) {
            for (Iterator i = customProperties.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                Object value = customProperties.get(key);
                if (value != null) {
                    put(key, value);
                }
            }
        }
    }

    //--- Standard row metadata properties below
    /**
     * Identifier/primary key for the row
     * @return
     */
    public String getId() {
        return getProperty(FormUtil.PROPERTY_ID);
    }

    public void setId(String id) {
        if (id != null) {
            setProperty(FormUtil.PROPERTY_ID, id);
        } else {
            remove(FormUtil.PROPERTY_ID);
        }
    }

    /**
     * Date the row was created
     * @return
     */
    public Date getDateCreated() {
        Date date = null;
        Object obj = get(FormUtil.PROPERTY_DATE_CREATED);
        if (obj != null && obj instanceof Date) {
            date = (Date) obj;
        }
        return date;
    }

    public void setDateCreated(Date date) {
        if (date != null) {
            put(FormUtil.PROPERTY_DATE_CREATED, date);
        } else {
            remove(FormUtil.PROPERTY_DATE_CREATED);
        }
    }

    /**
     * Date the row was created
     * @return
     */
    public Date getDateModified() {
        Date date = null;
        Object obj = get(FormUtil.PROPERTY_DATE_MODIFIED);
        if (obj != null && obj instanceof Date) {
            date = (Date) obj;
        }
        return date;
    }

    public void setDateModified(Date date) {
        if (date != null) {
            put(FormUtil.PROPERTY_DATE_MODIFIED, date);
        } else {
            remove(FormUtil.PROPERTY_DATE_MODIFIED);
        }
    }
    
    /**
     * Username who created the row 
     * @return
     */
    public String getCreatedBy() {
        return getProperty(FormUtil.PROPERTY_CREATED_BY);
    }

    public void setCreatedBy(String createdBy) {
        if (createdBy != null) {
            put(FormUtil.PROPERTY_CREATED_BY, createdBy);
        } else {
            remove(FormUtil.PROPERTY_CREATED_BY);
        }
    }
    
    /**
     * Username who last modified the row 
     * @return
     */
    public String getModifiedBy() {
        return getProperty(FormUtil.PROPERTY_MODIFIED_BY);
    }

    public void setModifiedBy(String modifiedBy) {
        if (modifiedBy != null) {
            put(FormUtil.PROPERTY_MODIFIED_BY, modifiedBy);
        } else {
            remove(FormUtil.PROPERTY_MODIFIED_BY);
        }
    }
    
    /**
     * User name who created the row 
     * @return
     */
    public String getCreatedByName() {
        return getProperty(FormUtil.PROPERTY_CREATED_BY_NAME);
    }

    public void setCreatedByName(String createdByName) {
        if (createdByName != null) {
            put(FormUtil.PROPERTY_CREATED_BY_NAME, createdByName);
        } else {
            remove(FormUtil.PROPERTY_CREATED_BY_NAME);
        }
    }
    
    /**
     * User name who last modified the row 
     * @return
     */
    public String getModifiedByName() {
        return getProperty(FormUtil.PROPERTY_MODIFIED_BY_NAME);
    }

    public void setModifiedByName(String modifiedByName) {
        if (modifiedByName != null) {
            put(FormUtil.PROPERTY_MODIFIED_BY_NAME, modifiedByName);
        } else {
            remove(FormUtil.PROPERTY_MODIFIED_BY_NAME);
        }
    }

    public String getOrgId() {
        String orgId = null;
        Object obj = get(FormUtil.PROPERTY_ORG_ID);
        if (obj != null) {
            orgId = (String) obj;
        }
        return orgId;
    }

    public void setOrgId(String orgId) {
        if(orgId != null) {
            put(FormUtil.PROPERTY_ORG_ID, orgId);
        } else {
            remove(FormUtil.PROPERTY_ORG_ID);
        }
    }

    public boolean getDeleted() {
        Object obj = get(FormUtil.PROPERTY_DELETED);
        if (obj != null) {
            return Boolean.parseBoolean(String.valueOf(obj));
        }
        return false;
    }

    public void setDeleted(Boolean deleted) {
        if (deleted != null) {
            put(FormUtil.PROPERTY_DELETED, String.valueOf(deleted));
        } else {
            remove(FormUtil.PROPERTY_DELETED);
        }
    }

    @Override
    public boolean equals(Object obj) {
        FormRow row = (FormRow) obj;
        if (row.getId() != null && getId().equals(row.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }
    
    public Map<String, String[]> getTempFilePathMap() {
        return tempFilePathMap;
    }
    
    public void setTempFilePathMap(Map<String, String[]> tempFilePathMap) {
        this.tempFilePathMap = tempFilePathMap;
    }
    
    public void putTempFilePath(String fieldId, String path) {
        if (tempFilePathMap == null) {
            tempFilePathMap = new HashMap<String, String[]>();
        }
        tempFilePathMap.put(fieldId, new String[]{path});
    }
    
    public void putTempFilePath(String fieldId, String[] path) {
        if (tempFilePathMap == null) {
            tempFilePathMap = new HashMap<String, String[]>();
        }
        tempFilePathMap.put(fieldId, path);
    }
    
    public String[] getTempFilePaths(String fieldId) {
        if (tempFilePathMap != null) {
            return tempFilePathMap.get(fieldId);
        }
        return null;
    }
    
    public String getTempFilePath(String fieldId) {
        if (tempFilePathMap != null) {
            String[] paths = tempFilePathMap.get(fieldId);
            if (paths != null && paths.length > 0) {
                return paths[0];
            }
        }
        return null;
    }
    
    public Map<String, String[]> getDeleteFilePathMap() {
        return deleteFilePathMap;
    }
    
    public void setDeleteFilePathMap(Map<String, String[]> deleteFilePathMap) {
        this.deleteFilePathMap = deleteFilePathMap;
    }
    
    public void putDeleteFilePath(String fieldId, String path) {
        if (deleteFilePathMap == null) {
            deleteFilePathMap = new HashMap<String, String[]>();
        }
        deleteFilePathMap.put(fieldId, new String[]{path});
    }
    
    public void putDeleteFilePath(String fieldId, String[] path) {
        if (deleteFilePathMap == null) {
            deleteFilePathMap = new HashMap<String, String[]>();
        }
        deleteFilePathMap.put(fieldId, path);
    }
    
    public String[] getDeleteFilePaths(String fieldId) {
        if (deleteFilePathMap != null) {
            return deleteFilePathMap.get(fieldId);
        }
        return null;
    }
    
    public String getDeleteFilePath(String fieldId) {
        if (deleteFilePathMap != null) {
            String[] paths = deleteFilePathMap.get(fieldId);
            if (paths != null && paths.length > 0) {
                return paths[0];
            }
        }
        return null;
    }
    
    public void putAll(FormRow row) {
        super.putAll(row);
        Map files = row.getTempFilePathMap();
        if (files != null && !files.isEmpty()) {
            if (tempFilePathMap == null) {
                tempFilePathMap = new HashMap<String, String[]>();
            }
            tempFilePathMap.putAll(files);
        }
        Map deleteFiles = row.getDeleteFilePathMap();
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            if (deleteFilePathMap == null) {
                deleteFilePathMap = new HashMap<String, String[]>();
            }
            deleteFilePathMap.putAll(deleteFiles);
        }
    }
    
    @Override
    public String getProperty(String key) {
        if (key == null) {
            return null;
        }
        
        Object oval = super.get(key);
        
        if (oval != null && oval instanceof Date) {
            return TimeZoneUtil.convertToTimeZone((Date) oval, null, AppUtil.getAppDateFormat());
        } else if (!key.isEmpty() && Character.isDigit(key.charAt(0))) {
            if (super.containsKey("t__" + key)) {
                return super.getProperty("t__" + key);
            } else {
                return super.getProperty(key);
            }
        } else {
            return super.getProperty(key);
        }
    }
}
