package org.joget.apps.app.service;

import org.joget.commons.util.LogInfoHelperImpl;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;

public class AppLogInfoHelperImpl extends LogInfoHelperImpl {

    private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    @Override
    public String prepareAdditionalLogMessage(String className, String level, String message) {
        if ("ERROR".equals(level) || "DEBUG".equals(level)) {
            if (IN_PROGRESS.get()) {
                return message;
            }
            IN_PROGRESS.set(true);
            try {
                JSONObject logInfo = new JSONObject();
                logInfo.put("message", message != null ? message : "");
                logInfo.put("username", getCurrentUsername());
                logInfo.put("url", getCurrentUrl());
                logInfo.put("thread", Thread.currentThread().getName());
                logInfo.put("params", getIdParameters());
                return logInfo.toString();
            } catch (Exception e) {
                // Fallback to simple format if JSON creation fails
                return message;
            } finally {
                IN_PROGRESS.remove();
            }
        }
        return message;
    }
    
    private String getCurrentUsername() {
        try {
            WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
            String username = wum.getCurrentUsername();
            if (username != null && !username.isEmpty()) {
                return username;
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
    
    private String getCurrentUrl() {
        try {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null) {
                String url = request.getRequestURL().toString();
                String queryString = request.getQueryString();
                if (queryString != null && !queryString.isEmpty()) {
                    url += "?" + queryString;
                }
                return url;
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
    
    private JSONObject getIdParameters() {
        JSONObject params = new JSONObject();
        try {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null) {
                // Get POST parameters
                java.util.Map<String, String[]> parameterMap = request.getParameterMap();
                for (java.util.Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String paramName = entry.getKey();
                    if (paramName.toLowerCase().contains("id")) {
                        String[] values = entry.getValue();
                        if (values != null && values.length > 0) {
                            if (values.length == 1) {
                                params.put(paramName, values[0]);
                            } else {
                                // Handle array of IDs
                                params.put(paramName, new org.json.JSONArray(values));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return params;
    }
}
