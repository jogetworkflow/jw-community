package org.joget.commons.util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("resourceBundleUtil")
public class ResourceBundleUtil implements ApplicationContextAware {

    private static ApplicationContext appContext;
    private static ResourceBundleMessageDao resourceBundleMessageDao;
    private static MessageSourceAccessor messages;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }

    public static MessageSourceAccessor getMessageSource() {
        if (messages == null) {
            MessageSource drms = (MessageSource) appContext.getBean("messageSource");
            messages = new MessageSourceAccessor(drms);
        }
        return messages;
    }

    public static void POFileImport(MultipartFile multipartFile, String locale) throws IOException {

        InputStream inputStream = multipartFile.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        List<ResourceBundleMessage> resourceBundleMessageList = new ArrayList<ResourceBundleMessage>();
        ResourceBundleMessage resourceBundleMessage;

        String line = null, key = null, original = null, translated = null;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.equalsIgnoreCase("") || line.length() == 0) {
                continue;
            }

            if (line.length() > 12 && line.substring(0, 11).equalsIgnoreCase("\"Language: ")) {
                //this is the locale
                locale = line.substring(11, line.length() - 3);

            } else if (line.length() > 4 && locale != null && line.substring(0, 3).equalsIgnoreCase("#: ")) {
                //this is the key
                key = line.substring(3, line.length());
                original = null;
                translated = null;

            } else if (line.length() > 8 && locale != null && key != null && line.substring(0, 7).equalsIgnoreCase("msgid \"")) {
                //this is the original string
                original = line.substring(7, line.length() - 1);

            } else if (line.length() > 9 && locale != null && key != null && original != null && line.substring(0, 8).equalsIgnoreCase("msgstr \"")) {
                //this is the translated string
                translated = line.substring(8, line.length() - 1);

            }

            if (key != null && original != null && translated != null) {
                //if this is a entry, insert into the list
                resourceBundleMessage = new ResourceBundleMessage();
                resourceBundleMessage.setKey(key);
                resourceBundleMessage.setLocale(locale);
                resourceBundleMessage.setMessage(translated);
                resourceBundleMessageList.add(resourceBundleMessage);
                key = null;
                original = null;
                translated = null;
            }
        }
        bufferedReader.close();

        if (resourceBundleMessageList.size() > 0) {
            bulkUpdatePO(resourceBundleMessageList);
        }
    }

    protected static void bulkUpdatePO(List<ResourceBundleMessage> resourceBundleMessageList) {
        for (ResourceBundleMessage r : resourceBundleMessageList) {
            getResourceBundleMessageDao().saveOrUpdate(r);
        }
    }

    public static ResourceBundleMessageDao getResourceBundleMessageDao() {
        if (resourceBundleMessageDao == null) {
            resourceBundleMessageDao = (ResourceBundleMessageDao) appContext.getBean("resourceBundleMessageDao");
        }
        return resourceBundleMessageDao;
    }

    public static String getMessage(String code) {
        try {
            return getMessageSource().getMessage(code);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMessage(String code, Object[] args) {
        try {
            return getMessageSource().getMessage(code, args);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMessage(String code, Object[] args, String defaultMessage) {
        try {
            return getMessageSource().getMessage(code, args, defaultMessage);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMessage(String code, String defaultMessage) {
        try {
            return getMessageSource().getMessage(code, defaultMessage);
        } catch (Exception e) {
            return null;
        }
    }
}
