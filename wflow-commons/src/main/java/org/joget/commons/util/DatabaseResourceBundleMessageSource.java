package org.joget.commons.util;

import org.joget.commons.spring.model.ResourceBundleMessage;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;

public class DatabaseResourceBundleMessageSource extends ResourceBundleMessageSource {

    @Autowired
    private ResourceBundleMessageDao resourceBundleMessageDao;
    
    @Autowired
    private SetupManager setupManager;

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        String localeToUse = locale.toString();
        try {
            String systemLocale = setupManager.getSettingValue("systemLocale");
            if (systemLocale != null && systemLocale.trim().length() > 0) {
                localeToUse = systemLocale;
                
                String[] temp = localeToUse.split("_");
                
                if(temp.length == 1){
                    locale = new Locale(temp[0]);
                }else if (temp.length == 2){
                    locale = new Locale(temp[0], temp[1]);
                }else if (temp.length == 3){
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }

                locale.setDefault(new Locale("en", "US"));
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error setting system locale from setting, using default locale");
        }
        
        ResourceBundleMessage resourceBundleMessage = null;
        try {
            resourceBundleMessage = resourceBundleMessageDao.getMessage(code, localeToUse);
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), null, "Error retrieving resource bundle message for " + code);
        }

        if (resourceBundleMessage != null) {
            return resourceBundleMessage.getMessage();
        } else {
            return super.resolveCodeWithoutArguments(code, locale);
        }
    }
}
