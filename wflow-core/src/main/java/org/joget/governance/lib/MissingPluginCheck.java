package org.joget.governance.lib;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovAppHealthCheck;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.springframework.context.ApplicationContext;

public class MissingPluginCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {

    @Override
    public String getName() {
        return "MissingPluginCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Missing Plugin";
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
        return ResourceBundleUtil.getMessage("governance.qualityAssurance");
    }

    @Override
    public String getSortPriority() {
        return "4";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setStatus(GovHealthCheckResult.Status.PASS);
        
        Set<String> checked = new HashSet<String>();
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        Collection<AppDefinition> latestAppDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        
        for (AppDefinition appDef : appDefinitionList) {
            findMissingPlugins(appDef, result);
            checked.add(appDef.getAppId() + "::" + appDef.getVersion());
        }
        
        for (AppDefinition appDef : latestAppDefinitionList) {
            if (!checked.contains(appDef.getAppId() + "::" + appDef.getVersion())) {
                findMissingPlugins(appDef, result);
                checked.add(appDef.getAppId() + "::" + appDef.getVersion());
            }
        }
        
        return result;
    }

    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setStatus(GovHealthCheckResult.Status.PASS);

        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        findMissingPlugins(appDef, result);
        
        return result;
    }
    
    private void findMissingPlugins(AppDefinition appDef, GovHealthCheckResult result) {
        List<String> plugins = AppUtil.findMissingPlugins(appDef);
        if (plugins != null && !plugins.isEmpty()) {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
            System.out.println(">>> " + appDef.getAppId());
            System.out.println(">>> " + StringUtils.join(plugins, "</li><li>"));
            result.addDetailWithAppId(ResourceBundleUtil.getMessage("missingPluginCheck.msg", new String[]{appDef.getName(), StringUtils.join(plugins, "</li><li>")}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/builders", null, appDef.getAppId());
        }
    }
}