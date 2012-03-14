package org.joget.apps.form.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.joget.apps.form.service.FormUtil;

/**
 * Represents a row of form data
 */
public class FormRow extends Properties {
    Map<String, String> tempFilePathMap;

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

    @Override
    public boolean equals(Object obj) {
        FormRow row = (FormRow) obj;
        if (getId().equals(row.getId())) {
            return true;
        }
        return false;
    }
    
    public Map<String, String> getTempFilePathMap() {
        return tempFilePathMap;
    }
    
    public void setTempFilePathMap(Map<String, String> tempFilePathMap) {
        this.tempFilePathMap = tempFilePathMap;
    }
    
    public void putTempFilePath(String fieldId, String path) {
        if (tempFilePathMap == null) {
            tempFilePathMap = new HashMap<String, String>();
        }
        tempFilePathMap.put(fieldId, path);
    }
    
    public String getTempFilePath(String fieldId) {
        if (tempFilePathMap != null) {
            return tempFilePathMap.get(fieldId);
        }
        return null;
    }
    
    public void putAll(FormRow row) {
        super.putAll(row);
        Map files = row.getTempFilePathMap();
        if (files != null && !files.isEmpty()) {
            if (tempFilePathMap == null) {
                tempFilePathMap = new HashMap<String, String>();
            }
            tempFilePathMap.putAll(files);
        }
    }
}
