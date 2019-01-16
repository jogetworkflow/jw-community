package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.apps.app.dao.UserReplacementDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.UserReplacement;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.PushServiceUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
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
        return "5.0.0";
    }

    public String getDescription() {
        return "";
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
    
    public Object execute(Map props) {
        AuditTrail auditTrail = (AuditTrail) props.get("auditTrail");
        
        if (validation(auditTrail)) {
            String method = auditTrail.getMethod();
            Object[] args = auditTrail.getArgs();
            
            String activityInstanceId = null;
            List<String> users = null;
            
            if (method.equals("getDefaultAssignments") && args.length == 3) {
                users = (List<String>) auditTrail.getReturnObject();
                activityInstanceId = (String) args[1];
            } else if (method.equals("assignmentReassign") && args.length == 5) {
                users = new ArrayList<String> ();
                users.add((String) args[3]);
                activityInstanceId = (String) args[2];
            }
            
            if (activityInstanceId != null && !activityInstanceId.isEmpty() && users != null) {
                WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                WorkflowActivity wfActivity = workflowManager.getActivityById(activityInstanceId);
                LogUtil.info(UserNotificationAuditTrail.class.getName(), "Users to notify: " + users);
                
                if (wfActivity != null && !excluded((String) props.get("exclusion"), wfActivity) && !users.isEmpty()) {
                    sendEmail(props, auditTrail, workflowManager, users, wfActivity);
                }
            }
        }
        
        return null;
    }
    
    protected void sendEmail (final Map props, final AuditTrail auditTrail, final WorkflowManager workflowManager, final List<String> users, final WorkflowActivity wfActivity) {
        final String smtpHost = (String) props.get("host");
        
        SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
        String setupSmtpHost = setupManager.getSettingValue("smtpHost");
        
        if ((smtpHost != null && !smtpHost.isEmpty()) || (setupSmtpHost != null && !setupSmtpHost.isEmpty())) {
            final String profile = DynamicDataSourceManager.getCurrentProfile();            
            new PluginThread(new Runnable() {

                public void run() {
                    WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");

                    String base = (String) props.get("base");
                    String smtpPort = (String) props.get("port");
                    String smtpUsername = (String) props.get("username");
                    String smtpPassword = (String) props.get("password");
                    String security = (String) props.get("security");

                    String decryptedSmtpPassword = smtpPassword;
                    if (smtpUsername != null && !smtpUsername.isEmpty()) {
                        if (decryptedSmtpPassword != null) {
                            decryptedSmtpPassword = SecurityUtil.decrypt(decryptedSmtpPassword);
                        }
                    }
                    
                    String from = (String) props.get("from");
                    String cc = (String) props.get("cc");

                    String subject = (String) props.get("subject");
                    String emailMessage = (String) props.get("emailMessage");

                    String url = (String) props.get("url");
                    String urlName = (String) props.get("urlName");
                    String parameterName = (String) props.get("parameterName");
                    String passoverMethod = (String) props.get("passoverMethod");
                    String isHtml = (String) props.get("isHtml");

                    Map<String, String> replace = null;
                    if ("true".equalsIgnoreCase(isHtml)) {
                        replace = new HashMap<String, String>();
                        replace.put("\\n", "<br/>");
                    }
                    
                    String activityInstanceId = wfActivity.getId();
                    String link = getLink(base, url, passoverMethod, parameterName, activityInstanceId);
                    
                    try {
                        for (String username : users) {
                            Collection<String> addresses = AppUtil.getEmailList(null, username, null, null);

                            if (addresses != null && addresses.size() > 0) {
                                workflowUserManager.setCurrentThreadUser(username);
                                WorkflowAssignment wfAssignment = null;
                                
                                int count = 0;
                                do {
                                    try {
                                        wfAssignment = workflowManager.getAssignment(activityInstanceId);
                                    } catch (Exception ex) {}
                                    
                                    if (wfAssignment == null) {
                                        Thread.sleep(4000); //wait for assignment creation
                                    }
                                    count++;
                                } while (wfAssignment == null && count < 5); // try max 5 times
                                
                                if (wfAssignment != null) {
                                    // create the email message
                                    HtmlEmail email = AppUtil.createEmail(smtpHost, smtpPort, security, smtpUsername, smtpPassword, from);
                                    if (email == null) {
                                        return;
                                    }
                                    
                                    if (cc != null && cc.length() != 0) {
                                        Collection<String> ccs = AppUtil.getEmailList(null, cc, wfAssignment, auditTrail.getAppDef());
                                        for (String address : ccs) {
                                            email.addCc(StringUtil.encodeEmail(address));
                                        }
                                    }
                                    
                                    //send to replacement user
                                    UserReplacementDao urDao = (UserReplacementDao) AppUtil.getApplicationContext().getBean("userReplacementDao");
                                    String args[] = wfAssignment.getProcessDefId().split("#");
                                    Collection<UserReplacement> replaces = urDao.getUserTodayReplacedBy(username, args[0], args[2]);
                                    if (replaces != null && !replaces.isEmpty()) {
                                        for (UserReplacement ur : replaces) {
                                            Collection<String> emails = AppUtil.getEmailList(null, ur.getReplacementUser(), null, null);
                                            if (emails != null && !emails.isEmpty()) {
                                                addresses.addAll(emails);
                                            }
                                        }
                                    }

                                    String emailToOutput = "";
                                    for (String address : addresses) {
                                        email.addTo(StringUtil.encodeEmail(address));
                                        emailToOutput += address + ", ";
                                    }

                                    String formattedSubject = "";
                                    if (subject != null && subject.length() != 0) {
                                        formattedSubject = WorkflowUtil.processVariable(subject, null, wfAssignment);
                                        email.setSubject(formattedSubject);
                                    }
                                    String formattedMessage = "";
                                    if (emailMessage != null && emailMessage.length() != 0) {

                                        String tempLink = link;
                                        if ("true".equalsIgnoreCase(isHtml)) {
                                            if (urlName != null && urlName.length() != 0) {
                                                tempLink = "<a href=\"" + link + "\">" + urlName + "</a>";
                                            } else {
                                                tempLink = "<a href=\"" + link + "\">" + link + "</a>";
                                            }
                                            String tempEmailMessage = emailMessage;
                                            if (emailMessage.contains("#assignment.link#")) {
                                                tempEmailMessage = tempEmailMessage.replaceAll(StringUtil.escapeRegex("#assignment.link#"), StringUtil.escapeRegex(tempLink));
                                            } else {
                                                tempEmailMessage = emailMessage + "<br/><br/><br/>" + tempLink;
                                            }
                                            formattedMessage = AppUtil.processHashVariable(tempEmailMessage, wfAssignment, null, replace);
                                            email.setHtmlMsg(formattedMessage);
                                        } else {
                                            String tempEmailMessage = emailMessage;
                                            if (emailMessage.contains("#assignment.link#")) {
                                                tempEmailMessage = tempEmailMessage.replaceAll(StringUtil.escapeRegex("#assignment.link#"), StringUtil.escapeRegex(tempLink));
                                            } else {
                                                tempEmailMessage = emailMessage + "\n\n\n" + tempLink;
                                            }
                                            formattedMessage = AppUtil.processHashVariable(tempEmailMessage, wfAssignment, null, replace);
                                            email.setMsg(formattedMessage);
                                        }
                                    }
                                    email.setCharset("UTF-8");
                                    
                                    AppUtil.emailAttachment(props, wfAssignment, auditTrail.getAppDef(), email);

                                    try {
                                        LogUtil.info(UserNotificationAuditTrail.class.getName(), "Sending email from=" + email.getFromAddress().toString() + " to=" + emailToOutput + ", subject=Workflow - Pending Task Notification");
                                        email.send();
                                        LogUtil.info(UserNotificationAuditTrail.class.getName(), "Sending email completed for subject=" + email.getSubject());
                                    } catch (EmailException ex) {
                                        LogUtil.error(UserNotificationAuditTrail.class.getName(), ex, "Error sending email");
                                    }
                                    
                                    if (!"true".equals(props.get("disablePush"))) {
                                        PushServiceUtil.sendUserPushNotification(username, formattedSubject, formattedMessage, link, "", "", true);
                                    }
                                } else {
                                    LogUtil.debug(UserNotificationAuditTrail.class.getName(), "Fail to retrieve assignment for " + username);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.error(UserNotificationAuditTrail.class.getName(), e, "Error executing plugin");
                    }
                }
            }).start();
            
        } else {
            LogUtil.info(this.getClassName(), "SMTP Host is not configured.");
        }
    }
    
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
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
                                option.put("label", p.getName() + " (" + p.getIdWithoutVersion() + ")" + " - " + a.getName() + " (" + a.getActivityDefId() + ")");
                                jsonArray.put(option);
                            }
                        }
                    }
                }
                
                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(UserNotificationAuditTrail.class.getName(), ex, "Get activity options Error!");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    protected boolean validation(AuditTrail auditTrail) {
        return auditTrail != null 
                && (auditTrail.getMethod().equals("getDefaultAssignments")
                || auditTrail.getMethod().equals("assignmentReassign"));
    }
    
    protected boolean excluded(String exclusion, WorkflowActivity activity) {
        Collection<String> exclusionIds = new ArrayList<String>();
        if (exclusion != null && !exclusion.isEmpty()) {
            exclusionIds.addAll(Arrays.asList(exclusion.split(";")));
        }
        
        return exclusionIds.contains(WorkflowUtil.getProcessDefIdWithoutVersion(activity.getProcessDefId()) + "-" + activity.getActivityDefId());
    }
    
    protected String getLink(String base, String url, String passoverMethod, String parameterName, String activityInstanceId) {
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
        
        return link;
    }
}
