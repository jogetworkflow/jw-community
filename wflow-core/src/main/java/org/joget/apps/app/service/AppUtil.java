package org.joget.apps.app.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.LogUtil;
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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

@Service("appUtil")
public class AppUtil implements ApplicationContextAware {

    public static final String PREFIX_WORKFLOW_VARIABLE = "var_";
    public static final String PROPERTY_WORKFLOW_VARIABLE = "workflowVariable";
    static ApplicationContext appContext;
    static ThreadLocal currentAppDefinition = new ThreadLocal();

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * Ties an AppDefinition to the current thread.
     * @param appDef
     * @throws BeansException
     */
    public static void setCurrentAppDefinition(AppDefinition appDef) throws BeansException {
        currentAppDefinition.set(appDef);
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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String designerwebBaseUrl = (request != null) ? request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() : "";
        if (WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0) {
            designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
        }
        if (designerwebBaseUrl.endsWith("/")) {
            designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length() - 1);
        }

        return designerwebBaseUrl;
    }

    /**
     * Read workflow variable values from request parameters and populate into a Map
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
     * Read workflow variable values from a Map and populate into another Map
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
     * @param
     * @return
     */
    public static String getAppLocale() {
        LocaleResolver localeResolver = (LocaleResolver) appContext.getBean("localeResolver");  
        return localeResolver.resolveLocale(WorkflowUtil.getHttpServletRequest()).toString();
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

    public static String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap) {
        return processHashVariable(content, wfAssignment, escapeFormat, replaceMap, null);
    }
    
    public static String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap, AppDefinition appDef) {
        // check for hash # to avoid unnecessary processing
        if (!containsHashVariable(content)) {
            return content;
        }

        //parse content
        if (content != null) {
            Pattern pattern = Pattern.compile("\\#([^#^\"^ ])*\\.([^#^\"])*\\#");
            Matcher matcher = pattern.matcher(content);
            List<String> varList = new ArrayList<String>();
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
                        String tempVar = var.replaceAll("#", "");
                        
                        for (Plugin p : pluginList) {
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

                                //process nested hash
                                while (tempVar.contains("{") && tempVar.contains("}")) {
                                    Pattern nestedPattern = Pattern.compile("\\{([^\\{^\\}])*\\}");
                                    Matcher nestedMatcher = nestedPattern.matcher(tempVar);
                                    while (nestedMatcher.find()) {
                                        String nestedHash = nestedMatcher.group();
                                        String nestedHashString = nestedHash.replace("{", "#");
                                        nestedHashString = nestedHashString.replace("}", "#");

                                        String processedNestedHashValue = processHashVariable(nestedHashString, wfAssignment, escapeFormat, replaceMap, appDef);
                                        tempVar = tempVar.replaceAll(StringUtil.escapeString(nestedHash, StringUtil.TYPE_REGEX, null), StringUtil.escapeString(processedNestedHashValue, escapeFormat, replaceMap));
                                    }
                                }
                                
                                //get result from plugin
                                String value = cachedPlugin.processHashVariable(tempVar);
                                
                                if (value != null && !StringUtil.TYPE_REGEX.equals(escapeFormat)) {
                                    value = StringUtil.escapeRegex(value);
                                }
                                
                                //escape special char in HashVariable
                                var = cachedPlugin.escapeHashVariable(var);

                                //replace
                                if (value != null) {
                                    content = content.replaceAll(var, StringUtil.escapeString(value, escapeFormat, replaceMap));
                                }
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

    public static boolean containsHashVariable(String content) {
        boolean result = (content != null && content.indexOf("#") >= 0);
        return result;
    }
    
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
}
