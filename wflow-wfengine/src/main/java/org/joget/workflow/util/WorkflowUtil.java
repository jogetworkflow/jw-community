package org.joget.workflow.util;

import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.commons.util.SetupManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class WorkflowUtil implements ApplicationContextAware {

    public static final String FORM_DATA = "form";
    public static final String WORKFLOW_ASSIGNMENT = "assignment";
    public static final String WORKFLOW_VARIABLE = "variable";
    public static final String WORKFLOW_USER = "user";
    public static final String WORKFLOW_USER_VARIABLE = "uservariable";
    public static final String DATE = "date";
    public static final String CURRENT_USERNAME = "currentUsername";
    public static final String PERFORMER = "performer";
    public static final String ROLE_ADMIN = WorkflowUserManager.ROLE_ADMIN;
    public static final String ACTIVITY_DEF_ID_RUN_PROCESS = "runProcess";
    public static final String PROCESS_START_WHITE_LIST = "processStartWhiteList";
    static ApplicationContext appContext;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public static List<String> getAssignmentUsers(String packageId, String procDefId, String procId, String version, String actId, String requesterUsername, String participantId) {
        List<String> resultList = null;

        try {
            WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");
            resultList = workflowMapper.getAssignmentUsers(packageId, procDefId, procId, version, actId, requesterUsername, participantId);
        } catch (Exception ex) {
            LogUtil.error(WorkflowUtil.class.getName(), ex, "");
        } finally {
            // remove duplicates
            if (resultList != null) {
                HashSet<String> resultSet = new HashSet<String>(resultList);
                resultList = new ArrayList<String>(resultSet);
            }
            return resultList;
        }
    }

    public static boolean containsHashVariable(String content) {
        boolean result = (content != null && content.indexOf("#") >= 0);
        return result;
    }

    public static String processVariable(String content, String formDataTable, WorkflowAssignment wfAssignment) {
        return processVariable(content, formDataTable, wfAssignment, null, null);
    }

    public static String processVariable(String content, String formDataTable, WorkflowAssignment wfAssignment, String escapeFormat) {
        return processVariable(content, formDataTable, wfAssignment, escapeFormat, null);
    }

    public static String processVariable(String content, String formDataTable, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap) {

        String result = content;
        try {
            WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");
            result = workflowMapper.processHashVariable(content, wfAssignment, escapeFormat, replaceMap);
        } catch (Exception e) {
            LogUtil.error(WorkflowUtil.class.getName(), e, "Error processing hash variable for: " + content);
        }
        return result;
    }

    public static String getUserAttribute(String username, String attribute) {
        String attributeValue = null;

        try {
            DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
            User user = directoryManager.getUserByUsername(username);

            if (user != null) {
                //convert first character to upper case
                char firstChar = attribute.charAt(0);
                firstChar = Character.toUpperCase(firstChar);
                attribute = firstChar + attribute.substring(1, attribute.length());

                Method method = User.class.getDeclaredMethod("get" + attribute, new Class[]{});
                String returnResult = (String) method.invoke(user, new Object[]{});
                if (returnResult == null || attribute.equals("Password")) {
                    returnResult = "";
                }

                attributeValue = returnResult;
            }
        } catch (Exception e) {
            LogUtil.error(WorkflowUtil.class.getName(), e, "Error retrieving user attribute " + attribute);
        }
        return attributeValue;
    }

    public static String getSystemSetupValue(String propertyName) {
        SetupManager setupManager = (SetupManager) appContext.getBean("setupManager");
        return setupManager.getSettingValue(propertyName);
    }

    public static String getCurrentUsername() {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");
        String username = workflowUserManager.getCurrentUsername();
        return username;
    }

    public static String getCurrentUserFullName() {
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        String username = getCurrentUsername();
        User user = directoryManager.getUserByUsername(username);
        if (user != null && user.getFirstName() != null && user.getFirstName().trim().length() > 0) {
            return user.getFirstName() + " " + user.getLastName();
        } else {
            return username;
        }
    }

    public static boolean isCurrentUserInRole(String role) {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");
        boolean result = workflowUserManager.isCurrentUserInRole(role);
        return result;
    }

    public static boolean isCurrentUserAnonymous() {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");
        boolean result = workflowUserManager.isCurrentUserAnonymous();
        return result;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }

    /**
     * Returns the HTTP Servlet Request associated with the current thread.
     * @return The HTTP request if it is available. If the request is not available, e.g. when triggered from a deadline, null is returned.
     */
    public static HttpServletRequest getHttpServletRequest() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            return request;
        } catch (Exception e) {
            // ignore if servlet request is not available, e.g. when triggered from a deadline
            return null;
        }
    }

    /**
     * Returns the HTTP Servlet Response associated with the current thread.
     * @return The HTTP Response if it is available. If the response is not available, e.g. when triggered from a deadline, null is returned.
     */
    public static HttpServletResponse getHttpServletResponse() {
        try {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            return response;
        } catch (NoClassDefFoundError e) {
            // ignore if servlet request class is not available
            return null;
        } catch (IllegalStateException e) {
            // ignore if servlet request is not available, e.g. when triggered from a deadline
            return null;
        }
    }
    
    /**
     * Retrieves the process definition ID without the version and package.
     * @param processDefId
     * @return
     */
    public static String getProcessDefIdWithoutVersion(String processDefId) {
        String result = processDefId;
        if (processDefId != null) {
            StringTokenizer st = new StringTokenizer(processDefId, "#");
            if (st.countTokens() > 2) {
                st.nextToken(); // packageId
                st.nextToken(); // version
                result = st.nextToken();
            }
        }
        return result;
    }

    /**
     * Retrieves the package ID from the process definition ID.
     * @param processDefId
     * @return
     */
    public static String getProcessDefPackageId(String processDefId) {
        String result = null;
        if (processDefId != null) {
            StringTokenizer st = new StringTokenizer(processDefId, "#");
            if (st.countTokens() > 2) {
                result = st.nextToken();
            }
        }
        return result;
    }

    /**
     * Retrieves the version from the process definition.
     * @param processDefId
     * @return
     */
    public static String getProcessDefVersion(String processDefId) {
        String result = null;
        if (processDefId != null) {
            StringTokenizer st = new StringTokenizer(processDefId, "#");
            if (st.countTokens() > 2) {
                st.nextToken(); // packageId
                result = st.nextToken();
            }
        }
        return result;
    }

    public static void addAuditTrail(String clazz, String method, String message) {
        addAuditTrail(clazz, method, message, null, null, null);
    }
    
    public static void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject) {
        try {
            WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");
            workflowMapper.addAuditTrail(clazz, method, message, paramTypes, args, returnObject);
        } catch (Exception e) {
            LogUtil.error(WorkflowUtil.class.getName(), e, "Error add audit trail");
        }
    }
    
    public static String getServiceLevelIndicator(double value){
        if (value >= 0) {
            String warningLevel = getSystemSetupValue("mediumWarningLevel");
            int mediumWarningLevel = (warningLevel != null && warningLevel.trim().length() > 0 ? 100 - Integer.parseInt(warningLevel) : 80);

            warningLevel = getSystemSetupValue("criticalWarningLevel");
            int criticalWarningLevel = (warningLevel != null && warningLevel.trim().length() > 0 ? 100 - Integer.parseInt(warningLevel) : 50);

            if (value <= criticalWarningLevel) {
                return "<span class=\"dot_red\">&nbsp;</span>";
            } else if (value > criticalWarningLevel && value <= mediumWarningLevel) {
                return "<span class=\"dot_yellow\">&nbsp;</span>";
            } else {
                return "<span class=\"dot_green\">&nbsp;</span>";
            }
        }else{
            return "-";
        }
    }
}
