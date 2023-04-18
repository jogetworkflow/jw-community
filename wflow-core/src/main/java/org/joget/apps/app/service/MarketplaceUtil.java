package org.joget.apps.app.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class MarketplaceUtil {
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
     * Download template from marketplace by on app id
     * @param id
     * @return 
     */
    public static byte[] downloadTemplate(String id) {
        // get URL InputStream
        HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy());
        CloseableHttpClient client = builder.build();
        InputStream in = null;
        try {
            id = StringUtil.stripAllHtmlTag(id);
        
            String marketPlaceUrl = ResourceBundleUtil.getMessage("appCenter.link.marketplace.url");
            String url = marketPlaceUrl + "/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + URLEncoder.encode(id, "UTF-8");
        
            HttpGet get = new HttpGet(url);
            HttpResponse httpResponse = client.execute(get);
            in = httpResponse.getEntity().getContent();

            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
                // read InputStream
                return readInputStream(in);
            }
        } catch (Exception e) {
            LogUtil.error(MarketplaceUtil.class.getName(), e, "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException e) {
            }
            try {
                client.close();
            } catch(IOException e) {
            }
        }
        
        return null;
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
                                newList.add("<a href=\""+marketPlaceUrl+"/jw/web/userview/mp/mpp/_/vad?id="+obj.getString("id")+"\" target=\"_blank\">"+obj.getString("name")+"</a>");
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
     * Update the cache by calling the market api
     */
    public static void update() {
        Date now = new Date();
        
        if (lastRetrieve == null || Math.abs(now.getTime() - lastRetrieve.getTime()) > 3600000) { //1 hour
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
}
