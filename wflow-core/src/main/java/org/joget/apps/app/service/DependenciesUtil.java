package org.joget.apps.app.service;

import java.io.ByteArrayInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DependenciesUtil {
    
    private static final XPath XPATH;
    
    static {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setXPathFunctionResolver(new RegexMatchesFunctionResolver());
        XPATH = xpath;
    }
    
    private static void findKeywords(JSONArray keywords, String text, String keyword) {
        int found = text.indexOf(keyword);
                        
        while (found > 0) {
            int start = (found - 40 < 0) ? 0 : found - 40;
            int end = ((found + keyword.length() + 40) >= text.length())? text.length() : (found + keyword.length() + 40);
            
            String words = text.substring(start, end);
            if (start != 0) {
                words = "..." + words;
            }
            if (end != text.length()) {
                words = words + "...";
            }
            keywords.put(words);

            text = text.substring(found + keyword.length());
            found = text.indexOf(keyword);
        }
    }
    
    private static void findHashVariableKeywords(JSONArray keywords, String text, String keyword) {
        int found = text.indexOf(keyword);
        String regex = ".*#[^#]+\\."+StringUtil.escapeRegex(keyword)+"([\\}?\\.\\[]+[^#]*)*#.*";
                        
        while (found > 0) {
            int start = (found - 40 < 0) ? 0 : found - 40;
            int end = ((found + keyword.length() + 40) >= text.length())? text.length() : (found + keyword.length() + 40);
            
            String words = text.substring(start, end);
            if (words.matches(regex)) {
                if (start != 0) {
                    words = "..." + words;
                }
                if (end != text.length()) {
                    words = words + "...";
                }
                keywords.put(words);
            }

            text = text.substring(found + keyword.length());
            found = text.indexOf(keyword);
        }
    }
    
    
    public static JSONArray getDependencies(String appId, String version, String type, String keyword, HttpServletRequest request) {
        keyword = SecurityUtil.validateStringInput(keyword);
        
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        
        Long appVersion;
        if (version == null || version.isEmpty()) {
            appVersion = appService.getPublishedVersion(appId);
        } else {
            appVersion = Long.parseLong(version);
        }
        byte[] defXml = appService.getAppDefinitionXml(appId, appVersion);
        JSONArray usages = new JSONArray();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input =  new ByteArrayInputStream(defXml);
            Document xml = builder.parse(input);
            
            String expression = "//json[contains(text(),'\""+keyword+"\"')]";
            expression += " | //pluginProperties[contains(text(),'\""+keyword+"\"')] ";
            
            //Hash variable
            String regex = ".*#[^#]+\\."+StringUtil.escapeRegex(keyword)+"([\\}?\\.\\[]+[^#]*)*#.*";
            expression += " | //json[jfn:regexmatches(text(),'"+regex+"')]";
            expression += " | //pluginProperties[jfn:regexmatches(text(),'"+regex+"')] ";
            
            if ("form".equals(type)) {
                //handle for beanshell
                expression += " | //json[contains(text(),'\""+keyword+"\\\"')]";
                expression += " | //pluginProperties[contains(text(),'\""+keyword+"\\\"')] ";
                
                expression += " | //packageActivityForm/formId[normalize-space(text())='"+keyword+"']";	       
            } else if ("table".equals(type)) {
                //handle for beanshell
                expression += " | //json[contains(text(),'\""+keyword+"\\\"')]";
                expression += " | //pluginProperties[contains(text(),'\""+keyword+"\\\"')] ";
                
                //handle for jdbc query
                expression += " | //json[contains(text(),'app_fd_"+keyword+"')]";
                expression += " | //pluginProperties[contains(text(),'app_fd_"+keyword+"')] ";
                
                //handle for form hash variable
                expression += " | //json[contains(text(),'form."+keyword+".')]";
                expression += " | //pluginProperties[contains(text(),'form."+keyword+".')] ";
            }
            NodeList nodeList = (NodeList) XPATH.compile(expression).evaluate(xml, XPathConstants.NODESET);
            
            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    JSONObject obj = new JSONObject();
                    Node nNode = nodeList.item(i);
                    
                    if ("formId".equals(nNode.getNodeName())) {
                        Element parent = (Element) nNode.getParentNode().getParentNode();
                        Node where = parent.getElementsByTagName("string").item(0);
                        String[] ids = where.getTextContent().split("::");
                        obj.put("where", where.getTextContent());
                        obj.put("label", where.getTextContent());
                        obj.put("type", "process_activity");
                        obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.activities"));
                        obj.put("link", request.getContextPath() + "/web/console/app/"+appId+"/"+version+"/processes/"+ids[0]+"?tab=activityList&activityDefId="+ids[1]);
                    } else if ("pluginProperties".equals(nNode.getNodeName())) {
                        if ("pluginDefaultProperties".equals(nNode.getParentNode().getNodeName())) {
                            Element parent = (Element) nNode.getParentNode();
                            Node where = parent.getElementsByTagName("id").item(0);
                            String className = where.getTextContent();
                            obj.put("where", className);
                            Node label = parent.getElementsByTagName("pluginName").item(0);
                            obj.put("label", label.getTextContent());
                            obj.put("type", "plugin_default_properties");
                            obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.pluginDefault"));
                            obj.put("link", request.getContextPath() + "/web/console/app/"+appId+"/"+version+"/properties?tab=pluginDefault&plugin="+className);
                        } else {
                            Element parent = (Element) nNode.getParentNode().getParentNode();
                            Node where = parent.getElementsByTagName("string").item(0);
                            String[] ids = where.getTextContent().split("::");

                            obj.put("where", where.getTextContent());
                            obj.put("label", where.getTextContent());

                            if ("packageParticipant".equals(nNode.getParentNode().getNodeName())) {
                                obj.put("type", "process_participant");
                                obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.participants"));
                                obj.put("link", request.getContextPath() + "/web/console/app/"+appId+"/"+version+"/processes/"+ids[0]+"?tab=participantList&participantId="+ids[1]);
                            } else {
                                obj.put("type", "process_tool");
                                obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.tools"));
                                obj.put("link", request.getContextPath() + "/web/console/app/"+appId+"/"+version+"/processes/"+ids[0]+"?tab=toolList&activityDefId="+ids[1]);
                            }
                        }
                    } else if ("json".equals(nNode.getNodeName())) {
                        Element parent = (Element) nNode.getParentNode();
                        Node where = parent.getElementsByTagName("id").item(0);
                        String id = where.getTextContent();
                        obj.put("where", id);
                        
                        Node label = parent.getElementsByTagName("name").item(0);
                        obj.put("label", label.getTextContent());
                            
                        String nodeType = "form";
                        if ("userviewDefinition".equals(nNode.getParentNode().getNodeName())) {
                            nodeType = "userview";
                        } else if ("datalistDefinition".equals(nNode.getParentNode().getNodeName())) {
                            nodeType = "datalist";
                        }
                        
                        if (nodeType.equals(type) && keyword.equals(id)) {
                            continue;
                        }
                        
                        obj.put("type", nodeType);
                        obj.put("category", ResourceBundleUtil.getMessage("dependency.usage."+nodeType));
                        obj.put("link", request.getContextPath() + "/web/console/app/"+appId+"/"+version+"/"+nodeType+"/builder/"+id);
                    }
                    
                    if ("pluginProperties".equals(nNode.getNodeName()) || "json".equals(nNode.getNodeName())) {
                        String text = nNode.getTextContent();
                        text = text.replaceAll("    ", "");
                        
                        JSONArray foundArr = new JSONArray();
                        findKeywords(foundArr, text, "\""+keyword+"\"");
                        findHashVariableKeywords(foundArr, text, keyword);
                        
                        if ("form".equals(type)) {
                            findKeywords(foundArr, text, "\\\""+keyword+"\\\"");
                        }
                        
                        if ("table".equals(type)) {
                            findKeywords(foundArr, text, "\\\""+keyword+"\\\"");
                            findKeywords(foundArr, text, "form."+keyword+".");
                            findKeywords(foundArr, text, "app_fd_"+keyword+"");
                        }
                        
                        obj.put("found", foundArr);
                    }
                    usages.put(obj);
                }
            }
            
        } catch (Exception e) {
            LogUtil.error(DependenciesUtil.class.getName(), e, "");
        }
        return usages;
    }
}
