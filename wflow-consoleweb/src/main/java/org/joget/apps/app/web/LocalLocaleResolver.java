package org.joget.apps.app.web;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class LocalLocaleResolver extends SessionLocaleResolver implements LocaleResolver, I18nResourceProvider{
    public static final String UNDEFINED_KEY = "???"; //$NON-NLS-1$
    public static final String PARAM_NAME = "_lang";
    public static final Locale DEFAULT = new Locale("en", "US");
    
    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request){
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        
        Locale locale = DEFAULT;
        try {
            String systemLocale = setupManager.getSettingValue("systemLocale");
            if (systemLocale != null && systemLocale.trim().length() > 0) {
                String[] temp = systemLocale.split("_");
                
                if(temp.length == 1){
                    locale = new Locale(temp[0]);
                }else if (temp.length == 2){
                    locale = new Locale(temp[0], temp[1]);
                }else if (temp.length == 3){
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }

                Locale.setDefault(DEFAULT);
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error setting system locale from setting, using default locale");
        }
        
        return locale;
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if (request != null && request.getParameter(PARAM_NAME) != null) {
            Locale locale = null;
            String[] temp = request.getParameter(PARAM_NAME).split("_");
  
            if (temp.length == 1 && !temp[0].isEmpty()) {
                locale = new Locale(temp[0]);
            } else if (temp.length == 2) {
                locale = new Locale(temp[0], temp[1]);
            } else if (temp.length == 3) {
                locale = new Locale(temp[0], temp[1], temp[2]);
            }
            if (locale != null) {
                setLocale(request, null, locale);
            } else {
                setLocale(request, null, determineDefaultLocale(request));
            }
        }
        return super.resolveLocale(request);
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale){
        super.setLocale(request, response, locale);
    }
    
    public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext pageContext) {
        return ResourceBundleUtil.getMessage(resourceKey, defaultValue);
    }
}
