package org.joget.apps.app.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MarketplaceUtil;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Lazy
public class MarketplaceController {
    
    @Autowired
    AppService appService;
    
    @Autowired
    SetupManager setupManager;
    
    @Autowired
    private PluginManager pluginManager;
    
    @RequestMapping({"/desktop/marketplace/app"})
    public String marketplaceApp(ModelMap model, @RequestParam(value = "url") String url) {
        boolean trusted = false;
        String trustedUrlsKey = "appCenter.link.marketplace.trusted";
        String trustedUrls = ResourceBundleUtil.getMessage(trustedUrlsKey);
        if (trustedUrls != null && !trustedUrls.isEmpty()) {
            StringTokenizer st = new StringTokenizer(trustedUrls, ",");
            while (st.hasMoreTokens()) {
                String trustedUrl = st.nextToken().trim();
                if (url.startsWith(trustedUrl)) {
                    trusted = true;
                    break;
                }
            }
        }
        
        if (trusted) {
            model.addAttribute("appUrl", url);
        } else {
            model.addAttribute("appUrl", "");
        }
        
        return "desktop/marketplaceApp";
    }
    
    @RequestMapping(value = "/json/marketplace/list", method = RequestMethod.GET)
    public void marketplaceList(HttpServletResponse response, Writer writer, @RequestParam(value = "callback", required = false) String callback, 
            @RequestParam(value = "search", required = false) String search, @RequestParam(value = "type", required = false) String type, 
            @RequestParam(value = "category", required = false) String category, @RequestParam(value = "isNew", required = false) Boolean isNew,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        AppUtil.writeJson(writer, MarketplaceUtil.getList(search, type, category, isNew, sort, desc, start, rows), callback);
    }
    
    @RequestMapping(value = "/json/marketplace/template/config", method = RequestMethod.GET)
    public void marketplaceTemplateConfig(HttpServletResponse response, Writer writer, @RequestParam(value = "id") String id) throws JSONException, IOException {
        AppUtil.writeJson(writer, MarketplaceUtil.getTemplateConfig(id), null);
    }
    
    @RequestMapping(value = "/json/apps/verify", method = RequestMethod.HEAD)
    public void verifyUrl(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String url) throws IOException {
        boolean trusted = validateTrustedUrl(url);
        if (!trusted) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Untrusted URL");
            return;
        }
        
        CloseableHttpClient client = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        try {
            HttpHead head = new HttpHead(url);
            HttpResponse httpResponse = client.execute(head);
            response.setStatus(httpResponse.getStatusLine().getStatusCode());
        } finally {
            client.close();
        }
    }
    
    @RequestMapping(value = "/json/apps/install", method = RequestMethod.POST)
    public void installMarketplaceApp(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "callback", required = false) String callback, @RequestParam("url") final String url) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        
        // validate trusted URL
        boolean trusted = validateTrustedUrl(url);
        if (!trusted) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Untrusted URL");
            return;
        }
        
        // get URL InputStream
        HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy());
        CloseableHttpClient client = builder.build();
        InputStream in = null;
        try {
            HttpGet get = new HttpGet(url);
            
            //authentication
            String marketplaceAuth = setupManager.getSettingValue("marketplaceAuth");
            if (marketplaceAuth != null && !marketplaceAuth.isEmpty()) {
                get.setHeader("Authorization", "Basic " + SecurityUtil.decrypt(marketplaceAuth));
            }
            
            HttpResponse httpResponse = client.execute(get);
            in = httpResponse.getEntity().getContent();

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
                    pluginManager.upload(filename, in);
                    jsonObject.accumulate("pluginName", filename);
                } else {
                    // read InputStream
                    byte[] fileContent = readInputStream(in);
                
                    // import app
                    final AppDefinition appDef = appService.importApp(fileContent);
                    if (appDef != null) {
                        TransactionTemplate transactionTemplate = (TransactionTemplate)AppUtil.getApplicationContext().getBean("transactionTemplate");
                        transactionTemplate.execute(new TransactionCallback<Object>() {
                            public Object doInTransaction(TransactionStatus ts) {
                                appService.publishApp(appDef.getId(), null);
                                return false;
                            }
                        });
                        jsonObject.accumulate("appId", appDef.getAppId());
                        jsonObject.accumulate("appName", appDef.getName());
                        jsonObject.accumulate("appVersion", appDef.getVersion());
                    }
                }
            }
        } finally {
            try {
                if (in != null) { // Check if 'in' is not null before closing
                    in.close();
                }
            } catch(IOException e) {
            }
            try {
                client.close();
            } catch(IOException e) {
            }
        }
        
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/marketplace", method = RequestMethod.POST)
    public String consoleAppMarketplace(ModelMap model, @RequestParam(value = "type") String type, @RequestParam(value = "pluginType") String pluginType, 
            @RequestParam(value = "username", required = false) String username, @RequestParam(value = "password", required = false) String password, 
            @RequestParam(value = "login", required = false) Boolean login) {
        
        if ("app".equals(type)) {
            type = "platform";
        }
        
        model.addAttribute("type", type);
        model.addAttribute("pluginType", pluginType);
        
        if (username != null && "_ContinueAsGuest".endsWith(username)) {
            Setting marketplaceAuth = new Setting();
            marketplaceAuth.setProperty("marketplaceAuth");
            marketplaceAuth.setValue("");
            model.addAttribute("isAnonymous", "true");

            setupManager.saveSetting(marketplaceAuth);
        } else if (username != null && password != null) {
            //validate login
            if (!MarketplaceUtil.login(username, password)) {
                model.addAttribute("error", "true");
                return "console/apps/marketplaceLogin";
            }
        } else {
            //authentication header not exist, require login
            String marketplaceAuth = setupManager.getSettingValue("marketplaceAuth");
            if ((login != null && login) || marketplaceAuth == null) {
                return "console/apps/marketplaceLogin";
            }
            
            if (marketplaceAuth.isEmpty()) {
                model.addAttribute("isAnonymous", "true");
            }
        }
        
        return "console/apps/marketplace";
    }
    
    @RequestMapping(value = "/json/marketplace/plugin/listCategory", method = RequestMethod.GET)
    public void getPluginCategories(Writer writer, @RequestParam(value = "component", required = false) String component, @RequestParam(value = "callback", required = false) String callback, HttpServletRequest request) throws JSONException, IOException {
        JSONObject jsonObject = MarketplaceUtil.listCategories(component);
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    //get plugin and plugin categories by calling marketplace api
    @RequestMapping(value = "/json/marketplace/plugin/list", method = RequestMethod.GET)
    public void getPlugins(Writer writer, @RequestParam(value = "search", required = false) String search, @RequestParam(value = "classes", required = false) String classes, @RequestParam(value = "categories", required = false) String categories, @RequestParam(value = "callback", required = false) String callback, HttpServletRequest request) throws JSONException, IOException {
        List<String> classesList = new ArrayList<String>();
        if (classes != null && !classes.isEmpty()) {
            classesList.addAll(Arrays.asList(classes.split(";")));
        }
        
        List<String> categoriesList = new ArrayList<String>();
        if (categories != null && !categories.isEmpty()) {
            categoriesList.addAll(Arrays.asList(categories.split(";")));
        }
        
        JSONObject jsonObject = MarketplaceUtil.listPlugins(search, categoriesList, classesList);
        
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    /**
     * Reads a specified InputStream, returning its contents in a byte array
     * @param in
     * @return
     * @throws IOException 
     */
    protected byte[] readInputStream(InputStream in) throws IOException {
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
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        }
    }    
    
    protected boolean validateTrustedUrl(String url) {
        boolean trusted = false;
        String trustedUrlsKey = "appCenter.link.marketplace.trusted";
        String trustedUrls = ResourceBundleUtil.getMessage(trustedUrlsKey);
        if (trustedUrls != null && !trustedUrls.isEmpty()) {
            StringTokenizer st = new StringTokenizer(trustedUrls, ",");
            while (st.hasMoreTokens()) {
                String trustedUrl = st.nextToken().trim();
                if (url.startsWith(trustedUrl)) {
                    trusted = true;
                    break;
                }
            }
        }
        return trusted;
    }
}
