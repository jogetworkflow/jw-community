package org.joget.governance.lib;

import java.util.Date;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.service.DirectoryManagerProxyImpl;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;

public class SecureAuthenticationCheck extends GovHealthCheckAbstract {

    @Override
    public String getName() {
        return "SecureAuthenticationCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Secure Authentication";
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
        return "1";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        Class secureDm = null;
        try {
            secureDm = Class.forName("org.joget.plugin.directory.SecureDirectoryManagerImpl");
        } catch (Exception e) {};
        
        boolean isSecureDm = false;
        if (secureDm != null) {
            DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) AppUtil.getApplicationContext().getBean("directoryManager");
            if (directoryManager != null && directoryManager.getDirectoryManagerImpl() != null) {
                if (directoryManager.getDirectoryManagerImpl().getClass().isAssignableFrom(secureDm)) {
                    isSecureDm = true;
                }
            }
        }
        
        if (isSecureDm) {
            result.setStatus(GovHealthCheckResult.Status.PASS);
            result.addDetail(ResourceBundleUtil.getMessage("secureAuthenticationCheck.pass"));
        } else {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
            result.addDetail(ResourceBundleUtil.getMessage("secureAuthenticationCheck.fail"));
        }
        
        return result;
    }
}
