package org.joget.governance.lib;

import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovAppHealthCheck;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class SecureUserviewsCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {

    @Override
    public String getName() {
        return "SecureUserviewsCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Secure Userviews";
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
        return "2";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setSuppressable(true);
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        boolean hasNonPermisisonSet = false;
        for (AppDefinition appDef: appDefinitionList) {
            for (UserviewDefinition userviewDef: appDef.getUserviewDefinitionList()) {
                if (!hasPermissionSet(userviewDef)) {
                    hasNonPermisisonSet = true;
                    result.addDetailWithAppId(ResourceBundleUtil.getMessage("secureUserviewsCheck.fail", new String[]{appDef.getName(), StringUtil.stripAllHtmlTag(userviewDef.getName())}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/userview/builder/"+userviewDef.getId(), null, appDef.getAppId());
                }
            }
        }
        if (hasNonPermisisonSet) {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        
        return result;
    }
    
    public boolean hasPermissionSet(UserviewDefinition userviewDef) {
        try {
            //has userview permission
            JSONObject userviewObj = new JSONObject(userviewDef.getJson());
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            JSONObject settingPropsObj = settingObj.getJSONObject("properties");
            if (settingPropsObj.has("permission")) {
                JSONObject permissionObj = settingPropsObj.getJSONObject("permission");
                if (permissionObj != null && permissionObj.has("className") && !permissionObj.getString("className").isEmpty()) {
                    return true;
                }
            }
            
            //has permission rules
            if (settingPropsObj.has("permission_rules")) {
                JSONArray permissionRules = settingObj.getJSONObject("properties").getJSONArray("permission_rules");
                if (permissionRules != null && permissionRules.length() > 0) {
                    for (int i = 0; i < permissionRules.length(); i++) {
                        JSONObject rule = permissionRules.getJSONObject(i);
                        if (rule.has("permission")) {
                            JSONObject rulePermissionObj = rule.optJSONObject("permission");
                            if (rulePermissionObj != null && rulePermissionObj.has("className") && !rulePermissionObj.getString("className").isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            //has category permission
            JSONArray categoriesArray = userviewObj.getJSONArray("categories");
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject categoryObj = (JSONObject) categoriesArray.get(i);
                JSONObject catPropsObj = categoryObj.getJSONObject("properties");
                if (catPropsObj.has("permission")) {
                    JSONObject cpermissionObj = catPropsObj.getJSONObject("permission");
                    if (cpermissionObj != null && cpermissionObj.has("className") && !cpermissionObj.getString("className").isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.debug(SecureUserviewsCheck.class.getName(), e.getLocalizedMessage());
        }
        
        return false;
    }

    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        boolean hasNonPermisisonSet = false;

        for (UserviewDefinition userviewDef : appDef.getUserviewDefinitionList()) {
            if (!hasPermissionSet(userviewDef)) {
                hasNonPermisisonSet = true;
                result.addDetail(ResourceBundleUtil.getMessage("secureUserviewsCheck.fail", new String[]{appDef.getName(), StringUtil.stripAllHtmlTag(userviewDef.getName())}), "/web/console/app/" + appDef.getAppId() + "/" + appDef.getVersion().toString() + "/userview/builder/" + userviewDef.getId(), null);
            }
        }

        if (hasNonPermisisonSet) {
            result.setStatus(GovHealthCheckResult.Status.FAIL);
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }

        return result;
    }
}