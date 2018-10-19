package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AppResourceDaoImpl extends AbstractAppVersionedObjectDao<AppResource> implements AppResourceDao {

    public static final String ENTITY_NAME = "AppResource";

    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }
    
    public Collection<AppResource> getResources(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ?)";
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getResourcesCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ?)";
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }
    
    @Override
    public boolean delete(String filename, AppDefinition appDef) {
        boolean result = false;
        try {
            AppResource obj = loadById(filename, appDef);

            // detach from app
            if (obj != null) {
                Collection<AppResource> list = appDef.getResourceList();
                for (AppResource object : list) {
                    if (obj.getId().equals(object.getId())) {
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);
                AppResourceUtil.deleteFile(appDef.getAppId(), appDef.getVersion().toString(), filename);

                // delete obj
                super.delete(getEntityName(), obj);
                
                // update app def
                appDefinitionDao.saveOrUpdate(appDef.getAppId(), appDef.getVersion(), false);
                
                result = true;
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
}
