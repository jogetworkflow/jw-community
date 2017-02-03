package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import javax.sql.DataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;

/**
 * The Platform Hash Variable is used to retrieve platform specific information.
 */
public class PlatformHashVariable extends DefaultHashVariablePlugin {
    
    @Override
    public String processHashVariable(String variableKey) {
        String result = null;
        if ("name".equals(variableKey)) {
            result = ResourceBundleUtil.getMessage("console.header.top.title");
        } else if ("version".equals(variableKey)) {
            result = ResourceBundleUtil.getMessage("console.footer.label.revision");
        } else if ("jdbcDriver".equals(variableKey)) {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            try {
                result = BeanUtils.getProperty(ds, "driverClassName");
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        } else if (variableKey.startsWith("license")) {
            result = "";
        } else if (variableKey.startsWith("setting.")) {
            String property = variableKey.substring("setting.".length());
            SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
            result = setupManager.getSettingValue(property);
        }
        return result;
    }

    public String getName() {
        return "Platform Hash Variable";
    }

    public String getPrefix() {
        return "platform";
    }

    public String getVersion() {
        return "6.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Platform Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }
    
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("platform.name");
        syntax.add("platform.version");
        syntax.add("platform.jdbcDriver");
        syntax.add("platform.setting.dataFileBasePath");
        syntax.add("platform.setting.deadlineCheckerInterval");
        syntax.add("platform.setting.fileSizeLimit");
        syntax.add("platform.setting.landingPage");
        syntax.add("platform.setting.systemDateFormat");
        syntax.add("platform.setting.systemLocale");
        syntax.add("platform.setting.systemTimeZone");
        return syntax;
    }
}
