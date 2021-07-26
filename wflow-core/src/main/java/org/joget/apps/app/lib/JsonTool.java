package org.joget.apps.app.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class JsonTool extends DefaultApplicationPlugin {

    public String getName() {
        return "Json Tool";
    }

    public String getDescription() {
        return "Reads a JSON feed URL, and inserts formatted data into form data table or workflow variable";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getLabel() {
        return "Json Tool";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/app/jsonTool.json", arguments, true, null);
        return json;
    }

    public Object execute(Map properties) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        String jsonUrl = (String) properties.get("jsonUrl");
        CloseableHttpClient client = null;
        HttpRequestBase request = null;
        
        try {
            client = HttpClients.createDefault();

            jsonUrl = WorkflowUtil.processVariable(jsonUrl, "", wfAssignment);

            jsonUrl = StringUtil.encodeUrlParam(jsonUrl);

            if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                LogUtil.info(JsonTool.class.getName(), ("post".equalsIgnoreCase(getPropertyString("requestType"))?"POST":"GET") + " : " + jsonUrl);
            }
            
            if ("post".equalsIgnoreCase(getPropertyString("requestType"))) {
                request = new HttpPost(jsonUrl);
                
                if ("jsonPayload".equals(getPropertyString("postMethod"))) {
                    JSONObject obj = new JSONObject();
                    Object[] paramsValues = (Object[]) properties.get("params");
                    for (Object o : paramsValues) {
                        Map mapping = (HashMap) o;
                        String name  = mapping.get("name").toString();
                        String value = mapping.get("value").toString();
                        obj.accumulate(name, WorkflowUtil.processVariable(value, "", wfAssignment));
                    }

                    StringEntity requestEntity = new StringEntity(obj.toString(4), "UTF-8");
                    ((HttpPost) request).setEntity(requestEntity);
                    request.setHeader("Content-type", "application/json");
                    if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                        LogUtil.info(JsonTool.class.getName(), "JSON Payload : " + obj.toString(4));
                    }
                } else if ("custom".equals(getPropertyString("postMethod"))) {
                    StringEntity requestEntity = new StringEntity(getPropertyString("customPayload"), "UTF-8");
                    ((HttpPost) request).setEntity(requestEntity);
                    request.setHeader("Content-type", "application/json");
                    if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                        LogUtil.info(JsonTool.class.getName(), "Custom JSON Payload : " + getPropertyString("customPayload"));
                    }
                } else {
                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    Object[] paramsValues = (Object[]) properties.get("params");
                    for (Object o : paramsValues) {
                        Map mapping = (HashMap) o;
                        String name  = mapping.get("name").toString();
                        String value = mapping.get("value").toString();
                        urlParameters.add(new BasicNameValuePair(name, WorkflowUtil.processVariable(value, "", wfAssignment)));
                        if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                            LogUtil.info(JsonTool.class.getName(), "Adding param " + name + " : " + value);
                        }
                    }
                    ((HttpPost) request).setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
                }
            } else {
                request = new HttpGet(jsonUrl);
            }
            
            Object[] paramsValues = (Object[]) properties.get("headers");
            for (Object o : paramsValues) {
                Map mapping = (HashMap) o;
                String name  = mapping.get("name").toString();
                String value = mapping.get("value").toString();
                if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
                    request.setHeader(name, value);
                    if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                        LogUtil.info(JsonTool.class.getName(), "Adding request header " + name + " : " + value);
                    }
                }
            }
            
            HttpResponse response = client.execute(request);
            if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                LogUtil.info(JsonTool.class.getName(), jsonUrl + " returned with status : " + response.getStatusLine().getStatusCode());
            }
            
            if (!"true".equalsIgnoreCase(getPropertyString("noResponse"))) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (jsonResponse != null && !jsonResponse.isEmpty()) {
                    if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
                        jsonResponse = "{ \"response\" : " + jsonResponse + " }";
                    }
                    if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                        LogUtil.info(JsonTool.class.getName(), jsonResponse);
                    }
                    Map object = PropertyUtil.getProperties(new JSONObject(jsonResponse));

                    storeToForm(wfAssignment, properties, object);
                    storeToWorkflowVariable(wfAssignment, properties, object);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                if (request != null) {
                    request.releaseConnection();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
        }

        return null;
    }

    protected void storeToForm(WorkflowAssignment wfAssignment, Map properties, Map object) {
        String formDefId = (String) properties.get("formDefId");
        if (formDefId != null && formDefId.trim().length() > 0) {
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            AppDefinition appDef = (AppDefinition) properties.get("appDef");

            Object[] fieldMapping = (Object[]) properties.get("fieldMapping");
            String multirowBaseObjectName = (String) properties.get("multirowBaseObject");

            FormRowSet rowSet = new FormRowSet();

            if (multirowBaseObjectName != null && multirowBaseObjectName.trim().length() > 0 && getObjectFromMap(multirowBaseObjectName, object) != null && getObjectFromMap(multirowBaseObjectName, object).getClass().isArray()) {
                Object[] baseObjectArray = (Object[]) getObjectFromMap(multirowBaseObjectName, object);
                if (baseObjectArray != null && baseObjectArray.length > 0) {
                    rowSet.setMultiRow(true);
                    for (int i = 0; i < baseObjectArray.length; i++) {
                        rowSet.add(getRow(wfAssignment, multirowBaseObjectName, i, fieldMapping, object));
                    }
                }
            } else {
                rowSet.add(getRow(wfAssignment, null, null, fieldMapping, object));
            }

            if (rowSet.size() > 0) {
                appService.storeFormData(appDef.getId(), appDef.getVersion().toString(), formDefId, rowSet, null);
            }
        }
    }

    protected void storeToWorkflowVariable(WorkflowAssignment wfAssignment, Map properties, Map object) {
        Object[] wfVariableMapping = (Object[]) properties.get("wfVariableMapping");
        if (wfVariableMapping != null && wfVariableMapping.length > 0) {
            ApplicationContext ac = AppUtil.getApplicationContext();
            WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");

            for (Object o : wfVariableMapping) {
                Map mapping = (HashMap) o;
                String variable = mapping.get("variable").toString();
                String jsonObjectName = mapping.get("jsonObjectName").toString();

                String value = (String) getObjectFromMap(jsonObjectName, object);

                if (value != null) {
                    workflowManager.activityVariable(wfAssignment.getActivityId(), variable, value);
                }
            }
        }
    }

    protected Object getObjectFromMap(String key, Map object) {
        if (key.contains(".")) {
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

    protected FormRow getRow(WorkflowAssignment wfAssignment, String multirowBaseObjectName, Integer rowNumber, Object[] fieldMapping, Map object) {
        FormRow row = new FormRow();

        for (Object o : fieldMapping) {
            Map mapping = (HashMap) o;
            String fieldName = mapping.get("field").toString();
            String jsonObjectName = WorkflowUtil.processVariable(mapping.get("jsonObjectName").toString(), null, wfAssignment, null, null);

            if (multirowBaseObjectName != null) {
                jsonObjectName = jsonObjectName.replace(multirowBaseObjectName, multirowBaseObjectName + "[" + rowNumber + "]");
            }

            String value = (String) getObjectFromMap(jsonObjectName, object);

            if (value == null) {
                value = jsonObjectName;
            }

            if (FormUtil.PROPERTY_ID.equals(fieldName)) {
                row.setId(value);
            } else {
                row.put(fieldName, value);
            }
        }

        if (row.getId() == null || (row.getId() != null && row.getId().trim().length() == 0)) {
            if (multirowBaseObjectName == null) {
                AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
                row.setId(appService.getOriginProcessId(wfAssignment.getProcessId()));
            } else {
                row.setId(UuidGenerator.getInstance().getUuid());
            }
        }

        Date currentDate = new Date();
        row.setDateModified(currentDate);
        row.setDateCreated(currentDate);

        return row;
    }

}
