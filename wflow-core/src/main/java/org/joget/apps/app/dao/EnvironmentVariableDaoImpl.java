package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.commons.util.LogUtil;

public class EnvironmentVariableDaoImpl extends AbstractAppVersionedObjectDao<EnvironmentVariable> implements EnvironmentVariableDao {

    public static final String ENTITY_NAME = "EnvironmentVariable";

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public Collection<EnvironmentVariable> getEnvironmentVariableList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getEnvironmentVariableListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            EnvironmentVariable obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<EnvironmentVariable> list = appDef.getEnvironmentVariableList();
                for (EnvironmentVariable object : list) {
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
