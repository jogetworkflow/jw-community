package org.joget.plugin.report;

import java.util.Map;
import org.joget.plugin.base.DefaultAuditTrailPlugin;

public class ReportPlugin extends DefaultAuditTrailPlugin {

    public String getName() {
        return "Report Plugin";
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
        return "Report Plugin";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
