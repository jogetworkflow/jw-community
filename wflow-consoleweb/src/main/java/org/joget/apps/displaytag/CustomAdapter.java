package org.joget.apps.displaytag;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;

public class CustomAdapter implements LocaleResolver, I18nResourceProvider {
    public static final String UNDEFINED_KEY = "???"; //$NON-NLS-1$

    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = new Locale("en", "US");
        try {
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            String systemLocale = setupManager.getSettingValue("systemLocale");
            if (systemLocale != null && systemLocale.trim().length() > 0) {
                String localeToUse = systemLocale;
                
                String[] temp = localeToUse.split("_");
                
                if(temp.length == 1){
                    locale = new Locale(temp[0]);
                }else if (temp.length == 2){
                    locale = new Locale(temp[0], temp[1]);
                }else if (temp.length == 3){
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error setting system locale from setting, using default locale");
        }
        return locale;
    }

    public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext pageContext) {
        return ResourceBundleUtil.getMessage(resourceKey, defaultValue);
    }
}
