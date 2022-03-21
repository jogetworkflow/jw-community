package org.joget.apps.form.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListBinder;
import static org.joget.apps.datalist.service.JsonUtil.parseBinderFromJsonObject;
import org.joget.apps.form.lib.Grid;
import org.joget.apps.form.lib.SelectBox;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormReferenceDataRetriever;
import org.joget.apps.form.model.GridInnerDataRetriever;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

public class FormERD {
    protected AppDefinition appDef;
    protected Map<String, Entity> entities = new HashMap<String, Entity>();
    protected Collection<Relation> relations = new ArrayList<Relation>();
    protected static final String[] FORM_ID_KEYS = new String[]{"formDefId", "formId", "formdefid", "formid", "form"};
    protected static final String[] DATALIST_ID_KEYS = new String[]{"listId", "datalistDefId", "datalistdefid", "listid", "list"};
    protected static final String[] FK_KEYS = new String[]{"foreignKey", "idColumn", "idField", "valueColumn", "valueField"};
    
    public FormERD(AppDefinition appDef) {
        this.appDef = appDef;
        init();
    }
    
    private void init() {
        if (appDef.getFormDefinitionList() != null && !appDef.getFormDefinitionList().isEmpty()) {
            //sort formDef by name length, shorter name will use as entity name
            List<FormDefinition> list = (List) appDef.getFormDefinitionList();
            Collections.sort(list, new Comparator<FormDefinition>() {

                @Override
                public int compare(FormDefinition o1, FormDefinition o2) {
                    return o1.getName().length() - o2.getName().length();
                }
            });
            
            //create all entities first
            for (FormDefinition formDef : list) {
                populateEntity(formDef);
            }
            
            for (FormDefinition formDef : list) {
                populateFields(formDef);
            }
            
            populateRelations();
        }
    }
    
    protected void populateEntity(FormDefinition formDef) {
        Entity entity = entities.get(formDef.getTableName());
        if (entity == null) {
            entity = new Entity(formDef.getTableName());
            entities.put(formDef.getTableName(), entity);
        }

        entity.setLabel(findCommonWords(entity.getLabel(), formDef.getName()));
        
        entity.addForm(formDef.getId(), formDef.getName());
    }
    
    protected void populateFields(FormDefinition formDef) {
        Entity entity = entities.get(formDef.getTableName());
        
        try {
            FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
            Element form = formService.createElementFromJson(formDef.getJson(), false);
            FormData formData = new FormData();
            formData.addFormResult(FormUtil.FORM_RESULT_LOAD_ALL_DATA, "true");
            
            populateField(form, formData, entity);
        } catch (Exception e) {
            LogUtil.error(FormUtil.class.getName(), e, "");
        }
    }
    
    protected void populateField(Element field, FormData formData, Entity entity) {
        if (field != null) {
            if (!(field instanceof FormContainer)) {
                String id = field.getPropertyString(FormUtil.PROPERTY_ID);
                if (id != null && !entity.hasField(id)) {
                    Field entityField = new Field(id, field.getClassName(), field.getI18nLabel());
                    
                    if (field instanceof FormReferenceDataRetriever) {
                        Relation r = getRelationFromDatalist(field, entity.getTableName(), id, true);
                        if (r != null) {
                            relations.add(r);
                        }
                    } else if (field instanceof SelectBox) {
                        FormBinder binder = (FormBinder) field.getOptionsBinder();
                        if (binder != null) {
                            Relation r = getRelationFromBinder(binder, entity.getTableName(), id, true);
                            if (r != null) {
                                relations.add(r);
                            }
                        }
                    }
                    
                    entity.addField(id, entityField);
                }
            } else if (field instanceof Grid || field instanceof GridInnerDataRetriever) {
                Relation r = null;
                FormBinder loadBinder = (FormBinder) field.getLoadBinder();
                if (loadBinder != null) {
                    r = getRelationFromBinder(loadBinder, entity.getTableName(), "id", false);
                }
                if (r == null) {
                    FormBinder storeBinder = (FormBinder) field.getStoreBinder();
                    if (storeBinder != null) {
                        r = getRelationFromBinder(storeBinder, entity.getTableName(), "id", false);
                    }
                }
                if (r != null) {
                    relations.add(r);
                }
            } else if (field instanceof AbstractSubForm) {
                String subformId = field.getPropertyString("formDefId");
                String pdid = field.getPropertyString(AbstractSubForm.PROPERTY_PARENT_SUBFORM_ID);
                String spId = field.getPropertyString(AbstractSubForm.PROPERTY_SUBFORM_PARENT_ID);
                
                if (!spId.isEmpty()) {
                    relations.add(new Relation("", entity.getTableName(), "id", subformId, "", spId));
                } else {
                    relations.add(new Relation(subformId, "", pdid, "", entity.getTableName(), "id"));
                }
            }
            
            if (!(field instanceof AbstractSubForm)) {
                Collection<Element> childs = field.getChildren(formData);
                if (!childs.isEmpty()) {
                    for (Element c : childs) {
                        populateField(c, formData, entity);
                    }
                }
            }
        }
    }
    
    protected Relation getRelationFromBinder(FormBinder binder, String entity, String field, boolean hasMany) {
        String formId = null;
        String fk = null;
        
        for (String k : FORM_ID_KEYS) {
            if (binder.getProperties().containsKey(k)) {
                formId = binder.getPropertyString(k);
                break;
            }
        }
        
        if (formId != null && !formId.isEmpty()) {
            for (String k : FK_KEYS) {
                if (binder.getProperties().containsKey(k)) {
                    fk = binder.getPropertyString(k);
                    break;
                }
            }
            
            if (hasMany && (fk == null || fk.isEmpty())) {
                fk = "id";
            }
            
            if (fk != null && !fk.isEmpty()) {
                if (hasMany) {
                    return new Relation(formId, "", fk, "", entity, field);
                } else {
                    return new Relation("", entity, field, formId, "", fk);
                }
            }
        }
        
        //TODO: get relation from jdbc binder/beanshell? but how to extract formId/tablename & foreign key from query/code?
        
        return null;
    }
    
    protected Relation getRelationFromDatalist(Element element, String entity, String field, boolean hasMany) {
        String listId = null;
        
        for (String k : DATALIST_ID_KEYS) {
            if (element.getProperties().containsKey(k)) {
                listId = element.getPropertyString(k);
                break;
            }
        }
        
        if (listId != null && !listId.isEmpty()) {
            if (appDef.getDatalistDefinitionList() != null) {
                DatalistDefinition listDef = null;
                for (DatalistDefinition l : appDef.getDatalistDefinitionList()) {
                    if (l.getId().equals(listId)) {
                        listDef = l;
                        break;
                    }
                }
                
                if (listDef != null) {
                    String json = listDef.getJson();
                    // strip enclosing brackets
                    json = json.trim();
                    if (json.startsWith("(")) {
                        json = json.substring(1);
                    }
                    if (json.endsWith(")")) {
                        json = json.substring(0, json.length() - 1);
                    }
                    try {
                        JSONObject obj = new JSONObject(json);
                        DataListBinder binder = parseBinderFromJsonObject(obj);
                        if (binder != null) {
                            String formId = null;
                            String fk = null;

                            for (String k : FORM_ID_KEYS) {
                                if (binder.getProperties().containsKey(k)) {
                                    formId = binder.getPropertyString(k);
                                    break;
                                }
                            }
                            
                            if (formId != null && !formId.isEmpty()) {
                                for (String k : FK_KEYS) {
                                    if (element.getProperties().containsKey(k)) {
                                        fk = element.getPropertyString(k);
                                        break;
                                    }
                                }
                                
                                if (hasMany && (fk == null || fk.isEmpty())) {
                                    fk = "id";
                                }

                                if (fk != null && !fk.isEmpty()) {
                                    if (hasMany) {
                                        return new Relation(formId, "", fk, "", entity, field);
                                    } else {
                                        return new Relation("", entity, field, formId, "", fk);
                                    }
                                }
                            }
                            
                            //TODO: get relation from jdbc binder/beanshell? but how to extract formId/tablename & foreign key from query/code?
                        }
                    } catch (Exception e) {
                        LogUtil.error(FormERD.class.getName(), e, "");
                    }
                }
            }
        }
        
        return null;
    }
    
    protected void populateRelations() {
        if (!relations.isEmpty()) {
            for (Relation r : relations) {
                Entity e = null;
                if (!r.getParentEntity().isEmpty()) {
                    e = entities.get(r.getParentEntity());
                } else if (!r.getParentFormId().isEmpty()) {
                    for (Entity en : entities.values()) {
                        if (en.hasForm(r.getParentFormId())) {
                            e = en;
                            r.setParentEntity(e.getTableName());
                            break;
                        }
                    }
                }
                
                if (e != null) {
                    Entity hasMany = null;
                    if (!r.getEntity().isEmpty()) {
                        hasMany = entities.get(r.getEntity());
                    } else if (!r.getEntityFormId().isEmpty()) {
                        for (Entity en : entities.values()) {
                            if (en.hasForm(r.getEntityFormId())) {
                                hasMany = en;
                                r.setEntity(hasMany.getTableName());
                                break;
                            }
                        }
                    }
                    
                    if (hasMany != null) {
                        hasMany.addOwnBy(new Relation("", "", r.getEntityFieldId(), "", r.getParentEntity(), r.getFieldId()));
                        e.addHasMany(r);
                    }
                }
            }
        }
    }
    
    protected String findCommonWords(String input1, String input2) {
        if (input1 == null || input1.isEmpty()) {
            return input2;
        }
        String words1[] = input1.replaceAll("[^a-zA-Z0-9 ]", " ").trim().split("\\s+");
        String words2[] = input2.replaceAll("[^a-zA-Z0-9 ]", " ").trim().split("\\s+");
        List<String>list1 = new ArrayList<>(Arrays.asList(words1));
        List<String>list2 = Arrays.asList(words2);
        list1.retainAll(list2);
        
        if (list1.isEmpty()) {
            return input1;
        } else {
            return StringUtils.join(list1, " ");
        }
    }
    
    public String getJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("entities", entities);
            
            return obj.toString(4);
        } catch (Exception e) {
            LogUtil.error(FormERD.class.getName(), e, "");
        }
        
        return null;
    }
    
    public class Entity extends HashMap<String, Object> {
        
        public Entity(String tableName) {
            super();
            this.put("tableName", tableName);
            this.put("forms", new HashMap<String, String>());
            this.put("fields", new HashMap<String, Field>());
            this.put("hasMany", new HashMap<String, Relation>());
            this.put("ownBy", new HashMap<String, Relation>());
        }
        
        public String getTableName() {
            return this.containsKey("tableName")?((String) this.get("tableName")):"";
        }
        
        public void setLabel(String label) {
            this.put("label", label);
        }
        
        public String getLabel() {
            return this.containsKey("label")?((String) this.get("label")):"";
        }
        
        public void addForm(String id, String label) {
            ((HashMap<String, String>) this.get("forms")).put(id, label);
        }
        
        public boolean hasForm(String id) {
            return ((HashMap<String, Field>) this.get("forms")).containsKey(id);
        }
        
        public void addField(String id, Field field) {
            if (!hasField(id)) {
                ((HashMap<String, Field>) this.get("fields")).put(id, field);
            }
        }
        
        public boolean hasField(String id) {
            return ((HashMap<String, Field>) this.get("fields")).containsKey(id);
        }
        
        public void addHasMany(Relation r) {
            Map<String, Relation> hasMany = (Map<String, Relation>) this.get("hasMany");
            if (!hasMany.containsKey(r.getEntity()) && !getTableName().equals(r.getEntity())) {
                r.clean();
                hasMany.put(r.getEntity(), r);
            }
        }
        
        public void addOwnBy(Relation r) {
            Map<String, Relation> ownBy = (Map<String, Relation>) this.get("ownBy");
            if (!ownBy.containsKey(r.getEntity()) && !getTableName().equals(r.getEntity())) {
                r.clean();
                ownBy.put(r.getEntity(), r);
            }
        }
    }
    
    public class Field extends HashMap<String, String> {
        public Field(String id, String className, String pluginLabel) {
            super();
            this.put("id", id);
            this.put("pluginClassName", className);
            this.put("pluginLabel", pluginLabel);
        }
        
        public String getId() {
            return this.get("id");
        }
        
        public String getPluginClassName() {
            return this.get("pluginClassName");
        }
        
        public String getPluginLabel() {
            return this.get("pluginLabel");
        }
    }
    
    public class Relation extends HashMap<String, String> {
        
        public Relation(String parentFormId, String parentEntity, String fieldId, String entityFormId, String entity, String entityFieldId) {
            super();
            this.put("parentFormId", parentFormId);
            this.put("parentEntity", parentEntity);
            this.put("fieldId", fieldId);
            this.put("entityFormId", entityFormId);
            this.put("entity", entity);
            this.put("entityFieldId", entityFieldId);
        }

        public String getParentFormId() {
            return this.containsKey("parentFormId")?((String) this.get("parentFormId")):"";
        }

        public String getParentEntity() {
            return this.containsKey("parentEntity")?((String) this.get("parentEntity")):"";
        }
        
        public String getFieldId() {
            return this.containsKey("fieldId")?((String) this.get("fieldId")):"";
        }

        public String getEntityFormId() {
            return this.containsKey("entityFormId")?((String) this.get("entityFormId")):"";
        }

        public String getEntity() {
            return this.containsKey("entity")?((String) this.get("entity")):"";
        }

        public String getEntityFieldId() {
            return this.containsKey("entityFieldId")?((String) this.get("entityFieldId")):"";
        }

        public void setFieldId(String fieldId) {
            this.put("fieldId", fieldId);
        }

        public void setParentEntity(String parentEntity) {
            this.put("parentEntity", parentEntity);
        }

        public void setEntity(String entity) {
            this.put("entity", entity);
        }
        
        public void clean() {
            this.remove("parentFormId");
            this.remove("parentEntity");
            this.remove("entityFormId");
        }
    }
}
