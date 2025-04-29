package org.joget.workflow.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.cache.Cache;
import org.apache.commons.collections.map.LRUMap;
import org.joget.commons.cache.InMemoryCacheManager;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.model.WorkflowProcessLink;

public class WorkflowProcessLinkDao extends AbstractSpringDao {

    public final static String ENTITY_NAME="WorkflowProcessLink";
    public final static String HISTORY_ENTITY_NAME="WorkflowProcessLinkHistory";
    
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

    
    static Cache workflowProcessLinkCache;
    
    /**
     * Get the workflow process link cache.
     * @return 
     */
    public static Cache getWorkflowProcessLinkCache() {  
        if (workflowProcessLinkCache == null) {
            String regionName = "workflow-process-link";
            long expiry = 60*60*1000L; // 1 hour
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            workflowProcessLinkCache = cacheManager.getCache(regionName, expiry);
        }
        return workflowProcessLinkCache;
    }
    
    public void addWorkflowProcessLink(WorkflowProcessLink wfProcessLink){
        // save in cache
        Cache cache = getWorkflowProcessLinkCache();
        if (cache != null) {
            String originId = wfProcessLink.getOriginProcessId();
            String processId = wfProcessLink.getProcessId();
            Collection<String> processIds = (Collection<String>)cache.get(originId);
            if (processIds == null) {
                processIds = new HashSet<>();
            }
            processIds.add(processId);
            cache.put(originId, processIds);
        }
        
        // persist
        saveOrUpdate(ENTITY_NAME, wfProcessLink);
    }
    
    public void addWorkflowProcessLinkHistory(WorkflowProcessLink wfProcessLink){
        if (getWorkflowProcessLinkHistory(wfProcessLink.getProcessId()) == null) {
            WorkflowProcessLink history = new WorkflowProcessLink();
            history.setProcessId(wfProcessLink.getProcessId());
            history.setOriginProcessId(wfProcessLink.getOriginProcessId());
            history.setParentProcessId(wfProcessLink.getParentProcessId());

            saveOrUpdate(HISTORY_ENTITY_NAME, history);

            try {
                wfProcessLink = internalGetWorkflowProcessLink(wfProcessLink.getProcessId());
                if (wfProcessLink != null) {
                    delete(wfProcessLink);
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public WorkflowProcessLink getWorkflowProcessLink(String processId){
        WorkflowProcessLink processLink = internalGetWorkflowProcessLink(processId);
        
        if (processLink == null) {
            processLink = getWorkflowProcessLinkHistory(processId);
        }
        return processLink;
    }
    
    public WorkflowProcessLink internalGetWorkflowProcessLink(String processId){
        return (WorkflowProcessLink) find(ENTITY_NAME, processId);
    }

    public void delete(WorkflowProcessLink wfProcessLink) {
        // remove from cache
        Cache cache = getWorkflowProcessLinkCache();
        if (cache != null) {
            String originId = wfProcessLink.getOriginProcessId();
            String processId = wfProcessLink.getProcessId();
            Collection<String> processIds = (Collection<String>)cache.get(originId);
            if (processIds != null) {
                processIds.remove(processId);
                cache.put(originId, processIds);
            }
        }
        
        // delete
        super.delete(ENTITY_NAME, wfProcessLink);
    }
    
    // least recently used (LRU) cache to hold original IDs
    static Map<String, Map<String, Collection<String>>> originalIdCache = Collections.synchronizedMap(new LRUMap(200));

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
                // lookup from cache
                Cache cache = getWorkflowProcessLinkCache();
                if (cache != null) {
                    Collection<String> pIds = (Collection<String>)cache.get(id);
                    if (pIds != null) {
                        processIds.put(id, pIds);
                        continue;
                    }
                }

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
                            pIds = new HashSet<>();
                        }
                        pIds.add(pid);
                        existIds.add(pid);

                        processIds.put(orgId, pIds);

                        // add to cache
                        if (cache != null) {
                            cache.put(orgId, pIds);
                        }
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
            // lookup from LRU cache
            String cacheKey = StringUtil.md5Base16Utf8(DynamicDataSourceManager.getCurrentProfile() + "::" + ids.toString()); // hash to minimize memory usage
            Map<String, Collection<String>> result = originalIdCache.get(cacheKey);
            if (result != null) {
                return result;
            }
            
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
            
            // save into cache
            originalIdCache.put(cacheKey, originalIds);                       
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
        
        //the histories link should have higher order to render consistent result in process status as DX7
        Collection<WorkflowProcessLink> tempHistory = super.find(HISTORY_ENTITY_NAME, conditions, new String[]{processId}, null, null, null, null);
        if (tempHistory != null && !tempHistory.isEmpty()) {
            links.addAll(tempHistory);
        }
        
        Collection<WorkflowProcessLink> temp = super.find(ENTITY_NAME, conditions, new String[]{processId}, null, null, null, null);
        if (temp != null && !temp.isEmpty()) {
            links.addAll(temp);
        }
        
        return links;
    }
    
    public WorkflowProcessLink getWorkflowProcessLinkHistory(String processId){
        return (WorkflowProcessLink) find(HISTORY_ENTITY_NAME, processId);
    }

    public void deleteHistory(WorkflowProcessLink wfProcessLink) {
        super.delete(HISTORY_ENTITY_NAME, wfProcessLink);
    }
    
    public Collection<WorkflowProcessLink> getHistoryLinks(String processId) {
        Collection<WorkflowProcessLink> links = new ArrayList<WorkflowProcessLink>();
        WorkflowProcessLink processLink = getWorkflowProcessLinkHistory(processId);
        String conditions = "where e.originProcessId = ?";
        if (processLink != null) {
            processId = processLink.getOriginProcessId();
        }
        WorkflowProcessLink origin = new WorkflowProcessLink();
        origin.setProcessId(processId);
        origin.setParentProcessId(processId);
        origin.setOriginProcessId(processId);
        links.add(origin);
        
        Collection<WorkflowProcessLink> temp = super.find(HISTORY_ENTITY_NAME, conditions, new String[]{processId}, null, null, null, null);
        if (temp != null && !temp.isEmpty()) {
            links.addAll(temp);
        }
        return links;
    }
    
    public void migrateCompletedProcessLinks() {
        Set<String> ids = new HashSet<String>();
        String conditions = " where e.processId not in (select p.processId from SharkProcess as p join p.state s where s.name like ?)";
        Collection<WorkflowProcessLink> temp = super.find(ENTITY_NAME, conditions, new String[]{"open.%"}, null, null, null, null);
        for (WorkflowProcessLink l : temp) {
            addWorkflowProcessLinkHistory(l);
            ids.add(l.getProcessId());
        }
        LogUtil.info(WorkflowProcessLinkDao.class.getName(), "Migrated " + ids.size() + " records in wf_process_link tables.");
        LogUtil.info(WorkflowProcessLinkDao.class.getName(), ids.toString());
    }
}
