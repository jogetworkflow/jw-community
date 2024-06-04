package org.joget.apps.app.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonApiUtil {
    
    public static Map<String, Object> callApi(Map properties, Map<String, String> params) {
        Map<String,Object> result = null;
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");
        String jsonUrl = JsonApiUtil.replaceParam(properties.get("jsonUrl").toString(), params);
        jsonUrl = WorkflowUtil.processVariable(jsonUrl, "", wfAssignment);
        CloseableHttpClient client = null;
        HttpRequestBase request = null;

        try {
            HttpServletRequest httpRequest = WorkflowUtil.getHttpServletRequest();

            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            URL urlObj = new URL(jsonUrl);
            
            //prevent recursive call
            if (isRecursiveCall(jsonUrl, httpRequest)) {
                return new HashMap<String, Object>();
            }

//            if ("https".equals(urlObj.getProtocol()) && "true".equalsIgnoreCase("allowedUntrustedCert")) {
//                SSLContextBuilder builder = new SSLContextBuilder();
//                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
//                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
//                httpClientBuilder.setSSLSocketFactory(sslsf);
//            }

            client = httpClientBuilder.build();

            if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                LogUtil.info(JsonApiUtil.class.getName(), ("post".equalsIgnoreCase(properties.get("requestType").toString())?"POST":"GET") + " : " + jsonUrl);
            }

            if ("post".equalsIgnoreCase(properties.get("requestType").toString()) || "put".equalsIgnoreCase(properties.get("requestType").toString())) {
                if ("post".equalsIgnoreCase(properties.get("requestType").toString())) {
                    request = new HttpPost(jsonUrl);
                } else {
                    request = new HttpPut(jsonUrl);
                }

                if ("jsonPayload".equals(properties.get("postMethod").toString())) {
                    JSONObject obj = new JSONObject();
                    Object[] paramsValues = (Object[]) properties.get("params");
                    if (paramsValues != null) {
                        for (Object o : paramsValues) {
                            Map mapping = (HashMap) o;
                            String name  = mapping.get("name").toString();
                            String value = JsonApiUtil.replaceParam(mapping.get("value").toString(), params);
                            obj.accumulate(name, WorkflowUtil.processVariable(value, "", wfAssignment));
                        }
                    }

                    StringEntity requestEntity = new StringEntity(obj.toString(4), "UTF-8");
                    ((HttpEntityEnclosingRequestBase) request).setEntity(requestEntity);
                    request.setHeader("Content-type", "application/json");
                    if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                        LogUtil.info(JsonApiUtil.class.getName(), "JSON Payload : " + obj.toString(4));
                    }
                } else if ("custom".equals(properties.get("postMethod"))) {
                    StringEntity requestEntity = new StringEntity(JsonApiUtil.replaceParam(properties.get("customPayload").toString(), params), "UTF-8");
                    ((HttpEntityEnclosingRequestBase) request).setEntity(requestEntity);
                    request.setHeader("Content-type", "application/json");
                    if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                        LogUtil.info(JsonApiUtil.class.getName(), "Custom JSON Payload : " + properties.get("customPayload").toString());
                    }
                } else {
                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    Object[] paramsValues = (Object[]) properties.get("params");
                    if (paramsValues != null) {
                        for (Object o : paramsValues) {
                            Map mapping = (HashMap) o;
                            String name  = mapping.get("name").toString();
                            String value = JsonApiUtil.replaceParam(mapping.get("value").toString(), params);
                            urlParameters.add(new BasicNameValuePair(name, WorkflowUtil.processVariable(value, "", wfAssignment)));
                            if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                                LogUtil.info(JsonApiUtil.class.getName(), "Adding param " + name + " : " + value);
                            }
                        }
                        ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
                    }
                }
            } else if ("delete".equalsIgnoreCase(properties.get("requestType").toString())) {
                request = new HttpDelete(jsonUrl);
            } else {
                request = new HttpGet(jsonUrl);
            }

            Object[] paramsValues = (Object[]) properties.get("headers");
            if (paramsValues != null) {
                for (Object o : paramsValues) {
                    Map mapping = (HashMap) o;
                    String name  = mapping.get("name").toString();
                    String value = JsonApiUtil.replaceParam(mapping.get("value").toString(), params);
                    if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
                        request.setHeader(name, value);
                        if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                            LogUtil.info(JsonApiUtil.class.getName(), "Adding request header " + name + " : " + value);
                        }
                    }
                }
            }
            if (httpRequest != null) {
                String referer = httpRequest.getHeader("referer");
                if (referer == null || referer.isEmpty()) {
                    referer = httpRequest.getRequestURL().toString();
                }
                request.setHeader("referer", referer);
                if ("true".equalsIgnoreCase(properties.get("copyCookies").toString())) {
                    request.setHeader("Cookie", httpRequest.getHeader("Cookie"));
                }
            }

            HttpResponse response = client.execute(request);
            if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                LogUtil.info(JsonApiUtil.class.getName(), jsonUrl + " returned with status : " + response.getStatusLine().getStatusCode());
            }

            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                jsonResponse = jsonResponse.trim();
                if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
                    jsonResponse = "{ \"response\" : " + jsonResponse + " }";
                } else if (!jsonResponse.startsWith("{") && !jsonResponse.endsWith("}")) {
                    jsonResponse = "{ \"response\" : \"" + jsonResponse + "\" }";
                }
                if ("true".equalsIgnoreCase(properties.get("debugMode").toString())) {
                    LogUtil.info(JsonApiUtil.class.getName(), jsonResponse);
                }
                result = JsonApiUtil.getJsonObjectMap(new JSONObject(jsonResponse));
            }
        } catch (Exception ex) {
            LogUtil.error(JsonApiUtil.class.getName(), ex, "");
        } finally {
            try {
                if (request != null) {
                    request.releaseConnection();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException ex) {
                LogUtil.error(JsonApiUtil.class.getName(), ex, "");
            }
        }
        if (result == null) {
            result = new HashMap<String, Object>();
        }
        
        return result;
    }
    
    public static boolean isRecursiveCall(String jsonUrl, HttpServletRequest httpRequest) {
        return jsonUrl != null && httpRequest != null &&
                (httpRequest.getRequestURL().toString().equals(jsonUrl) ||
                (httpRequest.getRequestURL().toString() + "?" + httpRequest.getQueryString()).contains(jsonUrl) ||
                jsonUrl.contains(httpRequest.getRequestURI()));
    }
    
    public static String replaceParam(String content, Map<String, String> params) {
        if (content != null && !content.isEmpty() && params != null && !params.isEmpty()) {
            for (String s : params.keySet()) {
                String value = params.get(s);
                content = content.replaceAll(StringUtil.escapeRegex("{"+s+"}"), StringUtil.escapeRegex(value != null?value:""));
            }
        }
        return content;
    }
    
    public static Object getObjectFromMap(String key, Map object) {
        if (key.endsWith("<>") || key.endsWith("[]")) { //to support retrieve map or array for looping in option binder
            key = key.substring(0, key.length() - 2);
        }
        
        /* added {} annotation to handle the keys which contains . eg { "user.Org":"Joget Inc"} 
    
         Using annotation like {user.Org} in the field mapping it will be able to parse the records
         */
        if (key.startsWith("{")) {
            String key1 = key.substring(1, key.indexOf("}")); //{search.name}
            Object tempObject = object.get(key1);

            String subKey = key.replace("{" + key1 + "}", ""); //{search.name}.first to .first
            if (subKey.startsWith(".")) {
                subKey = subKey.substring(0, 1);  //first
            }
            if (subKey.length() > 0) {

                if (tempObject != null && tempObject instanceof Map) {
                    return getObjectFromMap(subKey, (Map) tempObject);
                }
            }

            return tempObject;
        } else if (key.contains(".")) {
            String subKey = key.substring(key.indexOf(".") + 1);
            key = key.substring(0, key.indexOf("."));

            Map tempObject = (Map) getObjectFromMap(key, object);

            if (tempObject != null) {
                return getObjectFromMap(subKey, tempObject);
            }
        } else {
            if (key.contains("[") && key.contains("]")) {
                String tempKey = key.substring(0, key.indexOf("["));
                int number = Integer.parseInt(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
                Object tempObjectArray[] = (Object[]) object.get(tempKey);
                if (tempObjectArray != null && tempObjectArray.length > number) {
                    return tempObjectArray[number];
                }
            } else {
                return object.get(key);
            }
        }
        return null;
    }
    
    /**
     * Convenient method used by system to parses a JSON object in to a map,
     * this method is duplicate of PropertyUtil.getProperties to return value in its original data type instead of string
     * @param obj
     * @return 
     */
    public static Map<String, Object> getJsonObjectMap(JSONObject obj) {
        Map<String, Object> object = new HashMap<String, Object>();
        try {
            if (obj != null) {
                Iterator keys = obj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!obj.isNull(key)) {
                        Object value = obj.get(key);
                        if (value instanceof JSONArray) {
                            object.put(key, getArray((JSONArray) value));
                        } else if (value instanceof JSONObject) {
                            object.put(key, getJsonObjectMap((JSONObject) value));
                        } else {
                            String stringValue = obj.get(key).toString();
                            if ("{}".equals(stringValue)) {
                                object.put(key, new HashMap<String, Object>());
                            } else {
                                object.put(key, obj.get(key));
                            }
                        }
                    } else {
                        object.put(key, "");
                    }
                }
            }
        } catch (Exception e) {
        }
        return object;
    }
    
    private static Object[] getArray(JSONArray arr) throws Exception {
        Collection<Object> array = new ArrayList<Object>();
        if (arr != null && arr.length() > 0) {
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.get(i);
                if (value != null) {
                    if (value instanceof JSONArray) {
                        array.add(getArray((JSONArray) value));
                    } else if (value instanceof JSONObject) {
                        array.add(getJsonObjectMap((JSONObject) value));
                    } else if (value instanceof String) {
                        array.add(value);
                    }
                }
            }
        }
        return array.toArray();
    }
}
