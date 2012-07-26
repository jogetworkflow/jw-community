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
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginException;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

public class EmailTool extends DefaultApplicationPlugin {

    private DirectoryManager directoryManager;

    public String getName() {
        return "Email Tool";
    }

    public String getDescription() {
        return "Sends email message to targeted recipient(s)";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public Object execute(Map properties) {
        PluginManager pluginManager = (PluginManager) properties.get("pluginManager");
        directoryManager = (DirectoryManager) pluginManager.getBean("directoryManager");

        String formDataTable = (String) properties.get("formDataTable");
        String smtpHost = (String) properties.get("host");
        String smtpPort = (String) properties.get("port");
        String needAuthentication = (String) properties.get("needAuthentication");
        String smtpUsername = (String) properties.get("username");
        String smtpPassword = (String) properties.get("password");
        String security = (String) properties.get("security");
        
        final String from = (String) properties.get("from");
        final String cc = (String) properties.get("cc");
        String toParticipantId = (String) properties.get("toParticipantId");
        String toSpecific = (String) properties.get("toSpecific");

        String emailSubject = (String) properties.get("subject");
        String emailMessage = (String) properties.get("message");

        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        try {
            emailSubject = WorkflowUtil.processVariable(emailSubject, formDataTable, wfAssignment);
            emailMessage = WorkflowUtil.processVariable(emailMessage, formDataTable, wfAssignment);

            // create the email message
            final MultiPartEmail email = new MultiPartEmail();
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
                String ccStr = WorkflowUtil.processVariable(cc, formDataTable, wfAssignment);
                Collection<String> ccs = convertStringToInternetRecipientsList(ccStr);
                for (String address : ccs) {
                    email.addCc(address);
                }
            }

            final String fromStr = WorkflowUtil.processVariable(from, formDataTable, wfAssignment);
            email.setFrom(fromStr);
            email.setSubject(emailSubject);
            email.setMsg(emailMessage);
            String emailToOutput = "";

            if ((toParticipantId != null && toParticipantId.trim().length() != 0) || (toSpecific != null && toSpecific.trim().length() != 0)) {
                if (toParticipantId != null && toParticipantId.trim().length() != 0) {
                    WorkflowManager workflowManager = (WorkflowManager) pluginManager.getBean("workflowManager");
                    WorkflowProcess process = workflowManager.getProcess(wfAssignment.getProcessDefId());
                    String pIds[] = toParticipantId.split(",");


                    for (String pId : pIds) {
                        List<String> userList = null;
                        userList = WorkflowUtil.getAssignmentUsers(process.getPackageId(), wfAssignment.getProcessDefId(), wfAssignment.getProcessId(), wfAssignment.getProcessVersion(), wfAssignment.getActivityId(), "", pId.trim());

                        if (userList != null && userList.size() > 0) {
                            for (String username : userList) {
                                User user = directoryManager.getUserByUsername(username);
                                String userEmail = user.getEmail();
                                if (userEmail != null && userEmail.trim().length() > 0) {
                                    email.addTo(userEmail);
                                    emailToOutput += userEmail + ", ";
                                }
                            }
                        }
                    }
                }
                
                if (toSpecific != null && toSpecific.trim().length() != 0) {
                    String toSpecificStr = WorkflowUtil.processVariable(toSpecific, formDataTable, wfAssignment);
                    Collection<String> tss = convertStringToInternetRecipientsList(toSpecificStr);
                    for (String address : tss) {
                        email.addTo(address);
                        emailToOutput += address + ", ";
                    }
                }
            } else {
                throw new PluginException("no email specified");
            }

            final String to = emailToOutput;
            final String profile = DynamicDataSourceManager.getCurrentProfile();

            Thread emailThread = new Thread(new Runnable() {

                public void run() {
                    try {
                        HostManager.setCurrentProfile(profile);
                        LogUtil.info(getClass().getName(), "EmailTool: Sending email from=" + fromStr + ", to=" + to + "cc=" + cc + ", subject=" + email.getSubject());
                        email.send();
                        LogUtil.info(getClass().getName(), "EmailTool: Sending email completed for subject=" + email.getSubject());
                    } catch (EmailException ex) {
                        LogUtil.error(getClass().getName(), ex, "");
                    }
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();

        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }

        return null;
    }

    private Collection<String> convertStringToInternetRecipientsList(String s) throws AddressException {
        InternetAddress[] addresses;
        InternetAddress address;
        Collection<String> recipients = new ArrayList<String>();
        Set emailSet = new HashSet(); // to detect duplicate emails
        String addrStr;

        if (!("".equals(s) || s == null)) {
            s = s.replace(";", ","); // add support for MS-style semi-colon (;) as a delimiter
            addresses = InternetAddress.parse(s);
            for (int i = 0; i < addresses.length; i++) {
                address = addresses[i];
                addrStr = address.getAddress();

                if (addrStr == null || addrStr.trim().length() == 0) {
                    // ignore
                    continue;
                }

                //to support retrieve email by putting username
                if (!addrStr.contains("@")) {
                    try {
                        User user = directoryManager.getUserByUsername(addrStr);
                        if (user != null) {
                            String emailStr = user.getEmail();

                            if (emailStr != null && !emailStr.trim().equals("")) {
                                addrStr = emailStr;
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.info(getClass().getName(), "User not found!");
                    }
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

    public String getLabel() {
        return "Email Tool";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/emailTool.json", null, true, null);
    }
}
