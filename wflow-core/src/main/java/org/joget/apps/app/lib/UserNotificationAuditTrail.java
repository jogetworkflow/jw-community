package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class UserNotificationAuditTrail extends DefaultAuditTrailPlugin {

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

            final String from = (String) properties.get("from");
            final String cc = (String) properties.get("cc");

            final String subject = (String) properties.get("subject");
            final String emailMessage = (String) properties.get("emailMessage");

            if (smtpHost == null || smtpHost.trim().length() == 0) {
                return null;
            }

            if (auditTrail != null && (auditTrail.getMethod().equals("createAssignments") || auditTrail.getMethod().equals("getDefaultAssignments") || auditTrail.getMethod().equals("assignmentReassign") || auditTrail.getMethod().equals("assignmentForceComplete"))) {
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            List<String> userList = new ArrayList<String>();
                            String activityInstanceId = auditTrail.getMessage();
                            
                            int maxAttempt = 5;
                            int numOfAttempt = 0;
                            while (userList != null && userList.isEmpty() && numOfAttempt < maxAttempt) {
                                //LogUtil.info(getClass().getName(), "Attempting to get resource ids....");
                                Thread.sleep(4000);
                                WorkflowActivity wfActivity = workflowManager.getActivityById(activityInstanceId);
                                userList = workflowManager.getAssignmentResourceIds(wfActivity.getProcessDefId(), wfActivity.getProcessId(), activityInstanceId);
                                numOfAttempt++;
                            }

                            LogUtil.info(getClass().getName(), "Users to notify: " + userList);
                            if (userList != null) {
                                for (String username : userList) {
                                    workflowUserManager.setCurrentThreadUser(username);
                                    WorkflowAssignment wfAssignment = workflowManager.getAssignment(activityInstanceId);

                                    final User user = directoryManager.getUserByUsername(username);
                                    if (user.getEmail() != null && user.getEmail().trim().length() > 0) {
                                        // create the email message
                                        final MultiPartEmail email = new MultiPartEmail();
                                        email.setHostName(smtpHost);
                                        if (smtpPort != null && smtpPort.length() != 0) {
                                            email.setSmtpPort(Integer.parseInt(smtpPort));
                                        }
                                        if (needAuthentication != null && needAuthentication.length() != 0 && needAuthentication.equals("yes")) {
                                            email.setAuthentication(smtpUsername, smtpPassword);
                                        }
                                        if (cc != null && cc.length() != 0) {
                                            Collection<String> ccs = convertStringToInternetRecipientsList(cc);
                                            for (String address : ccs) {
                                                email.addCc(address);
                                            }
                                        }

                                        email.addTo(user.getEmail());
                                        email.setFrom(from);

                                        if (subject != null && subject.length() != 0) {
                                            email.setSubject(WorkflowUtil.processVariable(subject, null, wfAssignment));
                                        }
                                        if (emailMessage != null && emailMessage.length() != 0) {
                                            String urlMapping = "";

                                            if (base.endsWith("/")) {
                                                urlMapping = "web/client/app/assignment/";
                                            } else {
                                                urlMapping = "/web/client/app/assignment/";
                                            }

                                            email.setMsg(WorkflowUtil.processVariable(emailMessage + "\n\n\n" + base + urlMapping + activityInstanceId, null, wfAssignment));
                                        }

                                        try {
                                            LogUtil.info(getClass().getName(), "Sending email from=" + email.getFromAddress().toString() + " to=" + user.getEmail() + ", subject=Workflow - Pending Task Notification");
                                            email.send();
                                            LogUtil.info(getClass().getName(), "Sending email completed for subject=" + email.getSubject());
                                        } catch (EmailException ex) {
                                            LogUtil.error(getClass().getName(), ex, "Error sending email");
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

    private Collection<String> convertStringToInternetRecipientsList(String s) throws AddressException {
        InternetAddress[] addresses;
        InternetAddress address;
        Collection<String> recipients = new ArrayList<String>();
        Set emailSet = new HashSet(); // to detect duplicate emails
        String addrStr;

        if (!("".equals(s) || s == null)) {
            addresses = InternetAddress.parse(s);
            for (int i = 0; i < addresses.length; i++) {
                address = addresses[i];
                addrStr = address.getAddress();

                if (addrStr == null || addrStr.trim().length() == 0) {
                    // ignore
                    continue;
                }
                // allow invalid RFC email addresses. Uncomment to check - but not recommended
                // address.validate();
                if (!emailSet.contains(addrStr)) {
                    emailSet.add(addrStr);
                    recipients.add(addrStr);
                }
            }
        }

        return recipients;
    }
}
