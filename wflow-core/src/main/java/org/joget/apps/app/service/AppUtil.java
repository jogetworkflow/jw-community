package org.joget.apps.app.service;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.UserReplacementDao;
import org.joget.apps.app.lib.EmailTool;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CreateAppOption;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserReplacement;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.lib.AjaxUniversalTheme;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.apps.userview.model.UserviewV5Theme;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.DistributedIdGenerator;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupDao;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowManagerImpl;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

/**
 * Utility methods is used by App in runtime
 * 
 */
@Service("appUtil")
public class AppUtil implements ApplicationContextAware {

    public static final String PREFIX_WORKFLOW_VARIABLE = "var_";
    public static final String PROPERTY_WORKFLOW_VARIABLE = "workflowVariable";
    private static final String UI_SESSION_KEY = "UI_SESSION_KEY";
    private static final String HASH_NO_ESCAPE = "noescape";
    
    static ApplicationContext appContext;
    static ThreadLocal currentAssignment = new ThreadLocal();
    static ThreadLocal currentAssignmentRequiredReset = new ThreadLocal();
    static ThreadLocal currentAppDefinition = new ThreadLocal();
    static ThreadLocal resetAppDefinition = new ThreadLocal();
    static ThreadLocal processAppDefinition = new ThreadLocal();
    static String designerContextPath = "/jwdesigner";

    /**
     * Method used for system to set ApplicationContext
     * @param ac
     * @throws BeansException 
     */
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return WorkflowUtil.getApplicationContext();
    }

    /**
     * Used by system to sets designer context path
     * @param path 
     */
    public static void setDesignerContextPath(String path) {
        designerContextPath = path;
    }
    
    /**
     * Used by system to gets designer context path
     * @return 
     */
    public static String getDesignerContextPath() {
        return designerContextPath;
    }
    
    /**
     * Ties an Assignment to the current thread.
     * @param assignment
     * @throws BeansException
     */
    public static void setCurrentAssignment(WorkflowAssignment assignment) throws BeansException {
        setCurrentAssignment(assignment, false);
    }
    
    /**
     * Ties an Assignment to the current thread. Set requiredReset to true if for temporary.
     * Used by AppPluginUtil.getDefaultProperties
     * @param assignment
     * @param requiredReset
     * @throws BeansException
     */
    public static void setCurrentAssignment(WorkflowAssignment assignment, boolean requiredReset) throws BeansException {
        if (requiredReset) {
            WorkflowAssignment current = (WorkflowAssignment) currentAssignment.get();
            if ((assignment != null && current != null && !assignment.equals(current))
                    || (assignment != null && current == null)
                    || (assignment == null && current != null)) {
                if (current != null) {
                    currentAssignmentRequiredReset.set(current);
                } else {
                    currentAssignmentRequiredReset.set(Boolean.TRUE);
                }
            } else {
                currentAssignmentRequiredReset.set(null); //it is same, no need reset
            }
        }
        
        currentAssignment.set(assignment);
    }
    
    /**
     * Reset the Assignment tied to the current thread. Used by HashVariableSupportedMapImpl
     * @throws BeansException
     */
    public static void resetCurrentAssignment() throws BeansException {
        Object resetAssignment = currentAssignmentRequiredReset.get();
        if (resetAssignment != null) {
            if (resetAssignment instanceof WorkflowAssignment) {
                currentAssignment.set((WorkflowAssignment) resetAssignment);
            } else {
                currentAssignment.set(null);
            }
            currentAssignmentRequiredReset.set(null);
        }
    }

    /**
     * Retrieve the WorkflowAssignment for the current thread.
     * @return null if there is no WorkflowAssignment tied to the current thread.
     */
    public static WorkflowAssignment getCurrentAssignment() {
        WorkflowAssignment assignment = (WorkflowAssignment) currentAssignment.get();
        return assignment;
    }
    
    /**
     * Ties an AppDefinition to the current thread.
     * @param appDef
     * @throws BeansException
     */
    public static void setCurrentAppDefinition(AppDefinition appDef) throws BeansException {
        currentAppDefinition.set(appDef);
        resetAppDefinition.set(null);
    }

    /**
     * Retrieve the AppDefinition for the current thread.
     * @return null if there is no AppDefinition tied to the current thread.
     */
    public static AppDefinition getCurrentAppDefinition() {
        AppDefinition appDef = (AppDefinition) currentAppDefinition.get();
        return appDef;
    }

    /**
     * Method used by system to clear the AppDefinition of current thread once the request
     * is processing finish
     * @throws BeansException 
     */
    public static void resetAppDefinition() throws BeansException {
        resetAppDefinition.set(Boolean.TRUE);
    }

    /**
     * Method used by system to check whether there is an AppDefinition exist in 
     * current thread
     * @return
     * @throws BeansException 
     */
    public static boolean isAppDefinitionReset() throws BeansException {
        return resetAppDefinition.get() != null;
    }

    /**
     * Converts a String version to its Long equivalent.
     * @param version Null if no specific version is specified, or if there is a number format error.
     * @return
     */
    public static Long convertVersionToLong(String version) {
        Long versionLong = null;
        if (version == null || version.isEmpty() || AppDefinition.VERSION_LATEST.equals(version)) {
        } else {
            // load specific version
            try {
                versionLong = Long.parseLong(version);
            } catch (NumberFormatException e) {
                // if version is not empty and not number, get published version, 
                // issue is EnhancedWorkflowUserManager parsing the UI URL may not have version number, 
                // the version is UI id
                versionLong = -1l;
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
        }
        return versionLong;
    }

    /**
     * Forms the full process definition ID which includes the package and version.
     * @param appId
     * @param version
     * @param processDefId
     * @return
     */
    public static String getProcessDefIdWithVersion(String appId, String version, String processDefId) {
        String result = null;
        if (processDefId != null && processDefId.indexOf("#") > 0) {
            result = processDefId;
        } else {
            result = appId + "#" + version + "#" + processDefId;
        }
        return result;
    }

    /**
     * Returns the URL to the workflow web designer
     * @param request
     * @return
     */
    public static String getDesignerWebBaseUrl() {
        String defaultContext = getDesignerContextPath();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String serverBaseUrl = (request != null) ? request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() : "";
        String designerWebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
        if (designerWebBaseUrl == null || designerWebBaseUrl.trim().isEmpty() || "/".equals(designerWebBaseUrl)) {
            // use default context
            designerWebBaseUrl = serverBaseUrl + defaultContext;
        } else if (designerWebBaseUrl.startsWith("http")) {
            try {
                // remove trailing slash
                if (designerWebBaseUrl.endsWith("/")) {
                    designerWebBaseUrl = designerWebBaseUrl.substring(0, designerWebBaseUrl.length() - 1);
                }

                // check if context path is specified
                URL url = new URL(designerWebBaseUrl);
                String path = url.getPath();
                if (path == null || path.isEmpty()) {
                    designerWebBaseUrl += defaultContext;
                }
            } catch (Exception ex) {
                // use default context
                designerWebBaseUrl = serverBaseUrl + defaultContext;
            }
        } else {
            // remove preceding slash
            if (designerWebBaseUrl.startsWith("/")) {
                designerWebBaseUrl = designerWebBaseUrl.substring(1);
            }
            
            // prepend base URL
            designerWebBaseUrl = serverBaseUrl + "/" + designerWebBaseUrl;
        }
        return designerWebBaseUrl;
    }

    /**
     * Retrieves workflow variable values from request parameters and populate into a Map
     * @param request
     * @return
     */
    public static Map<String, String> retrieveVariableDataFromRequest(HttpServletRequest request) {
        Map<String, String> variables = new HashMap<String, String>();

        if (request != null) {
            Enumeration<String> enumeration = request.getParameterNames();
            //loop through all parameters to get the workflow variables
            while (enumeration.hasMoreElements()) {
                String paramName = enumeration.nextElement();
                if (paramName.startsWith(PREFIX_WORKFLOW_VARIABLE)) {
                    variables.put(paramName.replace(PREFIX_WORKFLOW_VARIABLE, ""), request.getParameter(paramName));
                }
            }
        }
        return variables;
    }

    /**
     * Retrieves workflow variable values from a Map and populate into another Map
     * @param parameters
     * @return
     */
    public static Map<String, String> retrieveVariableDataFromMap(Map parameters) {
        Map<String, String> variables = new HashMap<String, String>();

        if (parameters != null) {
            for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
                String paramName = (String) i.next();
                if (paramName.startsWith(PREFIX_WORKFLOW_VARIABLE)) {
                    variables.put(paramName.replace(PREFIX_WORKFLOW_VARIABLE, ""), (String) parameters.get(paramName));
                }
            }
        }
        return variables;
    }

    /**
     * Read locale from Setup
     * @return Locale code
     */
    public static String getAppLocale() {
        return LocaleContextHolder.getLocale().toString();
    }
    
    /**
     * Check it is in RTL mode
     * @return Locale code
     */
    public static boolean isRTL() {
        return "true".equalsIgnoreCase(WorkflowUtil.getSystemSetupValue("rightToLeft")) || getAppLocale().startsWith("ar");
    }
    
    /**
     * Read firstDayifWeek from locale
     * @return fdow
     */
    public static String getAppFirstDayOfWeek() {
        String fdow = "0";
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        if ("true".equalsIgnoreCase(setupManager.getSettingValue("datepickerFollowLocale"))) {
            Locale locale = LocaleContextHolder.getLocale();
            DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();

            if (firstDayOfWeek.toString().equalsIgnoreCase("Saturday")) {
                fdow = "6";
            } else if (firstDayOfWeek.toString().equalsIgnoreCase("Monday")) {
                fdow = "1";
            }
        }
        return fdow;
    }

    /**
     * Read language from Setup
     * @return Language code
     */
    public static String getAppLanguage() {
        return StringUtil.stripAllHtmlTag(LocaleContextHolder.getLocale().getLanguage());
    }
    
    /**
     * Read timezone from Setup
     * @return timezone id
     */
    public static String getAppTimezone() {
        return StringUtil.stripAllHtmlTag(LocaleContextHolder.getTimeZone().getID());
    }
    
    /**
     * Read date format from Setup
     * @return Date format
     */
    public static String getAppDateFormat() {
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");

        if ("true".equalsIgnoreCase(setupManager.getSettingValue("dateFormatFollowLocale"))) {
            Locale locale = LocaleContextHolder.getLocale();
            DateFormat dateInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
            if (dateInstance instanceof SimpleDateFormat) {
                return ((SimpleDateFormat) dateInstance).toPattern();
            }
        }
        String systemDateFormat = setupManager.getSettingValue("systemDateFormat");
        if (systemDateFormat != null && !systemDateFormat.isEmpty()) {
            return systemDateFormat;
        } else {
            return ResourceBundleUtil.getMessage("console.setting.general.default.systemDateFormat");
        }
    }

    /**
     * Convenience method to retrieve the current request context path
     * @return
     */
    public static String getRequestContextPath() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String url = (request != null) ? request.getContextPath() : "";
        return url;
    }

    /**
     * Reads a resource from a plugin
     * @param pluginName
     * @param resourceUrl
     * @return null if the resource is not found or in the case of an exception
     */
    public static String readPluginResource(String pluginName, String resourceUrl) {
        return readPluginResource(pluginName, resourceUrl, null, false, null);
    }

    /**
     * Reads a resource from a plugin. java.util.Formatter text patterns supported.
     * @param pluginName
     * @param resourceUrl
     * @param arguments
     * @param removeNewLines
     * @return null if the resource is not found or in the case of an exception
     * @see java.util.Formatter
     */
    public static String readPluginResource(String pluginName, String resourceUrl, Object[] arguments, boolean removeNewLines) {
        return readPluginResource(pluginName, resourceUrl, arguments, removeNewLines, null);
    }

    /**
     * Reads a resource from a plugin. java.util.Formatter text patterns supported.
     * @param pluginName
     * @param resourceUrl
     * @param arguments
     * @param removeNewLines
     * @param translationFileName
     * @return null if the resource is not found or in the case of an exception
     * @see java.util.Formatter
     */
    public static String readPluginResource(String pluginName, String resourceUrl, Object[] arguments, boolean removeNewLines, String translationFileName) {
        String output = null;
        if (pluginName != null && resourceUrl != null) {
            PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
            output = pluginManager.readPluginResourceAsString(pluginName, resourceUrl, arguments, removeNewLines, translationFileName);
        }
        // replace app path
        if (output != null && !output.isEmpty()) {
            String appPath = "";
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef != null) {
                appPath = "/" + appDef.getAppId() + "/" + appDef.getVersion();
            }
            output = output.replaceAll("\\[APP_PATH\\]", appPath);
        }
        return output;
    }
    
    /**
     * Used to escapes all the Hash Variables found in the content
     * @param content
     * @return 
     */
    public static String escapeHashVariable(String content) {
        content = StringUtil.decryptContent(content);
        
        // check for hash # to avoid unnecessary processing
        if (!containsHashVariable(content)) {
            return content;
        }

        //parse content
        if (content != null) {
            Pattern pattern = Pattern.compile("\\#([^#^\"^ ])*\\.([^#^\"])*\\#");
            Matcher matcher = pattern.matcher(content);
            Set<String> varList = new HashSet<String>();
            while (matcher.find()) {
                varList.add(matcher.group());
            }

            try {
                if (!varList.isEmpty()) {
                    PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
                    Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
                                
                    for (String var : varList) {
                        String tempVar = var.replaceAll("#", "");

                        for (Plugin p : pluginList) {
                            HashVariablePlugin hashVariablePlugin = (HashVariablePlugin) p;
                            if (tempVar.startsWith(hashVariablePlugin.getPrefix() + ".")) {
                                var = hashVariablePlugin.escapeHashVariable(var);
                                content = content.replaceAll(var, var.replaceAll("#", "&#35;"));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(AppUtil.class.getName(), ex, "");
            }
        }
        return content;
    }

    /**
     * Used to parses Hash Variables found in the content and replace it to the Hash
     * Variable value
     * @param content
     * @param wfAssignment
     * @param escapeFormat
     * @param replaceMap
     * @return 
     */
    public static String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap) {
        return processHashVariable(content, wfAssignment, escapeFormat, replaceMap, null);
    }
    
    /**
     * Used to parses Hash Variables found in the content and replace it to the Hash
     * Variable value
     * @param content
     * @param wfAssignment
     * @param escapeFormat
     * @param replaceMap
     * @param appDef
     * @return 
     */
    public static String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap, AppDefinition appDef) {
        return processHashVariable(content, wfAssignment, escapeFormat, replaceMap, null, true);
    }

    /**
     * Used to parses Hash Variables found in the content and replace it to the Hash
     * Variable value
     * @param content
     * @param wfAssignment
     * @param escapeFormat
     * @param replaceMap
     * @param appDef
     * @param decryptContent
     * @return 
     */
    public static String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap, AppDefinition appDef, boolean decryptContent) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        if (decryptContent) {
            content = StringUtil.decryptContent(content);
        }
        AppDefinition originalAppDef = AppUtil.getCurrentAppDefinition();
        
        try {
            if (!containsHashVariable(content)) {
                return content;
            }
            
            if (appDef != null) {
                AppUtil.setCurrentAppDefinition(appDef);
            }
            
            if (appDef == null && originalAppDef == null && wfAssignment != null) {
                //retrieve appDef based on wf assignment
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                if (wfAssignment.getProcessDefId() != null) {
                    appDef = appService.getAppDefinitionWithProcessDefId(wfAssignment.getProcessDefId());
                } else {
                    appDef = appService.getAppDefinitionForWorkflowProcess(wfAssignment.getProcessId());
                }
            }
            
            //parse content
            if (content != null) {
                Pattern pattern = Pattern.compile("\\#([^#\" ])*\\.([^#\"])*\\#");
                Matcher matcher = pattern.matcher(content);
                Set<String> varList = new HashSet<String>();
                while (matcher.find()) {
                    varList.add(matcher.group());
                }

                try {
                    if (!varList.isEmpty()) {
                        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
                        Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
                        Map<String, String> pluginPrefixMap = new HashMap<String, String>();
                        for (Plugin p : pluginList) {
                            HashVariablePlugin hashVariablePlugin = (HashVariablePlugin) p;
                            pluginPrefixMap.put(hashVariablePlugin.getPrefix(), hashVariablePlugin.getClassName());
                        }
                        
                        Map <String, HashVariablePlugin> hashVariablePluginCache = new HashMap<String, HashVariablePlugin>();

                        for (String var : varList) {
                            String tempVar = var.replaceAll("#", "");
                            String prefix = tempVar.substring(0, tempVar.indexOf("."));

                            String hashVariableClass = pluginPrefixMap.get(prefix);
                            if (hashVariableClass != null) {
                                tempVar = tempVar.replaceFirst(StringUtil.escapeRegex(prefix + "."), "");

                                HashVariablePlugin cachedPlugin = hashVariablePluginCache.get(hashVariableClass);
                                if (cachedPlugin == null) {
                                    cachedPlugin = (HashVariablePlugin) pluginManager.getPlugin(hashVariableClass);
                                    //get default plugin properties

                                    if (appDef == null) {
                                        appDef = AppUtil.getCurrentAppDefinition();
                                    }
                                    PluginDefaultProperties pluginDefaultProperties = AppPluginUtil.getPluginDefaultProperties(cachedPlugin.getClassName(), appDef);
                                    if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
                                        cachedPlugin.setProperties(PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperties.getPluginProperties()));
                                    }

                                    //put appDef & wfAssignment to properties
                                    cachedPlugin.setProperty("appDefinition", appDef);
                                    cachedPlugin.setProperty("workflowAssignment", wfAssignment);
                                    hashVariablePluginCache.put(hashVariableClass, cachedPlugin);
                                }

                                String nestedHashVar = tempVar;

                                //process nested hash
                                while (nestedHashVar.contains("{") && nestedHashVar.contains("}")) {
                                    boolean hasMatch = false;
                                    Pattern nestedPattern = Pattern.compile("\\{([^\\{\\}])*\\}");
                                    Matcher nestedMatcher = nestedPattern.matcher(nestedHashVar);
                                    while (nestedMatcher.find()) {
                                        hasMatch = true;
                                        
                                        String nestedHash = nestedMatcher.group();
                                        String nestedHashString = nestedHash.replace("{", "#");
                                        nestedHashString = nestedHashString.replace("}", "#");

                                        String processedNestedHashValue = processHashVariable(nestedHashString, wfAssignment, escapeFormat, replaceMap, appDef);

                                        //if being process
                                        if (!nestedHashString.equals(processedNestedHashValue)) {
                                            tempVar = tempVar.replaceAll(StringUtil.escapeRegex(nestedHash), StringUtil.escapeRegex(processedNestedHashValue));
                                        }

                                        //remove nested hash 
                                        nestedHashVar = nestedHashVar.replaceAll(StringUtil.escapeRegex(nestedHash), StringUtil.escapeRegex(processedNestedHashValue));
                                    }
                                    if (!hasMatch) {
                                        //no nested hash syntax found
                                        break;
                                    }
                                }
                                
                                if (escapeFormat != null) {
                                    tempVar = StringUtil.unescapeString(tempVar, escapeFormat, null);
                                }

                                //get result from plugin
                                try {
                                    String removeFormatVar = tempVar;
                                    String hashFormat = "";
                                    if (removeFormatVar.contains("?")) {
                                        hashFormat = tempVar.substring(tempVar.lastIndexOf("?")+1);
                                        if (!hashFormat.contains("}") && isHashEscapeFormat(hashFormat)) {
                                            removeFormatVar = tempVar.substring(0, tempVar.lastIndexOf("?"));
                                        }
                                    }
                                    
                                    String value = cachedPlugin.processHashVariable(removeFormatVar);

                                    if (value != null) {
                                        //escape based on hash variable
                                        if (hashFormat != null & !hashFormat.isEmpty()) {
                                            value = StringUtil.escapeString(value, hashFormat, null);
                                        }

                                        if (requiredXssPrevention(hashFormat)){
                                            // clean to prevent XSS
                                            value = StringUtil.stripHtmlRelaxed(value);
                                        }
                                        
                                        //escape based on api call
                                        value = StringUtil.escapeString(value, escapeFormat, replaceMap);
        
                                        //escape special char in HashVariable
                                        var = cachedPlugin.escapeHashVariable(var);
                                        value = cachedPlugin.escapeHashVariableValue(value);
                                        
                                        //escape regex for replaceAll
                                        if (!StringUtil.TYPE_REGEX.equals(escapeFormat)) {
                                            value = StringUtil.escapeRegex(value);
                                        }
                                        
                                        content = content.replaceAll(var, value);
                                    }
                                } catch (Exception e) {}
                            }
                        }
                    }
                } catch (Exception ex) {
                    LogUtil.error(AppUtil.class.getName(), ex, "");
                }
            }
        } finally {
            AppUtil.setCurrentAppDefinition(originalAppDef);
        }
        return content;
    }
    
    public static boolean hasUnparsedNestedHashVariable(String content) {
        if (content != null && !content.isEmpty()) {
            if (content.contains("{") && content.contains("}")) {
                Pattern nestedPattern = Pattern.compile("\\{[^\\}]+\\.[^\\}]+\\}");
                Matcher nestedMatcher = nestedPattern.matcher(content);
                Set<String> foundPrefixes = new HashSet<String>();
                while (nestedMatcher.find()) {
                    String var = nestedMatcher.group();
                    String prefix = var.substring(1, var.indexOf("."));
                    foundPrefixes.add(prefix);
                }
                if (!foundPrefixes.isEmpty()) {
                    PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
                    Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
                    for (Plugin p : pluginList) {
                        HashVariablePlugin hashVariablePlugin = (HashVariablePlugin) p;
                        if (foundPrefixes.contains(hashVariablePlugin.getPrefix())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    protected static boolean isHashEscapeFormat(String hashFormat) {
        boolean isValid = true;
        String[] formats = hashFormat.split(";");
        for (String format : formats) {
            if (!(format.equals(StringUtil.TYPE_HTML)
                    || format.equals(StringUtil.TYPE_JAVA)
                    || format.equals(StringUtil.TYPE_JAVASCIPT)
                    || format.equals(StringUtil.TYPE_JSON)
                    || format.equals(StringUtil.TYPE_NL2BR)
                    || format.equals(StringUtil.TYPE_IMG2BASE64)
                    || format.equals(StringUtil.TYPE_REGEX)
                    || format.equals(StringUtil.TYPE_SQL)
                    || format.equals(StringUtil.TYPE_URL)
                    || format.equals(StringUtil.TYPE_XML)
                    || format.startsWith(StringUtil.TYPE_SEPARATOR)
                    || format.equals(StringUtil.TYPE_EXP)
                    || format.startsWith(StringUtil.TYPE_DECIMAL)
                    || format.equals(HASH_NO_ESCAPE))) {
                isValid = false;
            }
            //check for 1 enough
            break;
        }
        return isValid;
    }
    
    protected static boolean requiredXssPrevention(String hashFormat) {
        boolean required = true;
        String[] formats = hashFormat.split(";");
        for (String format : formats) {
            if (format.equals(StringUtil.TYPE_HTML)
                    || format.equals(StringUtil.TYPE_JAVA)
                    || format.equals(StringUtil.TYPE_JAVASCIPT)
                    || format.equals(StringUtil.TYPE_JSON)
                    || format.equals(StringUtil.TYPE_REGEX)
                    || format.equals(StringUtil.TYPE_SQL)
                    || format.equals(StringUtil.TYPE_URL)
                    || format.equals(StringUtil.TYPE_XML)
                    || format.equals(StringUtil.TYPE_EXP)
                    || format.equals(HASH_NO_ESCAPE)) {
                required = false;
                break;
            }
        }
        return required;
    }

    /**
     * Used to checks a content may contains Hash Variable or not
     * @param content
     * @return 
     */
    public static boolean containsHashVariable(String content) {
        boolean result = (content != null && content.indexOf("#") >= 0);
        return result;
    }

    /**
     * Used to retrieves email list based on Participant Id and To email String.
     * Username will auto convert to email address belongs to the user.
     * @param toParticipantId
     * @param toSpecific
     * @param wfAssignment
     * @param appDef
     * @return 
     */
    public static Collection<String> getEmailList(String toParticipantId, String toSpecific, WorkflowAssignment wfAssignment, AppDefinition appDef) {
        Collection<String> addresses = new HashSet<String>();
        Collection<String> users = new HashSet<String>();

        if (toParticipantId != null && !toParticipantId.isEmpty() && wfAssignment != null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            WorkflowProcess process = workflowManager.getProcess(wfAssignment.getProcessDefId());
            toParticipantId = toParticipantId.replace(";", ",");
            String pIds[] = toParticipantId.split(",");
            for (String pId : pIds) {
                pId = pId.trim();
                if (pId.length() == 0) {
                    continue;
                }

                Collection<String> userList = null;
                userList = WorkflowUtil.getAssignmentUsers(process.getPackageId(), wfAssignment.getProcessDefId(), wfAssignment.getProcessId(), wfAssignment.getProcessVersion(), wfAssignment.getActivityId(), "", pId.trim());

                if (userList != null && userList.size() > 0) {
                    users.addAll(userList);
                }
            }
            
            //send to replacement user
            if (!users.isEmpty()) {
                Collection<String> userList = new HashSet<String>();
                String args[] = wfAssignment.getProcessDefId().split("#");
                
                for (String u : users) {
                    UserReplacementDao urDao = (UserReplacementDao) AppUtil.getApplicationContext().getBean("userReplacementDao");
                    Collection<UserReplacement> replaces = urDao.getUserTodayReplacedBy(u, args[0], args[2]);
                    if (replaces != null && !replaces.isEmpty()) {
                        for (UserReplacement ur : replaces) {
                            userList.add(ur.getReplacementUser());
                        }
                    }
                }
                
                if (userList.size() > 0) {
                    users.addAll(userList);
                }
            }
        }

        if (toSpecific != null && toSpecific.trim().length() != 0) {
            toSpecific = AppUtil.processHashVariable(toSpecific, wfAssignment, null, null, appDef);
            toSpecific = toSpecific.replace(";", ","); // add support for MS-style semi-colon (;) as a delimiter
            String emailList[] = toSpecific.split(",");
            for (String email : emailList) {
                email = email.trim();
                if (email.length() == 0) {
                    continue;
                }

                //to support retrieve email by putting username
                if (!email.contains("@")) {
                    users.add(email);
                } else {
                    addresses.add(email);
                }
            }
        }

        if (!users.isEmpty()) {
            DirectoryManager directoryManager = (DirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
            for (String username : users) {
                try {
                    User user = directoryManager.getUserByUsername(username);
                    if (user != null) {
                        String userEmail = user.getEmail().replace(";", ",");
                        String userEmails[] = userEmail.split(",");
                        for (String email : userEmails) {
                            email = email.trim();
                            if (email.length() == 0) {
                                continue;
                            }
                            addresses.add(email);
                        }
                    }
                } catch (Exception e) {}
            }
        }

        return addresses;
    }

    /**
     * Checks system settings whether front-end quick edit is enabled.
     * @return 
     */
    public static boolean isQuickEditEnabled() {
        String settingValue = null;
        boolean isAdmin = false;
        
        // lookup cache in request
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            settingValue = (String)request.getAttribute("disableAdminBar");
            isAdmin = "true".equals(request.getAttribute("isAdmin"));
        }
        if (settingValue == null) {
            // get from SetupManager
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            settingValue = setupManager.getSettingValue("disableAdminBar");
            if (settingValue == null) {
                settingValue = "false";
            }
            isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);
            if (request != null) {
                // cache value in request
                request.setAttribute("disableAdminBar", settingValue);
                request.setAttribute("isAdmin", Boolean.toString(isAdmin));
            }
        }
        boolean enabled = !"true".equals(settingValue) && isAdmin;
        return enabled;
    }
    
    /**
     * Returns the current system version.
     * @since 3.2
     * @return 
     */
    public static String getSystemVersion() {
        String version = ResourceBundleUtil.getMessage("console.footer.label.revision");
        return version;
    }

    /**
     * Used to set an once time HTML which will append to the page when next page load.
     * @param value 
     */
    public static void setSystemAlert(String value) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            HttpSession session = request.getSession(true);
            if (session != null) {
                String[] values = (String[]) session.getAttribute(UI_SESSION_KEY);
                Collection<String> sessionValues = new ArrayList<String>();
                if (values != null && values.length > 0) {
                    sessionValues.addAll(Arrays.asList(values));
                }
                sessionValues.add(value);
                session.setAttribute(UI_SESSION_KEY, sessionValues.toArray(new String[0]));
            }
        }
    }

    /**
     * Used by system to retrieves the once time HTML to append on the page load.
     * The once time HTML will be remove after this method call.
     * @return 
     */
    public static String getSystemAlert() {
        String script = "";
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            HttpSession session = request.getSession(true);
            if (session != null) {
                String[] values = (String[]) session.getAttribute(UI_SESSION_KEY);
                if (values != null && values.length > 0) {
                    session.removeAttribute(UI_SESSION_KEY);

                    for (String v : values) {
                        script += v;
                    }
                }
            }
        }
        return script;
    }
    
    /**
     * Get the userview theme css depends on the userview parameter in request
     * @return 
     */
    public static String getUserviewThemeCss() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && 
                ((request.getParameterValues("__a_") != null && request.getParameterValues("__a_").length > 0 && 
                request.getParameterValues("__u_") != null && request.getParameterValues("__u_").length > 0) || 
                (request.getRequestURI().contains("/jsp/userview/popupTemplate.jsp") && //is userview popup & referer is userview
                request.getHeader("referer") != null && (request.getHeader("referer").contains("/web/userview/") || 
                request.getHeader("referer").contains("/web/embed/userview/"))))) { 
            
            AppDefinition oriAppDef = AppUtil.getCurrentAppDefinition();
            
            try {
                String appId = "";
                String uId = "";
                
                if (request.getParameterValues("__a_") != null && request.getParameterValues("__a_").length > 0 && 
                    request.getParameterValues("__u_") != null && request.getParameterValues("__u_").length > 0) {
                    appId = request.getParameterValues("__a_")[0];
                    uId = request.getParameterValues("__u_")[0];
                } else {
                    String[] temp = request.getHeader("referer").substring(request.getHeader("referer").indexOf("/userview/") + 10).split("/");
                    if (temp.length > 2) {
                        appId = temp[0];
                        uId = temp[1];
                    }
                }
                appId = SecurityUtil.validateStringInput(appId);
                uId = SecurityUtil.validateStringInput(uId);

                if (!appId.isEmpty() && !uId.isEmpty()) {
                    UserviewService userviewService = (UserviewService) appContext.getBean("userviewService");
                    UserviewTheme theme = userviewService.getUserviewTheme(appId, uId);

                    if (theme != null) {
                        if (theme instanceof UserviewV5Theme) {
                            UserviewV5Theme v5Theme = (UserviewV5Theme) theme;
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put("params", request.getParameterMap());
                            data.put("context_path", request.getContextPath());
                            data.put("build_number", ResourceBundleUtil.getMessage("build.number"));
                            data.put("right_to_left", isRTL());
                            String locale = AppUtil.getAppLocale();
                            data.put("locale", locale);
                            data.put("is_popup_view", true);

                            String jsCssLib = v5Theme.getJsCssLib(data);
                            String css = v5Theme.getCss(data);
                            String js = v5Theme.getJs(data);

                            String html = jsCssLib;

                            if (js != null && !js.isEmpty()) {
                                html += "<script type=\"text/javascript\">\n" + js + "\n</script>";
                            }

                            if (css != null && !css.isEmpty()) {
                                html += "<style type=\"text/css\">\n" + css + "\n</style>";
                            }
                            
                            if (!(theme instanceof AjaxUniversalTheme)) {
                                html = "<link rel=\"stylesheet\" type=\"text/css\" href=\""+data.get("context_path").toString()+"/css/userview_popup.css?build="+data.get("build_number").toString()+"\">" + html;
                            }

                            return html;
                        } else if (theme.getCss() != null) {
                            return "<link href=\""+request.getContextPath()+"/wro/userview.min.css?build="+ResourceBundleUtil.getMessage("build.number")+"\" rel=\"stylesheet\" type=\"text/css\" />\n<style type=\"text/css\">\n" + theme.getCss() + "\n</style>";
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(AppUtil.class.getName(), e, "getUserviewThemeCss Error!");
            } finally {
                AppUtil.setCurrentAppDefinition(oriAppDef);
            }
        }
        
        return "";
    }
    
    /**
     * Convenient method used to write JSON Object to the response
     * @param writer
     * @param jsonObject
     * @param callback
     * @throws IOException
     * @throws JSONException 
     */
    public static void writeJson(Writer writer, JSONObject jsonObject, String callback) throws IOException, JSONException {
        if (callback != null && callback.trim().length() > 0) {
            writer.write(HtmlUtils.htmlEscape(callback) + "(");
        }
        jsonObject.write(writer);
        if (callback != null && callback.trim().length() > 0) {
            writer.write(")");
        }
    }

    /**
     * Convenient method used to write JSON Array to the response
     * @param writer
     * @param jsonArray
     * @param callback
     * @throws IOException
     * @throws JSONException 
     */
    public static void writeJson(Writer writer, JSONArray jsonArray, String callback) throws IOException, JSONException {
        if (callback != null && callback.trim().length() > 0) {
            writer.write(HtmlUtils.htmlEscape(callback) + "(");
        }
        jsonArray.write(writer);
        if (callback != null && callback.trim().length() > 0) {
            writer.write(")");
        }
    }
    
    /**
     * Method called at the start of a HTTP request
     */
    public static void initRequest() {
        // clear current app in thread
        AppUtil.resetAppDefinition();
        currentAssignment.set(null);
        currentAssignmentRequiredReset.set(null);
    }

    /**
     * Method called at the end of a HTTP request
     */
    public static void clearRequest() {
        AppUtil.clearAppMessages();
        currentAppDefinition.remove();
        resetAppDefinition.remove();
        processAppDefinition.remove();
        currentAssignment.remove();
        currentAssignmentRequiredReset.remove();
    }

    /**
     * Returns all the i18n messages for an app for the current locale in a Map
     *
     * @param appDef
     * @return Map contains messageKey=message
     */
    public static Map<String, String> getAppMessages(AppDefinition appDef) {
        Map<String, String> messageMap = new HashMap<String, String>();
        String currentLocale = AppUtil.getAppLocale();
        if (currentLocale == null) {
            currentLocale = "en_US";
        }
        if (appDef != null) {
            MessageDao messageDao = (MessageDao) getApplicationContext().getBean("messageDao");
            Map<String, Message> messageList = messageDao.getCachedMessageList(currentLocale, appDef);
            for (Message message : messageList.values()) {
                String key = message.getMessageKey();
                String label = message.getMessage();
                messageMap.put(key, label);
            }
        }
        return messageMap;
    }

    /**
     * Replace a label with an app-specific message
     *
     * @param label
     * @return
     */
    public static String replaceAppMessage(String label) {
        String result = label;
        Map<String, String> appMessages = getAppMessageFromStore();
        if (appMessages != null) {
            String text = StringUtil.stripAllHtmlTag(label);
            String messageKey = text; //text.replace(" ", "_");
            if (appMessages.containsKey(messageKey)) {
                String translated = appMessages.get(messageKey);
                result = result.replace(text, translated);
            }
        }
        return result;
    }
    
    // static pattern so that cpu intensive compile is only done once
    static Pattern appMessagePattern = Pattern.compile("((((['\"])label\\4\\s*:\\s*\\4)((?:\\\\\\4|(?:(?!\\4).))+)\\4)|(#i18n\\.([^#]+)#))");    
    
    // least recently used (LRU) cache to hold final content containing replaced messages
    static Map<String, String> appMessageCache = Collections.synchronizedMap(new LRUMap<>(200));
    
    /**
     * Replace all app-specific message in content
     *
     * @param label
     * @return
     */
    public static String replaceAppMessages(String content, String escapeType) {
        Map<String, String> appMessages = getAppMessageFromStore();
        if (appMessages != null) {
            // lookup from LRU cache
            String appMessageContent = appMessages.toString() + "::" + content + "::" + escapeType;
            String cacheKey = StringUtil.md5Base16Utf8(appMessageContent); // hash to minimize memory usage
            String cachedContent = appMessageCache.get(cacheKey);
            if (cachedContent != null) {
                return cachedContent;
            }
            
            Matcher matcher = appMessagePattern.matcher(content);
            String key = "", match = "";
            while (matcher.find()) {
                match = matcher.group();
                key = matcher.group(5);
                if (match.startsWith("#i18n.")) {
                    key = matcher.group(7);
                }
                if (escapeType != null) {
                    key = StringUtil.unescapeString(key, escapeType, null);
                }
                if (appMessages.containsKey(key)) {
                    String translated = appMessages.get(key);
                    if (escapeType != null) {
                        translated = StringUtil.escapeString(translated, escapeType, null);
                    }
                    if (!match.startsWith("#i18n.")) {
                        content = content.replaceAll(StringUtil.escapeRegex(match) , StringUtil.escapeRegex(matcher.group(3) + translated + matcher.group(4)));
                    } else {
                        content = content.replaceAll(StringUtil.escapeRegex(match) , StringUtil.escapeRegex(translated));
                    }
                }
            }
            // save into cache
            appMessageCache.put(cacheKey, content);
        }
        return content;
    }

    private static final ThreadLocal threadLocalAppMessages = new ThreadLocal();
    
    public static Map<String, String> getAppMessageFromStore() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            if (!AppUtil.isAppMessagesSet()) {
                AppUtil.initAppMessages(appDef);
            }
            
            Map<String, Map<String, String>> appMessageStore = (Map<String, Map<String, String>>) threadLocalAppMessages.get();
            if (appMessageStore != null && appMessageStore.containsKey(appDef.getAppId()+":"+appDef.getVersion())) {
                return appMessageStore.get(appDef.getAppId()+":"+appDef.getVersion());
            }
        }
        return null;
    }
    
    public static void initAppMessages(AppDefinition appDef) {
        if (appDef == null) {
            return;
        }
        
        Map<String, Map<String, String>> appMessageStore = (Map<String, Map<String, String>>) threadLocalAppMessages.get();
        if (appMessageStore == null) {
            appMessageStore = new HashMap<String, Map<String, String>>();
        }
        
        Map<String, String> appMessages = AppUtil.getAppMessages(appDef);
        appMessageStore.put(appDef.getAppId()+":"+appDef.getVersion(), appMessages);
                
        threadLocalAppMessages.set(appMessageStore);
    }

    public static boolean isAppMessagesSet() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        
        Map<String, Map<String, String>> appMessagesStore = (Map<String, Map<String, String>>) threadLocalAppMessages.get();
        
        return appDef != null && appMessagesStore != null && appMessagesStore.containsKey(appDef.getAppId()+":"+appDef.getVersion());
    }

    public static void clearAppMessages() {
        threadLocalAppMessages.remove();
    }
    
    public static HtmlEmail createEmail(String host, String port, String security, String username, String password, String form) throws EmailException {
        return createEmail(host, port, security, username, password, form, null, null, null);
    }
    
    public static HtmlEmail createEmail(String host, String port, String security, String username, String password, String form, String p12, String storepass, String alias) throws EmailException {
        //use system setting if host is empty
        if (host == null || host.isEmpty()) {
            SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
            host = setupManager.getSettingValue("smtpHost");
            port = setupManager.getSettingValue("smtpPort");
            security = setupManager.getSettingValue("smtpSecurity");
            username = setupManager.getSettingValue("smtpUsername");
            password = setupManager.getSettingValue("smtpPassword");
            form = setupManager.getSettingValue("smtpEmail");
            
            if (p12 == null || p12.isEmpty()) {
                p12 = setupManager.getSettingValue("smtpP12");
                storepass = SecurityUtil.decrypt(setupManager.getSettingValue("smtpStorepass"));
                alias = setupManager.getSettingValue("smtpIssuerAlias");
            }
        }
        
        if (host == null || host.isEmpty() || form == null || form.isEmpty()) {
            LogUtil.info(AppUtil.class.getName(), "SMTP is not configured!");
            return null;
        }
        
        HtmlEmail email = null;
        if (p12 != null && !p12.isEmpty() && storepass != null && !storepass.isEmpty() && alias != null && !alias.isEmpty()) {
            email = ((HtmlEmail) new DigitalSignedHtmlEmail(p12, storepass, alias, form));
        } else {
            email = new HtmlEmail();
        }
        email.setHostName(host);
        if (port != null && port.length() != 0) {
            email.setSmtpPort(Integer.parseInt(port));
        }
        if (username != null && !username.isEmpty()) {
            if (password != null) {
                password = SecurityUtil.decrypt(password);
            }
            email.setAuthentication(username, password);
        }
        if(security!= null){
            if(security.equalsIgnoreCase("SSL") ){
                email.setSSLOnConnect(true);
                email.setSSLCheckServerIdentity(true);
                if (port != null && port.length() != 0) {
                    email.setSslSmtpPort(port);
                }
            }else if(security.equalsIgnoreCase("TLS")){
                email.setStartTLSEnabled(true);
                email.setSSLCheckServerIdentity(true);
            }
        }
        email.setFrom(StringUtil.encodeEmail(form));
        
        return email;
    }
    
    public static void emailAttachment(Map properties, WorkflowAssignment wfAssignment, AppDefinition appDef, final HtmlEmail email) {
        //handle file attachment
        System.setProperty("mail.mime.encodeparameters", "false");
        String formDefId = (String) properties.get("formDefId");
        Object[] fields = null;
        if (properties.get("fields") instanceof Object[]){
            fields = (Object[]) properties.get("fields");
        }
        if (formDefId != null && !formDefId.isEmpty() && fields != null && fields.length > 0) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");

            FormData formData = new FormData();
            String primaryKey = appService.getOriginProcessId(wfAssignment.getProcessId());
            formData.setPrimaryKeyValue(primaryKey);
            Form loadForm = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formDefId, null, null, null, formData, null, null);

            Set<String> inlineImages = new HashSet<String>();
            for (Object o : fields) {
                Map mapping = (HashMap) o;
                String fieldId = mapping.get("field").toString();
                String embed = (String) mapping.get("embed");

                try {
                    Element el = FormUtil.findElement(fieldId, loadForm, formData);
                    String value = FormUtil.getElementPropertyValue(el, formData);
                    if (value.contains("/web/client/app/") && value.contains("/form/download/")) {
                        value = retrieveFileNames(value, appDef.getAppId(), formDefId, primaryKey);
                    }
                    if (value != null && !value.isEmpty()) {
                        String values[] = value.split(";");
                        for (String v : values) {
                            if (!v.isEmpty()) {
                                File file = FileUtil.getFile(v, loadForm, primaryKey);
                                if (file != null && file.exists()) {
                                    FileDataSource fds = new FileDataSource(file);
                                    String name = MimeUtility.encodeText(file.getName(), "UTF-8", null);
                                    if (embed != null && "true".equalsIgnoreCase(embed)) {
                                        email.embed(fds, name, name);
                                        inlineImages.add(file.getName());
                                    } else {
                                        email.attach(fds, name, "");
                                    }
                                }
                            }
                        }
                    }
                } catch(Exception e){
                    LogUtil.info(EmailTool.class.getName(), "Attached file fail from field \"" + fieldId + "\" in form \"" + formDefId + "\"");
                }
            }
            
            if (!inlineImages.isEmpty()) {
                try {
                    Field htmlField = HtmlEmail.class.getDeclaredField("html");
                    htmlField.setAccessible(true);
                    String html = (String) htmlField.get(email);
                    if (html != null && !html.isEmpty()) {
                        html = replaceInlineFormImageToCid(html, appDef.getAppId(), formDefId, primaryKey, inlineImages);
                        email.setHtmlMsg(html);
                    }
                } catch (Exception e) {
                    LogUtil.warn(AppUtil.class.getName(), "Not able to replace image in HTML to embed attachment content id.");
                }
            }
        }

        Object[] files = null;
        if (properties.get("files") instanceof Object[]){
            files = (Object[]) properties.get("files");
        }
        if (files != null && files.length > 0) {
            for (Object o : files) {
                Map mapping = (HashMap) o;
                String path = mapping.get("path").toString();
                String fileName = mapping.get("fileName").toString();
                String type = mapping.get("type").toString();
                String embed = (String) mapping.get("embed");

                try {
                    String name = MimeUtility.encodeText(fileName, "UTF-8", null);
                    if (embed != null && "true".equalsIgnoreCase(embed)) {
                        if ("system".equals(type)) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                FileDataSource fds = new FileDataSource(file);
                                email.embed(fds, name, name);
                            }
                        } else {
                            URL u = new URL(path);
                            email.embed(new CustomURLDataSource(u), name, name);
                        }
                    } else {
                        if ("system".equals(type)) {
                            EmailAttachment attachment = new EmailAttachment();
                            attachment.setPath(path);
                            attachment.setName(name);
                            email.attach(attachment);
                        } else {
                            URL u = new URL(path);
                            email.attach(new CustomURLDataSource(u), name, "");
                        }
                    }
                } catch(Exception e){
                    LogUtil.error(AppUtil.class.getName(), e, "File attachment failed from path \"" + path + "\"");
                }
            }
        }
        attachIcal(email, properties, wfAssignment, appDef);
    }
    
    protected static String retrieveFileNames(String content, String appId, String formId, String primaryKey) {
        Set<String> values = new HashSet<String>();
        
        Pattern pattern = Pattern.compile("<img[^>]*src=\"[^\"]*/web/client/app/"+StringUtil.escapeRegex(appId)+"/form/download/"+StringUtil.escapeRegex(formId)+"/"+StringUtil.escapeRegex(primaryKey)+"/([^\"]*)\\.\"[^>]*>");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String fileName = matcher.group(1);
            values.add(fileName);
        }
        
        return String.join(";", values);
    }
    
    protected static String replaceInlineFormImageToCid(String html, String appId, String formId, String primaryKey, Set<String> inlineImages) {
        for (String name : inlineImages) {
            if (!(html.contains("/web/client/app/") && html.contains("/form/download/"))) {
                break;
            }
            html = html.replaceAll("src=\"[^\"]*/web/client/app/"+StringUtil.escapeRegex(appId)+"/form/download/"+StringUtil.escapeRegex(formId)+"/"+StringUtil.escapeRegex(primaryKey)+"/"+StringUtil.escapeRegex(name)+"\\.\"", StringUtil.escapeRegex("src=\"cid:"+StringUtil.escapeString(name, StringUtil.TYPE_URL, null)+"\""));
        }
        return html;
    }
    
    protected static void attachIcal(final HtmlEmail email, Map properties, WorkflowAssignment wfAssignment, AppDefinition appDef) {
        try {
            if ("true".equalsIgnoreCase((String) properties.get("icsAttachement"))) {
                System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
                
                Calendar calendar = new Calendar();
                calendar.getProperties().add(Version.VERSION_2_0);
                calendar.getProperties().add(new ProdId("-//Joget DX//iCal4j 1.0//EN"));
                calendar.getProperties().add(CalScale.GREGORIAN);
                calendar.getProperties().add(Method.REQUEST); 
                
                String eventName = (String) properties.get("icsEventName");
                if (eventName.isEmpty()) {
                    eventName = email.getSubject();
                }
                
                String startDateTime = AppUtil.processHashVariable((String) properties.get("icsDateStart"), wfAssignment, null, null, appDef);
                String endDateTime = AppUtil.processHashVariable((String) properties.get("icsDateEnd"), wfAssignment, null, null, appDef);
                String dateFormat = AppUtil.processHashVariable((String) properties.get("icsDateFormat"), wfAssignment, null, null, appDef);
                String timezoneString = AppUtil.processHashVariable((String) properties.get("icsTimezone"), wfAssignment, null, null, appDef);
                SimpleDateFormat sdFormat =  new SimpleDateFormat(dateFormat);
                
                boolean isAllDay = ("true".equalsIgnoreCase((String) properties.get("icsAllDay"))) || isFullDayTimeframe(startDateTime, endDateTime);
                
                //ignore timezone when it is fullday event
                net.fortuna.ical4j.model.TimeZone timezone = null;
                if (!isAllDay) {
                    String gmt = WorkflowUtil.getSystemSetupValue("systemTimeZone");
                    if (gmt != null && !gmt.isEmpty() && !isAllDay) {
                        TimeZone timeZone = TimeZone.getTimeZone(TimeZoneUtil.getTimeZoneByGMT(gmt));
                        sdFormat.setTimeZone(timeZone);
                    }
                    
                    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
                    try {
                        if (!timezoneString.isEmpty()) {
                            timezone = registry.getTimeZone(timezoneString);
                        }
                    } catch (Exception et) {}

                    try {
                        if (timezone == null) {
                            timezone = registry.getTimeZone(TimeZoneUtil.getServerTimeZoneID());
                        }
                    } catch (Exception et) {}
                }
                
                java.util.Calendar startDate = new GregorianCalendar();
                if (timezone != null) {
                    startDate.setTimeZone(timezone);
                }
                startDate.setTime(sdFormat.parse(startDateTime));
                DateTime start = new DateTime(startDate.getTime());
                
                java.util.Calendar endDate = null;
                if (!endDateTime.isEmpty()) {
                    endDate = new GregorianCalendar();
                    if (timezone != null) {
                        endDate.setTimeZone(timezone);
                    }
                    endDate.setTime(sdFormat.parse(endDateTime));
                    
                    if (isAllDay) {
                        if (endDate.get(java.util.Calendar.HOUR_OF_DAY) == 0 && 
                            endDate.get(java.util.Calendar.MINUTE) == 0) {
                            //minus 1 day
                            endDate.add(java.util.Calendar.DATE, -1);
                        }
                        //if start date & end date is same day
                        if (startDate.get(java.util.Calendar.DATE) == endDate.get(java.util.Calendar.DATE) && 
                            startDate.get(java.util.Calendar.MONTH) == endDate.get(java.util.Calendar.MONTH)&& 
                            startDate.get(java.util.Calendar.YEAR) == endDate.get(java.util.Calendar.YEAR)) {
                            endDate = null;
                        }
                    }
                }
                
                VEvent event;
                
                if (endDate != null) {
                    event = new VEvent();
                    event.getProperties().add(new Summary(eventName));
                    
                    if (isAllDay) {
                        event.getProperties().add(new DtStart(new net.fortuna.ical4j.model.Date(startDate.getTime())));
                        event.getProperties().add(new DtEnd(new net.fortuna.ical4j.model.Date(endDate.getTime())));
                    } else {
                        DateTime end = new DateTime(endDate.getTime());
                
                        event.getProperties().add(new DtStart(start.toString(),timezone));
                        event.getProperties().add(new DtEnd(end.toString(),timezone));
                    }
                } else {
                    if (isAllDay) {
                        event = new VEvent(new net.fortuna.ical4j.model.Date(startDate.getTime()), eventName);
                    } else {
                        event = new VEvent(start, eventName);
                    }
                }
                
                UidGenerator ug = new FixedUidGenerator("joget-workflow");
                event.getProperties().add(ug.generateUid());
                
                String eventDesc = (String) properties.get("icsEventDesc");
                if (!eventDesc.isEmpty()) {
                    event.getProperties().add(new Description(eventDesc));
                }
                
                if (timezone != null) {
                    VTimeZone tz = timezone.getVTimeZone();
                    calendar.getComponents().add(tz);
                    event.getProperties().add(tz.getTimeZoneId());
                }
                
                String icsLocation = AppUtil.processHashVariable((String) properties.get("icsLocation"), wfAssignment, null, null, appDef);
                if (icsLocation != null && !icsLocation.isEmpty()) {
                    event.getProperties().add(new Location(icsLocation));
                }
                
                String icsOrganizerEmail = AppUtil.processHashVariable((String) properties.get("icsOrganizerEmail"), wfAssignment, null, null, appDef);
                if (icsOrganizerEmail != null && !icsOrganizerEmail.isEmpty()) {
                    event.getProperties().add(new Organizer("MAILTO:"+icsOrganizerEmail));
                } else {
                    event.getProperties().add(new Organizer("MAILTO:"+email.getFromAddress().getAddress()));
                }
                
                Object[] attendees = null;
                if (properties.get("icsAttendees") instanceof Object[]){
                    attendees = (Object[]) properties.get("icsAttendees");
                }
                if (attendees != null && attendees.length > 0) {
                    for (Object o : attendees) {
                        Map mapping = (HashMap) o;
                        String name = AppUtil.processHashVariable(mapping.get("name").toString(), wfAssignment, null, null, appDef);
                        String mailto = AppUtil.processHashVariable(mapping.get("email").toString(), wfAssignment, null, null, appDef);
                        String required = mapping.get("required").toString();

                        try {
                            Attendee att = new Attendee(URI.create("mailto:"+mailto));
                            if ("true".equals(required)) {
                                att.getParameters().add(Role.REQ_PARTICIPANT);
                            } else {
                                att.getParameters().add(Role.OPT_PARTICIPANT);
                            }
                            att.getParameters().add(new Cn(name));
                            event.getProperties().add(att);
                        } catch(Exception ex){
                            LogUtil.error(AppUtil.class.getName(), ex, "");
                        }
                    }
                }
                
                calendar.getComponents().add(event);
                
                email.attach(new ByteArrayDataSource(calendar.toString(), "text/calendar;charset=UTF-8;ENCODING=8BIT;method=REQUEST"), MimeUtility.encodeText("invite.ics"), "");
            }
        } catch (Exception e) {
            LogUtil.error(AppUtil.class.getName(), e, null);
        }
    }
    
    public static boolean isFullDayTimeframe(String startDate, String endDate) {
        return (startDate.endsWith("00:00") || startDate.toLowerCase().endsWith("12:00 am")) &&
                (endDate == null || endDate.isEmpty() || // no end date
                (endDate.endsWith("00:00") || endDate.toLowerCase().endsWith("12:00 am")) || //end date is start of second day
                (endDate.endsWith("23:59") || endDate.toLowerCase().endsWith("11:59 pm"))); //end date is end of the day
    }
    
    public static AppDefinition getAppDefinitionByProcess(String processDefId) {
        AppDefinition appDef = null;
        Map<String, AppDefinition> processAppDefMap = (Map<String, AppDefinition>) processAppDefinition.get();
        if (processAppDefMap == null) {
            processAppDefMap = new HashMap<String, AppDefinition>();
        }
        if (processAppDefMap.containsKey(processDefId)) {
            appDef = processAppDefMap.get(processDefId);
        } else {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            appDef = appService.getAppDefinitionWithProcessDefId(processDefId);
            processAppDefMap.put(processDefId, appDef);
            processAppDefinition.set(processAppDefMap);
        }
        return appDef;
    }
    
    public static boolean isEnterprise() {
        try {
            Class.forName("org.joget.apps.license.LicenseManager");
            return true;
        } catch (Exception e) {}
        
        return false;
    }
    
    public static String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }
    
    public static List<String> findMissingPlugins(AppDefinition appDef) {
        long start = System.nanoTime();
        
        List<String> missingPlugins = new ArrayList<String>();
        
        if (appDef == null) {
            appDef = AppUtil.getCurrentAppDefinition();
        }
        
        // combine all definitions into a string for matching
        String concatAppDef = "";
        if (appDef.getFormDefinitionList() != null) {
            for (FormDefinition o : appDef.getFormDefinitionList()) {
                concatAppDef += o.getJson() + "~~~";
            }
        }
        if (appDef.getDatalistDefinitionList() != null) {
            for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                concatAppDef += o.getJson() + "~~~";
            }
        }
        if (appDef.getUserviewDefinitionList() != null) {
            for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                concatAppDef += o.getJson() + "~~~";
            }
        }
        if (appDef.getBuilderDefinitionList() != null) {
            for (BuilderDefinition o : appDef.getBuilderDefinitionList()) {
                concatAppDef += o.getJson() + "~~~";
            }
        }
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            if (packageDef.getPackageActivityPluginMap() != null) {
                for (PackageActivityPlugin o : packageDef.getPackageActivityPluginMap().values()) {
                    concatAppDef += "\"className\":\"" + o.getPluginName() + "\"~~~";
                    concatAppDef += o.getPluginProperties() + "~~~";
                }
            }
            if (packageDef.getPackageParticipantMap() != null) {
                for (PackageParticipant o : packageDef.getPackageParticipantMap().values()) {
                    if (o.getType() != null && PackageParticipant.TYPE_PLUGIN.equals(o.getType())) {
                        concatAppDef += "\"className\":\"" + o.getValue() + "\"~~~";
                        concatAppDef += o.getPluginProperties() + "~~~";
                    }
                }
            }
        }

        // get plugins list
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> pluginList = pluginManager.list(null);
        Set<String> plugins = new HashSet<String>();

        // look for plugins used in any definition file
        for (Plugin plugin: pluginList) {
            String pluginClassName = ClassUtils.getUserClass(plugin).getName();
            plugins.add(pluginClassName);
        }
        
        //find "className": "" 
        Set<String> found = new HashSet<String>();
        Pattern pattern = Pattern.compile("([\"'])className\\1\\s*:\\s*\\1([^\"']+)\\1");
        Matcher matcher = pattern.matcher(concatAppDef);
        while (matcher.find()) {
            found.add(matcher.group(2));
        }
        
        //TODO: can't find missing hash variable plugin. there is no good way to detect a missing hash variable syntax
        
        found.remove("org.joget.apps.userview.model.Userview");
        found.remove("org.joget.apps.userview.model.UserviewCategory");
        found.remove("org.joget.apps.userview.model.UserviewSetting");
        found.remove("org.joget.apps.userview.model.UserviewPage");
        found.remove("org.joget.apps.userview.model.UserviewLayout");
        found.remove("org.joget.apps.userview.model.UserviewPermission");
        
        for (String p : found) {
            if (p.contains(".") && !plugins.contains(p)) {
                missingPlugins.add(p);
            }
        }
        
        missingPlugins = MarketplaceUtil.pluginClassToMarketplaceLink(missingPlugins);
        
        Collections.sort(missingPlugins);
        
        return missingPlugins;
    }
    
    /**
     * Retrieve the app template config based on app id & version
     * @param appId
     * @param appVersion
     * @return 
     */
    public static JSONObject getAppTemplateConfig(String appId, String appVersion) {
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        if (appVersion == null || appVersion.isEmpty()) {
            Long version = appService.getPublishedVersion(appId);
            appVersion = (version != null)?version.toString():null;
        }
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        
        if (appDef != null) {
            return getAppTemplateConfig(appDef);
        }
        return new JSONObject();
    }
    
    /**
     * Retrieve the app template config based on app definition
     * @param appDef
     * @return 
     */
    public static JSONObject getAppTemplateConfig(AppDefinition appDef) {
        JSONObject config = new JSONObject();
        
        try {
            if (appDef != null) {
                File json = AppResourceUtil.getFile(appDef.getAppId(), appDef.getVersion().toString(), "template.json");
                if (json != null && json.exists()) {
                    String content = FileUtils.readFileToString(json, StandardCharsets.UTF_8);
                    if (content != null && !content.isEmpty()) {
                        config = new JSONObject(content);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppUtil.class.getName(), e, "");
        }
        
        return config;
    }
    
    /**
     * Used to check is there any completed process in shark table
     */
    public static boolean hasNonArchivedProcessData() {
        WorkflowAssignmentDao workflowAssignmentDao = (WorkflowAssignmentDao)AppUtil.getApplicationContext().getBean("workflowAssignmentDao");
        SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
        
        return (workflowAssignmentDao.hasNonHistoryCompletedProcess() && setupManager.getSettingValue(WorkflowManagerImpl.ARCHIVE_SETTING) == null) || !isArchivedProcessDataModeEnabled();
    }
    
    /**
     * Used to check is archive process is enabled in system setting
     */
    public static boolean isArchivedProcessDataModeEnabled() {
        SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
        String mode = setupManager.getSettingValue("deleteProcessOnCompletion");
        return "archive".equalsIgnoreCase(mode);
    }
    
    /**
     * Used to retrieve the archiving process status percentage
     * negative value = paused
     * 100 = completed or no migration running
     */
    public static double getArchivedProcessStatus() {
        //not using setupManager due to the value is cached
        SetupDao setupDao = (SetupDao) WorkflowUtil.getApplicationContext().getBean("setupDao");
        Collection<Setting> result = setupDao.find("WHERE property = ?", new String[]{WorkflowManagerImpl.ARCHIVE_SETTING}, null, null, null, null);
        Setting status = (result.isEmpty()) ? null : result.iterator().next();
        
        if (status != null) {
            try {
                JSONObject statusObj = new JSONObject(status.getValue());

                //check the date to see if the archive migration thread is still running, assume it is stopped if no update for 15mins
                if (statusObj.getString("state").equals("STARTED")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ((new Date()).getTime() - sdf.parse(statusObj.getString("lastRun")).getTime() > (15 * 60 * 1000)) {
                        statusObj.put("state", "PAUSE");
                        status.setValue(statusObj.toString());
                        setupDao.saveOrUpdate(status);
                    }
                }
                
                double percentage = 0;
                double total = statusObj.getDouble("total");
                double completed = statusObj.getDouble("completed");
                
                if (total > completed) {
                    percentage = completed/total * 100;
                } else {
                    percentage = 95;
                }
                
                if (percentage == 0) {
                    percentage = 1;
                }
                
                if (statusObj.getString("state").equals("PAUSE")) {
                    //make it negative value is the migration thread is not running
                    percentage = percentage * -1;
                }
                
                return percentage;
            } catch (Exception e) {
                LogUtil.error(AppUtil.class.getName(), e, "");
            }
        }
        return 100;
    }
    
    /**
     * Return a map of create app option plugins
     * 
     * @return 
     */
    public static Map<String, Plugin> getCreateAppOptions() {
        Map<String, Plugin> options = new HashMap<String, Plugin>();
        
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> plugins = pluginManager.list(CreateAppOption.class);
        
        for (Plugin p : plugins) {
            if (((CreateAppOption) p).isAvailable()) {
                options.put(ClassUtils.getUserClass(p).getName(), p);
            }
        }
        
        return options;
    }
    
    /**
     * Execute create app option plugin, return errors if there is.
     * @param plugin
     * @param propertiesJson
     * @param appName
     * @param appId
     * 
     * @return 
     */
    @Transactional
    public static Collection<String> executeCreateAppOptionPlugin(CreateAppOption plugin, String propertiesJson, String appId, String appName, HttpServletRequest request) {
        Collection<String> errors = null;
        
        if (plugin != null) {
            try {
                if (plugin instanceof PropertyEditable) {
                    ((PropertyEditable) plugin).setProperties(PropertyUtil.getPropertiesValueFromJson(propertiesJson));
                }
                errors = plugin.createAppDefinition(appId, appName, request);
            } catch (Exception e) {
                LogUtil.error(AppUtil.class.getName(), e, "Error create app using " + plugin.getClassName());
            }
        }
        
        return errors;
    }
    
    /**
     * Generate ID for IdGeneratorField and IdGeneratorTool. To be used in a plugin only.
     * @param envVariable getPropertyString("envVariable")
     * @param format getPropertyString("format")
     * @param isDistributedGeneration getPropertyString("isDistributedGeneration")
     * @param pluginName getName()
     * @return the generated ID
     */
    public static String idGenerator(String envVariable, String format, boolean isDistributedGeneration, String pluginName) {
        AppDefinition appDef = getCurrentAppDefinition();
        EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) getApplicationContext().getBean("environmentVariableDao");

        // check which generation method setting used
        long count;
        if (isDistributedGeneration) {
            count = DistributedIdGenerator.getInstance().nextId();
        } else {
            count = environmentVariableDao.getIncreasedCounter(envVariable, "Used for plugin: " + pluginName, appDef);
        }

        String value = format;
        Matcher m = Pattern.compile("(\\?+)").matcher(format);
        if (m.find()) {
            String pattern = m.group(1);
            String formatter = pattern.replaceAll("\\?", "0");
            pattern = pattern.replaceAll("\\?", "\\\\?");

            DecimalFormat myFormatter = new DecimalFormat(formatter);
            String runningNumber = myFormatter.format(count);
            value = value.replaceAll(pattern, StringUtil.escapeRegex(runningNumber));
        }
        return value;
    }
    
    /**
     * Check to retrieve JSON from multipart file in POST body if json is null or empty
     * @param json
     * @return 
     */
    public static String getSubmittedJsonDefinition(String json) {
        if (json == null || json.isEmpty()) {
            // get json file in POST body
            MultipartFile jsonFile = null;
            try {
                jsonFile = FileStore.getFile("jsonFile");
            } catch (FileLimitException e) {
                LogUtil.warn(AppUtil.class.getName(), ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
            }
            
            if (jsonFile != null) {
                try {
                    json =  new String(jsonFile.getBytes(), "UTF-8");
                } catch (IOException e) {
                    LogUtil.error(AppUtil.class.getName(), e, "Fail to retrieve submitted JSON definition");
                }
            }
        }
        
        return json;
    }
}
