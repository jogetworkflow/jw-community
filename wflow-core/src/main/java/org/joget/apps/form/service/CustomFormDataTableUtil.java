package org.joget.apps.form.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.dao.FormDataDaoImpl;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

public class CustomFormDataTableUtil {
    public final static String TYPE = "formdata_table";
    
    public static String getTableDefinition(AppDefinition appDef, String id) {
        BuilderDefinition def = getDao().loadById(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + id, appDef);
        if (def != null) {
            return def.getJson();
        }
        return null;
    }
    
    public static void createTable(AppDefinition appDef, String id, String[] columns) {
        if (getTableDefinition(appDef, id) == null) {
            try {
                JSONObject defObj = new JSONObject();
                JSONObject columnsObj = new JSONObject();
                for (String c : columns) {
                    columnsObj.put(c, new JSONObject());
                }
                defObj.put("columns", columnsObj);
                
                BuilderDefinition def = new BuilderDefinition();
                def.setAppDefinition(appDef);
                def.setId(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + id);
                def.setName(id);
                def.setType(TYPE);
                def.setJson(defObj.toString());
                
                getDao().add(def);
                
                // initialize db table by making a dummy load
                String dummyKey = "xyz123";
                FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
                formDataDao.loadWithoutTransaction(id, id, dummyKey);
            } catch (Exception e) {
                LogUtil.error(CustomFormDataTableUtil.class.getName(), e, "fail to create table " + id);
            }
        }
    }
    
    public static Set<String> getColumns(AppDefinition appDef, String id) {
        Set<String> columns = null;
        String def = getTableDefinition(appDef, id);
        if (def != null && !def.isEmpty()) {
            columns = new HashSet<String>();
            try {
                JSONObject defObj = new JSONObject(def);
                JSONObject columnsObj = defObj.getJSONObject("columns");
                Iterator keys = columnsObj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    columns.add(key);
                }
            } catch (Exception e) {
                LogUtil.error(CustomFormDataTableUtil.class.getName(), e, "fail to retrieve columns for table " + id);
            }
        }
        
        return columns;
    }
    
    public static Collection<String> getTables(AppDefinition appDef) {
        Collection<String> tableNames = new ArrayList<String>();
        Collection<BuilderDefinition> tables = getDao().getBuilderDefinitionList(TYPE, "", appDef, "id", false, null, null);
        if (tables != null) {
            for (BuilderDefinition b : tables) {
                tableNames.add(b.getName());
            }
        }
        return tableNames;
    }
    
    protected static BuilderDefinitionDao getDao() {
        return (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
    }
}
