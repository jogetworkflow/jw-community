package org.joget.governance.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.joget.apps.app.lib.EmailTool;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovHealthCheck;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.governance.model.GovHealthCheckResult.Detail;
import org.joget.governance.model.GovHealthCheckResult.Status;
import org.joget.governance.model.GovHealthCheckTask;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.ProfilePluginCache;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class GovHealthCheckManager {
    
    public static final long INTERVAL_MINIMUM = 300; // 5 mins
    public static final String SETTING = "governanceCheckerInterval";
    public static final String DEACTIVATE_PREFIX = "governance_deactivate_";
    
    @Autowired
    PluginManager pluginManager;
    
    protected SetupManager setupManager;
    protected ThreadPoolTaskScheduler govHealthCheckScheduler;
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hhmmss");
    protected Map<String, GovHealthCheckTask> tasks = new HashMap<String, GovHealthCheckTask>();
    protected Map<String, Map<String, GovHealthCheckResult>> lastResults = new HashMap<String, Map<String, GovHealthCheckResult>>();
    protected Map<String, String> lastResultsJson = new HashMap<String, String>();
    protected Map<String, String> serverBase = new HashMap<String, String>();
    
    public GovHealthCheckManager(ThreadPoolTaskScheduler govHealthCheckScheduler, SetupManager setupManager) {
        this.setupManager = setupManager;
        this.govHealthCheckScheduler = govHealthCheckScheduler;
        
        if (HostManager.isVirtualHostEnabled()) {
            // find all profiles and start threads
            Properties profiles = DynamicDataSourceManager.getProfileProperties();
            Set<String> profileSet = new HashSet(profiles.values());
            for (String profile : profileSet) {
                if (profile.contains(",")) {
                    continue;
                }
                try {
                    HostManager.setCurrentProfile(profile);
                    initChecker();
                } catch(Exception e) {
                    LogUtil.error(getClass().getName(), e, "Error initializing GovHealthCheckManager for " + profile);                        
                } finally {
                    HostManager.resetProfile();
                }
            }
        }
        else {
            // start current profile
            initChecker();
        }
    }
    
    protected String getProfile() {
        String profile = null;
        try {
            profile = DynamicDataSourceManager.getCurrentProfile();
        } catch (Exception e) {
            LogUtil.debug(ProfilePluginCache.class.getName(), profile);
        }
        if (profile == null) {
            profile = DynamicDataSourceManager.DEFAULT_PROFILE;
        }
        return profile;
    }
    
    public GovHealthCheckTask getTask() {
        return tasks.get(getProfile());
    }
    
    public Map<String, GovHealthCheckResult> getLastResults() {
        return lastResults.get(getProfile());
    }
    
    public void setTask(GovHealthCheckTask task) {
        tasks.put(getProfile(), task);
    }
    
    protected String getFilepath(String name) {
        String serverName = ServerUtil.getServerName();
        return getFilepathByServer(name, serverName);
    }
    
    protected String getFilepathByServer(String name, String serverName) {
        if (serverName != null && !serverName.isEmpty()) {
            serverName = serverName + File.separator;
        }
        return SetupManager.getBaseDirectory() + File.separator + "app_governance" + File.separator + serverName + name + ".json";
    }
    
    public void setLastResults(Map<String, GovHealthCheckResult> lastResult) {
        String profile = getProfile();
        lastResults.put(profile, lastResult);
        Date timestamp = lastResult.get("lastCheckDate").getTimestamp();
        
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
                        // convert date to long
                        return new JsonPrimitive(date.getTime());
                    }
                }).create();
        
        String lastResultJson = gson.toJson(lastResult);
        lastResultsJson.put(profile, lastResultJson);
        
        try {
            File path = new File(getFilepath("latest"));
            if (!path.getParentFile().exists()) {
                path.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile(path, lastResultJson, "UTF-8");
            
            File path2 = new File(getFilepath(sdf.format(timestamp)));
            if (!path2.getParentFile().exists()) {
                path2.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile(path2, lastResultJson, "UTF-8");
        } catch (Exception e) {
            LogUtil.error(GovHealthCheckManager.class.getName(), e, "");
        } 
    }
    
    public String getLastResultsJson() {
        String json = lastResultsJson.get(getProfile());
        if (json == null) {
            json = "";
        }
        return json;
    }
    
    public String getLastResultsJson(String serverName) {
        String json = lastResultsJson.get(getProfile());
        if (json == null) {
            json = "";
        }
        return json;
    }
    
    public Map<String, List<GovHealthCheck>> getGovHealthChecker() {
        Map<String, List<GovHealthCheck>> categoryMap = new TreeMap<String, List<GovHealthCheck>>();

        // get available elements from the plugin manager
        Collection<Plugin> elementList = pluginManager.list(GovHealthCheck.class);

        for (Plugin element : elementList) {
            GovHealthCheck checker = (GovHealthCheck) element;
            PropertyEditable pe = (PropertyEditable) element;
            
            String deactivate = setupManager.getSettingValue(DEACTIVATE_PREFIX + pe.getClassName());
            pe.setProperty("deactivated", Boolean.toString(deactivate != null && deactivate.equals("true")));
            
            // get element list for the the category
            String category = checker.getCategory();
            List<GovHealthCheck> list = categoryMap.get(category);
            
            if (list == null) {
                list = new ArrayList<GovHealthCheck>();
                categoryMap.put(category, list);
            }

            // add element to the list
            list.add(checker);
        }
        
        for (String c : categoryMap.keySet()) {
            // sort by position
            Collections.sort(categoryMap.get(c), new Comparator<GovHealthCheck>() {

                @Override
                public int compare(GovHealthCheck o1, GovHealthCheck o2) {
                    return o1.getSortPriority().compareTo(o2.getSortPriority());
                }
            });
        }

        return categoryMap;
    }
    
    public void activate(String pluginClass) {
        setupManager.deleteSetting(DEACTIVATE_PREFIX + pluginClass);
    }
    
    public void deactivate(String pluginClass) {
        Setting deactivate = setupManager.getSettingByProperty(DEACTIVATE_PREFIX + pluginClass);
        if (deactivate == null) {
            deactivate = new Setting();
            deactivate.setProperty(DEACTIVATE_PREFIX + pluginClass);
        }
        deactivate.setValue("true");
        setupManager.saveSetting(deactivate);
    }
    
    public String suppress(String pluginClass, String detail) {
        Map<String, GovHealthCheckResult> prevResults = getLastResults();
        if (prevResults != null && prevResults.containsKey(pluginClass)) {
            GovHealthCheckResult result = prevResults.get(pluginClass);
            
            if (result != null && !result.getDetails().isEmpty()) {
                boolean allSuppressed = true;
                for (Detail d : result.getDetails()) {
                    if (detail.equals(StringUtil.stripAllHtmlTag(d.getDetail()))) {
                        d.setSuppressed(true);
                    }
                    
                    if (!d.getSuppressed()) {
                        allSuppressed = false;
                    }
                }
                
                if (allSuppressed) {
                    result.setStatus(Status.PASS);
                }
                
                setLastResults(prevResults);
            }
        }
        
        return getLastResultsJson();
    }
    
    public void cleanData() {
        File dir = new File(SetupManager.getBaseDirectory() + File.separator + "app_governance");
        if (dir.exists()) {
            FileUtil.deleteContents(dir, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.getName().equalsIgnoreCase("latest.json");
                }
            });
        }
    }
    
    public final void initChecker() {
        String checkerInterval = setupManager.getSettingValue(SETTING);
        startChecker(checkerInterval, 30000l); //set a 30sec delay to wait for application context ready
        
        try {
            String lastResultJson = readResultsByServer(null);
            if (lastResultJson != null && !lastResultJson.isEmpty()) {
                Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @Override
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            // convert long to date
                            return new Date(json.getAsLong());
                        }
                    }).create();
                Map<String, GovHealthCheckResult> lastResult = gson.fromJson(lastResultJson, new TypeToken<Map<String, GovHealthCheckResult>>(){}.getType());
                
                String profile = getProfile();
                lastResults.put(profile, lastResult);
                lastResultsJson.put(profile, lastResultJson);
            }
        } catch (Exception e) {
            LogUtil.error(GovHealthCheckManager.class.getName(), e, "");
        }
    }
    
    public String readResultsByServer(String serverName) {
        if (serverName == null) {
            serverName = ServerUtil.getServerName();
        }
        try {
            File file = new File(getFilepathByServer("latest", serverName));
            if (file.exists()) {
                return FileUtils.readFileToString(file, "UTF-8");
            }
        } catch (Exception e) {
            LogUtil.error(GovHealthCheckManager.class.getName(), e, "");
        }
        return null;
    }
    
    public String getServerBaseUrl() {
        if (HostManager.isVirtualHostEnabled()) {
            String profile = getProfile();
            if (!serverBase.containsKey(profile)) {
                Properties profiles = DynamicDataSourceManager.getProfileProperties();
                for (String key : profiles.stringPropertyNames()) {
                    String p = profiles.getProperty(key);
                    if (!p.isEmpty() && !p.contains(",")) {
                        serverBase.put(p, key);
                    }
                }
            }
            return serverBase.get(profile);
        } else {
            return "";
        }
    }
    
    public String getCheckInterval() {
        String interval = setupManager.getSettingValue(SETTING);
        if (interval == null || interval.isEmpty()) {
            interval = "0";
        }
        return interval;
    }
    
    public long getCheckIntervalInMs() {
        try {
            return Long.parseLong(getCheckInterval());
        } catch (Exception e) {}
        return 0;
    }
    
    public void updateCheckInterval(String interval) {
        Setting setting = setupManager.getSettingByProperty(SETTING);
        if (setting == null) {
            setting = new Setting();
            setting.setProperty(SETTING);
        }
        setting.setValue(interval);
        
        setupManager.saveSetting(setting);
        startChecker(interval, null);
    }
    
    public final void stopChecker() {
        GovHealthCheckTask task = getTask();
        if (task != null) {
            task.getScheduledFuture().cancel(true);
            LogUtil.debug(GovHealthCheckManager.class.getName(), "Gov Health Checker stopped");
        }
    }
    
    public final void startChecker(String intervalStr, Long delay) {
        long interval = 0;
        if (intervalStr != null && intervalStr.trim().length() > 0) {
            try {
                interval = Long.parseLong(intervalStr);
            } catch (Exception ex) {
                interval = 0;
            }
        }
        
        if (interval > 0) {
            if (interval < INTERVAL_MINIMUM) {
                interval = INTERVAL_MINIMUM;
            }
            
            GovHealthCheckTask task = getTask();
            if ((task != null && task.getInterval() != interval) || task == null) {
                stopChecker();
                
                if (task == null) {
                    task = new GovHealthCheckTask(getProfile(), interval);
                    setTask(task);
                }
                if (delay != null) {
                    ScheduledFuture scheduledFuture = govHealthCheckScheduler.scheduleAtFixedRate(task, new Date(((new Date()).getTime() + delay)), interval * 1000);
                    task.setScheduledFuture(scheduledFuture);
                } else {
                    ScheduledFuture scheduledFuture = govHealthCheckScheduler.scheduleAtFixedRate(task, interval * 1000);
                    task.setScheduledFuture(scheduledFuture);
                }
                LogUtil.info(GovHealthCheckManager.class.getName(), "Gov Health Checker started");
            }
        } else {
            stopChecker();
        }
    }
    
    public void runCheck() {
        Collection<Plugin> elementList = pluginManager.list(GovHealthCheck.class);
        
        long interval = getCheckIntervalInMs();
        Date now = new Date();
        LogUtil.debug(GovHealthCheckManager.class.getName(), "Gov Health Checker start checking on " + now);
        
        Map<String, GovHealthCheckResult> prevResults = getLastResults();
        Map<String, GovHealthCheckResult> results = new HashMap<String, GovHealthCheckResult>();
        
        GovHealthCheckResult lastCheckDate = new GovHealthCheckResult();
        lastCheckDate.setTimestamp(now);
        results.put("lastCheckDate", lastCheckDate);
        Status status = Status.PASS;
        
        for (Plugin element : elementList) {
            PropertyEditable pe = (PropertyEditable) element;
            String deactivate = setupManager.getSettingValue(DEACTIVATE_PREFIX + pe.getClassName());
            if (deactivate != null && deactivate.equals("true")) {
                continue;
            }
            
            String properties = setupManager.getSettingValue("governance_"+pe.getClassName());
            if (properties != null && !properties.isEmpty()) {
                Map propertiesMap = PropertyUtil.getPropertiesValueFromJson(properties);
                pe.setProperties(propertiesMap);
            }
            GovHealthCheck checker = (GovHealthCheck) element;
            
            Date lastCheck = null;
            GovHealthCheckResult prevResult = null;
            if (prevResults != null) {
                prevResult = prevResults.get(pe.getClassName());
                if (prevResult != null) {
                    lastCheck = prevResult.getTimestamp();
                }
            }
            
            try {
                Date checkTime = new Date();
                GovHealthCheckResult result = checker.performCheck(lastCheck, interval, prevResult);
                if (result != null) {
                    result.setTimestamp(checkTime);
                    results.put(pe.getClassName(), result);
                    
                    if (result.getSuppressable()) {
                        if (prevResult != null && !prevResult.getDetails().isEmpty()) {
                            Set<String> suppressed = new HashSet<String>();
                            for (Detail d : prevResult.getDetails()) {
                                if (d.getSuppressed()) {
                                    suppressed.add(d.getDetail());
                                }
                            }
                            
                            if (!suppressed.isEmpty()) {
                                boolean allSuppressed = true;
                                for (Detail d : result.getDetails()) {
                                    if (suppressed.contains(d.getDetail())) {
                                        d.setSuppressed(true);
                                    }
                                    
                                    if (!d.getSuppressed()) {
                                        allSuppressed = false;
                                    }
                                }
                                
                                if (allSuppressed) {
                                    result.setStatus(Status.PASS);
                                }
                            }

                            setLastResults(prevResults);
                        }
                    }
                    
                    if (Status.FAIL.equals(result.getStatus())) {
                        status = Status.FAIL;
                    } else if (Status.WARN.equals(result.getStatus()) && !Status.FAIL.equals(result.getStatus())) {
                        status = Status.WARN;
                    }
                }
            } catch (Exception e) {
                LogUtil.error(pe.getClassName(), e, "");
            }
        }
        lastCheckDate.setStatus(status);
        
        //send email once when it fail or recover
        GovHealthCheckResult prevResult = null;
        if (prevResults != null) {
            prevResult = prevResults.get("lastCheckDate");
        }
        if ((prevResult == null && !(status.equals(Status.PASS) || status.equals(Status.INFO))) 
                || (prevResult != null && !prevResult.getStatus().equals(status))) {
            sendEmail(results, status);
        }
        
        setLastResults(results);
        
        LogUtil.debug(GovHealthCheckManager.class.getName(), "Gov Health Checker completed check on " + (new Date()));
    }
    
    public void sendEmail(Map<String, GovHealthCheckResult> results, Status status) {
        String sysHost = setupManager.getSettingValue("smtpHost");
        
        if ((sysHost == null || sysHost.isEmpty())) {
            LogUtil.warn(GovHealthCheckManager.class.getName(), "Send health check email failed, SMTP host not configured");
        } else {
            try {
                String properties = setupManager.getSettingValue("governance_alert");
                if (properties != null && !properties.isEmpty()) {
                    Map emailProp = PropertyUtil.getPropertiesValueFromJson(properties);
                    
                    if (emailProp != null && emailProp.containsKey("toSpecific") && !emailProp.get("toSpecific").toString().isEmpty()) {
                        Map dataModel = new HashMap();

                        dataModel.put("serverName", ServerUtil.getServerName());
                        dataModel.put("lastResult", results);
                        dataModel.put("checker", getGovHealthChecker());

                        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
                        String content = pluginManager.getPluginFreeMarkerTemplate(dataModel, "org.joget.apps.app.lib.EmailTool", "/templates/governanceEmail.ftl", null);

                        String emailSubject = (String) emailProp.get("subject");
                        String serverName = ServerUtil.getServerName();
                        if (serverName != null && !serverName.isEmpty()) {
                            serverName = "[" + serverName + "]";
                        } else {
                            serverName = "";
                        }
                        emailSubject = emailSubject.replaceAll(StringUtil.escapeRegex("#serverName#"), serverName);
                        emailSubject = emailSubject.replaceAll(StringUtil.escapeRegex("#status#"), status.name());

                        emailProp.put("subject", emailSubject);
                        emailProp.put("isHtml", "true");
                        emailProp.put("message", content);

                        EmailTool tool = new EmailTool();
                        tool.execute(emailProp);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(GovHealthCheckManager.class.getName(), e, "Send health check email failed");
            }
        }
    }
}
