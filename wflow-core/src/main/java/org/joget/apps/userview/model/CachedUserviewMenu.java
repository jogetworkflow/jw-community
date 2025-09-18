package org.joget.apps.userview.model;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.displaytag.tags.TableTagParameters;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.AjaxUniversalTheme;
import org.joget.apps.userview.service.UserviewCache;
import org.joget.apps.util.DefaultPropertyValuesCache;
import org.joget.commons.cache.InMemoryCacheManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.osgi.framework.BundleContext;

public class CachedUserviewMenu extends UserviewMenu {

    private UserviewMenu delegate;
    private static Cache userviewMenuCache;
    
    public CachedUserviewMenu() {
    }
    
    public CachedUserviewMenu(UserviewMenu delegate) {
        this.delegate = delegate;
    }

    public UserviewMenu getDelegate() {
        return this.delegate;
    }
    
    /**
     * Return plugin label. This value will be used when a Resource Bundle
     * Message Key "<i>plugin.className</i>.pluginlabel" is not found by getI18nLabel() method.
     *
     * @return
     */
    @Override
    public String getLabel() {
        return delegate.getLabel();
    }

    /**
     * Return Class Name for the plugin.
     * @return
     */
    @Override
    public String getClassName() {
        return delegate.getClassName();
    }

    /**
     * Return a unique name for the plugin. You can override a existing System plugin by providing the same name as the System plugin.
     *
     * @return
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * Return plugin version
     *
     * @return
     */
    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    /**
     * Return a plugin description. This value will be used when a Resource
     * Bundle Message Key "plugin.className.pluginDesc" is not found by getI18nDescription method.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    /**
     * Method used by Felix OSGI framework to register the plugin
     *
     * @param context
     */
    public void start(BundleContext context) {
        delegate.start(context);
    }

    /**
     * Method used by Felix OSGI framework to unregister the plugin
     *
     * @param context
     */
    public void stop(BundleContext context) {
        delegate.stop(context);
    }

    /**
     * Return a plugin label for the plugin based on language setting.
     *
     * It will auto look for Resource Bundle Message Key "<i>plugin.className</i>.pluginLabel".
     * If resource key not found, org.joget.plugin.property.model.PropertyEditable.getLabel()
     * will be use if the plugin also implemented org.joget.plugin.property.model.PropertyEditable
     * interface. Else, value from getName() method is use. OSGI plugin is required
     * to override this method to provide an internationalization label.
     *
     * @return
     */
    public String getI18nLabel() {
        return delegate.getI18nLabel();
    }

    /**
     * Return a plugin description for the plugin based on language setting.
     *
     * It will auto look for Resource Bundle Message Key "<i>plugin.className</i>.pluginDesc".
     * If resource key not found, value from org.joget.plugin.base.Plugin.getDescription() is use.
     * OSGI plugin is required to override this method to provide an internationalization description.
     *
     * @return
     */
    public String getI18nDescription() {
        return delegate.getI18nDescription();
    }

    /**
     * Get plugin properties.
     * @return
     */
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    /**
     * Set plugin properties.
     * @param properties
     */
    public void setProperties(Map<String, Object> properties) {
        delegate.setProperties(properties);
    }

    /**
     * Get a plugin property value by property key.
     *
     * @param property
     */
    public Object getProperty(String property) {
        return delegate.getProperty(property);
    }

    /**
     * Get a plugin property value by property key and return in java.lang.String. Non-exist key
     * will return an empty string instead of NULL value.
     *
     * @param property
     */
    public String getPropertyString(String property) {
        return delegate.getPropertyString(property);
    }

    /**
     * Set a plugin property
     *
     * @param property A property key
     * @param value
     */
    public void setProperty(String property, Object value) {
        delegate.setProperty(property, value);
    }

    /**
     * Return a set of plugin properties to configure by admin user
     *
     * @Deprecated Since version 3, Joget introduced a better UI for plugin
     * configuration. A plugin should implement org.joget.plugin.property.model.PropertyEditable
     * interface to provide the plugin configuration options.
     *
     * @return
     */
    public PluginProperty[] getPluginProperties() {
        return delegate.getPluginProperties();
    }

    /**
     * To execute a plugin
     *
     * @Deprecated This method is only use by Process Tool plugin therefore it had
     * been moved to org.joget.plugin.base.DefaultApplicationPlugin
     *
     * @param pluginProperties Properties to be used by the plugin during execution
     *
     * @return
     */
    public Object execute(Map properties) {
        return delegate.execute(properties);
    }

    /**
     * Gets request parameters
     * @return
     */
    public Map getRequestParameters() {
        return delegate.getRequestParameters();
    }

    /**
     * Sets request parameters
     * @param requestParameters
     */
    public void setRequestParameters(Map requestParameters) {
        delegate.setRequestParameters(requestParameters);
    }

    /**
     * Convenience method to get a parameter value
     * @param requestParameter
     * @return
     */
    public Object getRequestParameter(String requestParameter) {
        return delegate.getRequestParameter(requestParameter);
    }

    /**
     * Convenience method to get a parameter String value
     * @param requestParameter
     * @return Empty string instead of null.
     */
    public String getRequestParameterString(String requestParameter) {
        return delegate.getRequestParameterString(requestParameter);
    }

    /**
     * Gets URL of this menu
     *
     * @return
     */
    public String getUrl() {
        return delegate.getUrl();
    }

    /**
     * Sets URL of this menu
     *
     * @param url
     */
    public void setUrl(String url) {
        delegate.setUrl(url);
    }

    /**
     * Gets userview key of this menu
     *
     * @return
     */
    public String getKey() {
        return delegate.getKey();
    }

    /**
     * Sets userview key of this menu
     *
     * @return
     */
    public void setKey(String key) {
        delegate.setKey(key);
    }

    /**
     * Category to be displayed in Userview Builder palette
     * @return
     */
    public String getCategory() {
        return delegate.getCategory();
    }

    /**
     * Icon path to be displayed in Userview Builder palette
     * @return
     */
    public String getIcon() {
        return delegate.getIcon();
    }

    /**
     * Get render HTML template for UI
     * @return
     */
    public String getRenderPage() {
        return delegate.getRenderPage();
    }

    /**
     * Used to determine this menu item can used as home page or not.
     * @return
     */
    public boolean isHomePageSupported() {
        return delegate.isHomePageSupported();
    }

    /**
     * Get path of JSP file to render the HTML template.
     *
     * If this value is not NULL, value returned by getRenderPage will be ignored.
     * It is used to use the system predefined template for rendering.
     * Options are as following:
     * - userview/plugin/datalist.jsp
     * - userview/plugin/form.jsp
     * - userview/plugin/runProcess.jsp
     * - userview/plugin/unauthorized.jsp
     * @return
     */
    public String getJspPage() {
        return delegate.getJspPage();
    }

    /**
     * Used by the system to retrieve the JSP file page to avoid the logic to run again.
     * It will called the getJspPage method once to initial the value.
     *
     * @return
     */
    public String getReadyJspPage() {
        return delegate.getReadyJspPage();
    }

    /**
     * Used by the system to retrieve the HTML template to avoid the logic to run again.
     * It will called the getRenderPage method once to initial the value.
     *
     * @return
     */
    public String getReadyRenderPage() {
        return delegate.getReadyRenderPage();
    }

    /**
     * Gets the userview which this menu is belongs to.
     * @return
     */
    public Userview getUserview() {
        return delegate.getUserview();
    }

    /**
     * Sets the userview which this menu is belongs to.
     * @param userview
     */
    public void setUserview(Userview userview) {
        delegate.setUserview(userview);
    }

    /**
     * Set this property to force the userview to redirect to a specific URL.
     * @param redirectUrl
     */
    public void setRedirectUrl(String redirectUrl) {
        delegate.setRedirectUrl(redirectUrl);
    }

    /**
     * Set this property to force the userview to redirect to a specific URL
     * with option to redirect in the parent window
     *
     * @param redirectUrl
     * @param redirectToParent set true to force redirection in parent frame.
     */
    public void setRedirectUrl(String redirectUrl, boolean redirectToParent) {
        delegate.setRedirectUrl(redirectUrl, redirectToParent);
    }

    /**
     * Set this property to display an alert message/prompt.
     * @param message
     */
    public void setAlertMessage(String message) {
        delegate.setAlertMessage(message);
    }

    public boolean equals(Object o) {
        Object target = o;
        if (o instanceof CachedUserviewMenu) {
            target = ((CachedUserviewMenu) o).delegate;
        }
        return this.delegate.equals(target);
    }
    
    public boolean instanceOf(Class clazz) {
        return clazz.isInstance(this.delegate);
    }

    public int hashCode() {
        return this.delegate.hashCode();
    }

    /**
     * Get Decorated menu HTML for rendering
     * @return
     */
    public String getDecoratedMenu() {
        return delegate.getDecoratedMenu();
    }

    /**
     * Get menu html for rendering. It will call getDecoratedMenu method
     * to retrieve the menu HTML. If empty value is return, a default menu
     * HTML will be generated based on getURL method and "label" property.
     * @return
     */
    public String getMenu() {
        String content = UserviewCache.getCachedContent(delegate, UserviewCache.CACHE_TYPE_MENU);
        if (content == null) {
            content = delegate.getMenu();
            UserviewCache.setCachedContent(delegate, UserviewCache.CACHE_TYPE_MENU, content);
        }
        return content;
    }

    /**
     * Return the plugin properties options in JSON format.
     * @return
     */
    @Override
    public String getPropertyOptions() {
        String propertyOptions = PropertyUtil.injectHelpLink(delegate.getHelpLink(), delegate.getPropertyOptions());
        String offlineOptions = "";
        String menuOfflineOptions = delegate.getOfflineOptions();
        if (menuOfflineOptions != null && !menuOfflineOptions.isEmpty()) {
            offlineOptions = ",{label : '@@userview.offline.offlineSetting@@', type : 'header', description : '@@userview.offline.desc@@'}";
            offlineOptions += "," + menuOfflineOptions;
        } else {
            offlineOptions = ",{label : '@@userview.offline.noOfflineSupport@@', type : 'header'}";
        }
        
        String cacheOptions = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/userviewCache.json", new String[]{offlineOptions}, true, "message/userview/userviewCache");
        if (cacheOptions != null && !cacheOptions.isEmpty()) {
            propertyOptions = propertyOptions.substring(0, propertyOptions.lastIndexOf("]")) + "," + cacheOptions + "]"; 
        }
        return propertyOptions;
    }
    
    public String getDefaultPropertyValues(){
        return DefaultPropertyValuesCache.getDefaultPropertyValues(delegate);
    }
    
    @Override
    public String getOfflineOptions() {
        return delegate.getOfflineOptions();
    }
    
    @Override
    public Set<String> getOfflineCacheUrls() {
        return delegate.getOfflineCacheUrls();
    }
    
    public String getPwaValidationType() {
        if (delegate instanceof PwaOfflineValidation) {
            return "checking";
        } else if (delegate instanceof PwaOfflineReadonly) {
            return "readonly";
        } else if (delegate instanceof PwaOfflineNotSupported) {
            return "notSupported";
        } else {
            return "supported";
        }
    }
    
    @Override
    public String getBuilderJavaScriptTemplate() {
        return delegate.getBuilderJavaScriptTemplate();
    }
    
    @Override
    public String render() {
        boolean isBuilder = "true".equalsIgnoreCase(getRequestParameterString("isBuilder"));
        if (isBuilder) {
            return delegate.render();
        } else {
            String content = UserviewCache.getCachedContent(delegate, UserviewCache.CACHE_TYPE_PAGE);
            if (content == null) {
                Cache menuAsyncCache = getUserviewMenuAsyncCache();
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

                // check for cache availability, ajax theme, list export, page component and non-GET request for async request support
                HttpSession session = request.getSession(false);
                String sessionId = (session != null) ? session.getId() : null;
                String cacheKey = UserviewCache.getCacheKey(delegate, UserviewCache.CACHE_TYPE_PAGE, null) + ":" + sessionId;
                boolean isAjaxTheme = getUserview().getSetting().getTheme() instanceof AjaxUniversalTheme;
                boolean isExportRequest = request.getParameter(TableTagParameters.PARAMETER_EXPORTING) != null;
                boolean isPageComponent = cacheKey.contains(":pc-");

                if (isExportRequest || isPageComponent || menuAsyncCache == null || !isAjaxTheme || !"GET".equals(request.getMethod())) {
                    // cache not available, default rendering
                    content = delegate.render();
                    UserviewCache.setCachedContent(delegate, UserviewCache.CACHE_TYPE_PAGE, content);
                } else {
                    // use cache to store content asynchronously if required
                    String loadingInProgressContent = getAsyncLoadingInProgressContent();
                    try {

                        // lookup from cache
                        content = (String)menuAsyncCache.get(cacheKey);
                        if (content != null && !content.isEmpty()) {
                            if (!loadingInProgressContent.equals(content)) {
                                // loading not in progress anymore, remove from cache so that subsequent calls will refresh
                                menuAsyncCache.remove(cacheKey);
                            }
                            return content;
                        }
                        
                        // remove existing cached content
                        menuAsyncCache.remove(cacheKey);

                        // load content asynchronously
                        LogUtil.debug(getClass().getName(), "Loading async content for " + cacheKey);
                        long timeout = PluginThread.getAsyncRequestTimeout();
                        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                        content = WorkflowUtil.executeAsync(() -> {
                            // set current appDef in thread
                            AppUtil.setCurrentAppDefinition(appDef);
                            
                            // call actual userview menu to render
                            String newContent = delegate.render();
                            
                            // add cache timestamp to content
                            String currentContent = (String)menuAsyncCache.get(cacheKey);
                            if (loadingInProgressContent.equals(currentContent)) {
                                newContent += getAsyncTimestampContent();
                            }
                            UserviewCache.setCachedContent(delegate, UserviewCache.CACHE_TYPE_PAGE, newContent);

                            // store in cache
                            menuAsyncCache.put(cacheKey, newContent);
                            LogUtil.debug(getClass().getName(), "Loaded async content for " + cacheKey);
                            return newContent;
                        }, timeout);
                        if (!loadingInProgressContent.equals(content)) {
                            // loading not in progress anymore, remove from cache
                            menuAsyncCache.remove(cacheKey);
                        }
                    } catch (TimeoutException e) {
                        // timed out, return pending content
                            content = loadingInProgressContent;
                            menuAsyncCache.put(cacheKey, content);
                        
                        // add slight delay to prevent blank response in JBoss EAP
                        try {
                            Thread.sleep(100);
                        } catch(InterruptedException ie) {
                            // ignore
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        // don't cache if exception encountered
                        menuAsyncCache.remove(cacheKey);
                    } catch (Throwable e) {
                        // don't cache if exception encountered
                        menuAsyncCache.remove(cacheKey);
                    }
                }
            }
            return content;
        }
    }

    /**
     * Return content to render when async loading is in progress.
     * @return 
     */
    protected String getAsyncLoadingInProgressContent() {
        String loadingInProgressScript = "<script>$(function() { AjaxUniversalTheme.triggerAsyncLoading() })</script>";
        String loadingInProgressContent = "<div class=\"async-loading\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.asyncLoading") + loadingInProgressScript + "</div>";
        return loadingInProgressContent;
    }

    /**
     * Return content to show async timestamp.
     * @return 
     */
    protected String getAsyncTimestampContent() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String timestamp = sdf.format(new Date());
        String timestampContent = "<div class=\"async-timestamp\">" + timestamp + "</div>";
        return timestampContent;
    }
    
    /**
     * Get the userview menu cache as a singleton object.
     * @return 
     */
    public static Cache getUserviewMenuAsyncCache() {        
        if (userviewMenuCache == null) {
            String regionName = "userview-menu-region";
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            userviewMenuCache = cacheManager.getCache(regionName, 0);
        }
        return userviewMenuCache;
    }
    
    @Override
    public String render(String id, String cssClass, String style, String attributes, boolean isBuilder) {
        return delegate.render(id, cssClass, style, attributes, isBuilder);
    }
    
    @Override
    public String getContentPlaceholderRules() {
        return delegate.getContentPlaceholderRules();
    }
    
    @Override
    public String getDeveloperMode() {
        return delegate.getDeveloperMode();
    }

    @Override
    public Set<String> getOfflineStaticResources() {
        return delegate.getOfflineStaticResources();
    }

    @Override
    public void setRedirectUrlToWindow(String redirectUrl, String windowType) {
        delegate.setRedirectUrlToWindow(redirectUrl, windowType);
    }

    @Override
    public String getPluginIcon() {
        return delegate.getPluginIcon();
    }

    @Override
    public boolean isUiMenu() {
        return delegate.isUiMenu();
    }

    @Override
    public String renderChildren() {
        return delegate.renderChildren();
    }

    @Override
    public String getAdditionalStyle(String styleClass, String selector, String prefix) {
        return delegate.getAdditionalStyle(styleClass, selector, prefix);
    }

    @Override
    public String getAdditionalStyle(String styleClass) {
        return delegate.getAdditionalStyle(styleClass);
    }

    @Override
    public boolean isPermissionHidden() {
        return delegate.isPermissionHidden();
    }

    @Override
    public void setChildren(Collection<PageComponent> children) {
        delegate.setChildren(children);
    }

    @Override
    public Collection<PageComponent> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public void setParent(PageComponent parent) {
        delegate.setParent(parent);
    }

    @Override
    public PageComponent getParent() {
        return delegate.getParent();
    }

    @Override
    public boolean isHiddenPlugin() {
        return delegate.isHiddenPlugin();
    }

    @Override
    public String getHelpLink() {
        return delegate.getHelpLink();
    }
}
