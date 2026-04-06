package org.joget.apps.app.service;

import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.SetupManagerHelper;
import org.joget.governance.service.GovHealthCheckManager;
import org.joget.plugin.base.ProfilePluginCache;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppSetupManagerHelperImpl implements SetupManagerHelper {

    /** List of properties whose values should not be logged */
    public static final Set<String> SENSITIVE_PROPERTIES = Set.of(
            SetupManager.MASTER_LOGIN_PASSWORD,
            SetupManager.SMTP_PASSWORD,
            "smtpStorepass"
    );

    private Map<String, Map<String, String>> settings = new HashMap<String, Map<String, String>>();
    private WorkflowManager workflowManager;
    private GovHealthCheckManager govHealthCheckManager;
    
    private Map<String, String> getOldSettings() {
        String profile = null;
        try {
            profile = DynamicDataSourceManager.getCurrentProfile();
        } catch (Exception e) {
            LogUtil.debug(ProfilePluginCache.class.getName(), profile);
        }
        if (profile == null) {
            profile = DynamicDataSourceManager.DEFAULT_PROFILE;
        }
        if (!settings.containsKey(profile)) {
            Map<String, String> s = new HashMap<String, String>();
            settings.put(profile, s);
        }
        
        return settings.get(profile);
    }
    
    @Override
    public void checkSettingChanges(Map<String, Setting> settingMaps) {
        try {
            if (settingMaps != null) {
                Map<String, String> oldSettings = getOldSettings();

                //Check deadline interval 
                Setting deadline = settingMaps.get("deadlineCheckerInterval");
                WorkflowManager workflowManager = getWorkflowManager(); // Store in variable
                if (workflowManager != null && deadline != null && deadline.getValue() != null &&
                    !deadline.getValue().equals(oldSettings.get("deadlineCheckerInterval"))) {
                    oldSettings.put("deadlineCheckerInterval", deadline.getValue());
                    workflowManager.internalUpdateDeadlineChecker(); //  Safe to use
                }

                //check health check interval
                Setting healthCheck = settingMaps.get(GovHealthCheckManager.SETTING);
                GovHealthCheckManager healthCheckManager = getGovHealthCheckManager(); // Store in variable
                if (healthCheckManager != null && healthCheck != null && healthCheck.getValue() != null &&
                    !healthCheck.getValue().equals(oldSettings.get(GovHealthCheckManager.SETTING))) {
                    oldSettings.put(GovHealthCheckManager.SETTING, healthCheck.getValue());
                    healthCheckManager.updateCheckInterval(healthCheck.getValue()); // Safe to use
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppSetupManagerHelperImpl.class.getName(), e, "");
        }
    }

    /**
     * Check and add audit trail record if setting changed
     * 
     * @param setting 
     */
    @Override
    public void auditSettingChange(Setting setting) {
        if (setting != null && 
                !setting.getProperty().startsWith("CACHE_LAST_CLEAR_") && 
                !setting.getProperty().contains("node::") && 
                !setting.getProperty().contains("LogToken")) { //do not track cache setting && cluster license changes
            if (setting.getValue() == null || setting.getOriginalValue() == null || !setting.getOriginalValue().equals(setting.getValue())) {
                
                String method = "saveSetting";
                if (setting.getValue() == null) {
                    method = "deleteSetting";
                }
                
                WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                boolean isSensitive = setting.isSensitive() || SENSITIVE_PROPERTIES.contains(setting.getProperty());

                // redact the whole value or try to mask secure values within the string
                String oldVal = isSensitive ? "***" : PropertyUtil.propertiesJsonLoadProcessing(setting.getOriginalValue());
                String newVal = isSensitive ? "***" : PropertyUtil.propertiesJsonLoadProcessing(setting.getValue());
                String auditMessage = "property=" + setting.getProperty()
                        + ", oldValue=" + oldVal
                        + ", newValue=" + newVal;
                workflowHelper.addAuditTrail(SetupManager.class.getName(), method, auditMessage, null, null, null);
            }
        }            
    }
    
    private WorkflowManager getWorkflowManager() {
        if (workflowManager == null) {
            try {
                workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            } catch (Exception e) {}
        }
        return workflowManager;
    }
    
    private GovHealthCheckManager getGovHealthCheckManager() {
        if (govHealthCheckManager == null) {
            try {
                govHealthCheckManager = (GovHealthCheckManager) AppUtil.getApplicationContext().getBean("govHealthCheckManager");
            } catch (Exception e) {}
        }
        return govHealthCheckManager;
    }
}
