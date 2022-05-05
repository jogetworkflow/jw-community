package org.joget.governance.lib;

import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovAppHealthCheck;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class SecureProcessesCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {

    @Override
    public String getName() {
        return "SecureProcessesCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Secure Processes";
    }

    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String getCategory() {
        return ResourceBundleUtil.getMessage("governance.security");
    }

    @Override
    public String getSortPriority() {
        return "3";
    }
    
    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        boolean hasNotMapped = false;
        for (AppDefinition appDef: appDefinitionList) {
            PackageDefinition packageDefinition = appDef.getPackageDefinition();
            if (packageDefinition != null) {
                Long packageVersion = packageDefinition.getVersion();
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDef.getAppId(), packageVersion.toString());
                for (WorkflowProcess wp : processList) {
                    //check process start whitelist
                    if (packageDefinition.getPackageParticipant(wp.getIdWithoutVersion(), "processStartWhiteList") == null){
                        hasNotMapped = true;
                        result.addDetailWithAppId(ResourceBundleUtil.getMessage("secureProcessesCheck.fail", new String[]{appDef.getName(), ResourceBundleUtil.getMessage("console.app.process.common.label.processStartWhiteList"), wp.getName()}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/process/builder#"+wp.getIdWithoutVersion(), null, appDef.getAppId());
                    }
                    
                    //check participant mapping
                    Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(wp.getId());
                    for (WorkflowParticipant p : participantList) {
                        if (packageDefinition.getPackageParticipant(wp.getIdWithoutVersion(), p.getId()) == null) {
                            if (workflowManager.participantHasActivities(wp.getId(), p.getId())) {
                                hasNotMapped = true;
                                result.addDetailWithAppId(ResourceBundleUtil.getMessage("secureProcessesCheck.fail", new String[]{appDef.getName(), p.getName(), wp.getName()}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/process/builder#"+wp.getIdWithoutVersion(), null, appDef.getAppId());
                            }
                        }
                    }
                }
            }
        }
        if (hasNotMapped) {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        
        return result;
    }

    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();

        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");

        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        boolean hasNotMapped = false;
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        if (packageDefinition != null) {
            Long packageVersion = packageDefinition.getVersion();
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDef.getAppId(), packageVersion.toString());
            for (WorkflowProcess wp : processList) {
                //check process start whitelist
                if (packageDefinition.getPackageParticipant(wp.getIdWithoutVersion(), "processStartWhiteList") == null) {
                    hasNotMapped = true;
                    result.addDetail(ResourceBundleUtil.getMessage("secureProcessesCheck.fail", new String[]{appDef.getName(), ResourceBundleUtil.getMessage("console.app.process.common.label.processStartWhiteList"), wp.getName()}), "/web/console/app/" + appDef.getAppId() + "/" + appDef.getVersion().toString() + "/process/builder#" + wp.getIdWithoutVersion(), null);
                }

                //check participant mapping
                Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(wp.getId());
                for (WorkflowParticipant p : participantList) {
                    if (packageDefinition.getPackageParticipant(wp.getIdWithoutVersion(), p.getId()) == null) {
                        if (workflowManager.participantHasActivities(wp.getId(), p.getId())) {
                            hasNotMapped = true;
                            result.addDetail(ResourceBundleUtil.getMessage("secureProcessesCheck.fail", new String[]{appDef.getName(), p.getName(), wp.getName()}), "/web/console/app/" + appDef.getAppId() + "/" + appDef.getVersion().toString() + "/process/builder#" + wp.getIdWithoutVersion(), null);
                        }
                    }
                }
            }
        }
        if (hasNotMapped) {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        return result;
    }
}