package org.joget.governance.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.context.ApplicationContext;

public class EmailUsageCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {
    private UserviewService userviewService;

    @Override
    public String getName() {
        return "EmailUsageCheck";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getLabel() {
        return "Email Usage";
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
        return "4";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setSuppressable(true);
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        Gson gson = new GsonBuilder().create();
        
        boolean hasEmail = false;
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
                    hasEmail = true;
                    result.getDetails().addAll(details);
                }
                
                result.setData(appDef.getAppId() + "_" + appDef.getVersion(), gson.toJson(newData));
            } else {
                result.setData(appDef.getAppId() + "_" + appDef.getVersion(), temp);
                Collection<Detail> details = preData.getDetails();
                if (!details.isEmpty()) {
                    hasEmail = true;
                    result.getDetails().addAll(details);
                }
            }
        }
        if (hasEmail) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            result.setMoreInfo(ResourceBundleUtil.getMessage("emailUsageCheck.info"));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
   
        return result;
    }
    
    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        Gson gson = new GsonBuilder().create();
        boolean hasEmail = false;

        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Data newData = new Data();
        newData.setTimestamp(appDef.getDateModified().getTime());
        Collection<Detail> details = new ArrayList<Detail>();
        newData.setDetails(details);

        checkAppDef(appDef, details);
        if (!details.isEmpty()) {
            hasEmail = true;
            result.getDetails().addAll(details);
        }

        result.setData(appDef.getAppId() + "_" + appDef.getVersion(), gson.toJson(newData));
        if (hasEmail) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            result.setMoreInfo(ResourceBundleUtil.getMessage("emailUsageCheck.info"));
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
                checkDefinition(o.getValue(), null, details, appDef.getName(), ResourceBundleUtil.getMessage("assist.EnvironmentVariable"), o.getId(), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/builders", appDef.getAppId());
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
                    Set<String> emails = pidhash.get(key);
                    if (!emails.isEmpty()) {
                        addEmailsToDetail(emails, details, appDef.getName(), ResourceBundleUtil.getMessage("pbuilder.label.process"), key, "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/process/builder#" + key, appDef.getAppId());
                    }
                }
            }
        }
    }
    
    protected void checkDefinition(String data, Set<String> emails, Collection<Detail> details, String appName, String objectTypeName, String objectName, String link, String appId) {
        if (data != null && data.contains("@")) {
            try {
                boolean addTodetail = false;
                if (emails == null) {
                    emails = new HashSet<String>();
                    addTodetail = true;
                }
                findEmails(data, emails);
                if (addTodetail) {
                    addEmailsToDetail(emails, details, appName, objectTypeName, objectName, link, appId);
                }
            } catch (Exception e) {
                LogUtil.error(HashVariableCheck.class.getName(), e, "");
            }
        }
    }
    
    protected void addEmailsToDetail(Set<String> emails, Collection<Detail> details, String appName, String objectTypeName, String objectName, String link, String appId) {
        if (!emails.isEmpty()) {
            String list = "<ol><li>" + StringUtils.join(emails, "</li><li>") + "</li></ol>";
            details.add(new Detail(ResourceBundleUtil.getMessage("emailUsageCheck.fail", new String[]{appName, objectTypeName, StringUtil.stripAllHtmlTag(objectName), list}), link, null, appId));
        }
    }
    
    protected void findEmails(String text, Set<String> emails) {
        if (text.contains("@")) {
            String emailRegex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                emails.add(matcher.group());
            }
        }
    }
    
    protected UserviewService getUserviewService() {
        if (userviewService == null) {
            userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");
        }
        return userviewService;
    }
    
    public static class Data {
        private Collection<GovHealthCheckResult.Detail> details;
        private Long timestamp;
        
        public Collection<GovHealthCheckResult.Detail> getDetails() {
            if (this.details == null) {
                this.details = new ArrayList<GovHealthCheckResult.Detail>();
            }
            return details;
        }

        public void setDetails(Collection<GovHealthCheckResult.Detail> details) {
            this.details = details;
        }

        public void addDetail(String detail) {
            addDetail(detail, null, null, null);
        }

        public void addDetail(String detail, String link, String linkLabel, String appId) {
            if (this.details == null) {
                this.details = new ArrayList<GovHealthCheckResult.Detail>();
            }
            this.details.add(new GovHealthCheckResult.Detail(detail, link, linkLabel, appId));
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}