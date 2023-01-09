package org.joget.plugin.base;

public class CustomPluginInterface {
    private Class classObj;
    private String classname;
    private String labelKey;
    private String resourceBundlePath;
    
    public CustomPluginInterface(Class classObj, String labelKey, String resourceBundlePath){
        this.classObj = classObj;
        this.classname = classObj.getName();
        this.labelKey = labelKey;
        this.resourceBundlePath = resourceBundlePath;
    }

    public Class getClassObj() {
        return classObj;
    }

    public String getClassname() {
        return classname;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getResourceBundlePath() {
        return resourceBundlePath;
    }
}
