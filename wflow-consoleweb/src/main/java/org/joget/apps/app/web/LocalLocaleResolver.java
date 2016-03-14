package org.joget.apps.app.web;

import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class LocalLocaleResolver extends SessionLocaleResolver implements LocaleResolver, I18nResourceProvider{
    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private SetupManager setupManager;
    
    public static final String UNDEFINED_KEY = "???"; //$NON-NLS-1$
    public static final String PARAM_NAME = "_lang";
    public static final Locale DEFAULT = new Locale("en", "US");
    public static final String DEFAULT_LOCALE_KEY = "defaultLocale";
    public static final String DEFAULT_LOCALE_EXPIRY_KEY = "locale_expiry";
    public static final String CURRENT_LOCALE_KEY = "currentLocale";
    public static final String LOCALE_OF_USER = "LOCALE_OF_USER";
    public final static String TIMEZONE_OF_USER = "TIMEZONE_OF_USER";
    public final static String SYSTEM_TIMEZONE = "SYSTEM_TIMEZONE";
    public final static String SYSTEM_TIMEZONE_EXPIRY_KEY = "SYSTEM_TIMEZONE_EXPIRY_KEY";
    public final static Long CACHE_DURATION =  5000L; // 5 seconds

    @Override
    protected TimeZone determineDefaultTimeZone(HttpServletRequest request) {
        TimeZone timezone = null;
        
        if (request != null) {
            // reset profile and set hostname
            HostManager.initHost();
            
            timezone = (TimeZone) request.getAttribute(SYSTEM_TIMEZONE);
            
            if (timezone == null) {
                // lookup in session
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Long defaultExpiry = (Long)session.getAttribute(SYSTEM_TIMEZONE_EXPIRY_KEY);
                    if (defaultExpiry == null || defaultExpiry.compareTo(System.currentTimeMillis()) < 0) {
                        session.removeAttribute(SYSTEM_TIMEZONE);
                    } else {
                        timezone = (TimeZone) session.getAttribute(SYSTEM_TIMEZONE);
                        request.setAttribute(SYSTEM_TIMEZONE, timezone);
                    }
                }
            }
            
            HttpSession session = request.getSession(false);
            if (timezone != null && session != null && !getWorkflowUserManager().getCurrentUsername().equals(session.getAttribute(TIMEZONE_OF_USER))) {
                timezone = null;
            }
        }
            
        if (timezone == null) {
            String gmt = null;
            try {
                if (!getWorkflowUserManager().isCurrentUserAnonymous()) {
                    String userGmt = null;
                    User user = getDirectoryManager().getUserByUsername(getWorkflowUserManager().getCurrentUsername());
                    if (user != null) {
                        userGmt =  user.getTimeZone();
                    }
                    if (userGmt != null && !userGmt.isEmpty()) {
                        gmt = userGmt;
                    }
                }

                if (gmt == null) {
                    //get system locale
                    String systemGmt = getSetupManager().getSettingValue("systemTimeZone");
                    if (systemGmt != null && !systemGmt.isEmpty()) {
                        gmt = systemGmt;
                    }
                }

                if (gmt != null && !gmt.isEmpty()) {
                    timezone = getTimeZoneByGMT(gmt);

                    if (timezone != null && request != null) {
                        request.setAttribute(SYSTEM_TIMEZONE, timezone);

                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            Long expiry = System.currentTimeMillis() + CACHE_DURATION;
                            session.setAttribute(SYSTEM_TIMEZONE_EXPIRY_KEY, expiry);
                            session.setAttribute(SYSTEM_TIMEZONE, timezone);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.warn(getClass().getName(), "Error setting system timezone from setting, using default timezone");
            }
        }
            
        
        if (timezone == null) {
            timezone = super.getDefaultTimeZone();
        }
        
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute(TIMEZONE_OF_USER, getWorkflowUserManager().getCurrentUsername());
            }
        }
            
        return timezone;
    }
            
    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request){
        Locale locale = null;
        if (request != null) {
            // reset profile and set hostname
            HostManager.initHost();
            
            locale = (Locale) request.getAttribute(DEFAULT_LOCALE_KEY);
            
            HttpSession session = request.getSession(false);
            if (locale == null) {
                // lookup in session
                if (session != null) {
                    Long defaultExpiry = (Long)session.getAttribute(DEFAULT_LOCALE_EXPIRY_KEY);
                    if (defaultExpiry == null || defaultExpiry.compareTo(System.currentTimeMillis()) < 0) {
                        session.removeAttribute(DEFAULT_LOCALE_KEY);
                    } else {
                        locale = (Locale) session.getAttribute(DEFAULT_LOCALE_KEY);
                        request.setAttribute(DEFAULT_LOCALE_KEY, locale);
                    }
                }
            }
            if (locale != null && session != null && !getWorkflowUserManager().getCurrentUsername().equals(session.getAttribute(LOCALE_OF_USER))) {
                locale = null;
            }
        }
        
        if (locale == null) {
            String localeCode = null;
            try {
                String enableUserLocale = getSetupManager().getSettingValue("enableUserLocale");
                if (enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true") && !getWorkflowUserManager().isCurrentUserAnonymous()) {
                    String userLocale = null;
                    User user = getDirectoryManager().getUserByUsername(getWorkflowUserManager().getCurrentUsername());
                    if (user != null) {
                        userLocale =  user.getLocale();
                    }
                    if (userLocale != null && !userLocale.isEmpty()) {
                        localeCode = userLocale;
                    }
                }

                if (localeCode == null) {
                    //get system locale
                    String systemLocale = getSetupManager().getSettingValue("systemLocale");
                    if (systemLocale != null && !systemLocale.isEmpty()) {
                        localeCode = systemLocale;
                    }
                }

                if (localeCode != null && !localeCode.isEmpty()) {
                    String[] temp = localeCode.split("_");

                    if(temp.length == 1){
                        locale = new Locale(temp[0]);
                    }else if (temp.length == 2){
                        locale = new Locale(temp[0], temp[1]);
                    }else if (temp.length == 3){
                        locale = new Locale(temp[0], temp[1], temp[2]);
                    }

                    Locale.setDefault(DEFAULT);

                    if (request != null) {
                        request.setAttribute(DEFAULT_LOCALE_KEY, locale);
                        
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            Long expiry = System.currentTimeMillis() + CACHE_DURATION;
                            session.setAttribute(DEFAULT_LOCALE_EXPIRY_KEY, expiry);
                            session.setAttribute(DEFAULT_LOCALE_KEY, locale);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.warn(getClass().getName(), "Error setting system locale from setting, using default locale");
            }
        }
        
        if (locale == null) {
            locale = DEFAULT;
            if (request != null) {
                request.setAttribute(DEFAULT_LOCALE_KEY, locale);
            }
        }
        
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute(LOCALE_OF_USER, getWorkflowUserManager().getCurrentUsername());
            }
        }
            
        return locale;
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = null;
        
        if (request != null) {
            locale = (Locale) request.getAttribute(CURRENT_LOCALE_KEY);
            if (request.getParameter(PARAM_NAME) != null 
                    && (locale == null || !request.getParameter(PARAM_NAME).equals(locale.toString()))) {
                locale = null;
                String paramValue = request.getParameter(PARAM_NAME);
                String[] temp = paramValue.split("_");

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
                    setLocale(request, null, null);
                }
                locale = super.resolveLocale(request);
                request.setAttribute(CURRENT_LOCALE_KEY, locale);
            } 
            
            if (locale == null) {
                locale = super.resolveLocale(request);
                request.setAttribute(CURRENT_LOCALE_KEY, locale);
            }
        } else {
            locale = determineDefaultLocale(null);
        }
        
        return locale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale){
        super.setLocale(request, response, locale);
    }
    
    public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext pageContext) {
        return ResourceBundleUtil.getMessage(resourceKey, defaultValue);
    }
    
    protected static TimeZone getTimeZoneByGMT(String gmt) {
        TimeZone timezone = null;
        
        try {
            if (gmt != null && gmt.trim().length() > 0) {
                if (gmt.contains(".")) {
                    Double rawoffset = Double.parseDouble(gmt) * 60 * 60 * 1000;
                    String[] tzs = TimeZone.getAvailableIDs(rawoffset.intValue());
                    if (tzs.length > 0) {
                        timezone = TimeZone.getTimeZone(tzs[0]);
                    }
                } else {
                    try {
                        if (Integer.parseInt(gmt) > 0) {
                            gmt = "+" + gmt;
                        }
                        timezone = TimeZone.getTimeZone("GMT" + gmt);
                    } catch (NumberFormatException e) {
                        timezone = TimeZone.getTimeZone(gmt);
                    }
                }
            }
        } catch (Exception e) {}

        return timezone;
    }
    
    public void setWorkflowUserManager(WorkflowUserManager workflowUserManager) {
        this.workflowUserManager = workflowUserManager;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public WorkflowUserManager getWorkflowUserManager() {
        if (workflowUserManager == null) {
            workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        }
        return workflowUserManager;
    }

    public DirectoryManager getDirectoryManager() {
        if (directoryManager == null) {
            directoryManager = (DirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
        }
        return directoryManager;
    }

    public SetupManager getSetupManager() {
        if (setupManager == null) {
            setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        }
        return setupManager;
    }
    
}
