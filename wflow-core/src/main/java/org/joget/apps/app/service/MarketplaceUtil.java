package org.joget.apps.app.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MarketplaceUtil {
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final long RETRIEVE_INTERVAL = 7200000; //2 hours
    public static String TYPE_APP = "App";
    public static String TYPE_Plugin = "Plugin";
    public static String TYPE_Template = "Template";
    private static JSONObject cache;
    private static Date lastRetrieve = null;
    
    /**
     * Retrieve marketplace product list
     * @param search
     * @param type
     * @param category
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public static JSONArray getList(String search, String type, String category, Boolean isNew, String sort, Boolean desc, Integer start, Integer rows) {
        update();
        
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            
            if (cache != null && cache.has("data")) {
                String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
                
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    
                    if (type != null && !type.isEmpty() && !type.equals(obj.getString("category"))) {
                        continue;
                    }
                    
                    if (category != null && !category.isEmpty() && !category.equals(obj.getString("subcategory"))) {
                        continue;
                    }
                    
                    if (search != null && !search.isEmpty() && !obj.getString("name").toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                    
                    if (isNew != null && isNew && !"New".equals(obj.get("isNew").toString())) {
                        continue;
                    }
                    
                    obj.put("url", marketPlaceUrl+"/jw/web/userview/mp/mpp/_/vad?id="+obj.getString("id"));
                    if (obj.has("img") && obj.getString("img").startsWith("/")) {
                        obj.put("img", marketPlaceUrl + obj.getString("img"));
                    }
                    
                    if (AppUtil.isEnterprise() || (!AppUtil.isEnterprise() && obj.getString("edition").contains("Community Edition"))) {
                        list.add(obj);
                    }
                }
                
                if (sort != null && !sort.isEmpty()) {
                    list = sort(list, sort, desc);
                }
                
                if (start != null || rows != null) {
                    list = subList(list, start, rows);
                }
                
                JSONArray arr = new JSONArray();
                for (JSONObject o : list) {
                    arr.put(o);
                }
                return arr;
            }
            
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        return new JSONArray();
    }
    
    /**
     * Get all available templates as options
     * 
     * @return 
     */
    public static Map<String, String> getTemplateOptions() {
        update();
        
        Map<String, String> options = new HashMap<String, String>();
         
        try {
            if (cache != null && cache.has("data")) {
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    if (TYPE_Template.equals(obj.getString("category"))) {
                        if (AppUtil.isEnterprise() || (!AppUtil.isEnterprise() && obj.getString("edition").contains("Community Edition"))) {
                            options.put(obj.getString("id"), obj.getString("name"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        if (options != null && !options.isEmpty()) {
            List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(options.entrySet());

            // Sort list with Collections.sort(), 
            Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
                public int compare(Map.Entry<String, String> o1,
                                   Map.Entry<String, String> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });

            // Loop the sorted list and put it into a new insertion order Map LinkedHashMap
            options = new LinkedHashMap<String, String>();
            for (Map.Entry<String, String> entry : list) {
                options.put(entry.getKey(), entry.getValue());
            }
        }
        
        return options;
    }
    
    /**
     * Retrieve the template config based on id
     * @param id
     * @return 
     */
    public static JSONObject getTemplateConfig(String id) {
        update();
        JSONObject config = new JSONObject();
        
        try {
            if (cache != null && cache.has("data")) {
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    if (TYPE_Template.equals(obj.getString("category")) && obj.getString("id").equals(id) && obj.has("config")) { 
                        config = new JSONObject(obj.getString("config"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        return config;
    }
    
    /**
     * Download template from marketplace by id
     * @param id
     * @return 
     */
    public static byte[] downloadTemplate(String id) {
        // get URL InputStream
        HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy());
        try (CloseableHttpClient client = builder.build()) {
            id = StringUtil.stripAllHtmlTag(id);
        
            String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
            String url = marketPlaceUrl + "/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + URLEncoder.encode(id, "UTF-8");
        
            HttpGet get = new HttpGet(url);
            
            //authentication
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            String marketplaceAuth = setupManager.getSettingValue("marketplaceAuth");
            if (marketplaceAuth != null && !marketplaceAuth.isEmpty()) {
                get.addHeader("referer", WorkflowUtil.getHttpServletRequest().getRequestURL().toString());
                get.addHeader("Authorization", "Basic " + SecurityUtil.decrypt(marketplaceAuth));
            }
                
            HttpResponse httpResponse = client.execute(get);
            
            try (InputStream in = httpResponse.getEntity().getContent()) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
                    // read InputStream
                    return readInputStream(in);
                }
            }
        } catch (Exception e) {
            LogUtil.warn(MarketplaceUtil.class.getName(), "Fail to download (" + id + ")");
        }
        return null;
    }
    
    /**
     * Download and install plugin from marketplace by id
     * @param id
     * @return 
     */
    public static void downloadAndInstallPlugin(String id) {
        // get URL InputStream
        HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy());
        try (CloseableHttpClient client = builder.build()) {
            id = StringUtil.stripAllHtmlTag(id);
        
            String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
            String url = marketPlaceUrl + "/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + URLEncoder.encode(id, "UTF-8");
        
            HttpGet get = new HttpGet(url);
            
            //authentication
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            String marketplaceAuth = setupManager.getSettingValue("marketplaceAuth");
            if (marketplaceAuth != null && !marketplaceAuth.isEmpty()) {
                get.addHeader("referer", WorkflowUtil.getHttpServletRequest().getRequestURL().toString());
                get.addHeader("Authorization", "Basic " + SecurityUtil.decrypt(marketplaceAuth));
            }
                
            HttpResponse httpResponse = client.execute(get);
            
            try (InputStream in = httpResponse.getEntity().getContent()) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
                    String filename = "";
                    //get all headers		
                    Header[] headers = httpResponse.getAllHeaders();
                    for (Header header : headers) {
                        if ("Content-Disposition".equalsIgnoreCase(header.getName())) {
                            filename = header.getValue().substring(header.getValue().indexOf("filename=") + 9);
                            break;
                        }
                    }

                    if (filename.endsWith(".jar")) {
                        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                        pluginManager.upload(filename, in);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.warn(MarketplaceUtil.class.getName(), "Fail to download (" + id + ")");
        }
    }
    
    /**
     * Return the marketplace plugin name & url based on class name
     * @param plugins
     * @return 
     */
    public static List<String> pluginClassToMarketplaceLink(List<String> plugins) {
        update();
        String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
        
        try {
            if (cache != null && cache.has("data")) {
                Set<String> found = new HashSet<String>();
                Set<String> newList = new HashSet<String>();
        
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    if (TYPE_Plugin.equals(obj.getString("category")) && obj.has("classes")) {
                        JSONArray classes = obj.getJSONArray("classes");
                        for (int j = 0 ; j < classes.length(); j++) {
                            String className = classes.getString(j);
                            int index = plugins.indexOf(className);
                            if (index != -1) {
                                found.add(className);
                                newList.add("<a class=\"marketplace-plugin\" data-id=\""+StringUtil.escapeString(obj.getString("id"), StringUtil.TYPE_HTML)+"\" href=\""+marketPlaceUrl+"/jw/web/userview/mp/mpp/_/vad?id="+StringUtil.escapeString(obj.getString("id"), StringUtil.TYPE_HTML)+"\" target=\"_blank\">"+StringUtil.escapeString(obj.getString("name"), StringUtil.TYPE_HTML)+"</a>");
                            }
                        }
                    }
                }
                
                for (String p : plugins) {
                    if (!found.contains(p)) {
                        newList.add(p);
                    }
                }
                
                plugins = new ArrayList<String>(newList);
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        return plugins;
    }
    
    /**
     * Based on the plugins used in AppDef, auto install/update it from marketplace
     * 
     * @param AppDef 
     */
    public static void autoInstallUpdatePlugins(AppDefinition appDef) {
        update();
        
        //find plugins used in the app
        List<String> plugins = AppUtil.findCustomPlugins(appDef, false, false);
        
        //get all installed plugins version
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map<String, Object> installedPlugins = pluginManager.getInstalledBundles(null, null, true);
        
        //compare version with marketplace
        Set<String> needUpdate = new HashSet<String>();
        try {
            if (cache != null && cache.has("data")) {
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    if (TYPE_Plugin.equals(obj.getString("category")) && obj.has("classes")) {
                        boolean found = false;
                        JSONArray classes = obj.getJSONArray("classes");
                        for (int j = 0 ; j < classes.length(); j++) {
                            String className = classes.getString(j);
                            int index = plugins.indexOf(className);
                            if (index != -1) {
                                found = true;
                            }
                        }
                        
                        if (found) {
                            //check for plugin installed or update available
                            String[] nameVersion = retrieveNameAndVersion(obj.getString("fileName"));
                            if (nameVersion != null && (installedPlugins.containsKey(nameVersion[0]) || installedPlugins.containsKey(nameVersion[0].replaceAll("_", "-")))) {
                                String version = (String) installedPlugins.get(nameVersion[0]);
                                if (version == null) {
                                    version = (String) installedPlugins.get(nameVersion[0].replaceAll("_", "-"));
                                }
                                if (compareVersion(version,nameVersion[1]) < 0) {
                                    needUpdate.add(obj.getString("id"));
                                }
                            } else {
                                needUpdate.add(obj.getString("id"));
                            }
                        }
                    }
                    
                    if (plugins.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        //install/update each of the plugins
        for (String id : needUpdate) {
            downloadAndInstallPlugin(id);
        }
    }
    
    /**
     * Update the cache by calling the market api
     */
    public static void update() {
        Date now = new Date();
        
        if (lastRetrieve == null || Math.abs(now.getTime() - lastRetrieve.getTime()) > RETRIEVE_INTERVAL) { 
            String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");

            try {
                // get URL InputStream
                HttpClientBuilder builder = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy())
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(3000) // 3 seconds
                                .setSocketTimeout(30000) // 30 seconds
                                .build());
                CloseableHttpClient client = builder.build();
                
                try {
                    String lastUpdate = "";
                    if (cache != null) {
                        lastUpdate = "?last_update="+URLEncoder.encode(cache.get("lastUpdateDate").toString(), "UTF-8");
                    }

                    HttpGet get = new HttpGet(marketPlaceUrl + "/jw/web/json/plugin/org.joget.marketplace.MarketplaceApi/service"+lastUpdate);
                    
                    //authentication
                    SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
                    String marketplaceAuth = setupManager.getSettingValue("marketplaceAuth");
                    if (marketplaceAuth != null && !marketplaceAuth.isEmpty()) {
                        get.addHeader("referer", WorkflowUtil.getHttpServletRequest().getRequestURL().toString());
                        get.addHeader("Authorization", "Basic " + SecurityUtil.decrypt(marketplaceAuth));
                    }
                    
                    HttpResponse httpResponse = client.execute(get);

                    if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
                        String jsonResponse = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

                        JSONObject data = new JSONObject(jsonResponse);
                        if (cache == null || (data.has("lastUpdateDate") && !cache.get("lastUpdateDate").equals(cache.get("lastUpdateDate")))) {
                            cache = data;
                        }
                    }
                } finally {
                    try {
                        client.close();
                    } catch(IOException e) {
                    }
                }
            } catch (Exception e) {
                LogUtil.warn(MarketplaceUtil.class.getName(), "Fail to retrieve data from marketplace.");
            }
            lastRetrieve = now;
        }
    }
    
    /**
     * Sort the list 
     * 
     * @param data
     * @param sort
     * @param desc
     * @return 
     */
    protected static List<JSONObject> sort(List<JSONObject> data, final String sort, final boolean desc) {
        
        Collections.sort(data, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject a, JSONObject b) {
                try {
                if (a.has(sort) && b.has(sort)) {
                    int result = 0;

                    if (a.get(sort) instanceof Number) {
                        result = (int) Math.round(a.getDouble(sort) - b.getDouble(sort));
                    } else if (a.get(sort) instanceof String) {
                        result = a.getString(sort).compareTo(b.getString(sort));
                    } else {
                        result = a.get(sort).toString().compareTo(b.get(sort).toString());
                    }

                    if (desc) {
                        result = - result;
                    }
                    return result;
                } else if (desc && !a.has(sort)) {
                    return 1;
                } else if (!desc && !b.has(sort)) {
                    return -1;
                } else {
                    return 0;
                }

                } catch (Exception e) {}
                return 0;
            }
        });
        
        
        return data;
    }
    
    /**
     * Paging the data 
     * 
     * @param data
     * @param start
     * @param rows
     * @return 
     */
    protected static List<JSONObject> subList(List<JSONObject> data, Integer start, Integer rows) {
        if (data == null) {
            return null;
        }
        int total = data.size();
        if (total > 0) {
            int begin = (start != null) ? start : 0;
            int end;
            if (begin < 0) {
                begin = 0;
            }
            if (rows == null || rows < 0) {
                end = total;
            } else {
                end = begin + rows;
            }
            if (end > total) {
                end = total;
            }
            List newList = data.subList(begin, end);
            return newList;
        } else {
            return data;
        }
    }
    
    /**
     * Reads a specified InputStream, returning its contents in a byte array
     * @param in
     * @return
     * @throws IOException 
     */
    protected static byte[] readInputStream(InputStream in) throws IOException {
        byte[] fileContent;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            BufferedInputStream bin = new BufferedInputStream(in);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = bin.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
            fileContent = out.toByteArray();
            return fileContent;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                LogUtil.warn(MarketplaceUtil.class.getName(), "Fail to retrieve template from marketplace.");
            }
        }
    }
    
    /**
     * Validate the username and password to login marketplace
     * 
     * @param username
     * @param password
     * @return 
     */
    public static boolean login(String username, String password) {
        HttpURLConnection connection = null;
        try {
            String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
            
            String authorizationHeader = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            
            URL siteURL = new URI(marketPlaceUrl + "/jw/web/json/directory/user/sso").toURL();
            
            connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("referer", WorkflowUtil.getHttpServletRequest().getRequestURL().toString());
            connection.setRequestProperty("Authorization", "Basic " + authorizationHeader);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.connect();

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                Setting marketplaceAuth = new Setting();
                marketplaceAuth.setProperty("marketplaceAuth");
                marketplaceAuth.setValue(SecurityUtil.encrypt(authorizationHeader));
                
                SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
                setupManager.saveSetting(marketplaceAuth);
                
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "Fail to login to Marketplace");
        } finally {
            connection.disconnect();
        }
        
        return false;
    }
    
    /**
     * Retrieve the cached categories for seamless marketplace plugin explorer
     * @param jogetComponent
     * @return 
     */
    public static JSONObject listCategories(String jogetComponent) {
        update();
        
        JSONObject result = new JSONObject();
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            
            if (cache != null && cache.has("plugin_categories")) {
                
                JSONArray c = cache.getJSONArray("plugin_categories");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    
                    if (jogetComponent != null && !jogetComponent.isEmpty() && !jogetComponent.equalsIgnoreCase(obj.getString("component"))) {
                        continue;
                    }
                    
                    list.add(obj);
                }
            }
            
            result.put("data", list);
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        return result;
    }
    
    /**
     * Retrieve the cached plugin list for seamless marketplace plugin explorer
     * 
     * @param search
     * @param categories
     * @param classes
     * @return 
     */
    public static JSONObject listPlugins(String search, List<String> categories, List<String> classes) {
        update();
        
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map<String, Object> installedPlugins = pluginManager.getInstalledBundles(classes, null, true);
        
        JSONObject result = new JSONObject();
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            
            if (cache != null && cache.has("data")) {
                String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
                
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    
                    if (!TYPE_Plugin.equals(obj.getString("category"))) {
                        continue;
                    }
                    
                    if (!categories.contains(obj.getString("subcategory"))) {
                        continue;
                    }
                    
                    if (search != null && !search.isEmpty() && !obj.getString("name").toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                    
                    JSONObject clone = new JSONObject(obj.toString());
                    
                    clone.put("url", marketPlaceUrl+"/jw/web/userview/mp/mpp/_/vad?id="+clone.getString("id"));
                    if (clone.has("img") && clone.getString("img").startsWith("/")) {
                        clone.put("img", marketPlaceUrl + clone.getString("img"));
                    }
                    
                    //check for plugin installed or update available
                    String[] nameVersion = retrieveNameAndVersion(clone.getString("fileName"));
                    if (nameVersion != null && (installedPlugins.containsKey(nameVersion[0]) || installedPlugins.containsKey(nameVersion[0].replaceAll("_", "-")))) {
                        String version = (String) installedPlugins.get(nameVersion[0]);
                        if (version == null) {
                            version = (String) installedPlugins.get(nameVersion[0].replaceAll("_", "-"));
                        }
                        clone.put("installed", version);
                        if (compareVersion(version,nameVersion[1]) < 0) {
                            clone.put("update", true);
                        }
                    }
                    
                    if (AppUtil.isEnterprise() || (!AppUtil.isEnterprise() && clone.getString("edition").contains("Community Edition"))) {
                        list.add(clone);
                    }
                }
            }
            
            result.put("data", list);
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        return result;
    }
   
    /**
     * Based on the installed plugin jar bundle, populate the label & description from marketplace if available
     * 
     * @param appDef
     * @param search
     * @param clazz
     * @param isUpdate
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public static JSONObject getInstalledBundledList(AppDefinition appDef, String search, String clazz, Boolean isUpdate, String sort, Boolean desc, Integer start, Integer rows) {
        update();
        
        JSONObject jsonObject = new JSONObject();
        try {
            List<String> classes = null;
            if (clazz != null && !clazz.isEmpty()) {
                classes = new ArrayList<String>();
                classes.add(clazz);
            }
            
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            
            //find plugins used in the app
            List<String> filtePluginClasses = null;
            if (appDef != null) {
                filtePluginClasses = AppUtil.findCustomPlugins(appDef, false, false);
            }
            
            Map<String, Object> installedPlugins = pluginManager.getInstalledBundles(classes, filtePluginClasses, false);
            
            //populate with marketplace data
            if (cache != null && cache.has("data")) {
                String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
                
                JSONArray c = cache.getJSONArray("data");
                for (int i = 0 ; i < c.length(); i++) {
                    JSONObject obj = c.getJSONObject(i);
                    
                    if (!TYPE_Plugin.equals(obj.getString("category"))) {
                        continue;
                    }
                    
                    //check for plugin installed 
                    String[] nameVersion = retrieveNameAndVersion(obj.getString("fileName"));
                    if (nameVersion != null && (installedPlugins.containsKey(nameVersion[0]) || installedPlugins.containsKey(nameVersion[0].replaceAll("_", "-")))) {
                        Map bundle = (Map) installedPlugins.get(nameVersion[0]);
                        if (bundle == null) {
                            bundle = (Map) installedPlugins.get(nameVersion[0].replaceAll("_", "-"));
                        }
                        bundle.put("id", obj.getString("id"));
                        bundle.put("latestVersion", nameVersion[1]);
                        bundle.put("url", marketPlaceUrl+"/jw/web/userview/mp/mpp/_/vad?id="+obj.getString("id"));
                        bundle.put("label", obj.getString("name"));
                        bundle.put("description", retrieveBrief(obj.getString("brief")));
                    }
                }
            }
            
            List result = new ArrayList();
            result.addAll(installedPlugins.values());
            
            try {
                //sort with label
                Collections.sort(result, new Comparator<Object>() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        String label1 = (String) ((Map) o1).get("label");
                        String label2 = (String) ((Map) o2).get("label");
                        
                        if (label1 == null || label1.isEmpty() || label2 == null || label2.isEmpty()) {
                            return 0;
                        }
                        return label1.compareTo(label2);
                    }
                });
                
                int counter = 0;
                JSONArray arr = new JSONArray();
                
                for (Object o : result) {
                    Map bundle = (Map) o;
                    String label = (String) bundle.get("label");
                    
                    if (search != null && !search.isEmpty() && !label.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                    
                    if (isUpdate != null && isUpdate) {
                        String latest = (String) bundle.get("latestVersion");
                        String current = (String) bundle.get("version");
                        
                        if (latest == null || compareVersion(current, latest) >= 0) {
                            continue;
                        }
                    }

                    if (start == null || rows == null || (counter >= start && counter < start + rows)) {
                        arr.put(bundle);
                    }
                    counter++;
                }

                jsonObject.put("data", arr);
                jsonObject.put("total", counter);
                jsonObject.put("start", start);
            } catch (Exception ex) {
                LogUtil.error(MarketplaceUtil.class.getName(), ex, "");
            }
            
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        }
        
        return jsonObject;
    }
    
    protected static String[] retrieveNameAndVersion(String filename) {
        // Define the pattern to match name and version
        Pattern pattern = Pattern.compile("^(.*)-(\\d+.*).jar$");
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String version = matcher.group(2);
            return new String[]{name, version};
        }
        
        return null;
    }
    
    protected static String retrieveBrief(String text) {
        try {
            Document temp = Jsoup.parse(text);
            Element span = temp.select("span").first();
            if (span != null) {
                text = span.text();
            }
        } catch (Exception e) {
            LogUtil.debug(MarketplaceUtil.class.getName(), e.getMessage());
        }
        return text;
    }
    
    public static int compareVersion(String current, String latest) {
        if (current.equals(latest)) {
            return 0;
        }
        
        String[] parts1 = current.split("\\.|-");
        String[] parts2 = latest.split("\\.|-");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            String part1 = i < parts1.length ? parts1[i] : "";
            String part2 = i < parts2.length ? parts2[i] : "";

            int cmp = compareVersionParts(part1, part2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
    
    protected static int compareVersionParts(String part1, String part2) {
        boolean isPart1Numeric = part1.matches("\\d+");
        boolean isPart2Numeric = part2.matches("\\d+");

        if (isPart1Numeric && isPart2Numeric) {
            return Integer.compare(Integer.parseInt(part1), Integer.parseInt(part2));
        } else if (isPart1Numeric) {
            return 1; // Numeric parts are greater than non-numeric parts
        } else if (isPart2Numeric) {
            return -1; // Non-numeric parts are less than numeric parts
        } else {
            return compareVersionSuffixes(part1, part2); // Compare suffixes
        }
    }
    
    // Define the order of suffixes
    protected static final List<String> SUFFIX_ORDER = Arrays.asList(
            "SNAPSHOT", "ALPHA", "BETA", "M", "MILESTONE", "RC", "DEV", "", "GA");
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("([A-Za-z]+)(\\d*)"); //suffix with or without number

    protected static int compareVersionSuffixes(String suffix1, String suffix2) {
        Matcher matcher1 = SUFFIX_PATTERN.matcher(suffix1);
        Matcher matcher2 = SUFFIX_PATTERN.matcher(suffix2);

        if (matcher1.matches() && matcher2.matches()) {
            String text1 = matcher1.group(1);
            String text2 = matcher2.group(1);

            int index1 = SUFFIX_ORDER.indexOf(text1);
            int index2 = SUFFIX_ORDER.indexOf(text2);

            if (index1 == -1) index1 = SUFFIX_ORDER.size(); // Unknown suffixes go to the end
            if (index2 == -1) index2 = SUFFIX_ORDER.size(); // Unknown suffixes go to the end

            int cmp = Integer.compare(index1, index2);
            if (cmp != 0) {
                return cmp;
            }

            String numPart1 = matcher1.group(2);
            String numPart2 = matcher2.group(2);

            return compareVersionParts(numPart1, numPart2);
        }

        return suffix1.compareTo(suffix2);
    }
}
