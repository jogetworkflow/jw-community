package org.kecak.apps.app.lib;

import org.joget.apps.app.dao.AuditTrailDao;
import org.joget.apps.app.model.AuditTrail;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
import org.joget.plugin.base.PluginManager;

import java.util.Map;

public class DataJsonControllerAuditTrail extends DefaultAuditTrailPlugin {
    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Store audit trail of DataJsonController";
    }

    @Override
    public Object execute(Map map) {
        final PluginManager pluginManager = (PluginManager) map.get("pluginManager");
        final AuditTrailDao auditTrailDao = (AuditTrailDao) pluginManager.getBean("auditTrailDao");
        final AuditTrail auditTrail = (AuditTrail) map.get("auditTrail");

        if (!auditTrail.getClazz().endsWith("DataJsonController")) {
            return null;
        }

        auditTrailDao.addAuditTrail(auditTrail);

        return null;
    }

    @Override
    public String getLabel() {
        return "Data Json API Audit Trail";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
}
