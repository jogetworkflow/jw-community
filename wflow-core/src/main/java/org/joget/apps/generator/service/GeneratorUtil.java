package org.joget.apps.generator.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListBinder;
import static org.joget.apps.datalist.service.JsonUtil.parseBinderFromJsonObject;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.lib.RunProcess;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.enterprise.CorporatiTheme;
import org.json.JSONArray;
import org.json.JSONObject;
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

            userviewId = StringEscapeUtils.escapeJavaScript(userviewId);
            userviewName = StringEscapeUtils.escapeJavaScript(userviewName);
            userviewDescription = StringEscapeUtils.escapeJavaScript(userviewDescription);
            theme = StringEscapeUtils.escapeJavaScript(theme);
            String json = AppUtil.readPluginResource(CorporatiTheme.class.getName(), "/resources/generator/userview/userview.json", new String[]{userviewId, userviewName, userviewDescription, theme, themeProperties}, true, null);

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
