package org.joget.apps.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joget.apps.app.dao.AuditTrailDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.AuditTrailPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

/**
 * Service methods used to add audit trail and trigger audit trail event.
 * 
 */
@Service("auditTrailManager")
@Lazy
public class AuditTrailManagerImpl implements AuditTrailManager {

    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private AuditTrailDao auditTrailDao;

    public static final String AUDIT_TRAIL_PLUGIN_NAME = "AUDIT_TRAIL_PLUGIN_NAME"; 
            
    private static ThreadLocal pluginList = new ThreadLocal();
    
    private static ThreadLocal executePluginInProgress = new ThreadLocal();
    
    private static ThreadLocal pluginPropertiesList = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new HashMap<String, List<Map>>();
        }
    };

    /**
     * The one caller trusted to invoke {@link #withPluginDispatchSuppressed(Runnable)} --
     * looked up reflectively (this module, wflow-core, must not compile-depend on the
     * enterprise-only cloud-wfengine module that declares it). {@code Class.forName(String)}
     * resolves via THIS class's own classloader (the shared platform/webapp classloader in any
     * real deployment), never by searching every classloader for a same-named class, so a
     * tenant's own OSGi bundle defining a class with this exact name cannot make this resolve to
     * its forgery -- only {@code ==} identity against whatever this lookup actually returns is
     * trusted, exactly as {@code MultiTenantSecurityManager.isCalledDirectlyFromAdminProfileWriter()}
     * trusts {@code Class} identity over class-name strings for the same reason. Absent (a
     * community-only, non-multitenant deployment, where the class does not exist at all) means no
     * caller can ever be trusted -- correct, since nothing in that deployment mode calls this.
     */
    private static final String TRUSTED_SUPPRESSION_CALLER_CLASS_NAME = "org.joget.commons.util.AdminProfileWriter";

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /**
     * Used by system to clear audit trail plugin cache
     */
    public void clean() {
        pluginList.remove();
        pluginPropertiesList.remove();
    }

    /**
     * Forces {@link #executePlugin(AuditTrail)}'s own {@code executePluginInProgress.get() ==
     * null} gate to fail for the duration of {@code task}, the same gate that already prevents a
     * dispatched plugin's own actions from recursing back into audit-trail plugin execution. This
     * is the actual chokepoint {@code executePlugin(..)} always checks, regardless of how {@code
     * addAuditTrail(..)} resolved (or re-derived, e.g. via the request referer/saved-URL
     * fallback) the {@code AuditTrail}'s app definition -- unlike suppressing dispatch by
     * clearing the caller's ambient {@code AppDefinition}, which that fallback can defeat.
     *
     * <p>Restricted to {@link #TRUSTED_SUPPRESSION_CALLER_CLASS_NAME}'s direct caller (adversarial
     * review -- Opus -- found the first version of this method callable, unguarded, by ANY OSGi/
     * BeanShell caller able to reach {@code AppUtil.getApplicationContext().getBean(
     * "auditTrailManager")}, letting ordinary tenant code silently defeat its own audit-trail
     * plugin dispatch -- e.g. a compliance/SIEM-forwarding plugin -- for arbitrary work, a
     * within-tenant integrity/monitoring bypass this method must not grant to anyone but the one
     * caller this fix exists for).
     */
    public void withPluginDispatchSuppressed(Runnable task) {
        if (!isCalledByTrustedSuppressionCaller()) {
            throw new SecurityException("No permission to suppress audit trail plugin dispatch");
        }
        Object previouslyInProgress = executePluginInProgress.get();
        executePluginInProgress.set(Boolean.TRUE);
        try {
            task.run();
        } finally {
            if (previouslyInProgress == null) {
                executePluginInProgress.remove();
            }
        }
    }

    private static boolean isCalledByTrustedSuppressionCaller() {
        final Class<?> trustedCallerClass;
        try {
            trustedCallerClass = Class.forName(TRUSTED_SUPPRESSION_CALLER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            return false;
        }
        Optional<StackFrame> nearestRealFrame = STACK_WALKER.walk(frames -> frames
                .filter(f -> f.getDeclaringClass() != AuditTrailManagerImpl.class && f.getDeclaringClass() != Thread.class)
                .findFirst());
        return nearestRealFrame.isPresent() && nearestRealFrame.get().getDeclaringClass() == trustedCallerClass;
    }

    /**
     * Simplify method to add audit trail and trigger audit trail event without 
     * passing method parameters and returned object
     * 
     * @param clazz
     * @param method
     * @param message 
     */
    public void addAuditTrail(String clazz, String method, String message) {
        addAuditTrail(clazz, method, message, null, null, null);
    }
    
    /**
     * Method to add audit trail and trigger audit trail event
     * @param clazz
     * @param method
     * @param message
     * @param paramTypes
     * @param args
     * @param returnObject 
     */
    public void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUsername(workflowUserManager.getCurrentUsername());
        auditTrail.setClazz(clazz);
        auditTrail.setMethod(method);
        auditTrail.setMessage(message);
        auditTrail.setParamTypes(paramTypes);
        auditTrail.setArgs(args);
        auditTrail.setReturnObject(returnObject);
        auditTrail.setTimestamp(new Date());
        
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            auditTrail.setAppDef(appDef);
            auditTrail.setAppId(appDef.getId());
            auditTrail.setAppVersion(appDef.getVersion().toString());
        } else if (args != null && args.length > 0) {
            // try to retrieve from args
            for (Object arg : args) {
                if (arg instanceof AppDefinition) {
                    AppDefinition appArg = (AppDefinition) arg;
                    auditTrail.setAppId(appArg.getId());
                    if (appArg.getVersion() != null) {
                        auditTrail.setAppVersion(appArg.getVersion().toString());
                    }
                    break;
                } else if (arg instanceof AbstractAppVersionedObject) {
                    AbstractAppVersionedObject appArg = (AbstractAppVersionedObject) arg;
                    auditTrail.setAppId(appArg.getAppId());
                    if (appArg.getAppVersion() != null) {
                        auditTrail.setAppVersion(appArg.getAppVersion().toString());
                    }
                    break;
                }
            }
        }
        
        if (auditTrail.getAppId() == null || auditTrail.getAppId().isEmpty()) {
            try {
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null) {
                    String savedUrl = null;
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        Object savedRequest = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                        if (savedRequest != null) {
                            try {
                                java.lang.reflect.Method getRedirectUrlMethod = savedRequest.getClass().getMethod("getRedirectUrl");
                                savedUrl = (String) getRedirectUrlMethod.invoke(savedRequest);
                            } catch (Exception e) {}
                        }
                    }
                    if (savedUrl == null) {
                        savedUrl = request.getHeader("referer");
                    }
                    if (savedUrl != null) {
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/web/(?:userview|ulogin|embed/userview|embed/ulogin|console/app)/([^/]+)/");
                        java.util.regex.Matcher matcher = pattern.matcher(savedUrl);
                        if (matcher.find()) {
                            String appId = matcher.group(1);
                            auditTrail.setAppId(appId);
                            
                            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                            if (appService != null) {
                                AppDefinition appDefFromUrl = appService.getPublishedAppDefinition(appId);
                                if (appDefFromUrl != null) {
                                    auditTrail.setAppDef(appDefFromUrl);
                                    if (appDefFromUrl.getVersion() != null) {
                                        auditTrail.setAppVersion(appDefFromUrl.getVersion().toString());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (dbLog(auditTrail)) {
            auditTrailDao.addAuditTrail(auditTrail);
        }
        executePlugin(auditTrail);
    }
    
    protected boolean dbLog(AuditTrail auditTrail) {
        String c = auditTrail.getClazz();
        String m = auditTrail.getMethod();
        
        if (c != null && m != null) {
            //login info
            if (m.equals("authenticate") || m.equals("logout") || m.equals("validateHost") ||
                    c.startsWith("org.joget.apps.app.") || 
                    c.startsWith("org.joget.directory.dao.") || 
                    c.endsWith("WorkflowManagerImpl") || 
                    c.endsWith("WorkflowToolActivityHandler") || 
                    c.endsWith("WorkflowAssignmentManager") || 
                    c.endsWith("PluginManager") || 
                    c.endsWith("SetupManager")) {
                return true;
            }
        }
        
        return false;
    }

    protected void executePlugin(AuditTrail auditTrail) {
        AppDefinition appDef = auditTrail.getAppDef();
        if (appDef != null && executePluginInProgress.get() == null) {
            executePluginInProgress.set(true);
            try {
                //get available audit trail plugins
                Map<String, AuditTrailPlugin> plugins = (HashMap<String, AuditTrailPlugin>) pluginList.get();
                if (plugins == null) {
                    Collection<Plugin> availablePluginsList = pluginManager.list(AuditTrailPlugin.class);
                    plugins = new HashMap<String, AuditTrailPlugin>();
                    for (Plugin p: availablePluginsList) {
                        plugins.put(p.getName(), (AuditTrailPlugin) p);
                    }
                    pluginList.set(plugins);
                }

                //get available properties setting for audit trail plugin, implemented this way for future development to accept multiple same plugin with different setting.
                String appId = appDef.getAppId();
                List<Map> propertiesList = ((HashMap<String, List<Map>>) pluginPropertiesList.get()).get(appId);
                if (propertiesList == null) {
                    propertiesList = new ArrayList<Map>();

                    Collection<PluginDefaultProperties> pluginDefaultPropertiesList = appDef.getPluginDefaultPropertiesList();

                    if (pluginDefaultPropertiesList != null && !pluginDefaultPropertiesList.isEmpty()) {
                        for (PluginDefaultProperties prop : pluginDefaultPropertiesList) {
                            if (plugins.containsKey(prop.getPluginName())) {
                                AuditTrailPlugin plugin = (AuditTrailPlugin) plugins.get(prop.getPluginName());
                                Map propertiesMap = new HashMap();

                                String json = prop.getPluginProperties();
                                propertiesMap = PropertyUtil.getPropertiesValueFromJson(json);

                                propertiesMap.put(AUDIT_TRAIL_PLUGIN_NAME, prop.getPluginName());
                                propertiesList.add(propertiesMap);
                            }
                        }
                    }
                    ((HashMap<String, List<Map>>) pluginPropertiesList.get()).put(appId, propertiesList);
                }

                //execute plugins
                for (Map props : propertiesList) {
                    props.put("auditTrail", auditTrail);
                    props.put("pluginManager", pluginManager);

                    AuditTrailPlugin plugin = (AuditTrailPlugin) plugins.get(props.get(AUDIT_TRAIL_PLUGIN_NAME).toString());

                    try {
                        if (plugin instanceof PropertyEditable) {
                            ((PropertyEditable) plugin).setProperties(props);
                            plugin.execute(((PropertyEditable) plugin).getProperties()); //to make the properties is HashVariableSupportedMap
                        } else {
                            plugin.execute(props);
                        }
                    } catch (Exception e) {
                        LogUtil.error(getClass().getName(), e, "Error executing audit trail plugin " + ClassUtils.getUserClass(plugin).getName());
                    }
                }
            } finally {
                executePluginInProgress.remove(); 
            }
        }
    }
}
