package org.joget.apps.app.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.workflow.util.WorkflowUtil;

/**
 * DAO to load/store PackageDefinition and mapping objects
 */
public class PackageDefinitionDaoImpl extends AbstractVersionedObjectDao<PackageDefinition> implements PackageDefinitionDao {

    public static final String ENTITY_NAME = "PackageDefinition";
    private AppDefinitionDao appDefinitionDao;

    public AppDefinitionDao getAppDefinitionDao() {
        return appDefinitionDao;
    }

    public void setAppDefinitionDao(AppDefinitionDao appDefinitionDao) {
        this.appDefinitionDao = appDefinitionDao;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public void delete(PackageDefinition obj) {
        AppDefinition appDef = obj.getAppDefinition();
        if (appDef != null) {
            // disassociate from app
            Collection<PackageDefinition> list = appDef.getPackageDefinitionList();
            for (Iterator<PackageDefinition> i = list.iterator(); i.hasNext();) {
                PackageDefinition def = i.next();
                if (def.getId() != null && def.getId().equals(obj.getId())) {
                    i.remove();
                }
            }
            appDefinitionDao.saveOrUpdate(appDef);
        }
        // delete package definition
        super.delete(getEntityName(), obj);
    }

    /**
     * Loads the package definition for a specific app version
     * @param appId
     * @param appVersion
     * @return
     */
    @Override
    public PackageDefinition loadAppPackageDefinition(String appId, Long appVersion) {
        PackageDefinition packageDef = null;

        // load the package definition
        String condition = " INNER JOIN e.appDefinition app WHERE app.id=? AND app.version=?";
        Object[] params = {appId, appVersion};
        Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
        if (results != null && !results.isEmpty()) {
            packageDef = results.iterator().next();
        }

        return packageDef;
    }

    /**
     * Loads the package definition
     * @param packageId
     * @param packageVersion
     * @return
     */
    @Override
    public PackageDefinition loadPackageDefinition(String packageId, Long packageVersion) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            // load the package definition
            String condition = " WHERE e.id=? AND e.version=?";
            Object[] params = {packageId, packageVersion};
            Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }

    /**
     * Loads the package definition based on a process definition ID
     * @param packageVersion
     * @param processDefId
     * @return
     */
    @Override
    public PackageDefinition loadPackageDefinitionByProcess(String packageId, Long packageVersion, String processDefId) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

            // load the package definition
            String condition = " INNER JOIN e.packageActivityFormMap paf WHERE e.id=? AND e.version=? AND paf.processDefId=?";
            Object[] params = {packageId, packageVersion, processDefId};
            Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }

    @Override
    public PackageDefinition createPackageDefinition(AppDefinition appDef, Long packageVersion) {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId(appDef.getId());
        packageDef.setVersion(packageVersion);
        packageDef.setName(appDef.getName());
        packageDef.setAppDefinition(appDef);
        saveOrUpdate(packageDef);
        return packageDef;
    }

    @Override
    public PackageDefinition updatePackageDefinitionVersion(PackageDefinition packageDef, Long packageVersion) {
        String packageId = packageDef.getId();

        // detach previous package version
        delete(packageDef);

        // update package definition
        packageDef.setId(packageId);
        packageDef.setVersion(packageVersion);
        if (packageDef.getPackageActivityFormMap() != null) {
            packageDef.setPackageActivityFormMap(new HashMap<String, PackageActivityForm>(packageDef.getPackageActivityFormMap()));
        }
        if (packageDef.getPackageActivityPluginMap() != null) {
            packageDef.setPackageActivityPluginMap(new HashMap<String, PackageActivityPlugin>(packageDef.getPackageActivityPluginMap()));
        }
        if (packageDef.getPackageParticipantMap() != null) {
            packageDef.setPackageParticipantMap(new HashMap<String, PackageParticipant>(packageDef.getPackageParticipantMap()));
        }

        // save app and package definition
        saveOrUpdate(packageDef);
        return packageDef;
    }

    @Override
    public void addAppActivityForm(String appId, Long appVersion, PackageActivityForm activityForm) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityForm.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityForm.setProcessDefId(processDefId);
        String activityDefId = activityForm.getActivityDefId();
        if (processDefId != null && activityDefId != null) {
            packageDef.removePackageActivityForm(processDefId, activityDefId);
            saveOrUpdate(packageDef);
        }
        packageDef.addPackageActivityForm(activityForm);
        saveOrUpdate(packageDef);
    }

    @Override
    public void removeAppActivityForm(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityForm(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppActivityPlugin(String appId, Long appVersion, PackageActivityPlugin activityPlugin) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityPlugin.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityPlugin.setProcessDefId(processDefId);
        String activityDefId = activityPlugin.getActivityDefId();
        if (processDefId != null && activityDefId != null) {
            packageDef.removePackageActivityPlugin(processDefId, activityDefId);
            saveOrUpdate(packageDef);
        }
        packageDef.addPackageActivityPlugin(activityPlugin);
        saveOrUpdate(packageDef);
    }

    @Override
    public void removeAppActivityPlugin(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityPlugin(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    @Override
    public void addAppParticipant(String appId, Long appVersion, PackageParticipant participant) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = participant.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        participant.setProcessDefId(processDefId);
        String participantId = participant.getParticipantId();
        if (processDefId != null && participantId != null) {
            packageDef.removePackageParticipant(processDefId, participantId);
            saveOrUpdate(packageDef);
        }
        packageDef.addPackageParticipant(participant);
        saveOrUpdate(packageDef);
    }

    @Override
    public void removeAppParticipant(String appId, Long appVersion, String processDefId, String participantId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageParticipant(processDefId, participantId);
        saveOrUpdate(packageDef);
    }
}
