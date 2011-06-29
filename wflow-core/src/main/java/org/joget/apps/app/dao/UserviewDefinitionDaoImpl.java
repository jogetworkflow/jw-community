package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.commons.util.LogUtil;

public class UserviewDefinitionDaoImpl extends AbstractAppVersionedObjectDao<UserviewDefinition> implements UserviewDefinitionDao {

    public static final String ENTITY_NAME = "UserviewDefinition";

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<UserviewDefinition> getUserviewDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getUserviewDefinitionListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or description like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean add(UserviewDefinition object) {
        object.setDateCreated(new Date());
        object.setDateModified(new Date());
        return super.add(object);
    }

    @Override
    public boolean update(UserviewDefinition object) {
        object.setDateModified(new Date());
        return super.update(object);
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            UserviewDefinition obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<UserviewDefinition> list = appDef.getUserviewDefinitionList();
                for (UserviewDefinition object : list) {
                    if (obj.getId().equals(object.getId())) {
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                result = true;
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
}
