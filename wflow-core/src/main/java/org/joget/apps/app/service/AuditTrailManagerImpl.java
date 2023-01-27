package org.joget.apps.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.dao.AuditTrailDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.AuditTrailPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

/**
 * Service methods used to add audit trail and trigger audit trail event.
 * 
 */
@Service("auditTrailManager")
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
     * Used by system to clear audit trail plugin cache
     */
    public void clean() {
        pluginList.set(null);
        pluginPropertiesList.set(new HashMap<String, List<Map>>());
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
            if (m.equals("authenticate") || m.equals("logout") || c.startsWith("org.joget.apps.app.") || c.startsWith("org.joget.directory.dao.") || c.endsWith("WorkflowManagerImpl") || c.endsWith("WorkflowToolActivityHandler") || c.endsWith("WorkflowAssignmentManager") || c.endsWith("PluginManager")) {
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

                                if (!(plugin instanceof PropertyEditable)) {
                                    try {
                                        propertiesMap = CsvUtil.getPluginPropertyMap(prop.getPluginProperties());
                                    } catch (IOException e) {}
                                } else {
                                    String json = prop.getPluginProperties();

                                    //process basic hash variable
                                    json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null);
                                    propertiesMap = PropertyUtil.getPropertiesValueFromJson(json);
                                }

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
                        }
                        plugin.execute(props);
                    } catch (Exception e) {
                        LogUtil.error(getClass().getName(), e, "Error executing audit trail plugin " + ClassUtils.getUserClass(plugin).getName());
                    }
                }
            } finally {
                executePluginInProgress.set(null);
            }
        }
    }
}
