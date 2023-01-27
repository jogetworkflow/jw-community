package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AppResourceDaoImpl extends AbstractAppVersionedObjectDao<AppResource> implements AppResourceDao {

    public static final String ENTITY_NAME = "AppResource";

    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    @Autowired
    AppService appService;
    
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
                appDefinitionDao.updateDateModified(appDef);
                
                // update app def
                appDefinitionDao.saveOrUpdate(appDef.getAppId(), appDef.getVersion(), false);
                
                if (!AppDevUtil.isGitDisabled()) {
                    // save and commit app definition
                    appDef = appService.loadAppDefinition(appDef.getAppId(), appDef.getVersion().toString());
                    String appDefFilename = "appDefinition.xml";
                    String xml = AppDevUtil.getAppDefinitionXml(appDef);
                    String commitMessage = "Update app definition " + appDef.getId();
                    AppDevUtil.fileSave(appDef, appDefFilename, xml, commitMessage);

                    AppDevUtil.dirSyncAppResources(appDef);
                }
                
                result = true;
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    @Override
    public boolean update(AppResource object) {
        object.getAppDefinition().setDateModified(new Date());
        boolean result = super.update(object);
        
        if (!AppDevUtil.isGitDisabled()) {
            // save and commit app definition
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            AppDevUtil.dirSyncAppResources(appDef);
        }
        
        return result;
    }
    
    @Override
    public boolean add(AppResource object) {
        object.getAppDefinition().setDateModified(new Date());
        boolean result = super.add(object);
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            // save and commit app definition
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);

            AppDevUtil.dirSyncAppResources(appDef);
        }
        return result;
    }    
    
}
