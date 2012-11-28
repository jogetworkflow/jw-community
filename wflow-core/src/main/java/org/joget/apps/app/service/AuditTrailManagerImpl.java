package org.joget.apps.app.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.dao.AuditTrailDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
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

@Service("auditTrailManager")
public class AuditTrailManagerImpl implements AuditTrailManager {

    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private AuditTrailDao auditTrailDao;

    public void addAuditTrail(String clazz, String method, String message) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUsername(workflowUserManager.getCurrentUsername());
        auditTrail.setClazz(clazz);
        auditTrail.setMethod(method);
        auditTrail.setMessage(message);
        auditTrail.setTimestamp(new Date());

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            auditTrail.setAppId(appDef.getId());
            auditTrail.setAppVersion(appDef.getVersion().toString());
        }

        auditTrailDao.addAuditTrail(auditTrail);
        executePlugin(auditTrail);
    }

    protected void executePlugin(AuditTrail auditTrail) {
        Collection<Plugin> pluginList = pluginManager.list(AuditTrailPlugin.class);
        for (Plugin plugin : pluginList) {
            AuditTrailPlugin p = (AuditTrailPlugin) plugin;
            try {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                if (appDef != null) {
                    PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
                    PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(plugin.getClass().getName(), appDef);

                    if (pluginDefaultProperties != null) {
                        Map propertiesMap = new HashMap();

                        if (!(plugin instanceof PropertyEditable)) {
                            propertiesMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                        } else {
                            String json = pluginDefaultProperties.getPluginProperties();

                            //process basic hash variable
                            json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null);
                            propertiesMap = PropertyUtil.getPropertiesValueFromJson(json);
                        }

                        propertiesMap.put("auditTrail", auditTrail);
                        propertiesMap.put("pluginManager", pluginManager);

                        p.execute(propertiesMap);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "Error executing audit trail plugin " + p.getClass().getName());
            }
        }
    }
}
