package org.joget.workflow.shark.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.shark.model.CustomActivityPersistenceObject;
import org.joget.workflow.shark.model.CustomDeadlinePersistenceObject;

public class DeadlineDao extends AbstractSpringDao {
    public final static String ENTITY_NAME="DeadlinePersistenceObject";
    
    public Map<CustomActivityPersistenceObject, Collection<CustomDeadlinePersistenceObject>> getDeadlines(String processId, Long timeLimitBoundary) {
        String conditions = "where e.activity.processId = ? and e.activity.state.name like ?";
        
        Collection params = new ArrayList();
        params.add(processId);
        params.add("open.%");
        
        if (timeLimitBoundary != null) {
            conditions += " and e.timeLimit <= ?";
            params.add(timeLimitBoundary);
        }
        
        Collection<CustomDeadlinePersistenceObject> deadlines = find(ENTITY_NAME, conditions, params.toArray(), null, null, null, null);
        
        Map<CustomActivityPersistenceObject, Collection<CustomDeadlinePersistenceObject>> deadlineMaps = new HashMap<CustomActivityPersistenceObject, Collection<CustomDeadlinePersistenceObject>>();
        if (deadlines != null && !deadlines.isEmpty()) {
            for (CustomDeadlinePersistenceObject d : deadlines) {
                Collection<CustomDeadlinePersistenceObject> temp = deadlineMaps.get(d.getActivity());
                if (temp == null) {
                    temp = new ArrayList<>();
                    deadlineMaps.put(d.getActivity(), temp);
                }
                temp.add(d);
            }
        }
        return deadlineMaps;
    }
    
    public Collection<String> getProcessIdsWithDeadlines(Long timeLimitBoundary) {
        Session session = findSession();
        String query = "SELECT distinct e.activity.processId FROM " + ENTITY_NAME + " e where e.activity.state.name like ?";
        
        if (timeLimitBoundary != null) {
            query += "and e.timeLimit <= ?";
        }

        Query q = session.createQuery(query);
        
        q.setParameter(0, "open.%");
        
        if (timeLimitBoundary != null) {
            q.setParameter(1, timeLimitBoundary);
        }

        return q.list();
    }
}
