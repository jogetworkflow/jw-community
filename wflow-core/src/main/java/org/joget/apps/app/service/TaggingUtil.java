package org.joget.apps.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.Date;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.commons.util.LogUtil;

public class TaggingUtil {
    protected final static String ID = "INTERNAL_TAGGING";
    protected final static String DEFAULT = "{\"labels\":{\"t01\":{\"color\":\"red\"},\"t02\":{\"color\":\"pink\"},\"t03\":{\"color\":\"orange\"},\"t04\":{\"color\":\"yellow\"},\"t05\":{\"color\":\"green\"},\"t06\":{\"color\":\"lime\"},\"t07\":{\"color\":\"blue\"},\"t08\":{\"color\":\"sky\"},\"t09\":{\"color\":\"purple\"},\"t10\":{\"color\":\"black\"}}, \"datas\":{\"form\":{},\"list\":{},\"userview\":{}}}";
    
    public static String getDefinition(AppDefinition appDef) {
        BuilderDefinition def = getDao().loadById(ID, appDef);
        if (def != null) {
            return def.getJson();
        }
        return DEFAULT;
    }
    
    public static String updateDefinitionWithPatch(AppDefinition appDef, String patch) {
        boolean isAdd = false;
        BuilderDefinition def = getDao().loadById(ID, appDef);
        if (def == null) {
            def = new BuilderDefinition();
            def.setAppDefinition(appDef);
            def.setType("internal");
            def.setName("Tagging");
            def.setId(ID);
            def.setDateCreated(new Date());
            isAdd = true;
        }
        
        if (def.getJson() == null || def.getJson().isEmpty()) {
            def.setJson(DEFAULT);
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode ori = objectMapper.readTree(def.getJson());
            JsonPatch jpatch = objectMapper.readValue(patch, JsonPatch.class);
            JsonNode patched = jpatch.apply(ori);
            def.setJson(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(patched));
        } catch (Exception e) {
            LogUtil.error(TaggingUtil.class.getName(), e, "");
        }
        
        def.setDateModified(new Date());
        if (isAdd) {
            getDao().add(def);
        } else {
            getDao().update(def);
        }
        
        return def.getJson();
    }
    
    protected static BuilderDefinitionDao getDao() {
        return (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
    }
}
