package org.joget.apps.app.lib;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginException;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class EmailTool extends DefaultApplicationPlugin implements PluginWebSupport {

    public String getName() {
        return "Email Tool";
    }

    public String getDescription() {
        return "Sends email message to targeted recipient(s)";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public Object execute(Map properties) {
        PluginManager pluginManager = (PluginManager) properties.get("pluginManager");
        
        String formDataTable = (String) properties.get("formDataTable");
        String smtpHost = (String) properties.get("host");
        String smtpPort = (String) properties.get("port");
        String smtpUsername = (String) properties.get("username");
        String smtpPassword = (String) properties.get("password");
        String security = (String) properties.get("security");

        final String from = (String) properties.get("from");
        final String cc = (String) properties.get("cc");
        final String bcc = (String) properties.get("bcc");
        String toParticipantId = (String) properties.get("toParticipantId");
        String toSpecific = (String) properties.get("toSpecific");

        String emailSubject = (String) properties.get("subject");
        String emailMessage = (String) properties.get("message");
        
        String isHtml = (String) properties.get("isHtml");

        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");
        AppDefinition appDef = (AppDefinition) properties.get("appDef");

        try {
            Map<String, String> replaceMap = null;
            if ("true".equalsIgnoreCase(isHtml)) {
                replaceMap = new HashMap<String, String>();
                replaceMap.put("\\n", "<br/>");
            }
            
            emailSubject = WorkflowUtil.processVariable(emailSubject, formDataTable, wfAssignment);
            emailMessage = AppUtil.processHashVariable(emailMessage, wfAssignment, null, replaceMap);
            
            smtpHost = AppUtil.processHashVariable(smtpHost, wfAssignment, null, null);
            smtpPort = AppUtil.processHashVariable(smtpPort, wfAssignment, null, null);
            smtpUsername = AppUtil.processHashVariable(smtpUsername, wfAssignment, null, null);
            smtpPassword = AppUtil.processHashVariable(smtpPassword, wfAssignment, null, null);
            security = AppUtil.processHashVariable(security, wfAssignment, null, null);
            final String fromStr = WorkflowUtil.processVariable(from, formDataTable, wfAssignment);
            
            // create the email message
            final HtmlEmail email = AppUtil.createEmail(smtpHost, smtpPort, security, smtpUsername, smtpPassword, fromStr, getPropertyString("p12"), getPropertyString("storepass"), getPropertyString("alias"));
            if (email == null) {
                return null;
            }
            
            if (cc != null && cc.length() != 0) {
                Collection<String> ccs = AppUtil.getEmailList(null, cc, wfAssignment, appDef);
                for (String address : ccs) {
                    email.addCc(StringUtil.encodeEmail(address));
                }
            }
            if (bcc != null && bcc.length() != 0) {
                Collection<String> ccs = AppUtil.getEmailList(null, bcc, wfAssignment, appDef);
                for (String address : ccs) {
                    email.addBcc(StringUtil.encodeEmail(address));
                }
            }

            email.setSubject(emailSubject);
            email.setCharset("UTF-8");
            
            if ("true".equalsIgnoreCase(isHtml)) {
                email.setHtmlMsg(emailMessage);
            } else {
                email.setMsg(emailMessage);
            }
            String emailToOutput = "";

            if ((toParticipantId != null && toParticipantId.trim().length() != 0) || (toSpecific != null && toSpecific.trim().length() != 0)) {
                Collection<String> tss = AppUtil.getEmailList(toParticipantId, toSpecific, wfAssignment, appDef);
                for (String address : tss) {
                    email.addTo(StringUtil.encodeEmail(address));
                    emailToOutput += address + ", ";
                }
            } else {
                throw new PluginException("no email specified");
            }

            final String to = emailToOutput;
            final String profile = DynamicDataSourceManager.getCurrentProfile();
            
            AppUtil.emailAttachment(properties, wfAssignment, appDef, email);
            
            String retryCountStr = (String) properties.get("retryCount");
            String retryIntervalStr = (String) properties.get("retryInterval");
            int retryCount = 0;
            long retryInterval = 10000;
            try {
                if (retryCountStr != null && !retryCountStr.isEmpty()) {
                    retryCount = Integer.parseInt(retryCountStr);
                }
                if (retryIntervalStr != null && !retryIntervalStr.isEmpty()) {
                    retryInterval = Integer.parseInt(retryIntervalStr) * 1000l;
                }
            } catch (Exception e) {
                LogUtil.debug(EmailTool.class.getName(), e.getLocalizedMessage());
            }
            
            final int emailRetryCount = retryCount;
            final long emailRetryInterval = retryInterval;

            Thread emailThread = new PluginThread(new Runnable() {
                int retry = 0;
                public void run() {
                    try {
                        LogUtil.info(EmailTool.class.getName(), "EmailTool: Sending email from=" + email.getFromAddress().toString() + ", to=" + to + "cc=" + cc + ", bcc=" + bcc + ", subject=" + email.getSubject());
                        email.send();
                        LogUtil.info(EmailTool.class.getName(), "EmailTool: Sending email completed for subject=" + email.getSubject());
                    } catch (EmailException ex) {
                        LogUtil.error(EmailTool.class.getName(), ex, "");
                        
                        while (retry < emailRetryCount) {
                            retry++;
                            try {
                                LogUtil.info(EmailTool.class.getName(), "EmailTool Attempt " + retry + " after " + (emailRetryInterval/1000) + " seconds");
                                Thread.sleep(emailRetryInterval);
                            } catch (Exception e) {}
                            
                            try {
                                LogUtil.info(EmailTool.class.getName(), "EmailTool Attempt " + retry + ": Sending email from=" + email.getFromAddress().toString() + ", to=" + to + "cc=" + cc + ", bcc=" + bcc + ", subject=" + email.getSubject());
                                email.sendMimeMessage();
                                break;
                            } catch (EmailException ex2) {
                                LogUtil.error(EmailTool.class.getName(), ex, "EmailTool Attempt " + retry + " failure.");
                            }
                        }
                    }
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();

        } catch (Exception e) {
            LogUtil.error(EmailTool.class.getName(), e, "");
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

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String action = request.getParameter("action");
        if ("testmail".equals(action)) {
            String message = "";
            try {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                
                String smtpHost = AppUtil.processHashVariable(request.getParameter("host"), null, null, null, appDef);
                String smtpPort = AppUtil.processHashVariable(request.getParameter("port"), null, null, null, appDef);
                String smtpUsername = AppUtil.processHashVariable(request.getParameter("username"), null, null, null, appDef);
                String smtpPassword = AppUtil.processHashVariable(SecurityUtil.decrypt(request.getParameter("password")), null, null, null, appDef);
                String security = AppUtil.processHashVariable(request.getParameter("security"), null, null, null, appDef);
                String from = AppUtil.processHashVariable(request.getParameter("from"), null, null, null, appDef);
                String to = AppUtil.processHashVariable(request.getParameter("toSpecific"), null, null, null, appDef);

                final HtmlEmail email = AppUtil.createEmail(smtpHost, smtpPort, security, smtpUsername, smtpPassword, from);
                if (email != null) {
                    email.setSubject(ResourceBundleUtil.getMessage("app.emailtool.testSubject"));
                    email.setCharset("UTF-8");
                    email.setHtmlMsg(ResourceBundleUtil.getMessage("app.emailtool.testMessage"));

                    if (to != null && to.length() != 0) {
                        Collection<String> tos = AppUtil.getEmailList(null, to, null, null);
                        for (String address : tos) {
                            email.addTo(address);
                        }
                    }

                    email.send();
                    message = ResourceBundleUtil.getMessage("app.emailtool.testEmailSent");
                } else {
                    message = ResourceBundleUtil.getMessage("app.emailtool.testEmailFail") + "\n" + ResourceBundleUtil.getMessage("app.emailtool.error.smtp");
                }
            } catch (Exception e) {
                LogUtil.error(this.getClassName(), e, "Test Email error");
                message = ResourceBundleUtil.getMessage("app.emailtool.testEmailFail") + "\n" + StringEscapeUtils.escapeJavaScript(e.getMessage());
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", message);
                jsonObject.write(response.getWriter());
            } catch (Exception e) {
                //ignore
            }
        } else if ("validate".equals(action)) {
            boolean error = false;
            
            String smtpHost = request.getParameter("host");
            String smtpPort = request.getParameter("port");
            String smtpFrom = request.getParameter("from");
            
            if (smtpHost == null || smtpHost.isEmpty() || smtpPort == null || smtpPort.isEmpty() || smtpFrom == null || smtpFrom.isEmpty()) {
                SetupManager setupManager = (SetupManager)AppUtil.getApplicationContext().getBean("setupManager");
                String host = setupManager.getSettingValue("smtpHost");
                String port = setupManager.getSettingValue("smtpPort");
                String from = setupManager.getSettingValue("smtpEmail");
                
                if (host == null || host.isEmpty() || port == null || port.isEmpty() || from == null || from.isEmpty()) {
                    error = true;
                }
            }
            
            try {
                JSONObject jsonObject = new JSONObject();
                
                if (!error) {
                    jsonObject.put("status", "success");
                } else {
                    jsonObject.put("status", "fail");
                    jsonObject.put("message", new JSONArray());
                }
                
                jsonObject.write(response.getWriter());
            } catch (Exception e) {
                //ignore
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    @Override
    public String getPluginIcon() {
        return "<i class=\"las la-envelope\"></i>";
    }
}
