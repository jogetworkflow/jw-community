package org.joget.apps.userview.service;

import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class UserviewCache {

    public static final String CACHE_KEY_PREFIX = "USERVIEW";
    public static final String CACHE_TYPE_MENU = "menu";
    public static final String CACHE_TYPE_PAGE = "page";
    public static final String CACHE_SCOPE_APPLICATION = "application";
    public static final String CACHE_SCOPE_USER = "user";
    public static final String PROPERTY_SCOPE = "userviewCacheScope";
    public static final String PROPERTY_DURATION = "userviewCacheDuration";
    
    public static void clearCachedContent(String userviewId, String menuId, String scope) {
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("userviewMenuCache");
        if (cache != null) {
            WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
            scope = (CACHE_SCOPE_APPLICATION.equals(scope)) ? CACHE_SCOPE_APPLICATION : workflowUserManager.getCurrentUsername();
            String regex = CACHE_KEY_PREFIX + ":" + userviewId + ":" + menuId + ":" + scope + ":*";
            Results results = cache.createQuery().includeKeys().addCriteria(Query.KEY.ilike(regex)).execute();
            for (Result result : results.all()) {
                String key = (String) result.getKey();
                cache.remove(key);
                if (LogUtil.isDebugEnabled(UserviewCache.class.getName())) {    
                    LogUtil.debug(UserviewCache.class.getName(), "clearCachedContent: " + key);
                }
            }
            results.discard();
        }
    }

    public static void setCachedContent(UserviewMenu userviewMenu, String type, String content) {
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("userviewMenuCache");
        if (cache != null) {
            String scope = userviewMenu.getPropertyString(PROPERTY_SCOPE);
            if (scope == null || scope.isEmpty()) {
                return;
            }
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null && !"GET".equals(request.getMethod())) {
                UserviewService userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");
                Userview userview = userviewMenu.getUserview();
                String userviewId = userview.getPropertyString(FormUtil.PROPERTY_ID);
                String menuId = userviewService.getMenuId(userviewMenu);
                clearCachedContent(userviewId, menuId, scope);
                return;
            }
            Integer duration = null;
            String durationStr = userviewMenu.getPropertyString(PROPERTY_DURATION);
            if (durationStr != null && !durationStr.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr);
                } catch(Exception e) {
                    // ignore
                }
            }
            String cacheKey = getCacheKey(userviewMenu, type, scope);
            Element element = new Element(cacheKey, content);
            if (duration != null && duration > 0) {
                element.setTimeToIdle(duration);
                element.setTimeToLive(duration);
            }
            cache.put(element);
            if (LogUtil.isDebugEnabled(UserviewCache.class.getName())) {    
                LogUtil.debug(UserviewCache.class.getName(), "setCachedContent: " + cacheKey + ", duration " + duration + "s");
            }
        }
    }

    public static String getCachedContent(UserviewMenu userviewMenu, String type) {
        String content = null;
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("userviewMenuCache");
        if (cache != null) {
            String scope = userviewMenu.getPropertyString(PROPERTY_SCOPE);
            if (scope == null || scope.isEmpty()) {
                return null;
            }
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null && !"GET".equals(request.getMethod())) {
                return null;
            }
            String cacheKey = getCacheKey(userviewMenu, type, scope);
            Element element = cache.get(cacheKey);
            if (element != null) {
                content = (String) element.getObjectValue();
                if (LogUtil.isDebugEnabled(UserviewCache.class.getName())) {    
                    LogUtil.debug(UserviewCache.class.getName(), "getCachedContent: " + cacheKey);
                }
            }
        }
        return content;
    }

    public static String getCacheKey(UserviewMenu userviewMenu, String type, String scope) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        UserviewService userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");
        Userview userview = userviewMenu.getUserview();
        String userviewKey = userview.getPropertyString(Userview.USERVIEW_KEY_VALUE);
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        String userviewId = userview.getPropertyString(FormUtil.PROPERTY_ID);
        String menuId = userviewService.getMenuId(userviewMenu);
        scope = (CACHE_SCOPE_APPLICATION.equals(scope)) ? CACHE_SCOPE_APPLICATION : workflowUserManager.getCurrentUsername();
        String params = "";
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            params = request.getQueryString();
            if (params == null) {
                params = (String)request.getAttribute("javax.servlet.forward.query_string");
            }
        }
        return CACHE_KEY_PREFIX + ":" + profile + ":" + appDef.getAppId() + ":" + userviewId + ":" + menuId + ":" + scope + ":" + userviewKey + ":" + type + ":" + params;
    }
    
}
