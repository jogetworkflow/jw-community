package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

public class UserNotificationAuditTrail extends DefaultAuditTrailPlugin implements PluginWebSupport {

    public String getName() {
        return "User Notification";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public Object execute(Map properties) {
        Object result = null;
        try {
            final AuditTrail auditTrail = (AuditTrail) properties.get("auditTrail");
            final PluginManager pluginManager = (PluginManager) properties.get("pluginManager");
            final WorkflowManager workflowManager = (WorkflowManager) pluginManager.getBean("workflowManager");
            final WorkflowUserManager workflowUserManager = (WorkflowUserManager) pluginManager.getBean("workflowUserManager");
            final DirectoryManager directoryManager = (DirectoryManager) pluginManager.getBean("directoryManager");

            final String base = (String) properties.get("base");
            final String smtpHost = (String) properties.get("host");
            final String smtpPort = (String) properties.get("port");
            final String needAuthentication = (String) properties.get("needAuthentication");
            final String smtpUsername = (String) properties.get("username");
            final String smtpPassword = (String) properties.get("password");
            final String security = (String) properties.get("security");

            final String from = (String) properties.get("from");
            final String cc = (String) properties.get("cc");

            final String subject = (String) properties.get("subject");
            final String emailMessage = (String) properties.get("emailMessage");
            
            final String url = (String) properties.get("url");
            final String parameterName = (String) properties.get("parameterName");
            final String passoverMethod = (String) properties.get("passoverMethod");
            final String exclusion = (String) properties.get("exclusion");
            final String isHtml = (String) properties.get("isHtml");
            Map<String, String> replaceMap = null;
            if ("true".equalsIgnoreCase(isHtml)) {
                replaceMap = new HashMap<String, String>();
                replaceMap.put("\\n", "<br/>");
            }
            final Map<String, String> replace = replaceMap;
                    
            String appId = auditTrail.getAppId();
            String appVersion = auditTrail.getAppVersion();
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            final AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

            if (smtpHost == null || smtpHost.trim().length() == 0) {
                return null;
            }

            if (auditTrail != null && (auditTrail.getMethod().equals("createAssignments") || auditTrail.getMethod().equals("getDefaultAssignments") || auditTrail.getMethod().equals("assignmentReassign") || auditTrail.getMethod().equals("assignmentForceComplete"))) {
                final String profile = DynamicDataSourceManager.getCurrentProfile();                
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            HostManager.setCurrentProfile(profile);
                            List<String> userList = new ArrayList<String>();
                            String activityInstanceId = auditTrail.getMessage();
                            
                            int maxAttempt = 5;
                            int numOfAttempt = 0;
                            WorkflowActivity wfActivity = null;
                            while (userList != null && userList.isEmpty() && numOfAttempt < maxAttempt) {
                                //LogUtil.info(getClass().getName(), "Attempting to get resource ids....");
                                Thread.sleep(4000);
                                wfActivity = workflowManager.getActivityById(activityInstanceId);
                                userList = workflowManager.getAssignmentResourceIds(wfActivity.getProcessDefId(), wfActivity.getProcessId(), activityInstanceId);
                                numOfAttempt++;
                            }

                            Collection<String> exclusionIds = new ArrayList<String>();
                            if (exclusion != null && !exclusion.isEmpty()) {
                                exclusionIds.addAll(Arrays.asList(exclusion.split(";")));
                            }
                            
                            if (!exclusionIds.contains(WorkflowUtil.getProcessDefIdWithoutVersion(wfActivity.getProcessDefId()) + "-" + wfActivity.getActivityDefId())) {
                                LogUtil.info(getClass().getName(), "Users to notify: " + userList);
                                if (userList != null) {
                                    for (String username : userList) {
                                        workflowUserManager.setCurrentThreadUser(username);
                                        WorkflowAssignment wfAssignment = workflowManager.getAssignment(activityInstanceId);
                                        
                                        Collection<String> addresses = AppUtil.getEmailList(null, username, null, null);
                                        
                                        if (addresses != null && addresses.size() > 0) {
                                            // create the email message
                                            final HtmlEmail email = new HtmlEmail();
                                            email.setHostName(smtpHost);
                                            if (smtpPort != null && smtpPort.length() != 0) {
                                                email.setSmtpPort(Integer.parseInt(smtpPort));
                                            }
                                            if (needAuthentication != null && needAuthentication.length() != 0 && needAuthentication.equals("yes")) {
                                                email.setAuthentication(smtpUsername, smtpPassword);
                                            }
                                            if(security!= null){
                                                if(security.equalsIgnoreCase("SSL") ){
                                                    email.setSSL(true);
                                                }else if(security.equalsIgnoreCase("TLS")){
                                                    email.setTLS(true);
                                                }
                                            }
                                            if (cc != null && cc.length() != 0) {
                                                Collection<String> ccs = AppUtil.getEmailList(null, cc, wfAssignment, appDef);
                                                for (String address : ccs) {
                                                    email.addCc(address);
                                                }
                                            }

                                            String emailToOutput = "";
                                            for (String address : addresses) {
                                                email.addTo(address);
                                                emailToOutput += address + ", ";
                                            }
                                            email.setFrom(from);

                                            if (subject != null && subject.length() != 0) {
                                                email.setSubject(WorkflowUtil.processVariable(subject, null, wfAssignment));
                                            }
                                            if (emailMessage != null && emailMessage.length() != 0) {
                                                String link = "";

                                                if (url != null && !url.isEmpty()) {
                                                    link += url;
                                                    if ("append".equals(passoverMethod)) {
                                                        if (!url.endsWith("/")) {
                                                            link += "/";
                                                        }
                                                        link += activityInstanceId;
                                                    } else if ("param".equals(passoverMethod)) {
                                                        if (url.contains("?")) {
                                                            link += "&";
                                                        } else {
                                                            link += "?";
                                                        }
                                                        link += parameterName + "=" + activityInstanceId;
                                                    }
                                                } else {
                                                    String urlMapping = "";

                                                    if (base.endsWith("/")) {
                                                        urlMapping = "web/client/app/assignment/";
                                                    } else {
                                                        urlMapping = "/web/client/app/assignment/";
                                                    }

                                                    link = base + urlMapping + activityInstanceId;
                                                }

                                                String msg = AppUtil.processHashVariable(emailMessage + "\n\n\n" + link, wfAssignment, null, replace);
                                                if ("true".equalsIgnoreCase(isHtml)) {
                                                    email.setHtmlMsg(msg);
                                                } else {
                                                    email.setMsg(msg);
                                                }
                                            }
                                            
                                            try {
                                                LogUtil.info(getClass().getName(), "Sending email from=" + email.getFromAddress().toString() + " to=" + emailToOutput + ", subject=Workflow - Pending Task Notification");
                                                email.send();
                                                LogUtil.info(getClass().getName(), "Sending email completed for subject=" + email.getSubject());
                                            } catch (EmailException ex) {
                                                LogUtil.error(getClass().getName(), ex, "Error sending email");
                                            }
                                        }
                                    }
                                }   
                            }
                        } catch (Exception ex) {
                            LogUtil.error(getClass().getName(), ex, "Error executing plugin");
                        }

                    }
                }).start();
            }
            return result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing plugin");
            return null;
        }
    }

    public String getLabel() {
        return "User Notification";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/userNotificationAuditTrail.json", null, true, null);
    }
    
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String appId = request.getParameter("appId");
        String appVersion = request.getParameter("appVersion");
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

        if ("getActivities".equals(action)) {
            try {
                JSONArray jsonArray = new JSONArray();
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                Long packageVersion = (packageDefinition != null) ? packageDefinition.getVersion() : new Long(1);
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());

                if (processList != null && !processList.isEmpty()) {
                    for (WorkflowProcess p : processList) {
                        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(p.getId());
                        for (WorkflowActivity a : activityList) {
                            if (!a.getType().equals(WorkflowActivity.TYPE_ROUTE) && !a.getType().equals(WorkflowActivity.TYPE_TOOL)) {
                                Map<String, String> option = new HashMap<String, String>();
                                option.put("value", p.getIdWithoutVersion() + "-" + a.getActivityDefId());
                                option.put("label", p.getName() + " - " + a.getName());
                                jsonArray.put(option);
                            }
                        }
                    }
                }
                
                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get activity options Error!");
            }
        }
    }
}
