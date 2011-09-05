package org.joget.plugin.base;

import java.util.Map;

public class SampleAuditTrailPlugin extends DefaultAuditTrailPlugin {

    public String getName() {
        return "SampleAuditTrailPlugin";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "Sample Audit Trail Plugin from Classpath";
    }

    public Object execute(Map properties) {
        return null;
    }

    public String getLabel() {
        return "Sample Audit Trail Plugin";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
