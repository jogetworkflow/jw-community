package org.joget.apps.app.service;

import java.util.HashMap;
import java.util.Map;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManagerHelper;
import org.joget.governance.service.GovHealthCheckManager;
import org.joget.plugin.base.ProfilePluginCache;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppSetupManagerHelperImpl implements SetupManagerHelper {
    
    @Autowired
    WorkflowManager workflowManager;
    
    @Autowired
    GovHealthCheckManager govHealthCheckManager;
    
    private Map<String, Map<String, String>> settings = new HashMap<String, Map<String, String>>();
    
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
                if (workflowManager != null && deadline != null && deadline.getValue() != null && !deadline.getValue().equals(oldSettings.get("deadlineCheckerInterval"))) {
                    oldSettings.put("deadlineCheckerInterval", deadline.getValue());
                    workflowManager.internalUpdateDeadlineChecker();
                }

                //check health check interval
                Setting healthCheck = settingMaps.get(GovHealthCheckManager.SETTING);
                if (govHealthCheckManager != null && healthCheck != null && healthCheck.getValue() != null && !healthCheck.getValue().equals(oldSettings.get(GovHealthCheckManager.SETTING))) {
                    oldSettings.put(GovHealthCheckManager.SETTING, healthCheck.getValue());
                    govHealthCheckManager.updateCheckInterval(healthCheck.getValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppSetupManagerHelperImpl.class.getName(), e, "");
        }
    }
}