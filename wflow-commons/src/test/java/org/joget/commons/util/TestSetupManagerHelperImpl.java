package org.joget.commons.util;

import java.util.HashMap;
import java.util.Map;
import org.joget.commons.spring.model.Setting;

public class TestSetupManagerHelperImpl implements SetupManagerHelper {
    public Map<String, Setting> audits = new HashMap<String, Setting>();
    public Map<String, Setting> cacheSettingMaps = new HashMap<String, Setting>();
    
    @Override
    public void checkSettingChanges(Map<String, Setting> settingMaps) {
        cacheSettingMaps = settingMaps;
    }
    
    @Override
    public void auditSettingChange(Setting setting) {
        if (setting != null) {
            audits.put(setting.getProperty(), setting);
        }
    }
}