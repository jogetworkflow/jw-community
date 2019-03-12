package org.joget.apps.app.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.UserReplacementDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserReplacement;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.workflow.model.ParticipantPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.DeadlinePlugin;
import org.joget.workflow.model.DecisionPlugin;
import org.joget.workflow.model.DecisionResult;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowDeadline;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service("workflowHelper")
public class AppWorkflowHelper implements WorkflowHelper {
    
    protected Map<String, AppDefinition> deadlineAppDefinitionCache = new HashMap<String, AppDefinition>();
    
    @Override
    public boolean executeTool(WorkflowAssignment assignment) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        AppDefinition appDef = null;
        AppDefinition originalAppDef = null;
        PackageDefinition packageDef = null;
        
        try {
            if (assignment != null) {
                WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
                PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) appContext.getBean("packageDefinitionDao");

                String processDefId = assignment.getProcessDefId();
                WorkflowProcess process = workflowManager.getProcess(processDefId);
                if (process != null) {
                    //check current appDef 
                    appDef = AppUtil.getCurrentAppDefinition();
                    if (appDef == null) {
                        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                        appDef = appService.getAppDefinitionWithProcessDefId(processDefId);
                    }
                    if (appDef != null) {
                        packageDef = appDef.getPackageDefinition();

                        if (!process.getPackageId().equals(appDef.getAppId()) || !process.getVersion().equals(packageDef.getVersion().toString()) ) {
                            packageDef = packageDefinitionDao.loadPackageDefinition(process.getPackageId(), Long.parseLong(process.getVersion()));
                            if (packageDef != null) {
                                originalAppDef = appDef;
                                appDef = packageDef.getAppDefinition();
                                AppUtil.setCurrentAppDefinition(appDef);
                            } else {
                                appDef = null;
                            }
                        }
                    }
                }
            }

            if (appDef != null && packageDef != null) {
                String processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(assignment.getProcessDefId());
                PackageActivityPlugin activityPluginMeta = packageDef.getPackageActivityPlugin(processDefId, assignment.getActivityDefId());

                Plugin plugin = null;

                if (activityPluginMeta != null) {
                    plugin = pluginManager.getPlugin(activityPluginMeta.getPluginName());
                }

                if (plugin != null) {
                    Map propertiesMap = AppPluginUtil.getDefaultProperties(plugin, activityPluginMeta.getPluginProperties(), appDef, assignment);
                    propertiesMap.put("workflowAssignment", assignment);
                    propertiesMap.put("pluginManager", pluginManager);
                    propertiesMap.put("appDef", appDef);

                    // add HttpServletRequest into the property map
                    try {
                        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                        if (request != null) {
                            propertiesMap.put("request", request);
                        }
                    } catch (Exception e) {
                        // ignore if class is not found
                    }

                    ApplicationPlugin appPlugin = (ApplicationPlugin) plugin;
                    if (appPlugin instanceof PropertyEditable) {
                        ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                    }
                    appPlugin.execute(propertiesMap);
                }
                return true;
            }

            return false;
        } finally {
            if (originalAppDef != null) {
                AppUtil.setCurrentAppDefinition(originalAppDef);
            }
        }
    }
    
    @Override
    public DecisionResult executeDecisionPlugin(String processDefId, String processId, String routeId, String routeActId, Map<String, String> variables) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) appContext.getBean("packageDefinitionDao");
                
        AppDefinition appDef = null;
        AppDefinition originalAppDef = null;
        PackageDefinition packageDef = null;
        
        try {
            WorkflowProcess process = workflowManager.getProcess(processDefId);
            if (process != null) {
                //check current appDef 
                appDef = AppUtil.getCurrentAppDefinition();
                if (appDef == null) {
                    AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                    appDef = appService.getAppDefinitionWithProcessDefId(processDefId);
                }
                if (appDef != null) {
                    packageDef = appDef.getPackageDefinition();

                    if (!process.getPackageId().equals(appDef.getAppId()) || !process.getVersion().equals(packageDef.getVersion().toString()) ) {
                        packageDef = packageDefinitionDao.loadPackageDefinition(process.getPackageId(), Long.parseLong(process.getVersion()));
                        if (packageDef != null) {
                            originalAppDef = appDef;
                            appDef = packageDef.getAppDefinition();
                            AppUtil.setCurrentAppDefinition(appDef);
                        } else {
                            appDef = null;
                        }
                    }
                }
            }

            if (appDef != null && packageDef != null) {
                String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
                PackageActivityPlugin activityPluginMeta = packageDef.getPackageActivityPlugin(processDefIdWithoutVersion, routeId);

                Plugin plugin = null;

                if (activityPluginMeta != null) {
                    plugin = pluginManager.getPlugin(activityPluginMeta.getPluginName());
                }

                if (plugin != null) {
                    WorkflowAssignment mockAssignment = new WorkflowAssignment();
                    mockAssignment.setProcessId(processId);
                    
                    Map propertiesMap = AppPluginUtil.getDefaultProperties(plugin, activityPluginMeta.getPluginProperties(), appDef, mockAssignment);
                    propertiesMap.put("pluginManager", pluginManager);
                    propertiesMap.put("appDef", appDef);
                    propertiesMap.put("processDefId", processDefId);
                    propertiesMap.put("processId", processId);
                    propertiesMap.put("routeId", routeId);

                    DecisionPlugin appPlugin = (DecisionPlugin) plugin;
                    if (appPlugin instanceof PropertyEditable) {
                        ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                    }
                    DecisionResult result = appPlugin.getDecision(processDefId, processId, routeId, variables);
                    
                    if (result != null && !result.getVariables().isEmpty()) {
                        workflowManager.activityVariables(routeActId, result.getVariables());
                        workflowManager.processVariables(processId, result.getVariables());
                    }
                    
                    return result;
                }
            }
        } finally {
            if (originalAppDef != null) {
                AppUtil.setCurrentAppDefinition(originalAppDef);
            }
        }
        return null;
    }

    @Override
    public List<String> getAssignmentUsers(String packageId, String procDefId, String procId, String version, String actId, String requesterUsername, String participantId) {
        List<String> resultList = null;
        AppDefinition originalAppDef = null;
        try {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) appContext.getBean("packageDefinitionDao");

            //check current app definition
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef == null) {
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                appDef = appService.getAppDefinitionWithProcessDefId(procDefId);
            }
            PackageDefinition packageDef = null;
            if (appDef != null) {
                packageDef = appDef.getPackageDefinition();

                if (packageDef == null || !packageId.equals(packageDef.getId()) || !version.equals(packageDef.getVersion().toString())) {
                    Long packageVersion = Long.parseLong(version);
                    packageDef = packageDefinitionDao.loadPackageDefinition(packageId, packageVersion);

                    if (packageDef != null) {
                        //Set app definition    
                        originalAppDef = appDef;
                        appDef = packageDef.getAppDefinition();
                        AppUtil.setCurrentAppDefinition(appDef);
                    }
                }
            }
            
            procDefId = WorkflowUtil.getProcessDefIdWithoutVersion(procDefId);
            
            if (packageDef != null) {
                PackageParticipant participant = packageDef.getPackageParticipant(procDefId, participantId);

                //if process start white list and app is not publish
                if (WorkflowUtil.PROCESS_START_WHITE_LIST.equals(participantId) && !appDef.isPublished()) {
                    resultList = getParticipantsByAdminUser();
                } else if (WorkflowUtil.PROCESS_START_WHITE_LIST.equals(participantId) && appDef.isPublished() && participant == null) {
                    resultList = getParticipantsByCurrentUser();
                } else if (participant != null) {
                    if (PackageParticipant.TYPE_USER.equals(participant.getType())) {
                        resultList = getParticipantsByUsers(participant);
                    } else if (PackageParticipant.TYPE_GROUP.equals(participant.getType())) {
                        resultList = getParticipantsByGroups(participant);
                    } else if (PackageParticipant.TYPE_REQUESTER.equals(participant.getType())) {
                        resultList = getParticipantsByRequester(participant, procDefId, procId, requesterUsername);
                    } else if (PackageParticipant.TYPE_REQUESTER_HOD.equals(participant.getType())) {
                        resultList = getParticipantsByRequesterHod(participant, procDefId, procId, requesterUsername);
                    } else if (PackageParticipant.TYPE_REQUESTER_HOD_IGNORE_REPORT_TO.equals(participant.getType())) {
                        resultList = getParticipantsByRequesterHodIgnoreReportTo(participant, procDefId, procId, requesterUsername);
                    } else if (PackageParticipant.TYPE_REQUESTER_SUBORDINATES.equals(participant.getType())) {
                        resultList = getParticipantsByRequesterSubordinates(participant, procDefId, procId, requesterUsername);
                    } else if (PackageParticipant.TYPE_REQUESTER_DEPARTMENT.equals(participant.getType())) {
                        resultList = getParticipantsByRequesterDepartment(participant, procDefId, procId, requesterUsername);
                    } else if (PackageParticipant.TYPE_DEPARTMENT.equals(participant.getType())) {
                        resultList = getParticipantsByDepartment(participant);
                    } else if (PackageParticipant.TYPE_HOD.equals(participant.getType())) {
                        resultList = getParticipantsByHod(participant);
                    } else if (PackageParticipant.TYPE_WORKFLOW_VARIABLE.equals(participant.getType())) {
                        resultList = getParticipantsByWorkflowVariable(participant, actId);
                    } else if (PackageParticipant.TYPE_PLUGIN.equals(participant.getType())) {
                        resultList = getParticipantsByPlugin(participant, procDefId, procId, version, actId);
                    } else if (PackageParticipant.TYPE_ROLE.equals(participant.getType()) && PackageParticipant.VALUE_ROLE_LOGGED_IN_USER.equals(participant.getValue())) {
                        resultList = getParticipantsByLoggedInUser();
                    } else if (PackageParticipant.TYPE_ROLE.equals(participant.getType()) && PackageParticipant.VALUE_ROLE_ADMIN.equals(participant.getValue())) {
                        resultList = getParticipantsByAdminUser();
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.error(WorkflowUtil.class.getName(), ex, "");
        } finally {
            if (originalAppDef != null) {
                AppUtil.setCurrentAppDefinition(originalAppDef);
            }
        }
        
        // remove duplicates
        if (resultList != null) {
            HashSet<String> resultSet = new HashSet<String>(resultList);
            resultList = new ArrayList<String>(resultSet);
        }
        return resultList;
    }

    /**
     * Retrieve participants mapped to one or more usernames.
     * @param participant
     * @return 
     */
    protected List<String> getParticipantsByUsers(PackageParticipant participant) {
        List<String> resultList = new ArrayList<String>();
        if (participant != null && participant.getValue() != null) {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
            String[] users = participant.getValue().replaceAll(";", ",").split(",");
            for (String userId : users) {
                User user = directoryManager.getUserById(userId);
                if (user != null) {
                    resultList.add(user.getUsername());
                }
            }
        }
        return resultList;
    }

    /**
     * Retrieve participants mapped to one or more groups.
     * @param participant
     * @return 
     */
    protected List<String> getParticipantsByGroups(PackageParticipant participant) {
        List<String> resultList = new ArrayList<String>();
        if (participant != null && participant.getValue() != null) {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
            String[] groups = participant.getValue().replaceAll(";", ",").split(",");
            for (String groupId : groups) {
                Collection<User> users = directoryManager.getUserByGroupId(groupId);
                for (User user : users) {
                    if (user != null) {
                        resultList.add(user.getUsername());
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * Retrieve a participant based on a performer of a previous activity.
     * @param participant
     * @param processDefId
     * @param processId
     * @param requesterUsername
     * @return 
     */
    protected List<String> getParticipantsByRequester(PackageParticipant participant, String processDefId, String processId, String requesterUsername) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        if (participant.getValue() != null && participant.getValue().trim().length() > 0) {
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            String activityDefId = participant.getValue();
            requesterUsername = workflowManager.getUserByProcessIdAndActivityDefId(processDefId, processId, activityDefId);
        }
        if (requesterUsername != null && requesterUsername.trim().length() > 0) {
            User user = directoryManager.getUserByUsername(requesterUsername);
            if (user != null) {
                resultList.add(user.getUsername());
            }
        }
        return resultList;
    }

    /**
     * Retrieve a participant based on the HOD of the performer of a previous activity.
     * @param participant
     * @param processDefId
     * @param processId
     * @param requesterUsername
     * @return 
     */
    protected List<String> getParticipantsByRequesterHod(PackageParticipant participant, String processDefId, String processId, String requesterUsername) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        if (participant.getValue() != null && participant.getValue().trim().length() > 0) {
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            requesterUsername = workflowManager.getUserByProcessIdAndActivityDefId(processDefId, processId, participant.getValue());
        }
        Collection<User> users = directoryManager.getUserHod(requesterUsername);
        for (User user : users) {
            if (user != null) {
                resultList.add(user.getUsername());
            }
        }
        return resultList;
    }
    
    protected List<String> getParticipantsByRequesterHodIgnoreReportTo(PackageParticipant participant, String processDefId, String processId, String requesterUsername) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        if (participant.getValue() != null && participant.getValue().trim().length() > 0) {
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            requesterUsername = workflowManager.getUserByProcessIdAndActivityDefId(processDefId, processId, participant.getValue());
        }
        
        User requester = directoryManager.getUserByUsername(requesterUsername);
        if (requester != null && requester.getEmployments() != null && !requester.getEmployments().isEmpty()) {
            Employment employment = (Employment) requester.getEmployments().iterator().next();
            if (employment != null && employment.getDepartment() != null) {
                Department dept = employment.getDepartment();
                User hod = directoryManager.getDepartmentHod(dept.getId());
                
                if (hod != null) {
                    resultList.add(hod.getUsername());
                }
            }
        }
        
        return resultList;
    }

    /**
     * Retrieve the participants based on the subordinates of the performer of a previous activity.
     * @param participant
     * @param processDefId
     * @param processId
     * @param requesterUsername
     * @return 
     */
    protected List<String> getParticipantsByRequesterSubordinates(PackageParticipant participant, String processDefId, String processId, String requesterUsername) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        if (participant.getValue() != null && participant.getValue().trim().length() > 0) {
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            requesterUsername = workflowManager.getUserByProcessIdAndActivityDefId(processDefId, processId, participant.getValue());
        }
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        Collection<User> users = directoryManager.getUserSubordinate(requesterUsername);
        for (User user : users) {
            if (user != null) {
                resultList.add(user.getUsername());
            }
        }
        return resultList;
    }

    /**
     * Retrieve the participants based on the department members of the performer of a previous activity.
     * @param participant
     * @param processDefId
     * @param processId
     * @param requesterUsername
     * @return 
     */
    protected List<String> getParticipantsByRequesterDepartment(PackageParticipant participant, String processDefId, String processId, String requesterUsername) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        if (participant.getValue() != null && participant.getValue().trim().length() > 0) {
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
            requesterUsername = workflowManager.getUserByProcessIdAndActivityDefId(processDefId, processId, participant.getValue());
        }
        Collection<User> users = directoryManager.getUserDepartmentUser(requesterUsername);
        for (User user : users) {
            if (user != null) {
                resultList.add(user.getUsername());
            }
        }
        return resultList;

    }

    /**
     * Retrieve the participants based on members of a specific department.
     * @param participant
     * @return 
     */
    protected List<String> getParticipantsByDepartment(PackageParticipant participant) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        String departmentId = participant.getValue();
        Collection<User> users = directoryManager.getUserByDepartmentId(departmentId);
        for (User user : users) {
            if (user != null) {
                resultList.add(user.getUsername());
            }
        }
        return resultList;
    }

    /**
     * Retrieve the participant based on the HOD of a specific department.
     * @param participant
     * @return 
     */
    protected List<String> getParticipantsByHod(PackageParticipant participant) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        String departmentId = participant.getValue();
        User user = directoryManager.getDepartmentHod(departmentId);
        if (user != null) {
            resultList.add(user.getUsername());
        }
        return resultList;
    }

    /**
     * Retrieve the participants based on the value of a workflow variable.
     * @param participant
     * @param activityId
     * @return 
     */
    protected List<String> getParticipantsByWorkflowVariable(PackageParticipant participant, String activityId) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        String variableName = null;
        String variableType = null;
        String variableStr = participant.getValue();
        if (variableStr != null) {
            StringTokenizer st = new StringTokenizer(variableStr.replaceAll(";", ","), ",");
            if (st.hasMoreTokens()) {
                variableName = st.nextToken();
            }
            if (st.hasMoreTokens()) {
                variableType = st.nextToken();
            }
        }
        //if is workflow variable
        Collection<WorkflowVariable> varList = workflowManager.getActivityVariableList(activityId);
        for (WorkflowVariable va : varList) {
            if (va.getName() != null && va.getName().equals(variableName)) {
                String variableValue = (String) va.getVal();
                variableValue = variableValue.replace(",", ";");

                StringTokenizer valueST = new StringTokenizer(variableValue, ";");
                Collection<User> users = new ArrayList<User>();

                while (valueST.hasMoreTokens()) {
                    String value = valueST.nextToken();
                    value = value.trim();

                    if (PackageParticipant.TYPE_GROUP.equals(variableType)) {
                        Collection<User> tempUsers = directoryManager.getUserByGroupId(value);
                        users.addAll(tempUsers);
                    } else if (PackageParticipant.TYPE_USER.equals(variableType)) {
                        User user = directoryManager.getUserByUsername(value);
                        users.add(user);
                    } else if (PackageParticipant.TYPE_HOD.equals(variableType)) {
                        User user = directoryManager.getDepartmentHod(value);
                        users.add(user);
                    } else if (PackageParticipant.TYPE_DEPARTMENT.equals(variableType)) {
                        Collection<User> tempUsers = directoryManager.getUserByDepartmentId(value);
                        users.addAll(tempUsers);
                    }
                }
                for (User user : users) {
                    if (user != null) {
                        resultList.add(user.getUsername());
                    }
                }
                break;
            }
        }
        return resultList;
    }

    /**
     * Retrieve the participants via a participant plugin.
     * @param participant
     * @param activityId
     * @param participantId
     * @param processId
     * @param version
     * @param processDefId
     * @return 
     */
    protected List<String> getParticipantsByPlugin(PackageParticipant participant, String processDefId, String processId, String version, String activityId) {
        List<String> resultList = new ArrayList<String>();
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        String properties = participant.getPluginProperties();
        String participantId = participant.getParticipantId();
        try {
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            ParticipantPlugin plugin = (ParticipantPlugin) pluginManager.getPlugin(participant.getValue());
            
            //create a mock assignemnt for Form Hash Variable
            WorkflowAssignment ass = new WorkflowAssignment();
            ass.setProcessId(processId);
            
            Map propertyMap = AppPluginUtil.getDefaultProperties((Plugin)plugin, properties, appDef, ass);
            propertyMap.put("pluginManager", pluginManager);
            WorkflowActivity activity = workflowManager.getActivityById(activityId);
            propertyMap.put("workflowActivity", activity);
            if (plugin instanceof PropertyEditable) {
                ((PropertyEditable) plugin).setProperties(propertyMap);
            }
            
            Collection<String> pluginResult = plugin.getActivityAssignments(propertyMap);
            if (pluginResult != null && pluginResult.size() > 0) {
                resultList.addAll(pluginResult);
            }
        } catch (Exception ex) {
            addAuditTrail(WorkflowUtil.class.getName(), "getAssignmentUsers", "Error executing plugin [pluginName=" + participant.getValue() + ", participantId=" + participantId + ", processId=" + processId + ", version=" + version + ", activityId=" + activityId + "]");
            LogUtil.error(WorkflowUtil.class.getName(), ex, "Error executing plugin [pluginName=" + participant.getValue() + ", participantId=" + participantId + ", processDefId=" + processDefId + ", version=" + version + ", activityId=" + activityId + "]");
        }
        return resultList;
    }
    
    /**
     * Retrieve the participants based on current user (Including anonymous)
     * @return 
     */
    protected List<String> getParticipantsByCurrentUser() {
        List<String> resultList = new ArrayList<String>();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        resultList.add(workflowUserManager.getCurrentUsername());
        return resultList;
    }
    
    /**
     * Retrieve the participants based on logged in user
     * @return 
     */
    protected List<String> getParticipantsByLoggedInUser() {
        List<String> resultList = new ArrayList<String>();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        if (!workflowUserManager.isCurrentUserAnonymous()) {
            resultList.add(workflowUserManager.getCurrentUsername());
        }
        return resultList;
    }
    
    /**
     * Retrieve the participants based on admin user
     * @return 
     */
    protected List<String> getParticipantsByAdminUser() {
        List<String> resultList = new ArrayList<String>();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        if (workflowUserManager.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN)) {
            resultList.add(workflowUserManager.getCurrentUsername());
        }
        return resultList;
    }

    @Override
    public String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap) {
        return AppUtil.processHashVariable(content, wfAssignment, escapeFormat, replaceMap);
    }
    
    @Override
    public void addAuditTrail(String clazz, String method, String message) {
        addAuditTrail(clazz, method, message, null, null, null);
    }

    @Override
    public void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        AuditTrailManager auditTrailManager = (AuditTrailManager) appContext.getBean("auditTrailManager");
        auditTrailManager.addAuditTrail(clazz, method, message, paramTypes, args, returnObject);
    }

    @Override
    public WorkflowDeadline executeDeadlinePlugin(String processId, String activityId, WorkflowDeadline deadline, Date processStartedTime, Date activityAcceptedTime, Date activityActivatedTime) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) AppUtil.getApplicationContext().getBean("packageDefinitionDao");
        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");

        Collection<Plugin> pluginList = pluginManager.list(DeadlinePlugin.class);
        for (Plugin plugin : pluginList) {
            DeadlinePlugin p = (DeadlinePlugin) plugin;
            AppDefinition originalAppDef = null;
            try {
                AppDefinition appDef = null;

                //get package definition by process id
                WorkflowProcess process = workflowManager.getRunningProcessById(processId);

                if (process != null) {
                    //check current appDef 
                    appDef = AppUtil.getCurrentAppDefinition();
                    if (appDef == null) {
                        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                        appDef = appService.getAppDefinitionForWorkflowProcess(processId);
                    }
                    if (appDef != null) {
                        PackageDefinition packageDef = appDef.getPackageDefinition();

                        if (!process.getPackageId().equals(appDef.getAppId()) || (packageDef != null && !process.getVersion().equals(packageDef.getVersion().toString()))) {
                            packageDef = packageDefinitionDao.loadPackageDefinition(process.getPackageId(), Long.parseLong(process.getVersion()));
                            if (packageDef != null) {
                                originalAppDef = appDef;
                                appDef = packageDef.getAppDefinition();
                                AppUtil.setCurrentAppDefinition(appDef);
                            } else {
                                appDef = null;
                            }
                        }
                    }
                }

                if (appDef != null) {
                    PluginDefaultProperties pluginDefaultProperties = AppPluginUtil.getPluginDefaultProperties(ClassUtils.getUserClass(plugin).getName(), appDef);

                    if (pluginDefaultProperties != null) {
                        Map propertiesMap = new HashMap();

                        if (!(plugin instanceof PropertyEditable)) {
                            propertiesMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                        } else {
                            String json = pluginDefaultProperties.getPluginProperties();

                            //process basic hash variable
                            json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null);
                            propertiesMap = PropertyUtil.getPropertiesValueFromJson(json);
                        }

                        propertiesMap.put("processId", processId);
                        propertiesMap.put("activityId", activityId);
                        propertiesMap.put("workflowDeadline", deadline);
                        propertiesMap.put("processStartedTime", processStartedTime);
                        propertiesMap.put("activityAcceptedTime", activityAcceptedTime);
                        propertiesMap.put("activityActivatedTime", activityActivatedTime);
                        propertiesMap.put("pluginManager", pluginManager);
                        
                        if (p instanceof PropertyEditable) {
                            ((PropertyEditable) p).setProperties(propertiesMap);
                        }

                        return p.evaluateDeadline(propertiesMap);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "Error executing Deadline plugin " + p.getClass().getName());
            } finally {
                if (originalAppDef != null) {
                    AppUtil.setCurrentAppDefinition(originalAppDef);
                }
            }
        }
        return deadline;
    }
    
    @Override
    public String getPublishedPackageVersion(String packageId) {
        //appID same with packageId
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        
        AppDefinition appDef = appService.getPublishedAppDefinition(packageId);
        if (appDef != null) {
            PackageDefinition packageDef = appDef.getPackageDefinition();

            if (packageDef != null && packageDef.getVersion() != null) {
                return packageDef.getVersion().toString();
            }
        }
        return null;
    }

    public Map<String, Collection<String>> getReplacementUsers(String username) {
        UserReplacementDao userReplacementDao = (UserReplacementDao) AppUtil.getApplicationContext().getBean("userReplacementDao");
        Map<String, Collection<String>> replacements = new HashMap<String, Collection<String>>();
        String profile = DynamicDataSourceManager.getCurrentProfile();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String cacheKey = profile + ":USER_REPLACEMENT_" + username + "_" + sf.format(new Date());
        
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("setupManagerCache");
        if (cache != null) {
            Element element = cache.get(cacheKey);
            if (element != null) {
                replacements = (HashMap<String, Collection<String>>) element.getObjectValue();
                return replacements;
            }
        }
        
        Collection<UserReplacement> userReplacements = userReplacementDao.getTodayUserReplacements(username);
        for (UserReplacement ur : userReplacements) {
            Collection<String> processes = replacements.get(ur.getUsername());
            if (processes == null) {
                processes = new ArrayList<String>();
            }
            if (ur.getAppId() != null && !ur.getAppId().isEmpty()) {
                if (ur.getProcessIds() != null && !ur.getProcessIds().isEmpty()) {
                    processes.addAll(Arrays.asList(ur.getProcessIds().split(";")));
                } else {
                    processes.addAll(Arrays.asList(ur.getAppId().split(";")));
                }
            }
            
            replacements.put(ur.getUsername(), processes);
            if (cache != null) {
                Element element = new Element(cacheKey, replacements);
                cache.put(element);
            }
        }
        
        return replacements;
    }
    
    @Override
    public Map<String, String> getPublishedPackageVersions() {
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) WorkflowUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> list = appDefinitionDao.findPublishedApps(null, null, null, null);
        Map<String, String> map = new HashMap<String, String>();
        
        for (AppDefinition appDef : list) {
            PackageDefinition packageDefiniton = appDef.getPackageDefinition();
            if (packageDefiniton != null) {
                map.put(packageDefiniton.getAppId(), packageDefiniton.getVersion().toString());
            }
        }
        
        return map;
    }
    
    public void cleanDeadlineAppDefinitionCache(String packageId, String packageVersion) {
        deadlineAppDefinitionCache.remove(HostManager.getCurrentProfile() + ":" + packageId + ":" + packageVersion);
    }

    public void updateAppDefinitionForDeadline(String processId, String packageId, String packageVersion) {
        AppDefinition appDef = deadlineAppDefinitionCache.get(HostManager.getCurrentProfile() + ":" + packageId + ":" + packageVersion);
        if (appDef == null) {
            PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) WorkflowUtil.getApplicationContext().getBean("packageDefinitionDao");
            appDef = packageDefinitionDao.getAppDefinitionByPackage(packageId, Long.parseLong(packageVersion));
            deadlineAppDefinitionCache.put(HostManager.getCurrentProfile() + ":" + packageId + ":" + packageVersion, appDef);
        }
        AppUtil.setCurrentAppDefinition(appDef);
    }

    @Override
    public String translateProcessLabel(String processId, String processDefId, String activityDefId, String defaultLabel) {
        AppDefinition orgAppDef = AppUtil.getCurrentAppDefinition();
        try {
            String key = "plabel." + WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
            if (activityDefId != null) {
                key = key + "." + activityDefId;
            }
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef == null || (appDef != null && !processDefId.startsWith(appDef.getAppId() + "#"))) {
                appDef = AppUtil.getAppDefinitionByProcess(processDefId);
                AppUtil.setCurrentAppDefinition(appDef);
            }
            Map<String, String> labels = AppUtil.getAppMessages(appDef);
            if (labels != null && !labels.isEmpty() && labels.containsKey(key)) {
                WorkflowAssignment ass = new WorkflowAssignment();
                ass.setProcessId(processId);
                return AppUtil.processHashVariable(labels.get(key), ass, null, null, appDef);
            }
        } catch (Exception e) {
        } finally {
            AppUtil.setCurrentAppDefinition(orgAppDef);
        }
        return defaultLabel;
    }
    
    public void cleanForDeadline() {
        AuditTrailManager auditTrailManager = (AuditTrailManager) WorkflowUtil.getApplicationContext().getBean("auditTrailManager");
        auditTrailManager.clean();
    }
}
