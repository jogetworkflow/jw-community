package org.joget.apps.app.dao;

import java.util.Collection;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.AppDefinition;

public interface AppVersionedObjectDao<T extends AbstractAppVersionedObject> {

    String getEntityName();

    T loadById(String id, AppDefinition appDefinition);

    boolean add(T object);

    boolean update(T object);

    boolean delete(String id, AppDefinition appDefinition);

    Collection<T> getList(AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    Long getCount(AppDefinition appDefinition);

    Collection<T> find(String condition, Object[] params, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows);

    Long count(String condition, Object[] params, AppDefinition appDefinition);
    
}
