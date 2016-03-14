package org.joget.apps.app.service;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.apps.userview.model.UserviewV5Theme;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    static ApplicationContext appContext;
    static ThreadLocal currentAppDefinition = new ThreadLocal();
    static ThreadLocal resetAppDefinition = new ThreadLocal();
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
        return appContext;
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
                // TODO: handle exception
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
     * Read date format from Setup
     * @return Date format
     */
    public static String getAppDateFormat() {
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");

        String systemDateFormat = setupManager.getSettingValue("systemDateFormat");
        if (systemDateFormat != null && !systemDateFormat.isEmpty()) {
            return systemDateFormat;
        } if ("true".equalsIgnoreCase(setupManager.getSettingValue("dateFormatFollowLocale"))) {
            Locale locale = LocaleContextHolder.getLocale();
            DateFormat dateInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
            if (dateInstance instanceof SimpleDateFormat) {
                return ((SimpleDateFormat) dateInstance).toPattern();
            }
        }

        return null;
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
                    PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) appContext.getBean("pluginDefaultPropertiesDao");
                    Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
                    Map <String, HashVariablePlugin> hashVariablePluginCache = new HashMap<String, HashVariablePlugin>();

                    for (String var : varList) {
                        for (Plugin p : pluginList) {
                            String tempVar = var.replaceAll("#", "");
                            
                            HashVariablePlugin hashVariablePlugin = (HashVariablePlugin) p;
                            if (tempVar.startsWith(hashVariablePlugin.getPrefix() + ".")) {
                                tempVar = tempVar.replaceFirst(hashVariablePlugin.getPrefix() + ".", "");

                                HashVariablePlugin cachedPlugin = hashVariablePluginCache.get(hashVariablePlugin.getClassName());
                                if (cachedPlugin == null) {
                                    cachedPlugin = (HashVariablePlugin) pluginManager.getPlugin(hashVariablePlugin.getClassName());
                                    //get default plugin properties

                                    if (appDef == null) {
                                        appDef = AppUtil.getCurrentAppDefinition();
                                    }
                                    PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(cachedPlugin.getClassName(), appDef);
                                    if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
                                        cachedPlugin.setProperties(PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperties.getPluginProperties()));
                                    }

                                    //put appDef & wfAssignment to properties
                                    cachedPlugin.setProperty("appDefinition", appDef);
                                    cachedPlugin.setProperty("workflowAssignment", wfAssignment);
                                    hashVariablePluginCache.put(hashVariablePlugin.getClassName(), cachedPlugin);
                                }

                                String nestedHashVar = tempVar;
                                        
                                //process nested hash
                                while (nestedHashVar.contains("{") && nestedHashVar.contains("}")) {
                                    Pattern nestedPattern = Pattern.compile("\\{([^\\{^\\}])*\\}");
                                    Matcher nestedMatcher = nestedPattern.matcher(nestedHashVar);
                                    while (nestedMatcher.find()) {
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
                                }

                                //unescape hash variable
                                tempVar = StringEscapeUtils.unescapeJavaScript(tempVar);

                                //get result from plugin
                                try {
                                    String removeFormatVar = tempVar;
                                    String hashFormat = "";
                                    if (removeFormatVar.contains("?")) {
                                        hashFormat = tempVar.substring(tempVar.lastIndexOf("?")+1);
                                        removeFormatVar = tempVar.substring(0, tempVar.lastIndexOf("?"));
                                    }
                                    
                                    String value = cachedPlugin.processHashVariable(removeFormatVar);
                                    
                                    if (value != null) {
                                        //escape based on hash variable
                                        if (hashFormat != null & !hashFormat.isEmpty()) {
                                            value = StringUtil.escapeString(value, hashFormat, null);
                                        }
                                        
                                        // clean to prevent XSS
                                        value = StringUtil.stripHtmlRelaxed(value);
                                        
                                        //escape based on api call
                                        value = StringUtil.escapeString(value, escapeFormat, replaceMap);
                                        
                                        //escape regex for replaceAll
                                        if (!StringUtil.TYPE_REGEX.equals(escapeFormat)) {
                                            value = StringUtil.escapeRegex(value);
                                        }

                                        //escape special char in HashVariable
                                        var = cachedPlugin.escapeHashVariable(var);
                                        
                                        content = content.replaceAll(var, value);
                                        break;
                                    }
                                } catch (Exception e) {}
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
        if (request != null
                && request.getParameterValues("__a_") != null && request.getParameterValues("__a_").length > 0
                && request.getParameterValues("__u_") != null && request.getParameterValues("__u_").length > 0) {
            try {
                String appId = request.getParameterValues("__a_")[0];
                String uId = request.getParameterValues("__u_")[0];

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
                            String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
                            data.put("right_to_left", "true".equalsIgnoreCase(rightToLeft));
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

                            return html;
                        } else if (theme.getCss() != null) {
                            return "<style type=\"text/css\">\n" + theme.getCss() + "\n</style>";
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(AppUtil.class.getName(), e, "getUserviewThemeCss Error!");
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
}
