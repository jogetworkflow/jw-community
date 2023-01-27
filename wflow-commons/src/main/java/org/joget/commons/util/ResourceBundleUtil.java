package org.joget.commons.util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility method to get i18n label
 * 
 */
@Service("resourceBundleUtil")
public class ResourceBundleUtil implements ApplicationContextAware {

    private static ApplicationContext appContext;
    private static ApplicationContext setupAppContext;
    private static ResourceBundleMessageDao resourceBundleMessageDao;
    private static MessageSourceAccessor messages;

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * Method used for system to set ApplicationContext
     * @param context
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }

    /**
     * Utility method to retrieve the Setup ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getSetupApplicationContext() {
        if (setupAppContext == null) {
            setupAppContext = new ClassPathXmlApplicationContext("setupApplicationContext.xml");
        }
        return setupAppContext;
    }

    /**
     * Gets the message source
     * @return 
     */
    public static MessageSourceAccessor getMessageSource() {
        if (messages == null) {
            if (appContext != null) {
                MessageSource drms = (MessageSource) appContext.getBean("messageSource");
                messages = new MessageSourceAccessor(drms);
            } else {
                ApplicationContext setup = getSetupApplicationContext();
                MessageSource drms = (MessageSource) setup.getBean("messageSource");
                return new MessageSourceAccessor(drms);
            }
        }
        return messages;
    }

    /**
     * Method used to import message from PO file
     * @param multipartFile
     * @param locale
     * @throws IOException 
     */
    public static void POFileImport(MultipartFile multipartFile, String locale) throws IOException {

    InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            List<ResourceBundleMessage> resourceBundleMessageList = new ArrayList<ResourceBundleMessage>();
            ResourceBundleMessage resourceBundleMessage;

            Set<String> keys = new HashSet<String>();
            String nextLine = null, line = null, original = null, translated = null;

            while (((nextLine = bufferedReader.readLine()) != null)) {
                if (line != null) {
                    if (line.equalsIgnoreCase("") || line.length() == 0) {
                        line = nextLine;
                        continue;
                    }

                    if (line.length() > 12 && line.substring(0, 11).equalsIgnoreCase("\"Language: ")) {
                        //this is the locale
                        locale = line.substring(11, line.length() - 3);

                    } else if (line.length() > 4 && locale != null && line.substring(0, 3).equalsIgnoreCase("#: ")) {
                        //this is the key
                        String[] temp = line.substring(3, line.length()).split(" ");
                        for (String t : temp) {
                            if (!t.isEmpty()) {
                                keys.add(t);
                            }
                        }

                    } else if (line.length() > 7 && locale != null && !keys.isEmpty() && line.substring(0, 7).equalsIgnoreCase("msgid \"")) {
                        //this is the original string
                        original = line.substring(7, line.length() - 1);
                        while (nextLine != null && nextLine.startsWith("\"")) {
                            original += nextLine.substring(1, nextLine.length() - 1);
                            nextLine = bufferedReader.readLine();
                        }
                    } else if (line.length() > 8 && locale != null && !keys.isEmpty() && original != null && line.substring(0, 8).equalsIgnoreCase("msgstr \"")) {
                        //this is the translated string
                        translated = line.substring(8, line.length() - 1);
                        while (nextLine != null && nextLine.startsWith("\"")) {
                            translated += nextLine.substring(1, nextLine.length() - 1);
                            nextLine = bufferedReader.readLine();
                        }
                    }

                    if (!keys.isEmpty() && original != null && translated != null) {
                        if (!translated.isEmpty() && !translated.startsWith("_: ")) {
                            for (String key : keys.toArray(new String[0])) {
                                if (!key.equals("console.footer.label.revision") &&
                                        !key.equals("build.number") &&
                                        !key.equals("build.version")) {
                                    //if this is a entry, insert into the list
                                    resourceBundleMessage = getResourceBundleMessageDao().getMessage(key, locale);
                                    if (resourceBundleMessage == null) {
                                        resourceBundleMessage = new ResourceBundleMessage();
                                        resourceBundleMessage.setKey(key);
                                        resourceBundleMessage.setLocale(locale);
                                    }   
                                    resourceBundleMessage.setMessage(translated.replaceAll(StringUtil.escapeRegex("\\\""), "\""));
                                    resourceBundleMessageList.add(resourceBundleMessage);
                                }
                            }
                        }

                        keys = new HashSet<String>();
                        original = null;
                        translated = null;
                    }
                }
                line = nextLine;
            }
            bufferedReader.close();

            if (resourceBundleMessageList.size() > 0) {
                bulkUpdatePO(resourceBundleMessageList);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    protected static void bulkUpdatePO(List<ResourceBundleMessage> resourceBundleMessageList) {
        for (ResourceBundleMessage r : resourceBundleMessageList) {
            getResourceBundleMessageDao().saveOrUpdate(r);
        }
    }

    /**
     * Method used by the system to get resource bundle message dao
     * @return 
     */
    public static ResourceBundleMessageDao getResourceBundleMessageDao() {
        if (resourceBundleMessageDao == null) {
            resourceBundleMessageDao = (ResourceBundleMessageDao) appContext.getBean("resourceBundleMessageDao");
        }
        return resourceBundleMessageDao;
    }

    /**
     * Gets the i18n message based on code
     * @param code
     * @return 
     */
    public static String getMessage(String code) {
        try {
            return getMessageSource().getMessage(code);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message with arguments based on code 
     * @param code
     * @return 
     */
    public static String getMessage(String code, Object[] args) {
        try {
            return getMessageSource().getMessage(code, args);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message with arguments based on code
     * @param code
     * @param args
     * @param defaultMessage
     * @return default message if i18n message not found
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        try {
            return getMessageSource().getMessage(code, args, defaultMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message based on code
     * @param code
     * @param defaultMessage
     * @return default message if i18n message not found
     */
    public static String getMessage(String code, String defaultMessage) {
        try {
            return getMessageSource().getMessage(code, defaultMessage);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets the i18n message based on code and locale
     * @param code
     * @param locale
     * @return 
     */
    public static String getMessage(String code, Locale locale) {
        try {
            return getMessageSource().getMessage(code, locale);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message with arguments based on code and locale
     * @param code
     * @param args
     * @param locale
     * @return 
     */
    public static String getMessage(String code, Object[] args, Locale locale) {
        try {
            return getMessageSource().getMessage(code, args, locale);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message with arguments based on code and locale
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return default message if i18n message not found
     */
    public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            return getMessageSource().getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the i18n message based on code and locale
     * @param code
     * @param defaultMessage
     * @param locale
     * @return default message if i18n message not found
     */
    public static String getMessage(String code, String defaultMessage, Locale locale) {
        try {
            return getMessageSource().getMessage(code, defaultMessage, locale);
        } catch (Exception e) {
            return null;
        }
    }
    
}
