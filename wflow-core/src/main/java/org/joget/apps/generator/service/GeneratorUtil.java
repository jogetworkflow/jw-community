package org.joget.apps.generator.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListBinder;
import static org.joget.apps.datalist.service.JsonUtil.parseBinderFromJsonObject;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.generator.model.GeneratorPlugin;
import org.joget.apps.userview.lib.RunProcess;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.enterprise.CorporatiTheme;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utility methods can be used by Generator Plugin
 * 
 */
public class GeneratorUtil {
    
    /**
     * Method used to replaces syntax below to form meta value.
     * [formId], [formName], [formTableName], [appId], [appName] & [appVersion] 
     * @param content
     * @param formId
     * @param appDef
     * @return 
     */
    public static String populateFormMeta(String content, String formId, AppDefinition appDef) {
        if (content != null && !content.isEmpty() && formId != null && !formId.isEmpty() && appDef != null) {
            FormDefinitionDao formDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
            FormDefinition formDef = formDao.loadById(formId, appDef);
            
            if (formDef != null) {
                content = content.replaceAll("\\[formId\\]", formDef.getId());
                content = content.replaceAll("\\[formName\\]", formDef.getName());
                content = content.replaceAll("\\[formTableName\\]", formDef.getTableName());
                content = content.replaceAll("\\[appId\\]", appDef.getAppId());
                content = content.replaceAll("\\[appName\\]", appDef.getName());
                content = content.replaceAll("\\[appVersion\\]", appDef.getVersion().toString());
            }
        }
        return content;
    } 
    
    /**
     * Gets the Form object by Id
     * @param formId
     * @param appDef
     * @return
     * @throws RuntimeException 
     */
    public static Form getFormObject(String formId, AppDefinition appDef) throws RuntimeException {
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        Form form = null;
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        if (formDef != null) {
            String formJson = formDef.getJson();
                    
            if (formJson != null) {
                form = (Form) formService.createElementFromJson(formJson, false);
                return form;
            }
        } 
        throw new RuntimeException(ResourceBundleUtil.getMessage("generator.form.notExist"));
    }
    
    
    /**
     * Creates a new userview definition json
     * @param userviewId
     * @param userviewName
     * @param userviewDescription
     * @return 
     */
    public static String createNewUserviewJson(String userviewId, String userviewName, String userviewDescription) {
        return createNewUserviewJson(userviewId, userviewName, userviewDescription, null);
    }
    
    /**
     * Creates a new userview definition json based another userview definition
     * @param userviewId
     * @param userviewName
     * @param userviewDescription
     * @param copy
     * @return 
     */
    public static String createNewUserviewJson(String userviewId, String userviewName, String userviewDescription, UserviewDefinition copy) {
        if (copy != null) {
            String copyJson = copy.getJson();
            try {
                JSONObject obj = new JSONObject(copyJson);
                if (!obj.isNull("properties")) {
                    JSONObject objProperty = obj.getJSONObject("properties");
                    objProperty.put("id", userviewId);
                    objProperty.put("name", userviewName);
                    objProperty.put("description", userviewDescription);
                }
                return obj.toString();
            } catch (Exception e) {
            }
        } else {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            String className = ResourceBundleUtil.getMessage("userview.default.theme.classname");
            String theme = null;
            if (className != null && !className.isEmpty()) {
                Plugin plugin = pluginManager.getPlugin(className);
                if (plugin != null && plugin instanceof UserviewTheme) {
                    theme = className;
                }
            }
            if (theme == null || theme.isEmpty()) {
                theme = ResourceBundleUtil.getMessage("generator.userview.theme");
            }
            String themeProperties = ResourceBundleUtil.getMessage("generator.userview.theme."+theme+".properties");
            if (themeProperties == null || themeProperties.isEmpty()) {
                themeProperties = ResourceBundleUtil.getMessage("generator.userview.theme.default.properties");
            }

            userviewId = StringUtil.escapeString(userviewId, StringUtil.TYPE_JSON, null);
            userviewName = StringUtil.escapeString(userviewName, StringUtil.TYPE_JSON, null);
            userviewDescription = StringUtil.escapeString(userviewDescription, StringUtil.TYPE_JSON, null);
            theme = StringUtil.escapeString(theme, StringUtil.TYPE_JSON, null);
            String json = AppUtil.readPluginResource(CorporatiTheme.class.getName(), "/resources/generator/userview/userview.json", new String[]{userviewId, userviewName, userviewName, userviewDescription, theme, themeProperties}, true, null);

            if (json != null && !json.isEmpty()) {
                return json;
            }
        }
        return null;
    }
    
    /**
     * Adds an userview category json to an existing userview json
     * @param categoryJson
     * @param userviewJson
     * @return 
     */
    public static String addCategoryJsonToUserviewJson(String categoryJson, String userviewJson) {
        try {
            JSONObject userviewObject = new JSONObject(userviewJson);
            JSONObject categoryObject = new JSONObject(categoryJson);
            
            JSONArray categories = userviewObject.getJSONArray("categories");
            categories.put(categoryObject);
            
            return userviewObject.toString();
        } catch (Exception e) {
            LogUtil.error(GeneratorUtil.class.getName(), e, "addCategoryJsonToUserviewJson error");
        }
        
        return userviewJson;
    }
    
    /**
     * Create a XPDL with empty process package
     * @param appDef
     * @return 
     */
    public static String createProcessPackageXpdl(AppDefinition appDef) {
        String appId = StringEscapeUtils.escapeXml(appDef.getAppId());
        String appName = StringEscapeUtils.escapeXml(appDef.getName());
        String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
        String version = StringEscapeUtils.escapeXml(ResourceBundleUtil.getMessage("console.footer.label.revision"));
        
        String xpdl = AppUtil.readPluginResource(RunProcess.class.getName(), "/resources/generator/process/package.xpdl", new String[]{appId, appName, date, version}, true, null);
                    
        if (xpdl != null && !xpdl.isEmpty()) {
            return xpdl;
        }
        return null;
    }
    
    /**
     * Add participants xml and process xml to an existing xpdl
     * @param participantsXml
     * @param processXml
     * @param xpdl
     * @return
     * @throws RuntimeException 
     */
    public static String addParticipantsAndProcessXmlToXpdl(String participantsXml, String processXml, String xpdl) throws RuntimeException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            domFactory.setExpandEntityReferences(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document xpdlDoc = builder.parse(new InputSource(new ByteArrayInputStream(xpdl.getBytes("UTF-8"))));
            
            DocumentBuilderFactory domFactory2 = DocumentBuilderFactory.newInstance();
            domFactory2.setExpandEntityReferences(false);
            DocumentBuilder builder2 = domFactory2.newDocumentBuilder();
            Document processDoc = builder2.parse(new InputSource(new ByteArrayInputStream(processXml.getBytes("UTF-8"))));
        
            NodeList xpdlNodeList = xpdlDoc.getChildNodes();
            NodeList processNodeList = processDoc.getChildNodes();
            
            Node xpdlPackageNode = getNode("Package", xpdlNodeList);
            Node participatsNode = getNode("Participants", xpdlPackageNode.getChildNodes());
            Node processesNode = getNode("WorkflowProcesses", xpdlPackageNode.getChildNodes());
            
            if (participantsXml != null && !participantsXml.isEmpty()) {
                DocumentBuilderFactory domFactory3 = DocumentBuilderFactory.newInstance();
                domFactory3.setExpandEntityReferences(false);
                DocumentBuilder builder3 = domFactory3.newDocumentBuilder();
                Document participantsDoc = builder3.parse(new InputSource(new ByteArrayInputStream(participantsXml.getBytes("UTF-8"))));
            
                NodeList participantsList = participantsDoc.getChildNodes();
                Node tempParticipatsNode = getNode("Participants", participantsList);
                if (tempParticipatsNode != null && tempParticipatsNode.hasChildNodes()) {
                    NodeList tempParticipatsNodeList = tempParticipatsNode.getChildNodes();
                    for (int x = 0; x < tempParticipatsNodeList.getLength(); x++) {
                        Node node = tempParticipatsNodeList.item(x);
                        Node importedNode = xpdlDoc.importNode(node, true);
                        participatsNode.appendChild(importedNode);
                    }
                }
            }
            
            Node processNode = getNode("WorkflowProcess", processNodeList);
            Node importedNode = xpdlDoc.importNode(processNode, true);
            processesNode.appendChild(importedNode);

            OutputFormat format = new OutputFormat(xpdlDoc);
            format.setIndenting(true);
            format.setEncoding("UTF-8");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLSerializer serializer = new XMLSerializer(output, format);
            serializer.serialize(xpdlDoc);
            xpdl = output.toString("UTF-8");
        } catch (Exception e) {
            LogUtil.error(GeneratorUtil.class.getName(), e, "Generate XPDL error");
            throw new RuntimeException (ResourceBundleUtil.getMessage("generator.process.invalidXpdl"));
        }
        return xpdl;
    }
    
    /**
     * Retrieves a List id which using the form id in binder
     * @param appDef
     * @param formId
     * @return 
     */
    public static String getFirstAvailableListIdByFormId(AppDefinition appDef, String formId) {
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) AppUtil.getApplicationContext().getBean("datalistDefinitionDao");
        String listId = "";
        Collection<DatalistDefinition> list = datalistDefinitionDao.getList(appDef, null, null, null, null);
        
        if (list != null && !list.isEmpty()) {
            for (DatalistDefinition d : list) {
                String json = d.getJson();
                if (json != null && !json.isEmpty()) {
                    if ((json.contains("org.joget.apps.datalist.lib.FormRowDataListBinder") || json.contains("org.joget.plugin.enterprise.AdvancedFormRowDataListBinder")) && json.contains(formId)) {
                        try {
                            // create json object
                            JSONObject obj = new JSONObject(json);

                            DataListBinder binder = parseBinderFromJsonObject(obj);
                            if (binder != null && formId.equals(binder.getPropertyString("formDefId"))) {
                                return d.getId();
                            }
                        } catch (Exception ex) {}
                    }
                }
            }
        }
        return listId;
    }
    
    /**
     * Retrieves the first userview id in the app
     * @param appDef
     * @return 
     */
    public static String getFirstAvailableUserviewId(AppDefinition appDef) {
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
        String userviewId = "";
        
        Collection<UserviewDefinition> list = userviewDefinitionDao.getList(appDef, null, null, 0, 1);
        if (list != null && !list.isEmpty()) {
            userviewId = list.iterator().next().getId();
        }
        
        return userviewId;
    }
    
    /**
     * Deploy process with new participants and new process
     * 
     * @param appDef
     * @param participantsXml
     * @param processXml
     * @return
     * @throws UnsupportedEncodingException
     * @throws Exception 
     */
    public static PackageDefinition deployProcess(AppDefinition appDef, String participantsXml, String processXml) throws UnsupportedEncodingException, Exception {
        String xpdl = null;
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            byte[] content = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
            xpdl = new String(content, "UTF-8");
        }
        if (xpdl == null || xpdl.isEmpty()) {
            xpdl = GeneratorUtil.createProcessPackageXpdl(appDef);
        }
        xpdl = GeneratorUtil.addParticipantsAndProcessXmlToXpdl(participantsXml, processXml, xpdl);
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        return appService.deployWorkflowPackage(appDef.getId(), appDef.getVersion().toString(), xpdl.getBytes("UTF-8"), false);
    }
    
    /**
     * processing the generator resource file with plugin properties and variables
     * 
     * @param plugin
     * @param resourceUrl
     * @param resourceArgs
     * @param translationFileName
     * @param variables
     * @param escapeFormat
     * @return 
     */
    public static String processResourceFile(GeneratorPlugin plugin, String resourceUrl, Object[] resourceArgs, String translationFileName, Map<String, String> variables,  String escapeFormat) {
        String content = AppUtil.readPluginResource(plugin.getClassName(), resourceUrl, resourceArgs, true, translationFileName);
        
        while (content.contains("${{uuid}}")) {
            content = content.replaceFirst(StringUtil.escapeRegex("${{uuid}}"), UuidGenerator.getInstance().getUuid());
        }
        
        if (content.contains("${{DATETIME}}")) {
            content = content.replaceAll(StringUtil.escapeRegex("${{DATETIME}}"), StringUtil.escapeRegex((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())));
        }
        if (content.contains("${{DATE}}")) {
            content = content.replaceAll(StringUtil.escapeRegex("${{DATE}}"), StringUtil.escapeRegex((new SimpleDateFormat("yyyy-MM-dd")).format(new Date())));
        }
        
        for (String key : variables.keySet()) {
            if (!(content.contains("${{") && content.contains("}}"))) {
                break;
            }
            if (content.contains("${{"+ key +"}}")) {
                String value = variables.get(key);
                if (escapeFormat != null && !escapeFormat.isEmpty()) {
                    value = StringUtil.escapeString(value, escapeFormat, null);
                }
                content = content.replaceAll(StringUtil.escapeRegex("${{"+ key +"}}"), StringUtil.escapeRegex(value));
            }
        }
        
        for (String key : plugin.getProperties().keySet()) {
            if (!(content.contains("${{") && content.contains("}}"))) {
                break;
            }
            if (content.contains("${{"+ key +"}}")) {
                if (plugin.getProperty(key) instanceof String) {
                    String value = plugin.getPropertyString(key);
                    if (value.contains("[") && value.contains("]")) {
                        for (String vkey : variables.keySet()) {
                            if (value.contains("["+ vkey +"]")) {
                                value = value.replaceAll(StringUtil.escapeRegex("["+vkey+"]"), StringUtil.escapeRegex(variables.get(vkey)));
                            }
                        }
                    }
                    if (escapeFormat != null && !escapeFormat.isEmpty()) {
                        value = StringUtil.escapeString(value, escapeFormat, null);
                    }
                    content = content.replaceAll(StringUtil.escapeRegex("${{"+ key +"}}"), StringUtil.escapeRegex(value));
                }
            }
        }
        
        return content;
    }
    
    /**
     * Generate variables based on form definition
     * 
     * @param formDef
     * @return 
     */
    public static Map<String, String> getVariables(FormDefinition formDef) {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("prefix", formDef.getId());
        variables.put("formId", formDef.getId());
        variables.put("formName", formDef.getName());
        variables.put("formTableName", formDef.getTableName());
        variables.put("appId", formDef.getAppDefinition().getAppId());
        variables.put("appVersion", formDef.getAppDefinition().getVersion().toString());
        variables.put("appName", formDef.getAppDefinition().getName());
        return variables;
    }
    
    /**
     * Add participant mapping for a process
     * 
     * @param appDef
     * @param packageDef
     * @param processId
     * @param participantId
     * @param type
     * @param value
     * @param pluginProperties 
     */
    public static void addParticipantMapping(AppDefinition appDef, PackageDefinition packageDef, String processId, String participantId, String type, String value, String pluginProperties) {
        PackageParticipant mapping = new PackageParticipant();
        mapping.setPackageDefinition(packageDef);
        mapping.setProcessDefId(processId);
        mapping.setParticipantId(participantId);
        mapping.setType(type);
        mapping.setValue(value);
        mapping.setPluginProperties(pluginProperties);
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) AppUtil.getApplicationContext().getBean("packageDefinitionDao");
        packageDefinitionDao.addAppParticipant(appDef.getId(), appDef.getVersion(), mapping);
    }
    
    /**
     * Add tool mapping for a process
     * 
     * @param appDef
     * @param packageDef
     * @param processId
     * @param activityDefId
     * @param pluginName
     * @param pluginProperties 
     */
    public static void addToolMapping(AppDefinition appDef, PackageDefinition packageDef, String processId, String activityDefId, String pluginName, String pluginProperties) {
        PackageActivityPlugin mapping = new PackageActivityPlugin();
        mapping.setPackageDefinition(packageDef);
        mapping.setProcessDefId(processId);
        mapping.setActivityDefId(activityDefId);
        mapping.setPluginName(pluginName);
        mapping.setPluginProperties(pluginProperties);
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) AppUtil.getApplicationContext().getBean("packageDefinitionDao");
        packageDefinitionDao.addAppActivityPlugin(appDef.getId(), appDef.getVersion(), mapping);
    }
    
    /**
     * Add form mapping for a process
     * 
     * @param appDef
     * @param packageDef
     * @param processId
     * @param activityDefId
     * @param formId 
     */
    public static void addFormMapping(AppDefinition appDef, PackageDefinition packageDef, String processId, String activityDefId, String formId) {
        PackageActivityForm mform = new PackageActivityForm();
        mform.setPackageDefinition(packageDef);
        mform.setProcessDefId(processId);
        mform.setActivityDefId(activityDefId);
        mform.setFormId(formId);
        mform.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
        
        PackageDefinitionDao packageDefinitionDao = (PackageDefinitionDao) AppUtil.getApplicationContext().getBean("packageDefinitionDao");
        packageDefinitionDao.addAppActivityForm(appDef.getId(), appDef.getVersion(), mform);
    }
    
    /**
     * Adding new userview category to a existing userview or a new created userview
     * 
     * @param appDef
     * @param id
     * @param newId
     * @param name
     * @param desc
     * @param categoryJson
     * @return 
     */
    public static UserviewDefinition updateOrCreateUserviewWithNewCategory(AppDefinition appDef, String id, String newId, String name, String desc, String categoryJson) {
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
            
        UserviewDefinition userviewDef = null;
        String json = null;
        if (id != null && !id.isEmpty()) {         
            userviewDef = userviewDefinitionDao.loadById(id, appDef);
        }
        if (userviewDef != null) {
            name = userviewDef.getName();
            desc = userviewDef.getDescription();
            json = userviewDef.getJson();
        } else {
            id = newId;
            int count = 0;
            while (isExist(id, userviewDefinitionDao, appDef)) {
                count++;
                id = id + count;
            }
        }
        if (json == null || json.isEmpty()) {
            json = GeneratorUtil.createNewUserviewJson(id, name, desc);
        }
        
        json = addCategoryJsonToUserviewJson(categoryJson, json);
        
        if (userviewDef != null) { 
            userviewDef.setJson(json);
            userviewDefinitionDao.update(userviewDef);
        }else {
            userviewDef = new UserviewDefinition();
            userviewDef.setJson(json);
            userviewDef.setId(id);
            userviewDef.setName(name);
            userviewDef.setAppDefinition(appDef);
            userviewDefinitionDao.add(userviewDef);

            //Set current published version
            final AppDefinition currentAppDef = appDef;
            TransactionTemplate transactionTemplate = (TransactionTemplate)AppUtil.getApplicationContext().getBean("transactionTemplate");
            transactionTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus ts) {
                    AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
                    appService.publishApp(currentAppDef.getId(), currentAppDef.getVersion().toString());
                    return null;
                }
            });
        }
        
        return userviewDef;
    }
    
    /**
     * Add element json to a form
     * 
     * @param formDef
     * @param json
     * @param addToFirst first form element|last form element| first column element | last column element
     * @param addToColumn 
     */
    public static void addElementJsonToForm(FormDefinition formDef, String json, boolean addToFirst, boolean addToColumn) {
        try {
            JSONObject formObject = new JSONObject(formDef.getJson());
            JSONArray elements = formObject.getJSONArray("elements"); //form childs > sections
            if (elements.length() > 0) { //get last section's child
                elements = elements.getJSONObject(elements.length() - 1).getJSONArray("elements");
            }
            if (addToColumn) {
                int index = 0;
                if (!addToFirst) {
                    index = elements.length() - 1;
                }
                elements = elements.getJSONObject(index).getJSONArray("elements");
            }
                
            if (json.startsWith("{") && json.endsWith("}")) {
                JSONObject elmentObject = new JSONObject(json);

                if (addToFirst) {
                    for (int i = elements.length(); i > 0; i--){
                        elements.put(i, elements.get(i-1));
                    }
                    elements.put(0, elmentObject);
                } else {
                    elements.put(elmentObject);
                }
            } else if (json.startsWith("[") && json.endsWith("]")) {
                JSONArray elmentsObject = new JSONArray(json);

                if (addToFirst) {
                    int size = elmentsObject.length();
                    for (int i = elements.length() - 1; i >= 0; i--){
                        elements.put(i + size, elements.get(i));
                    }
                    for (int i = 0; i < size; i++) {
                        elements.put(i, elmentsObject.get(i));
                    }
                } else {
                    for (int i = 0; i < elmentsObject.length(); i++) {
                        elements.put(elmentsObject.get(i));
                    }
                }
            }
            
            formDef.setJson(formObject.toString());
            FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
            formDefinitionDao.update(formDef);
        } catch (Exception e) {
            LogUtil.error(GeneratorUtil.class.getName(), e, "addElementJsonToForm error");
        }
    }
    
    protected static boolean isExist(String id, UserviewDefinitionDao userviewDefinitionDao, AppDefinition appDef) {
        Long count = userviewDefinitionDao.count("AND id = ?", new String[]{id}, appDef);
        return count > 0;
    }
    
    protected static Node getNode(String tagName, NodeList nodes) {
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }

        return null;
    }
}
