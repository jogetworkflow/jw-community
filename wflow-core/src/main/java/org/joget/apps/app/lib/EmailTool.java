package org.joget.apps.app.lib;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginException;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class EmailTool extends DefaultApplicationPlugin {

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
        AppDefinition appDef = (AppDefinition) properties.get("appDef");

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
                Collection<String> ccs = AppUtil.getEmailList(null, cc, wfAssignment, appDef);
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
                Collection<String> tss = AppUtil.getEmailList(toParticipantId, toSpecific, wfAssignment, appDef);
                for (String address : tss) {
                    email.addTo(address);
                    emailToOutput += address + ", ";
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
