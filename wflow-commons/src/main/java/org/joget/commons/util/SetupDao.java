package org.joget.commons.util;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.spring.model.Setting;
import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.StaleStateException;

public class SetupDao extends AbstractSpringDao {

    public static final String ENTITY_NAME = "Setting";

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    public void delete(Object obj) {
        super.delete(ENTITY_NAME, obj);
    }

    public void delete(String property) {
        Collection<Setting> result = find("WHERE property = ?", new String[]{ property }, null, null, null, null);
        Setting setting = (result.isEmpty()) ? null : result.iterator().next();
        if (setting != null) {
            super.delete(ENTITY_NAME, setting);
        }
    }

    public Object find(String id) {
        return super.find(ENTITY_NAME, id);
    }

    public Collection<Setting> find(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        Session session = findSession();

        Collection<Setting> settings =(Collection<Setting>) super.find(ENTITY_NAME, condition, params, sort, desc, start, rows);
        return settings;
    }

    /**
     * Reads one setting's value by property, for callers that must not be served a cached answer.
     *
     * <p>Returns a SCALAR rather than a {@code Setting}, and that is the load-bearing detail: an
     * entity query is resolved through the session's persistence context, so a row already managed
     * in this session is handed back with its OLD field values and the freshly read columns are
     * discarded. That is Hibernate's first-level cache, it is always on, and no cache setting
     * affects it. A scalar projection never touches the identity map.</p>
     *
     * <p>It also builds its own query instead of calling
     * {@link #find(String, Object[], String, Boolean, Integer, Integer)}, which routes through
     * {@code AbstractSpringDao.setCacheable} and therefore opts every other query in this DAO into
     * the Hibernate query cache. Nothing here opts in, so the read is not query-cached; the
     * second-level entity and query caches are otherwise left to invalidate themselves as normal.</p>
     *
     * <p><b>Returns EVERY matching row, and that is deliberate.</b> {@code wf_setup.property} is not
     * unique, and {@code SetupManager.updateSetting} resolves the existing row through the debounced
     * setup cache — so two nodes creating a given setting for the first time can both miss and insert
     * their own UUID row. An earlier version applied {@code setMaxResults(1)} with no {@code ORDER
     * BY}, which picks an arbitrary one of the duplicates and can therefore keep returning a row that
     * has stopped being updated. Handing back all of them lets the caller resolve the conflict with
     * knowledge this DAO does not have — see {@code LongTermCache.getLastClearTimeFresh}, which takes
     * the MAXIMUM because a CACHE_LAST_CLEAR_ value only ever moves forward.</p>
     *
     * <p>Callers must throttle: this is an indexed lookup, but it is a database round trip every
     * call.</p>
     *
     * @param property the setting property key
     * @return every value stored under that property; empty if absent
     */
    public Collection<String> findFreshValues(String property) {
        Session session = findSession();
        org.hibernate.query.Query q = session.createQuery(
                "SELECT e.value FROM " + ENTITY_NAME + " e WHERE e.property = ?1");
        q.setParameter(1, property);
        return q.list();
    }

    public Serializable save(Object obj) {
        try {
            Setting setting = (Setting)obj;
            if (setting.getId() == null) {
                setting.setId(UuidGenerator.getInstance().getUuid());
            }
            return super.save(ENTITY_NAME, obj);
        } finally {
            super.findSession().evict(obj);
        }
    }

    @Transactional(dontRollbackOn={OptimisticLockException.class, StaleStateException.class})
    public void saveOrUpdate(Object obj) {
        try {
            Setting setting = (Setting)obj;
            if (setting.getId() == null) {
                setting.setId(UuidGenerator.getInstance().getUuid());
            }
            super.saveOrUpdate(ENTITY_NAME, obj);
            
            super.findSession().evict(obj);
        } catch (OptimisticLockException | StaleStateException e) {
            //ignore exception when trying to update a setting which deleted by another cluster node 
            if (!(e.getMessage().contains("Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1")
                    || e.getMessage().contains("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect"))) {
                throw e;
            } else {
                
            }
        }
    }
}
