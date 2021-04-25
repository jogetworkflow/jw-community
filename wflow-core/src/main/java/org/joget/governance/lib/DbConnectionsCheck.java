package org.joget.governance.lib;

import java.util.Date;
import javax.sql.DataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;

public class DbConnectionsCheck extends GovHealthCheckAbstract {

    @Override
    public String getName() {
        return "DbConnectionsCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "DB Connections";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/governance/dbConnectionsCheck.json", null, true, null);
    }

    @Override
    public String getCategory() {
        return ResourceBundleUtil.getMessage("governance.performance");
    }

    @Override
    public String getSortPriority() {
        return "4";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        int maxActiveAlertNum = 20;
        String maxActiveAlert = getPropertyString("maxActiveAlert");
        if (!maxActiveAlert.isEmpty()) {
            try {
                maxActiveAlertNum = Integer.parseInt(maxActiveAlert);
            } catch (Exception e) {}
        }
        
        try {
            DataSource ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
            int numActive = Integer.parseInt(BeanUtils.getProperty(ds, "numActive"));
            
            if (numActive <= maxActiveAlertNum) {
                double score = 100 - (Double.valueOf(numActive)/Double.valueOf(maxActiveAlertNum) * 25);
                result.setScore((int) Math.floor(score));
            } else {
                double score = 1 - ((Double.valueOf(numActive) - Double.valueOf(maxActiveAlertNum))/Double.valueOf(maxActiveAlertNum)) * 75;
                result.setScore((int) Math.floor(score));
            }
            if (numActive > 0) {
                result.addDetail(ResourceBundleUtil.getMessage("dbConnectionsCheck.maxActive", new String[]{Integer.toString(maxActiveAlertNum)}));
            }
        } catch (Exception e) {}
        
        return result;
    }
}