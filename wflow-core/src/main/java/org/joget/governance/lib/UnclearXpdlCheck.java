package org.joget.governance.lib;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckAction;
import org.joget.governance.model.GovHealthCheckActionProvider;
import org.joget.governance.model.GovHealthCheckActionResult;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;

public class UnclearXpdlCheck extends GovHealthCheckAbstract implements GovHealthCheckActionProvider {
    
    private List<GovHealthCheckAction> actions;
    
    @Override
    public String getName() {
        return "UnclearXpdlCheck";
    }

    @Override
    public String getVersion() {
        return "9.1-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Unclear Process XPDL Check";
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
        return ResourceBundleUtil.getMessage("governance.performance");
    }

    @Override
    public String getSortPriority() {
        return "8";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setSuppressable(true);
        
        Set<String> versions = findUnclearVersion();

        if (!versions.isEmpty()) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            String list = "<ol><li>" +  StringUtils.join(versions, "</li><li>") + "</li></ol>";
            result.addDetail(ResourceBundleUtil.getMessage("UnclearXpdlCheck.warn", new String[]{list}));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        
        return result;
    }
    
    private Set<String> findUnclearVersion() {
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) AppUtil.getApplicationContext().getBean("packageDefinitionDao");
        
        Collection<WorkflowProcess> existingProcesses = workflowManager.getProcessList(null);
        Set<String> versions = new HashSet<String>();
        for (WorkflowProcess p : existingProcesses) {
            versions.add(p.getPackageId() + " : " + p.getVersion());
        }

        //removed version of latest package used by each app version
        Collection<PackageDefinition> allPackages = packageDefinitionDao.findByVersion(null, null, null, null, null, null, null, null);
        for (PackageDefinition p : allPackages) {
            versions.remove(p.getId() + " : " + p.getVersion().toString());
        }
        
        return versions;
    }

    @Override
    public List<GovHealthCheckAction> getActions() {
        if (actions == null) {
            actions = List.of(
                new GovHealthCheckAction("clean", ResourceBundleUtil.getMessage("UnclearXpdlCheck.clear"), "fa-solid fa-broom", (GovHealthCheckAction action, String pluginClass, String detail, HttpServletRequest request, HttpServletResponse response) -> {
                    WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                    Set<String> versions = findUnclearVersion();
                    
                    if (!versions.isEmpty()) {
                        for (String version : versions) {
                            String[] data = version.split(" : ");
                            String packageId = data[0];
                            String v = data[1];
                            try {
                                LogUtil.debug(getClass().getName(), "Trying to remove package " + packageId + " version " + v);
                                workflowManager.processDeleteAndUnloadVersion(packageId, v);
                            } catch (Exception e) {
                                LogUtil.debug(getClass().getName(), "Fail to remove package " + packageId + " version " + v);
                            }
                        }
                    }
                    
                    return GovHealthCheckActionResult.success(true, ResourceBundleUtil.getMessage("UnclearXpdlCheck.clear.success"));
                })
            );
        }
        return actions;
    }
    
    
}