package org.joget.apps.app.lib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.joget.apps.app.dao.UserReplacementDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserReplacement;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.PushServiceUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

public class PushNotificationTool extends DefaultApplicationPlugin{
    
    @Override
    public String getName() {
        return "Push Notification";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Send Push Notification to users with PWA Notification enabled.";
    }

    @Override
    public String getLabel() {
        return "Push Notification";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/pushNotificationTool.json", null, true, null);
    }
    
    protected Collection<String> getUserList(String toParticipantId, String toSpecific, WorkflowAssignment wfAssignment, AppDefinition appDef) {
        Collection<String> users = new HashSet<String>();

        if (toParticipantId != null && !toParticipantId.isEmpty() && wfAssignment != null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            if(wfAssignment.getProcessDefId()!=null){

                WorkflowProcess process = workflowManager.getProcess(wfAssignment.getProcessDefId());
                toParticipantId = toParticipantId.replace(";", ",");
                String pIds[] = toParticipantId.split(",");
                for (String pId : pIds) {
                    pId = pId.trim();
                    if (pId.length() == 0) {
                        continue;
                    }

                    Collection<String> userList = null;
                    if(process != null){
                        userList = WorkflowUtil.getAssignmentUsers(process.getPackageId(), wfAssignment.getProcessDefId(), wfAssignment.getProcessId(), wfAssignment.getProcessVersion(), wfAssignment.getActivityId(), "", pId.trim());
                    }else{
                       LogUtil.info(PushNotificationTool.class.getName(), "Retrieving Assignment Users Failed");
                    }
                    
                    if (userList != null && userList.size() > 0) {
                        users.addAll(userList);
                    }
                }
            }else{
                LogUtil.info(PushNotificationTool.class.getName(), "Unable to retrieve process details");
            }

            //send to replacement user
            if (!users.isEmpty()) {
                Collection<String> userList = new HashSet<String>();
                String args[] = wfAssignment.getProcessDefId().split("#");
                
                for (String u : users) {
                    UserReplacementDao urDao = (UserReplacementDao) AppUtil.getApplicationContext().getBean("userReplacementDao");
                    Collection<UserReplacement> replaces = urDao.getUserTodayReplacedBy(u, args[0], args[2]);
                    if (replaces != null && !replaces.isEmpty()) {
                        for (UserReplacement ur : replaces) {
                            userList.add(ur.getReplacementUser());
                        }
                    }
                }
                
                if (userList.size() > 0) {
                    users.addAll(userList);
                }
            }
        }

        if (toSpecific != null && toSpecific.trim().length() != 0) {
            toSpecific = AppUtil.processHashVariable(toSpecific, wfAssignment, null, null, appDef);
            toSpecific = toSpecific.replace(";", ","); // add support for MS-style semi-colon (;) as a delimiter
            String userlList[] = toSpecific.split(",");
            for (String user : userlList) {
                user = user.trim();
                if (user.length() == 0) {
                    continue;
                }
                users.add(user);
            }
        }

        return users;
    }
    
    @Override
    public Object execute(Map props) {
        try{
            WorkflowAssignment wfAssignment = (WorkflowAssignment) props.get("workflowAssignment");
            AppDefinition appDef = (AppDefinition) props.get("appDef");
            String subject = (String) props.get("subject");
            String message = (String) props.get("message");
            String toParticipantId = (String) props.get("toParticipantId");
            String toSpecific = (String) props.get("toSpecific");
            String url = (String) props.get("url");

            String formattedSubject = "";
            if (subject != null && subject.length() != 0) {
                formattedSubject = WorkflowUtil.processVariable(subject, null, wfAssignment);
            }
            String formattedMessage = "";
            if (message != null && message.length() != 0) {
                formattedMessage = WorkflowUtil.processVariable(message, null, wfAssignment);
            }

            Collection<String> users = getUserList(toParticipantId, toSpecific, wfAssignment, appDef);
            for (String user : users) {
                LogUtil.info(PushNotificationTool.class.getName(), "Sending push notification to=" + user + ", subject=" + subject);

                int result = PushServiceUtil.sendUserPushNotification(user, formattedSubject, formattedMessage, url, "", "", true);

                if(result == 1){
                    LogUtil.info(PushNotificationTool.class.getName(), "Sending push notification to=" + user + ", subject=" + subject + ", result=Success");
                }else{
                    LogUtil.info(PushNotificationTool.class.getName(), "Sending push notification to=" + user + ", subject=" + subject + ", result=Failed");
                }
            }
        } catch (Exception e) {
            LogUtil.error(PushNotificationTool.class.getName(), e, "");
        }
        return null;
    }
}
