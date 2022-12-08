package org.kecak.apps.scheduler.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.kecak.apps.scheduler.model.SchedulerDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Kecak Exclusive
 */
@Transactional
public class SchedulerDetailsDao extends AbstractSpringDao {

	public static final String ENTITY_NAME = "SchedulerDetails";
	
	public void saveOrUpdate(SchedulerDetails schedulerDetails) {
		super.saveOrUpdate(ENTITY_NAME, schedulerDetails);
	}

	public void delete(SchedulerDetails schedulerDetails) {
		super.delete(ENTITY_NAME, schedulerDetails);
	}
	
	public SchedulerDetails getSchedulerDetailsById(String id) {
		return (SchedulerDetails) super.find(ENTITY_NAME, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<SchedulerDetails> getSchedulerDetails(String condition, String[] param, String sort, Boolean desc, Integer start, Integer rows) {
        if (condition != null && condition.trim().length() != 0) {
            return (List<SchedulerDetails>) super.find(ENTITY_NAME, condition, param, sort, desc, start, rows);
        } else {
            return (List<SchedulerDetails>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
        }
    }

	public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

	@SuppressWarnings("unchecked")
	public SchedulerDetails getSchedulerDetailsByJob(String jobName, String groupJobName) {
		Collection<SchedulerDetails> results = super.find(ENTITY_NAME, "WHERE e.jobName = ? AND e.groupJobName = ?", new String[] { jobName, groupJobName }, null, null, null, null);
		
		SchedulerDetails result = null;
		if (results != null && results.size() != 0) {
			result = results.iterator().next();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public SchedulerDetails getSchedulerDetailsByTrigger(String triggerName, String groupTriggerName) {
		Collection<SchedulerDetails> results = super.find(ENTITY_NAME, "WHERE e.triggerName = ? AND e.groupTriggerName = ?", new String[] { triggerName, groupTriggerName }, null, null, null, null);
		
		SchedulerDetails result = null;
		if (results != null && results.size() != 0) {
			result = results.iterator().next();
		}
		return result;
	}
}
