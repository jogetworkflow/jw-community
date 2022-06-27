package org.joget.governance.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovAppHealthCheck;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.governance.model.GovHealthCheckResult.Detail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class HashVariableCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {
    private UserviewService userviewService;
    private final String[] matches = new String[] {"query", "sql", "extraCondition", "script"};
    private final String[] escapeMatches = new String[] {"?sql", "?java", "?javascript"};
    private final String[] hashMatches = new String[] {"#requestParam.", "#form.", "#binder.", "#variable.", "#envVariable.", "#beanshell."};

    @Override
    public String getName() {
        return "HashVariableCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Secure Hash Variable Usage";
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
        result.setSuppressable(true);
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        Gson gson = new GsonBuilder().create();
        
        boolean hasPotentialUnsecureHashVariable = false;
        for (AppDefinition appDef: appDefinitionList) {
            String temp = null;
            if (prevResult != null) {
                temp = prevResult.getData(appDef.getAppId() + "_" + appDef.getVersion());
            }
            Data preData = null;
            if (temp != null) {
                preData = gson.fromJson(temp, Data.class);
            }
            
            if (preData == null || !preData.getTimestamp().equals(appDef.getDateModified().getTime())) {
                Data newData = new Data();
                newData.setTimestamp(appDef.getDateModified().getTime());
                Collection<Detail> details = new ArrayList<Detail>();
                newData.setDetails(details);

                checkAppDef(appDef, details);

                if (!details.isEmpty()) {
                    hasPotentialUnsecureHashVariable = true;
                    result.getDetails().addAll(details);
                }
                
                result.setData(appDef.getAppId() + "_" + appDef.getVersion(), gson.toJson(newData));
            } else {
                result.setData(appDef.getAppId() + "_" + appDef.getVersion(), temp);
                Collection<Detail> details = preData.getDetails();
                if (!details.isEmpty()) {
                    hasPotentialUnsecureHashVariable = true;
                    result.getDetails().addAll(details);
                }
            }
        }
        if (hasPotentialUnsecureHashVariable) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            result.setMoreInfo(ResourceBundleUtil.getMessage("hashVariableCheck.info"));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
   
        return result;
    }
    
    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        Gson gson = new GsonBuilder().create();
        boolean hasPotentialUnsecureHashVariable = false;

        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Data newData = new Data();
        newData.setTimestamp(appDef.getDateModified().getTime());
        Collection<Detail> details = new ArrayList<Detail>();
        newData.setDetails(details);

        checkAppDef(appDef, details);
        if (!details.isEmpty()) {
            hasPotentialUnsecureHashVariable = true;
            result.getDetails().addAll(details);
        }

        result.setData(appDef.getAppId() + "_" + appDef.getVersion(), gson.toJson(newData));
        if (hasPotentialUnsecureHashVariable) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            result.setMoreInfo(ResourceBundleUtil.getMessage("hashVariableCheck.info"));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }

        return result;
    }
    
    protected void checkAppDef(AppDefinition appDef, Collection<Detail> details) {
        if (appDef.getFormDefinitionList() != null && !appDef.getFormDefinitionList().isEmpty()) {
            for (FormDefinition o : appDef.getFormDefinitionList()) {
                checkDefinition(o.getJson(), null, details, appDef.getName(), ResourceBundleUtil.getMessage("adminBar.label.form"), o.getName(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/form/builder/"+o.getId(), appDef.getAppId());
            }
        }
        if (appDef.getDatalistDefinitionList() != null && !appDef.getDatalistDefinitionList().isEmpty()) {
            for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                checkDefinition(o.getJson(), null, details, appDef.getName(), ResourceBundleUtil.getMessage("adminBar.label.list"), o.getName(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/datalist/builder/"+o.getId(), appDef.getAppId());
            }
        }
        if (appDef.getUserviewDefinitionList() != null && !appDef.getUserviewDefinitionList().isEmpty()) {
            for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                o = getUserviewService().combinedUserviewDefinition(o);
                checkDefinition(o.getJson(), null, details, appDef.getName(), ResourceBundleUtil.getMessage("adminBar.label.userview"), o.getName(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/userview/builder/"+o.getId(), appDef.getAppId());
            }
        }
        if (appDef.getBuilderDefinitionList() != null && !appDef.getBuilderDefinitionList().isEmpty()) {
            for (BuilderDefinition o : appDef.getBuilderDefinitionList()) {
                CustomBuilder builder = CustomBuilderUtil.getBuilder(o.getType());
                if (builder != null) {
                    checkDefinition(o.getJson(), null, details, appDef.getName(), builder.getObjectLabel(), o.getName(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/cbuilder/"+o.getType()+"/design/"+o.getId(), appDef.getAppId());
                }
            }
        }
        if (appDef.getEnvironmentVariableList() != null && !appDef.getEnvironmentVariableList().isEmpty()) {
            for (EnvironmentVariable o : appDef.getEnvironmentVariableList()) {
                Set<String> hashVariables = new HashSet<String>();
                findUnsecureHash(o.getValue(), hashVariables);
                addHashVariableToDetail(hashVariables, details, appDef.getName(), ResourceBundleUtil.getMessage("assist.EnvironmentVariable"), o.getId(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/builders", appDef.getAppId());
            }
        }
        if (appDef.getPackageDefinition() != null) {
            PackageDefinition pd = appDef.getPackageDefinition();
            Map<String, PackageActivityPlugin> map = pd.getPackageActivityPluginMap();
            Map<String, Set<String>> pidhash = new HashMap<String, Set<String>>();
            if (map != null && !map.isEmpty()) {
                for (String key : map.keySet()) {
                    PackageActivityPlugin o = map.get(key);
                    Set<String> hash = pidhash.get(o.getProcessDefId());
                    if (hash == null) {
                        hash = new HashSet<String>();
                        pidhash.put(o.getProcessDefId(), hash);
                    }
                    checkDefinition(o.getPluginProperties(), hash, null, null, null, null, null, appDef.getAppId());
                }
                for (String key : pidhash.keySet()) {
                    Set<String> hash = pidhash.get(key);
                    if (!hash.isEmpty()) {
                        addHashVariableToDetail(hash, details, appDef.getName(), ResourceBundleUtil.getMessage("pbuilder.label.process"), key, "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/process/builder#" + key, appDef.getAppId());
                    }
                }
            }
        }
    }
    
    protected void checkDefinition(String data, Set<String> hashVariables, Collection<Detail> details, String appName, String objectTypeName, String objectName, String link, String appId) {
        if (AppUtil.containsHashVariable(data) && hasKeywords(data, matches)) {
            try {
                boolean addTodetail = false;
                if (hashVariables == null) {
                    hashVariables = new HashSet<String>();
                    addTodetail = true;
                }
                findUnsecureHash(new JSONObject(data), hashVariables);
                if (addTodetail) {
                    addHashVariableToDetail(hashVariables, details, appName, objectTypeName, objectName, link, appId);
                }
            } catch (Exception e) {
                LogUtil.error(HashVariableCheck.class.getName(), e, "");
            }
        }
    }
    
    protected void addHashVariableToDetail(Set<String> hashVariables, Collection<Detail> details, String appName, String objectTypeName, String objectName, String link, String appId) {
        if (!hashVariables.isEmpty()) {
            String list = "<ol><li>" + StringUtils.join(hashVariables, "</li><li>") + "</li></ol>";
            details.add(new Detail(ResourceBundleUtil.getMessage("hashVariableCheck.fail", new String[]{appName, objectTypeName, StringUtil.stripAllHtmlTag(objectName), list}), link, null, appId));
        }
    }
    
    protected void findUnsecureHash(JSONObject obj, Set<String> hashVariables) throws JSONException {
        if (obj != null) {
            Iterator keys = obj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = obj.get(key);
                if (hasKeywords(key, matches) && value instanceof String) {
                    findUnsecureHash((String) value, hashVariables);
                } else {
                    if (value instanceof JSONArray) {
                        findUnsecureHash((JSONArray) value, hashVariables);
                    } else if (value instanceof JSONObject) {
                        findUnsecureHash((JSONObject) value, hashVariables);
                    }
                }
            }
        }
    }
    
    protected void findUnsecureHash(JSONArray obj, Set<String> hashVariables) throws JSONException {
        if (obj != null) {
            for (int i = 0; i < obj.length(); i++) {
                findUnsecureHash(obj.getJSONObject(i), hashVariables);
            }
        }
    }
    
    protected void findUnsecureHash(String obj, Set<String> hashVariables) {
        if (AppUtil.containsHashVariable(obj)) {
            Pattern pattern = Pattern.compile("\\#([^#^\"^ ])*\\.([^#^\"])*\\#");
            Matcher matcher = pattern.matcher(obj);
            Set<String> varList = new HashSet<String>();
            while (matcher.find()) {
                varList.add(matcher.group());
            }
            
            for (String var : varList) {
                if (hasKeywords(var, hashMatches) && !hasKeywords(var, escapeMatches)) {
                    hashVariables.add(var);
                }
            }
        }
    }
    
    protected boolean hasKeywords(String data, String[] matches) {
        boolean hasKeyword = false;
        for (String s : matches) {
            if (data.contains(s)){
                hasKeyword = true;
                break;
            }
        }
        return hasKeyword;
    }
    
    protected UserviewService getUserviewService() {
        if (userviewService == null) {
            userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");
        }
        return userviewService;
    }
    
    public static class Data {
        private Collection<Detail> details;
        private Long timestamp;
        
        public Collection<Detail> getDetails() {
            if (this.details == null) {
                this.details = new ArrayList<Detail>();
            }
            return details;
        }

        public void setDetails(Collection<Detail> details) {
            this.details = details;
        }

        public void addDetail(String detail) {
            addDetail(detail, null, null, null);
        }

        public void addDetail(String detail, String link, String linkLabel, String appId) {
            if (this.details == null) {
                this.details = new ArrayList<Detail>();
            }
            this.details.add(new Detail(detail, link, linkLabel, appId));
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
