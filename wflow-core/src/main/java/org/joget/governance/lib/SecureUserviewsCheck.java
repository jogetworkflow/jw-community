package org.joget.governance.lib;

import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class SecureUserviewsCheck extends GovHealthCheckAbstract {

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
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        boolean hasNonPermisisonSet = false;
        for (AppDefinition appDef: appDefinitionList) {
            for (UserviewDefinition userviewDef: appDef.getUserviewDefinitionList()) {
                if (!hasPermissionSet(userviewDef)) {
                    hasNonPermisisonSet = true;
                    result.addDetail(ResourceBundleUtil.getMessage("secureUserviewsCheck.fail", new String[]{appDef.getName(), StringUtil.stripAllHtmlTag(userviewDef.getName())}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/userview/builder/"+userviewDef.getId(), null);
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
            JSONObject permissionObj = settingObj.getJSONObject("properties").getJSONObject("permission");
            if (permissionObj != null && permissionObj.has("className") && !permissionObj.getString("className").isEmpty()) {
                return true;
            }
            
            //has permission rules
            if (settingObj.getJSONObject("properties").has("permission_rules")) {
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
                JSONObject cpermissionObj = categoryObj.getJSONObject("properties").getJSONObject("permission");
                if (cpermissionObj != null && cpermissionObj.has("className") && !cpermissionObj.getString("className").isEmpty()) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.debug(SecureUserviewsCheck.class.getName(), e.getLocalizedMessage());
        }
        
        return false;
    }
}