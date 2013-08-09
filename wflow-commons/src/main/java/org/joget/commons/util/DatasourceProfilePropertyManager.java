package org.joget.commons.util;

import java.util.Properties;

/**
 * Allow the injection of custom Properties implementations(for example
 * to handle property encryption).
 */
public class DatasourceProfilePropertyManager {

    private String propertyClass = null;
    
    public Properties newInstance() {
        try {
            return (Properties) Class.forName(propertyClass).newInstance();
        } catch (Exception ex) {
            String msg = "Unable to create instance of Property class [" + propertyClass +"]";
            LogUtil.error(DatasourceProfilePropertyManager.class.getName(), ex, msg);
            throw new RuntimeException(msg, ex);
        } 
    }

    public String getPropertyClass() {
        return propertyClass;
    }

    public void setPropertyClass(String propertyClass) {
        this.propertyClass = propertyClass;
    }
    
    
}
