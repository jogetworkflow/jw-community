package org.joget.governance.lib;

import java.util.Date;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.workflow.util.DeadlineThreadManager;

public class DeadlineCheckerAvailabilityCheck extends GovHealthCheckAbstract {

    @Override
    public String getName() {
        return "DeadlineCheckerAvailabilityCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Deadline Checker Availability";
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
        return "5";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        if (!DeadlineThreadManager.isDeadlineChekerRunning()) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            result.addDetail(ResourceBundleUtil.getMessage("deadlineCheckerAvailabilityCheck.fail"), "/web/console/setting/general", null);
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        
        return result;
    }
}