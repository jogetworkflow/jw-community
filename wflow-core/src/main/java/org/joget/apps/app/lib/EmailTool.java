package org.joget.apps.app.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
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
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
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
            final HtmlEmail email = AppUtil.createEmail(smtpHost, smtpPort, security, smtpUsername, smtpPassword, fromStr);
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
                            String values[] = value.split(";");
                            for (String v : values) {
                                if (!v.isEmpty()) {
                                    File file = FileUtil.getFile(v, loadForm, primaryKey);
                                    if (file != null) {
                                        FileDataSource fds = new FileDataSource(file);
                                        email.attach(fds, MimeUtility.encodeText(file.getName()), "");
                                    }
                                }
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
            attachIcal(email);

            Thread emailThread = new PluginThread(new Runnable() {

                public void run() {
                    try {
                        LogUtil.info(EmailTool.class.getName(), "EmailTool: Sending email from=" + email.getFromAddress().toString() + ", to=" + to + "cc=" + cc + ", bcc=" + bcc + ", subject=" + email.getSubject());
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
    
    protected void attachIcal(final HtmlEmail email) {
        try {
            if ("true".equalsIgnoreCase(getPropertyString("icsAttachement"))) {
                Calendar calendar = new Calendar();
                calendar.getProperties().add(Version.VERSION_2_0);
                calendar.getProperties().add(new ProdId("-//Joget Workflow//iCal4j 1.0//EN"));
                calendar.getProperties().add(CalScale.GREGORIAN);
                
                String eventName = getPropertyString("icsEventName");
                if (eventName.isEmpty()) {
                    eventName = email.getSubject();
                }
                
                String startDateTime = getPropertyString("icsDateStart");
                String endDateTime = getPropertyString("icsDateEnd");
                String dateFormat = getPropertyString("icsDateFormat");
                String timezoneString = getPropertyString("icsTimezone");
                SimpleDateFormat sdFormat =  new SimpleDateFormat(dateFormat);
                
                TimeZone timezone = null;
                TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
                try {
                    if (!timezoneString.isEmpty()) {
                        timezone = registry.getTimeZone(timezoneString);
                    } else {
                        timezone = registry.getTimeZone(TimeZoneUtil.getServerTimeZoneID());
                    }
                } catch (Exception et) {}
                
                java.util.Calendar startDate = new GregorianCalendar();
                if (timezone != null) {
                    startDate.setTimeZone(timezone);
                }
                startDate.setTime(sdFormat.parse(startDateTime));
                DateTime start = new DateTime(startDate.getTime());
                
                VEvent event;
                if (endDateTime.isEmpty()) {
                    event = new VEvent(start, eventName);
                } else {
                    java.util.Calendar endDate = new GregorianCalendar();
                    if (timezone != null) {
                        endDate.setTimeZone(timezone);
                    }
                    endDate.setTime(sdFormat.parse(endDateTime));
                    DateTime end = new DateTime(endDate.getTime());
                    
                    event = new VEvent(start, end, eventName);
                }
                
                String eventDesc = getPropertyString("icsEventDesc");
                if (!eventDesc.isEmpty()) {
                    event.getProperties().add(new Description(getPropertyString("icsEventDesc")));
                }
                
                if (timezone != null) {
                    VTimeZone tz = timezone.getVTimeZone();
                    event.getProperties().add(tz.getTimeZoneId());
                }
                
                if ("true".equalsIgnoreCase(getPropertyString("icsAllDay"))) {
                    event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);
                }
                
                if (!getPropertyString("icsLocation").isEmpty()) {
                    event.getProperties().add(new Location(getPropertyString("icsLocation")));
                }
                
                if (!getPropertyString("icsOrganizerEmail").isEmpty()) {
                    event.getProperties().add(new Organizer("MAILTO:"+getPropertyString("icsOrganizerEmail")));
                } else {
                    event.getProperties().add(new Organizer("MAILTO:"+email.getFromAddress().getAddress()));
                }
                
                Object[] attendees = null;
                if (getProperty("icsAttendees") instanceof Object[]){
                    attendees = (Object[]) getProperty("icsAttendees");
                }
                if (attendees != null && attendees.length > 0) {
                    for (Object o : attendees) {
                        Map mapping = (HashMap) o;
                        String name = mapping.get("name").toString();
                        String mailto = mapping.get("email").toString();
                        String required = mapping.get("required").toString();

                        try {
                            Attendee att = new Attendee(URI.create("mailto:"+mailto));
                            if ("true".equals(required)) {
                                att.getParameters().add(Role.REQ_PARTICIPANT);
                            } else {
                                att.getParameters().add(Role.OPT_PARTICIPANT);
                            }
                            att.getParameters().add(new Cn(name));
                            event.getProperties().add(att);
                        } catch(Exception ex){
                            LogUtil.error(getClassName(), ex, "");
                        }
                    }
                }
                
                calendar.getComponents().add(event);
                email.attach(new ByteArrayDataSource(calendar.toString(), "text/calendar;charset=UTF-8;ENCODING=8BIT;method=REQUEST"), MimeUtility.encodeText("invite.ics"), "");
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, null);
        }
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
}