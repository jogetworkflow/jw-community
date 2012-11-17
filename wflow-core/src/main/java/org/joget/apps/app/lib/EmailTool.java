package org.joget.apps.app.lib;

import java.io.File;
import java.lang.String;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.activation.FileDataSource;
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

            final String fromStr = WorkflowUtil.processVariable(from, formDataTable, wfAssignment);
            email.setFrom(fromStr);
            email.setSubject(emailSubject);
            
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
            Object[] fields = (Object[]) properties.get("fields");
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
                                email.attach(fds, file.getName(), "");
                            }
                        }
                    } catch(Exception e){
                        LogUtil.info("EmailTool", "Attached file fail from field \"" + fieldId + "\" in form \"" + formDefId + "\"");
                    }
                }
            }
            
            Object[] files = (Object[]) properties.get("files");
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
                            attachment.setName(fileName);
                            email.attach(attachment);
                        } else {
                            URL u = new URL(path);
                            email.attach(u, fileName, "");
                        }
                        
                    } catch(Exception e){
                        LogUtil.info("EmailTool", "Attached file fail from path \"" + path + "\"");
                        e.printStackTrace();
                    }
                }
            }

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
