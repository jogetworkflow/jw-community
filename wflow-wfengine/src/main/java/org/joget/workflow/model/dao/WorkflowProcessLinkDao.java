package org.joget.workflow.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowProcessLink;

public class WorkflowProcessLinkDao extends AbstractSpringDao {

    public final static String ENTITY_NAME="WorkflowProcessLink";
    
    public void addWorkflowProcessLink(String parentProcessId, String processInstanceId){
        WorkflowProcessLink wfProcessLink = new WorkflowProcessLink();
        WorkflowProcessLink parentWfProcessLink = getWorkflowProcessLink(parentProcessId);
        wfProcessLink.setParentProcessId(parentProcessId);

        if (parentWfProcessLink != null) {
            wfProcessLink.setOriginProcessId(parentWfProcessLink.getOriginProcessId());
        } else {
            wfProcessLink.setOriginProcessId(parentProcessId);
        }

        wfProcessLink.setProcessId(processInstanceId);

        addWorkflowProcessLink(wfProcessLink);
    }

    public void addWorkflowProcessLink(WorkflowProcessLink wfProcessLink){
        saveOrUpdate(ENTITY_NAME, wfProcessLink);
        
        super.findSession().evict(wfProcessLink);
    }

    public WorkflowProcessLink getWorkflowProcessLink(String processId){
        return (WorkflowProcessLink) find(ENTITY_NAME, processId);
    }

    public void delete(WorkflowProcessLink wfProcessLink) {
        super.delete(ENTITY_NAME, wfProcessLink);
    }
    
    /**
     * From record ids, return the process ids for each record ids
     * @param ids
     * @return 
     */
    public Map<String, Collection<String>> getProcessIdsFromRecordIds(Collection<String> ids) {
        Map<String, Collection<String>> processIds = new HashMap<String, Collection<String>>();
        Collection<String> existIds = new ArrayList<String>();
        
        if (!ids.isEmpty()) {
            String conditions = "";
            Collection<WorkflowProcessLink> links = null;
            Collection<String> values = null;
            
            int i = 0;
            for (String id : ids) {
                if (i % 1000 == 0) {
                    values = new ArrayList<String>();
                    conditions = "where e.originProcessId in (";
                }
                
                conditions += "?,";
                values.add(id);
                
                if (i % 1000 == 999 || i == ids.size() -1) {
                    conditions = conditions.substring(0, conditions.length() - 1) + ")";
                    links = super.find(ENTITY_NAME, conditions, values.toArray(new String[0]), null, null, null, null);
                    
                    for (WorkflowProcessLink link : links) {
                        String orgId = link.getOriginProcessId();
                        String pid = link.getProcessId();

                        Collection<String> pIds = processIds.get(orgId);
                        if (pIds == null) {
                            pIds = new ArrayList<String>();
                        }
                        pIds.add(pid);
                        existIds.add(pid);

                        processIds.put(orgId, pIds);
                    }
                }
                i++;
            }
            
            // for those does not has link
            for (String id : ids) {
                if (!existIds.contains(id)) {
                    Collection<String> pIds = processIds.get(id);
                    if (pIds == null) {
                        pIds = new ArrayList<String>();
                    }
                    pIds.add(id);
                    existIds.add(id);
                    processIds.put(id, pIds);
                }
            }
        }
        
        return processIds;
    }
    
    public Map<String, Collection<String>> getOriginalIds(Collection<String> ids) {
        Map<String, Collection<String>> originalIds = new HashMap<String, Collection<String>>();
        Collection<String> existIds = new ArrayList<String>();
        
        if (ids.size() > 0) {
            String conditions = "";
            Collection<WorkflowProcessLink> links = null;
            Collection<String> values = null;
            
            int i = 0;
            for (String id : ids) {
                if (i % 1000 == 0) {
                    values = new ArrayList<String>();
                    conditions = "where e.processId in (";
                }
                
                conditions += "?,";
                values.add(id);
                
                if (i % 1000 == 999 || i == ids.size() -1) {
                    conditions = conditions.substring(0, conditions.length() - 1) + ")";
                    links = super.find(ENTITY_NAME, conditions, values.toArray(new String[0]), null, null, null, null);
                    
                    for (WorkflowProcessLink link : links) {
                        String orgId = link.getOriginProcessId();
                        String pid = link.getProcessId();

                        Collection<String> pIds = originalIds.get(orgId);
                        if (pIds == null) {
                            pIds = new ArrayList<String>();
                        }
                        pIds.add(pid);
                        existIds.add(pid);

                        originalIds.put(orgId, pIds);
                    }
                }
                i++;
            }
            
            // for those does not has link
            for (String id : ids) {
                if (!existIds.contains(id)) {
                    Collection<String> pIds = originalIds.get(id);
                    if (pIds == null) {
                        pIds = new ArrayList<String>();
                    }
                    pIds.add(id);
                    existIds.add(id);
                    originalIds.put(id, pIds);
                }
            }
        }
        
        return originalIds;
    }
    
    public Collection<WorkflowProcessLink> getLinks(String processId) {
        Collection<WorkflowProcessLink> links = new ArrayList<WorkflowProcessLink>();
        WorkflowProcessLink processLink = getWorkflowProcessLink(processId);
        String conditions = "where e.originProcessId = ?";
        if (processLink != null) {
            processId = processLink.getOriginProcessId();
        }
        WorkflowProcessLink origin = new WorkflowProcessLink();
        origin.setProcessId(processId);
        origin.setParentProcessId(processId);
        origin.setOriginProcessId(processId);
        links.add(origin);
        
        Collection<WorkflowProcessLink> temp = super.find(ENTITY_NAME, conditions, new String[]{processId}, null, null, null, null);
        if (temp != null && !temp.isEmpty()) {
            links.addAll(temp);
        }
        return links;
    }
}
