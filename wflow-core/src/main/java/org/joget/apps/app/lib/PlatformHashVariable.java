package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import static org.joget.commons.util.TimeZoneUtil.getTimeZoneByGMT;

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
        } else if ("build".equals(variableKey)) {
            result = ResourceBundleUtil.getMessage("build.number");
        } else if ("marketplaceUrl".equals(variableKey)) {
            result = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
        } else if ("jdbcDriver".equals(variableKey)) {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            try {
                result = BeanUtils.getProperty(ds, "driverClassName");
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        } else if (variableKey.startsWith("license")) {
            result = "";
        } else if (variableKey.startsWith("currentLocale")) {
            result = AppUtil.getAppLocale();
        } else if (variableKey.startsWith("currentLanguage")) {
            result = AppUtil.getAppLanguage();
        } else if (variableKey.startsWith("currentTimezone")) {
            result = AppUtil.getAppTimezone();
        } else if (variableKey.startsWith("currentDateFormat")) {
            result = AppUtil.getAppDateFormat();
        } else if (variableKey.startsWith("isRTL")) {
            result = Boolean.toString(AppUtil.isRTL());
        } else if (variableKey.startsWith("firstDayOfWeek")) {
            result = AppUtil.getAppFirstDayOfWeek();
        } else if (variableKey.startsWith("isEnterprise")) {
            result = Boolean.toString(AppUtil.isEnterprise());
        } else if (variableKey.startsWith("isQuickEditAvailable")) {
            result = Boolean.toString(AppUtil.isQuickEditEnabled());
        } else if (variableKey.startsWith("setting.")) {
            String property = variableKey.substring("setting.".length());
            SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
            result = setupManager.getSettingValue(property);
            
            if ("systemTimeZone".equals(property) && result != null && !result.isEmpty()) {
                TimeZone tz = TimeZone.getTimeZone(getTimeZoneByGMT(result));
                if (tz != null) {
                    result = tz.getID();
                }
            }
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
        syntax.add("platform.build");
        syntax.add("platform.jdbcDriver");
        syntax.add("platform.currentLocale");
        syntax.add("platform.currentLanguage");
        syntax.add("platform.currentTimezone");
        syntax.add("platform.currentDateFormat");
        syntax.add("platform.firstDayOfWeek");
        syntax.add("platform.marketplaceUrl");
        syntax.add("platform.isEnterprise");
        syntax.add("platform.isQuickEditAvailable");
        syntax.add("platform.isRTL");
        syntax.add("platform.setting.dataFileBasePath");
        syntax.add("platform.setting.deadlineCheckerInterval");
        syntax.add("platform.setting.fileSizeLimit");
        syntax.add("platform.setting.landingPage");
        syntax.add("platform.setting.systemDateFormat");
        syntax.add("platform.setting.systemLocale");
        syntax.add("platform.setting.systemTimeZone");
        return syntax;
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/platformHashVariable.json", null, true, null);
    }
}
