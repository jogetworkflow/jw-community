package org.joget.workflow.shark.model.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.migrate.model.MigrateAssignment;
import org.joget.workflow.shark.migrate.model.MigrateProcess;
import org.joget.workflow.shark.migrate.model.MigrateProcessDefinition;
import org.joget.workflow.shark.model.SharkActivityHistory;
import org.joget.workflow.shark.model.SharkActivity;
import org.joget.workflow.shark.model.SharkAssignment;
import org.joget.workflow.shark.model.SharkProcess;
import org.joget.workflow.shark.model.SharkProcessHistory;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

public class WorkflowAssignmentDao extends AbstractSpringDao {
    
    private WorkflowProcessLinkDao workflowProcessLinkDao;
    public final static String ENTITY_NAME="SharkAssignment";
    public final static String PROCESS_ENTITY_NAME="SharkProcess";
    public final static String ACTIVITY_ENTITY_NAME="SharkActivity";
    public final static String PROCESS_HISTORY_ENTITY_NAME="SharkProcessHistory";
    public final static String ACTIVITY_HISTORY_ENTITY_NAME="SharkActivityHistory";
    
    public Collection<WorkflowProcess> getProcesses(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        String customField = ", (select link.originProcessId from WorkflowProcessLink as link where e.processId = link.processId) as recordId";
        
        //required to disable lazy loading 
        String condition = "join e.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("id")) {
                sort = "e.processId";
            } else if (sort.equals("Started") || sort.equals("startedTime")) {
                sort = "e.started";
            } else if (sort.equals("Created") || sort.equals("createdTime")) {
                sort = "e.created";
            } else if (sort.equals("name")) {
                sort = "e.processName";
            } else if (sort.equals("requesterId")) {
                sort = "e.ResourceRequesterId";
            }
        }
        
        condition += " where 1=1";
        
        if (packageId != null || processDefId != null || processId != null || processName != null || version != null || recordId != null || username != null || state != null) {
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and e.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (version != null && !version.isEmpty()) {
                condition += " and e.processDefId like ?";
                params.add("%#"+version+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and e.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and e.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (processName != null && !processName.isEmpty()) {
                condition += " and e.processName like ?";
                params.add("%" + processName + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.resourceRequesterId = ?";
                params.add(username);
            }
            
            if (recordId != null && !recordId.isEmpty()) {
                condition += " and ((select link.originProcessId from WorkflowProcessLink as link where e.processId = link.processId) like ? or e.processId like ?)";
                params.add("%" + recordId + "%");
                params.add("%" + recordId + "%");
            }
            
            if (state != null && !state.isEmpty()) {
                if (state.contains(".")) {
                    condition += " and s.name = ?";
                    params.add(state);
                } else {
                    condition += " and s.name like ?";
                    params.add(state + ".%");
                }
            }
        }
        Collection shProcess = find(PROCESS_ENTITY_NAME, customField, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        if ((shProcess == null || shProcess.isEmpty()) && state != null && state.startsWith("close")) {
            return getProcessHistories(packageId, processDefId, processId, processName, version, recordId, username, sort, desc, start, rows);
        } else {
            return transformToWorkflowProcess(shProcess);
        }
    }
    
    public long getProcessesSize(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String state) {
        //required to disable lazy loading 
        String condition = "";
        String where = "";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || processName != null || version != null || recordId != null || username != null || state != null) {
            
            if (recordId != null && !recordId.isEmpty()) {
                where += " where ((select link.originProcessId from WorkflowProcessLink as link where e.processId = link.processId) like ? or e.processId like ?)";
                params.add("%" + recordId + "%");
                params.add("%" + recordId + "%");
            } else {
                where += " where 1 = 1";
            }
            
            if (packageId != null && !packageId.isEmpty()) {
                where += " and e.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (version != null && !version.isEmpty()) {
                where += " and e.processDefId like ?";
                params.add("%#"+version+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                where += " and e.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                where += " and e.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (processName != null && !processName.isEmpty()) {
                where += " and e.processName like ?";
                params.add("%" + processName + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                where += " and e.resourceRequesterId = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " join e.state s";
                if (state.contains(".")) {
                    where += " and s.name = ?";
                    params.add(state);
                } else {
                    where += " and s.name like ?";
                    params.add(state + ".%");
                }
            }
        }
        long result = count(PROCESS_ENTITY_NAME, condition+where, params.toArray(new String[0]));
        if (result == 0 && state != null && state.startsWith("close")) {
            return getProcessHistoriesSize(packageId, processDefId, processId, processName, version, recordId, username);
        } else {
            return result;
        }
    }
    
    /**
     * Returns all the id of running process instances by requester
     * @return 
     */
    public Collection<String> getProcessIdsByRequester(String packageId, String processDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = "join fetch e.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || username != null || state != null) {
            condition += " where 1 = 1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and e.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and e.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.resourceRequesterId = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                if (state.contains(".")) {
                    condition += " and s.name = ?";
                    params.add(state);
                } else {
                    condition += " and s.name like ?";
                    params.add(state + ".%");
                }
            }
        }
        Collection<SharkProcess> shProcess = find(PROCESS_ENTITY_NAME, condition, params.toArray(new String[0]), null, null, null, null);
        Set<String> ids = new HashSet<String>();
        
        if (shProcess != null && !shProcess.isEmpty()) {
            for (SharkProcess s : shProcess) {
                ids.add(s.getProcessId());
            }
        }
        
        return ids;
    };
    
    public String getRunningActivityIdByRecordId(String id, String processDefId, String activityDefId, String username) {
        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        condition += " where e.isValid is true";
        
        Collection<WorkflowProcessLink> links = getWorkflowProcessLinkDao().getLinks(id);
        Set<String> ids = new HashSet<String>();
        
        if (links != null && !links.isEmpty()) {
            condition += " and (";
            int i = 0;
            for (WorkflowProcessLink l : links) {
                if (i % 1000 == 0) {
                    if (i > 0) {
                        condition += " OR ";
                    }
                    condition += "p.processId in (";
                }

                condition += " ?,";
                params.add(l.getProcessId());

                if (i % 1000 == 999 || i == links.size() -1) {
                    condition = condition.substring(0, condition.length() - 1) + ")";
                }
                i++;
            }
            condition += ")";
        }
        
        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " and p.processDefId like ?";
            processDefId = ignoreVersion(processDefId);
            params.add(processDefId);
        }
        
        if (activityDefId != null && !activityDefId.isEmpty()) {
            condition += " and a.activityDefId = ?";
            params.add(activityDefId);
        }
        
        if (username != null && !username.isEmpty()) {
            condition += getUserFilter(params, username);
        }
        
        Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), null, null, null, null);
        if (shAss != null && !shAss.isEmpty()) {
            for (SharkAssignment s : shAss) {
                return s.getActivity().getActivityId();
            }
        }
        return null;
    }
    
    public WorkflowAssignment getAssignmentByRecordId(String id, String processDefId, String activityDefId, String username) {
        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        condition += " where e.isValid is true";
        
        Collection<WorkflowProcessLink> links = getWorkflowProcessLinkDao().getLinks(id);
        Set<String> ids = new HashSet<String>();
        
        if (links != null && !links.isEmpty()) {
            condition += " and (";
            int i = 0;
            for (WorkflowProcessLink l : links) {
                if (i % 1000 == 0) {
                    if (i > 0) {
                        condition += " OR ";
                    }
                    condition += "p.processId in (";
                }

                condition += " ?,";
                params.add(l.getProcessId());

                if (i % 1000 == 999 || i == links.size() -1) {
                    condition = condition.substring(0, condition.length() - 1) + ")";
                }
                i++;
            }
            condition += ")";
        }
        
        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " and p.processDefId like ?";
            processDefId = ignoreVersion(processDefId);
            params.add(processDefId);
        }
        
        if (activityDefId != null && !activityDefId.isEmpty()) {
            condition += " and a.activityDefId = ?";
            params.add(activityDefId);
        }
        
        if (username != null && !username.isEmpty()) {
            condition += getUserFilter(params, username);
        }
        
        Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), null, null, null, null);
        if (shAss != null && !shAss.isEmpty()) {
            Collection<WorkflowAssignment> ass = transformToWorkflowAssignment(shAss);
            for (WorkflowAssignment s : ass) {
                return s;
            }
        }
        return null;
    }
    
    public Set<String> getAssignmentProcessIds(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        
        //required to disable lazy loading 
        String condition = "join e.process p join e.activity a join a.state s";
        Collection<String> params = new ArrayList<>();
        
        condition += " where e.isValid is true";
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += getUserFilter(params, username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        Collection<String> shAss = findAssignmentProcessIds(condition, params.toArray(new String[0]), null, null, null, null);
        Set<String> ass = new HashSet<>(shAss);        
        return ass;
    }
    
    /**
     * Retrieve Collection of assignment process IDs without populating other object attributes.
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    protected Collection<String> findAssignmentProcessIds(final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        Session session = findSession();
        String query = "SELECT p.processId FROM " + ENTITY_NAME + " e " + newCondition;

        if (sort != null && !sort.equals("")) {
            String filteredSort = filterSpace(sort);
            query += " ORDER BY " + filteredSort;

            if (desc) {
                query += " DESC";
            }
        }
        Query q = session.createQuery(query);

        int s = (start == null) ? 0 : start;
        q.setFirstResult(s);

        if (rows != null && rows > 0) {
            q.setMaxResults(rows);
        }

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }    
        
    public Collection<WorkflowAssignment> getAssignmentsByProcessIds(Collection<String> processIds, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        //sorting
        if (sort != null && !sort.isEmpty()) {
            if ("processDefId".equals(sort)) {
                sort = "p.processDefId";
            } else if ("processVersion".equals(sort)) {
                sort = "p.processVersion";
            } else if ("processId".equals(sort)) {
                sort = "p.processId";
            } else if ("processName".equals(sort)) {
                sort = "p.processName";
            } else if ("processRequesterId".equals(sort)) {
                sort = "p.processRequesterId";
            } else if ("activityName".equals(sort)) {
                sort = "a.activityName";
            } else if ("activityDefId".equals(sort)) {
                sort = "a.activityDefId";
            } else if ("dateCreated".equals(sort)) {
                sort = "a.activated";
            } else if ("assigneeName".equals(sort)) {
                sort = "e.assigneeName";
            }
        }

        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (processIds != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (processIds != null && !processIds.isEmpty()) {
                condition += " and p.processId in (?";
                for (int i = 1; i < processIds.size(); i++) {
                    condition += ",?";
                }
                condition += ")";
                params.addAll(processIds);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += getUserFilter(params, username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformToWorkflowAssignment(shAss);
    }
    
    public Collection<WorkflowAssignment> getAssignments(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        //sorting
        if (sort != null && !sort.isEmpty()) {
            if ("processDefId".equals(sort)) {
                sort = "p.processDefId";
            } else if ("processVersion".equals(sort)) {
                sort = "p.processVersion";
            } else if ("processId".equals(sort)) {
                sort = "p.processId";
            } else if ("processName".equals(sort)) {
                sort = "p.processName";
            } else if ("processRequesterId".equals(sort)) {
                sort = "p.processRequesterId";
            } else if ("activityName".equals(sort)) {
                sort = "a.activityName";
            } else if ("activityDefId".equals(sort)) {
                sort = "a.activityDefId";
            } else if ("dateCreated".equals(sort)) {
                sort = "a.activated";
            } else if ("assigneeName".equals(sort)) {
                sort = "e.assigneeName";
            }
        }

        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        condition += " where e.isValid is true";
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += getUserFilter(params, username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformToWorkflowAssignment(shAss);
    }
    
    public Collection<WorkflowActivity> getClosedActivities(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {

        //sorting
        if (sort != null && !sort.isEmpty()) {
            if ("processDefId".equals(sort)) {
                sort = "processDefId";
            } else if ("processId".equals(sort)) {
                sort = "processId";
            } else if ("resourceId".equals(sort)) {
                sort = "resourceId";
            } else if ("activityName".equals(sort)) {
                sort = "activityName";
            } else if ("activityDefId".equals(sort)) {
                sort = "activityDefId";
            } else if ("dateCreated".equals(sort)) {
                sort = "activated";
            }
        }

        //required to disable lazy loading 
        String condition = " join e.state s";
        Collection<String> params = new ArrayList<String>();
        condition += " where 1=1";
      
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {           
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and PDefName like ?";                
                params.add(packageId+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and PDefName like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and ProcessId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and ActivityDefinitionId = ?";
                params.add(activityDefId);
            }
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add("closed."+state);
            }
            if (username != null && !username.isEmpty()) {
                condition += " and ResourceId like ?";
                params.add(username);
            }
        }
                                        
        Collection<SharkActivity> shAss = find(ACTIVITY_ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformToWorkflowActivity(shAss);
    }
    
    public Collection<WorkflowActivity> getArchivedActivities(String packageId, String processDefId, String processId, String actDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {

        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("id")) {
                sort = "e.activityId";
            } else if (sort.equals("Started") || sort.equals("startedTime") || sort.equals("dateStarted")) {
                sort = "e.accepted";
            } else if (sort.equals("Created") || sort.equals("createdTime") || sort.equals("dateCreated")) {
                sort = "e.activated";
            } else if (sort.equals("name")) {
                sort = "e.activityName";
            } else if (sort.equals("performer")) {
                sort = "e.performer";
            }
        }
        
        //required to disable lazy loading 
        String condition = " join e.process p where 1=1";
        Collection<String> params = new ArrayList<String>();

        if (packageId != null || processDefId != null || processId != null || actDefId != null || username != null || state != null) {              
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";          
                params.add(packageId+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
               condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (actDefId != null && !actDefId.isEmpty()) {
                condition += " and e.activityDefId = ?";
                params.add(actDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and e.processId = ?";
                params.add(processId);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and e.state like ?";
                params.add("closed."+state);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.performer = ?";
                params.add(username);
            }
        }
                              
        Collection<SharkActivityHistory> shAct = find(ACTIVITY_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformHistoryToWorkflowActivity(shAct);
    }

    public int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = "join e.process p join e.activity a join a.state s"; 
        Collection<String> params = new ArrayList<String>();
        
        condition += " where e.isValid is true";
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += getUserFilter(params, username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        Long total = count(ENTITY_NAME, condition, params.toArray(new String[0]));
        
        if (total != null) {
            return total.intValue();
        }
        return 0;
    }
    
    public int getClosedActivitiesSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = " join e.state s";
        Collection<String> params = new ArrayList<String>();
        condition += " where 1=1";
      
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {           
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and PDefName like ?";                
                params.add(packageId+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and PDefName like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and ProcessId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and ActivityDefinitionId = ?";
                params.add(activityDefId);
            }
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add("closed."+state);
            }
            if (username != null && !username.isEmpty()) {
                condition += " and ResourceId like ?";
                params.add(username);
            }
        }
        Long total = count(ACTIVITY_ENTITY_NAME, condition, params.toArray(new String[0]));
        
        if (total != null) {
            return total.intValue();
        }
        return 0;
    }
    
    public int getArchivedActivitiesSize(String packageId, String processDefId, String processId, String actDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = " join e.process p where 1=1";
        Collection<String> params = new ArrayList<String>();

        if (packageId != null || processDefId != null || processId != null || actDefId != null || username != null || state != null) {              
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";          
                params.add(packageId+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
               condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (actDefId != null && !actDefId.isEmpty()) {
                condition += " and e.activityDefId = ?";
                params.add(actDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and e.processId = ?";
                params.add(processId);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and e.state like ?";
                params.add("closed."+state);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.performer = ?";
                params.add(username);
            }
        }
        Long total = count(ACTIVITY_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]));
        
        if (total != null) {
            return total.intValue();
        }
        return 0;
    }
    
    /**
     * Only stuck tools having "open.running" status during startup
     * @return 
     */
    public Collection<Object[]> getStuckTools() {
        Session session = findSession();
        String query = "SELECT e.processDefId, e.processId, e.activityId FROM SharkActivity e WHERE e.state.name = ?1";
        Query q = session.createQuery(query);

        q.setParameter(1, "open.running");
        return q.list();
    }
    
    public Collection<String> getPackageDefIds(String packageId) {
        Session session = findSession();
        String query = "SELECT distinct e.processDefId FROM SharkProcess e WHERE e.processDefId like ?1";
        Query q = session.createQuery(query);
        
        q.setParameter(1, packageId + "#%");

        return q.list();
    }
    
    protected Collection find(final String entityName, final String customField, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        Session session = findSession();
        String query = "SELECT e.processId, e.processDefId, e.processName, e.resourceRequesterId, s.name";
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        query += customField + " FROM " + entityName + " e " + newCondition;

        if (sort != null && !sort.equals("")) {
            String filteredSort = filterSpace(sort);
            query += " ORDER BY " + filteredSort;

            if (desc) {
                query += " DESC";
            }
        }
        Query q = session.createQuery(query);

        int s = (start == null) ? 0 : start;
        q.setFirstResult(s);

        if (rows != null && rows > 0) {
            q.setMaxResults(rows);
        }

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }
    
    protected Collection<WorkflowAssignment> transformToWorkflowAssignment(Collection<SharkAssignment> shAss) {
        Collection<WorkflowAssignment> ass = new ArrayList<WorkflowAssignment>();
        
        Map<String, Long> limits = new HashMap<String, Long>();
        WorkflowManager wm = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        
        //transform to WorkflowAssignment
        if (shAss != null && !shAss.isEmpty()) {
            for (SharkAssignment s : shAss) {
                WorkflowAssignment a = new WorkflowAssignment();
                a.setActivityId(s.getActivity().getActivityId());
                a.setActivityDefId(s.getActivity().getActivityDefId());
                a.setActivityName(s.getActivity().getActivityName());
                a.setProcessDefId(s.getProcess().getProcessDefId());
                a.setProcessId(s.getProcess().getProcessId());
                a.setProcessName(s.getProcess().getProcessName());
                a.setProcessRequesterId((s.getProcess().getProcessRequesterId() != null)?s.getProcess().getProcessRequesterId():s.getProcess().getResourceRequesterId());
                a.setProcessVersion(s.getProcess().getProcessVersion());
                a.setAssigneeName(s.getAssigneeName());
                a.setAccepted(s.getIsAccepted());
                
                Date dateCreated = new Date(s.getActivity().getActivated());
                a.setDateCreated(dateCreated);
                
                //get duedate & service level value
                Long limit = limits.get(a.getProcessDefId() + "|" + a.getActivityDefId());
                if (limit == null) {
                    limit = wm.getActivityLimit(a.getProcessDefId(), a.getActivityDefId());
                    limits.put(a.getProcessDefId() + "|" + a.getActivityDefId(), limit);
                }
                
                if (limit > 0) {
                    Date dueDate = wm.getDueDateProceedByPlugin(a.getProcessId(), a.getActivityDefId(), limit, dateCreated, dateCreated);
                    a.setDueDate(dueDate);
                    
                    a.setServiceLevelValue(wm.getServiceLevelValue(dateCreated, null, dueDate));
                } else {
                    a.setServiceLevelValue(-1);
                }
                
                //subflow
                if (s.getProcess().getProcessRequesterId() != null && !s.getProcess().getProcessRequesterId().isEmpty()) {
                    a.setSubflow(true);
                    if (workflowProcessLinkDao.getWorkflowProcessLink(a.getProcessId()) == null) {
                        workflowProcessLinkDao.addWorkflowProcessLink(s.getProcess().getProcessRequesterId(), a.getProcessId());
                    }
                }
                
                if (WorkflowUtil.containsHashVariable(a.getProcessName())) {
                    a.setProcessName(WorkflowUtil.processVariable(a.getProcessName(), null, a));
                }
                
                ass.add(a);
            }
        }
        return ass;
    }
    
    protected Collection<WorkflowProcess> transformToWorkflowProcess(Collection shProcess) {
        Collection<WorkflowProcess> processes = new ArrayList<WorkflowProcess>();
        WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        
        if (shProcess != null && !shProcess.isEmpty()) {
            for (Object o : shProcess) {
                Object[] temp = (Object[]) o;
                String processId = (String) temp[0];
                String processDefId = (String) temp[1];
                String processName = (String) temp[2];
                String resourceRequesterId = (String) temp[3];
                String state = (String) temp[4];
                String recordId = (String) temp[5];
                if (recordId == null) {
                    recordId = processId;
                }
                
                WorkflowProcess workflowProcess = new WorkflowProcess();
                workflowProcess.setRecordId(recordId);
                workflowProcess.setId(processDefId);
                workflowProcess.setInstanceId(processId);
                workflowProcess.setName(processName);
                workflowProcess.setState(state);
                workflowProcess.setPackageId(WorkflowUtil.getProcessDefPackageId(processDefId));
                workflowProcess.setVersion(WorkflowUtil.getProcessDefVersion(processDefId));
                workflowProcess.setRequesterId(resourceRequesterId);
                
                WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
                workflowProcess.setStartedTime(trackWflowProcess.getStartedTime());
                workflowProcess.setFinishTime(trackWflowProcess.getFinishTime());
                workflowProcess.setDue(trackWflowProcess.getDue());
                processes.add(workflowProcess);
            }
        }
        
        return processes;
    }

    protected Collection<WorkflowActivity> transformToWorkflowActivity(Collection<SharkActivity> shActivity) {
        Collection<WorkflowActivity> activity = new ArrayList<>();
        if (shActivity != null && !shActivity.isEmpty()) {
            for (SharkActivity shAct : shActivity) {
                WorkflowActivity wfActivity = new WorkflowActivity();         
                wfActivity.setId(shAct.getActivityId());
                wfActivity.setName(shAct.getActivityName());
                wfActivity.setActivityDefId(shAct.getActivityDefId());
                wfActivity.setState(shAct.getState().getName());              
                wfActivity.setProcessId(shAct.getProcessId());
                wfActivity.setProcessDefId(shAct.getProcessDefId());
                wfActivity.setPerformer(shAct.getResourceId());               
                activity.add(wfActivity);
            }
        }
        return activity;
    }
    
    private String ignoreVersion(String processDefId) {
        processDefId = processDefId.replaceAll("#[0-9]+#", "#%#");
        return processDefId;
    }

    public WorkflowProcessLinkDao getWorkflowProcessLinkDao() {
        return workflowProcessLinkDao;
    }

    public void setWorkflowProcessLinkDao(WorkflowProcessLinkDao workflowProcessLinkDao) {
        this.workflowProcessLinkDao = workflowProcessLinkDao;
    }
    
    protected String getUserFilter(Collection<String> params, String username) {
        String condition = "";
                
        Map<String, Collection<String>> replacementUsers = WorkflowUtil.getReplacementUsers(username);

        if (replacementUsers == null || replacementUsers.isEmpty()) {
            condition += " and e.assigneeName = ?";
            params.add(username);
        } else {
            condition += " and (e.assigneeName = ?";
            params.add(username);
            
            for (String u : replacementUsers.keySet()) {
                Collection<String> processes = replacementUsers.get(u);
                condition += " or (e.assigneeName = ?";
                params.add(u);
                
                if (processes != null && !processes.isEmpty()) {
                    condition += " and (";
                    String processCond = "";
                    for (String p : processes) {
                        String[] temp = p.split(":");
                        if (temp.length > 0 && !temp[0].isEmpty()) {
                            String processDefId = temp[0] + "#%";
                            if (temp.length > 1 && !temp[1].isEmpty()) {
                                processDefId += "#" + temp[1];
                            }
                            if (!processCond.isEmpty()) {
                                processCond += " or ";
                            }
                            
                            processCond += "p.processDefId like ?";
                            params.add(processDefId);                            
                        }
                    }
                    condition += processCond + ")";
                }
                
                condition += ")";
            }

            condition += ")";
        }
        
        return condition;  
    }
    
    /**
     * Migrate the process instance from one version to another version
     * @param packageId 
     * @return  
     */
    public Collection<String> getMigrateProcessInstances(String packageId){
        Session session = findSession();
        String query = "SELECT e.id" + " FROM MigrateProcess e WHERE e.name like ?1 and e.state in (1000000, 1000002, 1000004) ORDER BY e.oid";

        Query q = session.createQuery(query);
        q.setFirstResult(0);

        q.setParameter(1, packageId + "#%");

        return q.list();
    }
    
    public MigrateActivity getActivityProcessDefId(String actId) {
        return (MigrateActivity) find("MigrateActivity", actId);
    }
    
    public Set<String> getUsedVersion(String packageId) {
        Session session = findSession();
        String query = "SELECT e.processDefId" + " FROM MigrateActivity e WHERE e.processDefId like ?1";

        Query q = session.createQuery(query);
        q.setFirstResult(0);

        q.setParameter(1, packageId + "#%");

        return new HashSet<String>(q.list());
    }
    
    /**
     * Migrate the process instance from one version to another version
     * @param processId 
     * @param newVersion 
     * @return  
     */
    public boolean migrateProcessInstance(String processId, String newVersion){
        try {
            //get running process for migration
            ArrayList<MigrateProcess> processes = (ArrayList<MigrateProcess>) find("MigrateProcess", "where e.id = ? and e.state in (1000000, 1000002, 1000004)", new String[]{processId}, null, null, null, null);

            if (!processes.isEmpty()) {
                MigrateProcess process = processes.get(0);

                String processDefId = process.getName().replaceAll("#[0-9]+#", "#" + newVersion + "#");

                if (process.getName().equalsIgnoreCase(processDefId)) {
                    return true;
                }

                ArrayList<MigrateProcessDefinition> definitions = (ArrayList<MigrateProcessDefinition>) find("MigrateProcessDefinition", "where e.name = ?", new String[]{processDefId}, null, null, null, null);

                if (!definitions.isEmpty()) {
                    MigrateProcessDefinition definition = definitions.get(0);

                    //get running activity for migration
                    Collection<MigrateActivity> acts = find("MigrateActivity", "where e.processId = ? and e.state in (1000001, 1000003, 1000005)", new String[]{processId}, null, null, null, null);

                    if (!acts.isEmpty()) {
                        boolean hasMissingActivity = false;

                        WorkflowManager wm = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");

                        Map<String, WorkflowActivity> currentActivities = new HashMap<String, WorkflowActivity>();
                        Collection<WorkflowActivity> currentActivityList = wm.getProcessActivityDefinitionList(process.getName());
                        for (WorkflowActivity act : currentActivityList) {
                            currentActivities.put(act.getId(), act);
                        }
                        Map<String, WorkflowActivity> activities = new HashMap<String, WorkflowActivity>();
                        Collection<WorkflowActivity> activityList = wm.getProcessActivityDefinitionList(processDefId);
                        for (WorkflowActivity act : activityList) {
                            activities.put(act.getId(), act);
                        }

                        for (MigrateActivity a : acts) {
                            WorkflowActivity cAct = currentActivities.get(a.getDefId());
                            WorkflowActivity nAct = activities.get(a.getDefId());

                            if (cAct != null && nAct != null && cAct.getType().equals(nAct.getType())) {
                                if (a.getPerformer() != null && nAct.getType().equals(WorkflowActivity.TYPE_SUBFLOW)) {
                                    if (!migrateProcessInstance(a.getPerformer(), newVersion)) {
                                        hasMissingActivity = true;
                                        break;
                                    }
                                }
                            } else {
                                hasMissingActivity = true;
                                break;
                            }
                        }

                        if (!hasMissingActivity) {
                            process.setName(processDefId);
                            process.setProcessDefinition(definition.getOid());
                            saveOrUpdate("MigrateProcess", process);

                            //get running activity for migration
                            Collection<MigrateActivity> allActs = find("MigrateActivity", "where e.processId = ?", new String[]{processId}, null, null, null, null);
                            for (MigrateActivity a : allActs) {
                                if (activities.containsKey(a.getDefId())) {
                                    a.setProcessDefId(processDefId);
                                    saveOrUpdate("MigrateActivity", a);
                                }
                            }

                            //get running activity for migration
                            Collection<MigrateAssignment> assignments = find("MigrateAssignment", "where e.processId = ?", new String[]{processId}, null, null, null, null);
                            if (!assignments.isEmpty()) {
                                for (MigrateAssignment a : assignments) {
                                    a.setProcessDefId(processDefId);
                                    saveOrUpdate("MigrateAssignment", a);
                                }
                            }
                            
                            wm.internalUpdateMigratedProcess(process, acts);
                            
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(WorkflowAssignmentDao.class.getName(), e, processId);
        }
        
        return false;
    }
    
    public void saveProcessHistory(SharkProcessHistory history) {
        super.saveOrUpdate(PROCESS_HISTORY_ENTITY_NAME, history);
    }
    
    public void saveActivityHistory(SharkActivityHistory history) {
        super.saveOrUpdate(ACTIVITY_HISTORY_ENTITY_NAME, history);
    }
    
    /**
     * Used to check is there any completed process in shark table
     */
    public boolean hasNonHistoryCompletedProcess() {
        //required to disable lazy loading 
        String condition = "join e.state s";
        Collection<String> params = new ArrayList<String>();
        
        condition += " where 1=1";
        condition += " and s.name like ?";
        params.add("closed.%");
        Collection shProcess = find(PROCESS_ENTITY_NAME, "", condition, params.toArray(new String[0]), null, null, 0, 1);
        
        return !shProcess.isEmpty();
    }
    
    public Collection<WorkflowProcess> getProcessHistories(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String sort, Boolean desc, Integer start, Integer rows) {
        //required to disable lazy loading 
        String condition = "join fetch e.link link";
        Collection<String> params = new ArrayList<String>();
        
        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("id")) {
                sort = "e.processId";
            } else if (sort.equals("Started") || sort.equals("startedTime") || sort.equals("dateStarted")) {
                sort = "e.started";
            } else if (sort.equals("Created") || sort.equals("createdTime") || sort.equals("dateCreated")) {
                sort = "e.created";
            } else if (sort.equals("name")) {
                sort = "e.processName";
            } else if (sort.equals("requesterId")) {
                sort = "e.resourceRequesterId";
            }
        }
        
        condition += " where 1=1";
        
        if (packageId != null || processDefId != null || processId != null || processName != null || version != null || recordId != null || username != null) {
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and e.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (version != null && !version.isEmpty()) {
                condition += " and e.processDefId like ?";
                params.add("%#"+version+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and e.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and e.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (processName != null && !processName.isEmpty()) {
                condition += " and e.processName like ?";
                params.add("%" + processName + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.resourceRequesterId = ?";
                params.add(username);
            }
            
            if (recordId != null && !recordId.isEmpty()) {
                condition += " and (link.originProcessId like ? or e.processId like ?)";
                params.add("%" + recordId + "%");
                params.add("%" + recordId + "%");
            }
        }
        Collection<SharkProcessHistory> shProcess = find(PROCESS_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        return transformHistoryToWorkflowProcess(shProcess);
    }
    
    public long getProcessHistoriesSize(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username) {
        String condition = "";
        String where = "";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || processName != null || version != null || recordId != null || username != null) {
            
            if (recordId != null && !recordId.isEmpty()) {
                condition += " join e.link link";
                where += " where (link.originProcessId like ? or e.processId like ?)";
                params.add("%" + recordId + "%");
                params.add("%" + recordId + "%");
            } else {
                where += " where 1 = 1";
            }
            
            if (packageId != null && !packageId.isEmpty()) {
                where += " and e.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (version != null && !version.isEmpty()) {
                where += " and e.processDefId like ?";
                params.add("%#"+version+"#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                where += " and e.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                where += " and e.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (processName != null && !processName.isEmpty()) {
                where += " and e.processName like ?";
                params.add("%" + processName + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                where += " and e.resourceRequesterId = ?";
                params.add(username);
            }
        }
        return count(PROCESS_HISTORY_ENTITY_NAME, condition+where, params.toArray(new String[0]));
    }
    
    public Collection<WorkflowActivity> getActivityHistories(String processId, String actDefId, String username, String sort, Boolean desc, Integer start, Integer rows) {
        String condition = " join fetch e.process p";
        Collection<String> params = new ArrayList<String>();
        
        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("id")) {
                sort = "e.activityId";
            } else if (sort.equals("Started") || sort.equals("startedTime") || sort.equals("dateStarted")) {
                sort = "e.accepted";
            } else if (sort.equals("Created") || sort.equals("createdTime") || sort.equals("dateCreated")) {
                sort = "e.activated";
            } else if (sort.equals("name")) {
                sort = "e.activityName";
            } else if (sort.equals("performer")) {
                sort = "e.performer";
            }
        }
        
        condition += " where 1=1";
        
        if (processId != null || actDefId != null || username != null) {
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (actDefId != null && !actDefId.isEmpty()) {
                condition += " and e.activityDefId like ?";
                params.add("%" + actDefId + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.performer = ?";
                params.add(username);
            }
        }
        Collection<SharkActivityHistory> shAct = find(ACTIVITY_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        return transformHistoryToWorkflowActivity(shAct);
    }
    
    public long getActivityHistoriesSize(String processId, String actDefId, String username) {
        String condition = " join e.process p where 1=1";
        Collection<String> params = new ArrayList<String>();
        
        if (processId != null || actDefId != null || username != null) {
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId like ?";
                params.add("%" + processId + "%");
            }
            
            if (actDefId != null && !actDefId.isEmpty()) {
                condition += " and e.activityDefId like ?";
                params.add("%" + actDefId + "%");
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.performer = ?";
                params.add(username);
            }
        }
        return count(ACTIVITY_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]));
    }
    
    public WorkflowProcess getProcessHistoryById(String processId) {
        SharkProcessHistory shProcess = (SharkProcessHistory) find(PROCESS_HISTORY_ENTITY_NAME, processId);
        if (shProcess != null) {
            return transformHistoryToWorkflowProcess(shProcess);
        }
        return null;
    }
    
    public WorkflowActivity getActivityHistoryById(String activityId) {
        SharkActivityHistory shAct = (SharkActivityHistory) find(ACTIVITY_HISTORY_ENTITY_NAME, activityId);
        if (shAct != null) {
            return transformHistoryToWorkflowActivity(shAct);
        }
        return null;
    }
    
    public void deleteProcessHistory(String processId) {
        String condition = " join e.process p";
        Collection<String> params = new ArrayList<String>();
        condition += " where p.processId = ?";
        params.add(processId);
        Collection<SharkActivityHistory> shAct = find(ACTIVITY_HISTORY_ENTITY_NAME, condition, params.toArray(new String[0]), null, null, null, null);
        for (SharkActivityHistory a : shAct) {
            delete(ACTIVITY_HISTORY_ENTITY_NAME, a);
        }
        
        SharkProcessHistory shProcess = (SharkProcessHistory) find(PROCESS_HISTORY_ENTITY_NAME, processId);
        delete(PROCESS_HISTORY_ENTITY_NAME, shProcess);
    }
    
    protected Collection<WorkflowProcess> transformHistoryToWorkflowProcess(Collection<SharkProcessHistory> shProcess) {
        Collection<WorkflowProcess> processes = new ArrayList<WorkflowProcess>();
        
        if (shProcess != null && !shProcess.isEmpty()) {
            for (SharkProcessHistory o : shProcess) {
                processes.add(transformHistoryToWorkflowProcess(o));
            }
        }
        
        return processes;
    }
    
    protected WorkflowProcess transformHistoryToWorkflowProcess(SharkProcessHistory shProcess) {
        WorkflowProcess workflowProcess = new WorkflowProcess();
        workflowProcess.setRecordId((shProcess.getLink() != null)? shProcess.getLink().getOriginProcessId():shProcess.getProcessId());
        workflowProcess.setId(shProcess.getProcessDefId());
        workflowProcess.setInstanceId(shProcess.getProcessId());
        workflowProcess.setName(shProcess.getProcessName());
        workflowProcess.setState(shProcess.getState());
        workflowProcess.setPackageId(WorkflowUtil.getProcessDefPackageId(shProcess.getProcessDefId()));
        workflowProcess.setVersion(WorkflowUtil.getProcessDefVersion(shProcess.getProcessDefId()));
        workflowProcess.setRequesterId(shProcess.getResourceRequesterId());
        workflowProcess.setLimit(shProcess.getLimit());
        workflowProcess.setDue(shProcess.getDue());
        
        Calendar startedCal = Calendar.getInstance();
        startedCal.setTimeInMillis(shProcess.getStarted());
        workflowProcess.setStartedTime(startedCal.getTime());
        
        Calendar createdCal = Calendar.getInstance();
        createdCal.setTimeInMillis(shProcess.getCreated());
        workflowProcess.setCreatedTime(createdCal.getTime());
        
        Calendar completionCal = Calendar.getInstance();
        completionCal.setTimeInMillis(shProcess.getLastStateTime());
        workflowProcess.setFinishTime(completionCal.getTime());
        
        workflowProcess.setLimit(shProcess.getLimit());
        workflowProcess.setDue(shProcess.getDue());
        
        if (workflowProcess.getDue() != null && workflowProcess.getFinishTime().after(workflowProcess.getDue())) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(workflowProcess.getDue());
            long delayInMilliseconds = completionCal.getTimeInMillis() - dueCal.getTimeInMillis();
            long delayInSeconds = (long) delayInMilliseconds / 1000;
            workflowProcess.setDelayInSeconds(delayInSeconds);
            workflowProcess.setDelay(convertTimeInSecondsToString(delayInSeconds));
        }
        
        //time taken for completion from date started
        long timeTakenInMilliSeconds = (workflowProcess.getFinishTime() != null && workflowProcess.getStartedTime() != null) ? workflowProcess.getFinishTime().getTime() - workflowProcess.getStartedTime().getTime() : 0;
        long timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

        workflowProcess.setTimeConsumingFromDateStartedInSeconds(timeTakenInSeconds);
        workflowProcess.setTimeConsumingFromDateStarted(convertTimeInSecondsToString(timeTakenInSeconds));

        //time taken for completion from date created
        timeTakenInMilliSeconds = (workflowProcess.getFinishTime() != null && workflowProcess.getCreatedTime() != null) ? workflowProcess.getFinishTime().getTime() - workflowProcess.getCreatedTime().getTime() : 0;
        timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

        workflowProcess.setTimeConsumingFromDateCreatedInSeconds(timeTakenInSeconds);
        workflowProcess.setTimeConsumingFromDateCreated(convertTimeInSecondsToString(timeTakenInSeconds));
        
        //workflow variable
        Collection<WorkflowVariable> workflowVariable = new ArrayList<WorkflowVariable>();
        if (shProcess.getVariables() != null && !shProcess.getVariables().isEmpty()) {
            try {
                JSONObject obj = new JSONObject(shProcess.getVariables());
                Iterator keys = obj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    WorkflowVariable v = new WorkflowVariable();
                    v.setId(key);
                    v.setName(key);
                    v.setVal(obj.getString(key));
                    workflowVariable.add(v);
                }
            } catch (Exception e) {
                LogUtil.error(WorkflowAssignmentDao.class.getName(), e, "");
            }
        }
        workflowProcess.setVariableList(workflowVariable);
        
        return workflowProcess;
    }
    
    protected Collection<WorkflowActivity> transformHistoryToWorkflowActivity(Collection<SharkActivityHistory> shAct) {
        Collection<WorkflowActivity> activities = new ArrayList<WorkflowActivity>();
        
        if (shAct != null && !shAct.isEmpty()) {
            for (SharkActivityHistory o : shAct) {
                activities.add(transformHistoryToWorkflowActivity(o));
            }
        }
        
        return activities;
    }
    
    protected WorkflowActivity transformHistoryToWorkflowActivity(SharkActivityHistory shAct) {
        WorkflowActivity workflowActivity = new WorkflowActivity();
        
        workflowActivity.setId(shAct.getActivityId());
        workflowActivity.setActivityDefId(shAct.getActivityDefId());
        workflowActivity.setName(shAct.getActivityName());
        workflowActivity.setState(shAct.getState());
        workflowActivity.setProcessDefId(shAct.getProcess().getProcessDefId());
        workflowActivity.setProcessId(shAct.getProcess().getProcessId());
        workflowActivity.setProcessName(shAct.getProcess().getProcessName());
        workflowActivity.setProcessVersion(shAct.getProcess().getVersion());
        workflowActivity.setProcessStatus(shAct.getProcess().getState());
        workflowActivity.setType(shAct.getType());
        workflowActivity.setNameOfAcceptedUser(shAct.getPerformer());
        workflowActivity.setPerformer(shAct.getParticipantId());
        if (shAct.getAssignmentUsers() != null) {
            workflowActivity.setAssignmentUsers(shAct.getAssignmentUsers().split(";"));
        } else {
            workflowActivity.setAssignmentUsers(new String[0]);
        }
        
        Calendar startedCal = Calendar.getInstance();
        startedCal.setTimeInMillis(shAct.getAccepted());
        workflowActivity.setStartedTime(startedCal.getTime());
        
        Calendar createdCal = Calendar.getInstance();
        createdCal.setTimeInMillis(shAct.getActivated());
        workflowActivity.setCreatedTime(createdCal.getTime());
        
        Calendar completionCal = Calendar.getInstance();
        completionCal.setTimeInMillis(shAct.getLastStateTime());
        workflowActivity.setFinishTime(completionCal.getTime());
        
        workflowActivity.setLimit(shAct.getLimit());
        workflowActivity.setDue(shAct.getDue());
        
        if (workflowActivity.getDue() != null && workflowActivity.getFinishTime().after(workflowActivity.getDue())) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(workflowActivity.getDue());
            long delayInMilliseconds = completionCal.getTimeInMillis() - dueCal.getTimeInMillis();
            long delayInSeconds = (long) delayInMilliseconds / 1000;
            workflowActivity.setDelayInSeconds(delayInSeconds);
            workflowActivity.setDelay(convertTimeInSecondsToString(delayInSeconds));
        }
        
        //time taken for completion from date started
        long timeTakenInMilliSeconds = (workflowActivity.getFinishTime() != null && workflowActivity.getStartedTime() != null) ? workflowActivity.getFinishTime().getTime() - workflowActivity.getStartedTime().getTime() : 0;
        long timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

        workflowActivity.setTimeConsumingFromDateStartedInSeconds(timeTakenInSeconds);
        workflowActivity.setTimeConsumingFromDateStarted(convertTimeInSecondsToString(timeTakenInSeconds));

        //time taken for completion from date created
        timeTakenInMilliSeconds = (workflowActivity.getFinishTime() != null && workflowActivity.getCreatedTime() != null) ? workflowActivity.getFinishTime().getTime() - workflowActivity.getCreatedTime().getTime() : 0;
        timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

        workflowActivity.setTimeConsumingFromDateCreatedInSeconds(timeTakenInSeconds);
        workflowActivity.setTimeConsumingFromDateCreated(convertTimeInSecondsToString(timeTakenInSeconds));
        
        //workflow variable
        Collection<WorkflowVariable> workflowVariable = new ArrayList<WorkflowVariable>();
        if (shAct.getVariables() != null && !shAct.getVariables().isEmpty()) {
            try {
                JSONObject obj = new JSONObject(shAct.getVariables());
                Iterator keys = obj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    WorkflowVariable v = new WorkflowVariable();
                    v.setId(key);
                    v.setName(key);
                    v.setVal(obj.getString(key));
                    workflowVariable.add(v);
                }
            } catch (Exception e) {
                LogUtil.error(WorkflowAssignmentDao.class.getName(), e, "");
            }
        }
        workflowActivity.setVariableList(workflowVariable);
        
        return workflowActivity;
    }
    
    protected String convertTimeInSecondsToString(long timeInSeconds) {
        long timeInMinutes = (long) timeInSeconds / 60;
        long timeInHours = (long) timeInMinutes / 60;
        long timeInDays = (long) timeInHours / 24;
                    
        String temp = "";
        
        if (timeInSeconds < 60) {
            temp = timeInSeconds + " second(s)";
        } else if (timeInSeconds >= 60 && timeInMinutes < 60) {
            temp = timeInMinutes + " minutes(s) " + (timeInSeconds % 60) + " second(s)";
        } else if (timeInMinutes >= 60 && timeInHours < 24) {
            temp = timeInHours + " hour(s) " + (timeInMinutes % 60) + " minute(s) " + (timeInSeconds % 60) + " second(s)";
        } else if (timeInHours >= 24) {
            temp = timeInDays + " day(s) " + (timeInHours % 24) + " hour(s) " + (timeInMinutes % 60) + " minutes(s) " + (timeInSeconds % 60) + " second(s)";
        }
        
        return temp;
    }
}
