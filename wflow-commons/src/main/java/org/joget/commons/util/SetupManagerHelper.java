package org.joget.commons.util;

import java.util.Map;
import org.joget.commons.spring.model.Setting;

public interface SetupManagerHelper {
    
    public void checkSettingChanges(Map<String, Setting> settingMap);
    
    public void auditSettingChange(Setting setting);
}
