package org.kecak.apps.incomingEmail.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.kecak.apps.incomingEmail.model.IncomingEmail;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Transactional
public class IncomingEmailDao extends AbstractSpringDao {
    public static final String ENTITY_NAME = "IncomingEmail";

    public IncomingEmail load(final String id) {
        return (IncomingEmail) find(ENTITY_NAME, id);
    }
    public Collection<IncomingEmail> find(final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        return find(ENTITY_NAME, condition, params, sort, desc, start, rows);
    }

    public long count(final String condition, final Object[] params) {
        return count(ENTITY_NAME, condition, params);
    }
    public void saveOrUpdate(IncomingEmail incomingEmail) {
        saveOrUpdate(ENTITY_NAME, incomingEmail);
    }

    public void delete(IncomingEmail incomingEmail) {
        delete(ENTITY_NAME, incomingEmail);
    }
}
