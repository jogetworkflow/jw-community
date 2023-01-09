package org.joget.commons.util;

import java.text.MessageFormat;
import java.util.HashMap;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.support.ResourceBundleMessageSource;

public class DatabaseResourceBundleMessageSource extends ResourceBundleMessageSource {

    private ResourceBundleMessageDao resourceBundleMessageDao;
    private final Map<String, MessageFormat> cachedMessageFormats = new HashMap<String, MessageFormat>();

    public ResourceBundleMessageDao getResourceBundleMessageDao() {
        return resourceBundleMessageDao;
    }

    public void setResourceBundleMessageDao(ResourceBundleMessageDao resourceBundleMessageDao) {
        this.resourceBundleMessageDao = resourceBundleMessageDao;
    }
    
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        String localeToUse = locale.toString();
        
        ResourceBundleMessage resourceBundleMessage = null;
        try {
            resourceBundleMessage = resourceBundleMessageDao.getMessage(code, localeToUse);
        } catch (Exception e) {
            //LogUtil.error(getClass().getName(), null, "Error retrieving resource bundle message for " + code);
        }

        if (resourceBundleMessage != null) {
            return resourceBundleMessage.getMessage();
        } else {
            return super.resolveCodeWithoutArguments(code, locale);
        }
    }
    
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String localeToUse = locale.toString();
        
        ResourceBundleMessage resourceBundleMessage = null;
        try {
            resourceBundleMessage = resourceBundleMessageDao.getMessage(code, localeToUse);
        } catch (Exception e) {
            //LogUtil.error(getClass().getName(), null, "Error retrieving resource bundle message for " + code);
        }

        if (resourceBundleMessage != null) {
            if (!cachedMessageFormats.containsKey(resourceBundleMessage.getMessage() + ":" + locale)) {
                cachedMessageFormats.put(resourceBundleMessage.getMessage() + ":" + locale, createMessageFormat(resourceBundleMessage.getMessage(), locale));
            }
            return cachedMessageFormats.get(resourceBundleMessage.getMessage() + ":" + locale);
        } else {
            return super.resolveCode(code, locale);
        }
    }
}
