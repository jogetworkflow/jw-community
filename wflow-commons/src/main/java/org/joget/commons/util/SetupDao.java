package org.joget.commons.util;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.spring.model.Setting;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import org.hibernate.StaleStateException;

public class SetupDao extends AbstractSpringDao {

    public static final String ENTITY_NAME = "Setting";

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    public void delete(Object obj) {
        super.delete(ENTITY_NAME, obj);
    }

    public Object find(String id) {
        return super.find(ENTITY_NAME, id);
    }

    public Collection<Setting> find(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        return (Collection<Setting>) super.find(ENTITY_NAME, condition, params, sort, desc, start, rows);
    }

    public Serializable save(Object obj) {
        return super.save(ENTITY_NAME, obj);
    }

    @Transactional(dontRollbackOn={OptimisticLockException.class, StaleStateException.class})
    public void saveOrUpdate(Object obj) {
        try {
            super.saveOrUpdate(ENTITY_NAME, obj);
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
