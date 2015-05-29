package org.joget.apps.app.lib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginException;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

public class EmailTool extends DefaultApplicationPlugin implements PluginWebSupport {

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

            // create the email message
            final HtmlEmail email = new HtmlEmail();
            email.setHostName(smtpHost);
            if (smtpPort != null && smtpPort.length() != 0) {
                email.setSmtpPort(Integer.parseInt(smtpPort));
            }
            if (smtpUsername != null && !smtpUsername.isEmpty()) {
                if (smtpPassword != null) {
                    smtpPassword = SecurityUtil.decrypt(smtpPassword);
                }
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
            if (bcc != null && bcc.length() != 0) {
                Collection<String> ccs = AppUtil.getEmailList(null, bcc, wfAssignment, appDef);
                for (String address : ccs) {
                    email.addBcc(address);
                }
            }

            final String fromStr = WorkflowUtil.processVariable(from, formDataTable, wfAssignment);
            email.setFrom(fromStr);
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
                    email.addTo(address);
                    emailToOutput += address + ", ";
                }
            } else {
                throw new PluginException("no email specified");
            }

            final String to = emailToOutput;
            final String profile = DynamicDataSourceManager.getCurrentProfile();
            
            //handle file attachment
            String formDefId = (String) properties.get("formDefId");
            Object[] fields = null;
            if (properties.get("fields") instanceof Object[]){
                fields = (Object[]) properties.get("fields");
            }
            if (formDefId != null && !formDefId.isEmpty() && fields != null && fields.length > 0) {
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                
                FormData formData = new FormData();
                String primaryKey = appService.getOriginProcessId(wfAssignment.getProcessId());
                formData.setPrimaryKeyValue(primaryKey);
                Form loadForm = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formDefId, null, null, null, formData, null, null);
                
                for (Object o : fields) {
                    Map mapping = (HashMap) o;
                    String fieldId = mapping.get("field").toString();
                        
                    try {
                        Element el = FormUtil.findElement(fieldId, loadForm, formData);
                        
                        String value = FormUtil.getElementPropertyValue(el, formData);
                        if (value != null && !value.isEmpty()) {
                            File file = FileUtil.getFile(value, loadForm, primaryKey);
                            if (file != null) {
                                FileDataSource fds = new FileDataSource(file);
                                email.attach(fds, MimeUtility.encodeText(file.getName()), "");
                            }
                        }
                    } catch(Exception e){
                        LogUtil.info(EmailTool.class.getName(), "Attached file fail from field \"" + fieldId + "\" in form \"" + formDefId + "\"");
                    }
                }
            }
            
            Object[] files = null;
            if (properties.get("files") instanceof Object[]){
                files = (Object[]) properties.get("files");
            }
            if (files != null && files.length > 0) {
                for (Object o : files) {
                    Map mapping = (HashMap) o;
                    String path = mapping.get("path").toString();
                    String fileName = mapping.get("fileName").toString();
                    String type = mapping.get("type").toString();
                        
                    try {
                        
                        if ("system".equals(type)) {
                            EmailAttachment attachment = new EmailAttachment();
                            attachment.setPath(path);
                            attachment.setName(MimeUtility.encodeText(fileName));
                            email.attach(attachment);
                        } else {
                            URL u = new URL(path);
                            email.attach(u, MimeUtility.encodeText(fileName), "");
                        }
                        
                    } catch(Exception e){
                        LogUtil.info(EmailTool.class.getName(), "Attached file fail from path \"" + path + "\"");
                        e.printStackTrace();
                    }
                }
            }

            Thread emailThread = new Thread(new Runnable() {

                public void run() {
                    try {
                        HostManager.setCurrentProfile(profile);
                        LogUtil.info(EmailTool.class.getName(), "EmailTool: Sending email from=" + fromStr + ", to=" + to + "cc=" + cc + ", bcc=" + bcc + ", subject=" + email.getSubject());
                        email.send();
                        LogUtil.info(EmailTool.class.getName(), "EmailTool: Sending email completed for subject=" + email.getSubject());
                    } catch (EmailException ex) {
                        LogUtil.error(EmailTool.class.getName(), ex, "");
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
                String smtpHost = request.getParameter("host");
                String smtpPort = request.getParameter("port");
                String smtpUsername = request.getParameter("username");
                String smtpPassword = SecurityUtil.decrypt(request.getParameter("password"));
                String security = request.getParameter("security");
                String from = request.getParameter("from");
                String to = request.getParameter("toSpecific");

                final HtmlEmail email = new HtmlEmail();
                email.setHostName(smtpHost);
                if (smtpPort != null && smtpPort.length() != 0) {
                    email.setSmtpPort(Integer.parseInt(smtpPort));
                }
                if (smtpUsername != null && !smtpUsername.isEmpty()) {
                    if (smtpPassword != null) {
                        smtpPassword = SecurityUtil.decrypt(smtpPassword);
                    }
                    email.setAuthentication(smtpUsername, smtpPassword);
                }
                if(security!= null){
                    if(security.equalsIgnoreCase("SSL") ){
                        email.setSSL(true);
                    }else if(security.equalsIgnoreCase("TLS")){
                        email.setTLS(true);
                    }
                }
                
                email.setFrom(from);
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
            } catch (Exception e) {
                LogUtil.error(this.getClassName(), e, "Test Email error");
                message = ResourceBundleUtil.getMessage("app.emailtool.testEmailFail") + "\n" + StringEscapeUtils.escapeJavaScript(e.getMessage());
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("message", message);
                jsonObject.write(response.getWriter());
            } catch (Exception e) {
                //ignore
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}