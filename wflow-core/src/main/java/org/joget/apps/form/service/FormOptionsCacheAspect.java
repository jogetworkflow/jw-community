package org.joget.apps.form.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Aspect
public class FormOptionsCacheAspect {
    public static final String CACHE_KEY_PREFIX = "FORMOPTIONS";
    public static final String LAST_ACTIVE_CACHE_KEY_PREFIX = "LAST_ACTIVE_";
    public static final int BUFFER_SECONDS = 5;
    private static final ThreadLocal callInProgress = new ThreadLocal();
    
    @Pointcut("execution(* org.joget.plugin.base.ExtDefaultPlugin.setProperties(..))")
    private void setPropertiesMethod() {
    }

    @Around("org.joget.apps.form.service.FormOptionsCacheAspect.setPropertiesMethod()")
    public Object setProperties(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = pjp.proceed();
        
        Object thisObj = pjp.getThis();
        if (thisObj instanceof FormLoadOptionsBinder && !((FormBinder)thisObj).getPropertyString("cacheInterval").isEmpty()) {
            final String cacheKey = getCacheKey((FormBinder) thisObj);
            if (!isInProgress(cacheKey)) {
                final String cacheInterval = ((FormBinder)thisObj).getPropertyString("cacheInterval");
                final String cacheIdlePause = ((FormBinder)thisObj).getPropertyString("cacheIdlePause");
                final AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                Thread startThread = new PluginThread(new Runnable() {
                    public void run() {
                        startSyncCache(cacheKey, cacheInterval, cacheIdlePause, appDef);
                    }
                });
                startThread.setDaemon(true);
                startThread.start();
            }
        }
        
        return obj;
    }
    
    @Pointcut("execution(* org.joget.apps.form.model.FormLoadOptionsBinder.load(..))")
    private void loadMethod() {
    }

    @Around("org.joget.apps.form.service.FormOptionsCacheAspect.loadMethod()")
    public Object load(ProceedingJoinPoint pjp) throws Throwable {
        FormBinder thisObj = (FormBinder) pjp.getThis();
        if (thisObj instanceof FormLoadOptionsBinder && !thisObj.getPropertyString("cacheInterval").isEmpty()) {
            String cacheKey = getCacheKey(thisObj);
            if (!isInProgress(cacheKey)) {
                return getCachedOptions(cacheKey, thisObj.getPropertyString("cacheIdlePause"), pjp);
            }
        }
        return pjp.proceed();
    }
    
    @Pointcut("execution(* org.joget.apps.form.model.FormBinder.getPropertyOptions(..))")
    private void getPropertyOptionsMethod() {
    }

    @Around("org.joget.apps.form.service.FormOptionsCacheAspect.getPropertyOptionsMethod()")
    public Object getPropertyOptions(ProceedingJoinPoint pjp) throws Throwable {
        Object thisObj = pjp.getThis();
        Object jsonObj = pjp.proceed();
        
        if (jsonObj != null && thisObj instanceof FormLoadOptionsBinder) {
            try {
                JSONArray jarr = new JSONArray(jsonObj.toString());
                JSONObject cacheOption = new JSONObject();
                cacheOption.put("type", "header");
                cacheOption.put("label", ResourceBundleUtil.getMessage("formOptionsCache.header"));
                JSONObject cacheOption1 = new JSONObject();
                cacheOption1.put("name", "cacheInterval");
                cacheOption1.put("type", "textfield");
                cacheOption1.put("label", ResourceBundleUtil.getMessage("formOptionsCache.interval"));
                cacheOption1.put("description", ResourceBundleUtil.getMessage("formOptionsCache.interval.desc"));
                cacheOption1.put("regex_validation", "^[0-9]+$");
                cacheOption1.put("validation_message", ResourceBundleUtil.getMessage("formOptionsCache.interval.error"));
                JSONObject cacheOption2 = new JSONObject();
                cacheOption2.put("name", "cacheIdlePause");
                cacheOption2.put("type", "textfield");
                cacheOption2.put("value", "120");
                cacheOption2.put("required", "true");
                cacheOption2.put("label", ResourceBundleUtil.getMessage("formOptionsCache.cacheIdlePause"));
                cacheOption2.put("description", ResourceBundleUtil.getMessage("formOptionsCache.cacheIdlePause.desc"));
                cacheOption2.put("regex_validation", "^[0-9]+$");
                cacheOption2.put("validation_message", ResourceBundleUtil.getMessage("formOptionsCache.interval.error"));
                cacheOption2.put("control_field", "cacheInterval");
                cacheOption2.put("control_value", "^[0-9]+$");
                cacheOption2.put("control_use_regex", "true");
                if (jarr.length() > 0) {
                    jarr.getJSONObject(0).accumulate("properties", cacheOption);
                    jarr.getJSONObject(0).accumulate("properties", cacheOption1);
                    jarr.getJSONObject(0).accumulate("properties", cacheOption2);
                }
                jsonObj = jarr.toString(4);
            } catch (Exception e) {}
        }
        
        return jsonObj;
    }
    
    public static FormRowSet getCachedOptions(String cacheKey, String idleStr, ProceedingJoinPoint pjp) throws Throwable {
        FormRowSet rowset = null;
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("formOptionsCache");
        if (cache != null) {
            Element element = cache.get(cacheKey);
            try {
                int count = 0;
                while (element == null && count < 100) { //try for 10sec
                    if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {
                        LogUtil.debug(FormOptionsCacheAspect.class.getName(), "cache " + cacheKey + " is not ready! waiting...");
                    }
                    Thread.sleep(100);
                    element = cache.get(cacheKey);
                    count++;
                }
                if (element == null && count == 100) { //fallback
                    if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {
                        LogUtil.debug(FormOptionsCacheAspect.class.getName(), "cache " + cacheKey + " is not able to retrieve after 10sec");
                    }
                    return (FormRowSet) pjp.proceed();
                }
            } catch (Exception e) {
                if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                    LogUtil.error(FormOptionsCacheAspect.class.getName(), e, "getCachedOptions: " + cacheKey);
                }
            }
            if (element != null) {
                rowset = (FormRowSet) element.getObjectValue();
                updateLastActive(cacheKey, idleStr);
                if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                    LogUtil.debug(FormOptionsCacheAspect.class.getName(), "getCachedOptions: " + cacheKey);
                }
            }
        }
        return rowset;
    }
    
    public static synchronized void startSyncCache(String cacheKey, String durationStr, String idleStr, AppDefinition appDef) {
        if (syncPaused(cacheKey)) {
            Integer duration = 0;
            if (durationStr != null && !durationStr.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr); 
                } catch(Exception ex) {}
            }

            if (duration > 0) {
                if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                    LogUtil.debug(FormOptionsCacheAspect.class.getName(), "start sync cache for " + cacheKey + " with duration " + duration + "s");
                }
                updateLastActive(cacheKey, idleStr);
                
                ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) AppUtil.getApplicationContext().getBean("formOptionsCacheExecutor");
                String profile = DynamicDataSourceManager.getCurrentProfile();
                FormOptionsCacheTask task = new FormOptionsCacheTask(profile, cacheKey, appDef);
                ScheduledFuture scheduledFuture = scheduler.scheduleAtFixedRate(task, duration * 1000);
                task.setScheduledFuture(scheduledFuture);
            }   
        }
    }
    
    public static void updateLastActive(String cacheKey, String idleStr) {
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("formOptionsCache");
        if (cache != null) {
            Integer duration = 0;
            if (idleStr != null && !idleStr.isEmpty()) {
                try {
                    duration = Integer.parseInt(idleStr); 
                } catch(Exception ex) {}
            }
            
            Element element = new Element(LAST_ACTIVE_CACHE_KEY_PREFIX + cacheKey, new Date());
            if (duration != null && duration > 0) {
                element.setTimeToIdle(duration);
                element.setTimeToLive(duration);
            }
            cache.put(element);
        }
    }
    
    public static synchronized boolean syncPaused(String cacheKey) {
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("formOptionsCache");
        if (cache != null) {
            Element element = cache.get(LAST_ACTIVE_CACHE_KEY_PREFIX + cacheKey);
            if (element != null) {
                return false;
            }
        }
        return true;
    }
    
    public static synchronized void syncOptionsCache(String cacheKey) {
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("formOptionsCache");
        if (cache != null) {
            String[] params = cacheKey.split("::");
            String className = params[2];
            String json = params[3];

            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            FormBinder optionBinder = (FormBinder) pluginManager.getPlugin(className);

            if (optionBinder != null) {
                setInProgress(cacheKey, true);
                optionBinder.setProperties(PropertyUtil.getPropertiesValueFromJson(json));
                try {
                    FormRowSet rowset = ((FormLoadBinder) optionBinder).load(null, null, null);
                    if (rowset != null) {
                        Integer duration = null;
                        String durationStr = optionBinder.getPropertyString("cacheInterval");
                        if (durationStr != null && !durationStr.isEmpty()) {
                            try {
                                duration = Integer.parseInt(durationStr) + BUFFER_SECONDS; 
                            } catch(Exception ex) {}
                        }
                        Element element = new Element(cacheKey, rowset);
                        if (duration != null && duration > 0) {
                            element.setTimeToIdle(duration);
                            element.setTimeToLive(duration);
                        }
                        cache.put(element);
                        if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                            LogUtil.debug(FormOptionsCacheAspect.class.getName(), "syncOptionsCache: " + cacheKey);
                        }
                    }
                } catch (Exception e) {
                    if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                        LogUtil.error(FormOptionsCacheAspect.class.getName(), e, "syncOptionsCache: " + cacheKey);
                    }
                } finally {
                    setInProgress(cacheKey, false);
                }
            }
        }
    }
    
    protected static String getCacheKey(FormBinder binder) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        String json = "";
        try {
            json = new JSONObject(binder.getProperties()).toString();
        } catch (Exception e) {}
        return CACHE_KEY_PREFIX + "::" + profile + "::" + binder.getClassName() + "::" + json;
    }
    
    protected static boolean isInProgress(String cacheKey) {
        Map<String, Boolean> flags = (Map<String, Boolean>) callInProgress.get();
        return flags != null && flags.containsKey(cacheKey);
    }
    
    protected static void setInProgress(String cacheKey, boolean inProgress) {
        Map<String, Boolean> flags = (Map<String, Boolean>) callInProgress.get();
        if (flags == null) {
            flags = new HashMap<String, Boolean>();
        }
        if (inProgress) {
            flags.put(cacheKey, true);
        } else {
            flags.remove(cacheKey);
        }
        callInProgress.set(flags);
    }
}
