package org.joget.plugin.property.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONUtils;
import net.sf.json.xml.XMLSerializer;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.property.model.PropertyOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertyUtil {
    public final static String PASSWORD_PROTECTED_VALUE = "****SECURE_VALUE****-";
    public final static String TYPE_PASSWORD = "password";
    public final static String TYPE_ELEMENT_SELECT = "elementselect";

    /**
     * Parse property xml file and convert to json output
     * @return
     */
    public static String getPropertiesJSONObject(String[] fileNames) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document output = docBuilder.newDocument();

            Element root = output.createElement("configuration");

            //create dummy page
            Node dummyPage = output.createElement("page");
            Node dummyPageHidden = output.createElement("hidden");
            dummyPageHidden.setTextContent("True");
            dummyPage.appendChild(dummyPageHidden);
            root.appendChild(dummyPage);

            for (String fileName : fileNames) {
                Document doc = docBuilder.parse(PropertyUtil.class.getClassLoader().getResourceAsStream(fileName));

                //processing the xml
                //change options_source_class to option
                NodeList nodes = doc.getElementsByTagName("options_source_class");
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        Node node = nodes.item(i);
                        Node parentNode = node.getParentNode();

                        String clazzName = node.getTextContent();
                        Node optionsNode = doc.createElement("options");

                        Class clazz = Class.forName(clazzName);
                        PropertyOptions optionClass = (PropertyOptions) clazz.newInstance();
                        Map optionMap = optionClass.toOptionsMap();

                        for (Object obj : optionMap.entrySet()) {
                            Map.Entry entry = (Map.Entry) obj;
                            Node option = doc.createElement("option");
                            Node value = doc.createElement("value");
                            Node label = doc.createElement("label");
                            value.setTextContent(entry.getKey().toString());
                            label.setTextContent(entry.getValue().toString());
                            option.appendChild(value);
                            option.appendChild(label);

                            optionsNode.appendChild(option);
                        }

                        parentNode.appendChild(optionsNode);
                    } catch (Exception e) {
                        //ignore
                    }
                }

                NodeList pageNodes = doc.getElementsByTagName("page");
                for (int i = 0; i < pageNodes.getLength(); i++) {
                    Node importedPageNode = output.importNode(pageNodes.item(i), true);
                    root.appendChild(importedPageNode);
                }
            }

            output.appendChild(root);

            //setting up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();

            //generating string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(output);
            trans.transform(source, result);
            String xmlString = sw.toString();

            //convert to json string
            XMLSerializer xmlSerializer = new XMLSerializer();
            JSON json = xmlSerializer.read(xmlString);
            String jsonString = json.toString(2);
            return jsonString;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Parse default properties string from json
     */
    public static String getDefaultPropertyValues(String json) {
        try {
            JSONArray pages = (JSONArray) JSONSerializer.toJSON(json);
            String defaultProperties = "{";

            //loop page
            if (!JSONUtils.isNull(pages)) {
                for (int i = 0; i < pages.size(); i++) {
                    JSONObject page = (JSONObject) pages.get(i);
                    if (!JSONUtils.isNull(page)) {
                        //loop properties
                        JSONArray properties = (JSONArray) page.get("properties");
                        for (int j = 0; j < properties.size(); j++) {
                            JSONObject property = (JSONObject) properties.get(j);
                            if (property.containsKey("value")) {
                                defaultProperties += "'" + property.getString("name") + "':'" + StringUtil.escapeString(property.getString("value"), StringUtil.TYPE_JSON, null) + "',";
                            }
                        }
                    }
                }
            }
            if (defaultProperties.endsWith(",")) {
                defaultProperties = defaultProperties.substring(0, defaultProperties.length() - 1);
            }
            defaultProperties += "}";
            return defaultProperties;
        }catch(Exception ex){
            LogUtil.error("PropertyUtil", ex, json);
        }
        return "{}";
    }

    /**
     * Parse Json and return properties map
     * @return
     */
    public static Map<String, Object> getPropertiesValueFromJson(String json) {
        JSONObject obj = (JSONObject) JSONSerializer.toJSON(json);
        return getProperties(obj);
    }

    /**
     * Recursively call to get properties from json object
     * @return
     */
    private static Map<String, Object> getProperties(JSONObject obj) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (obj != null) {
            for (Object key : obj.keySet()) {
                Object value = obj.get(key);
                if (!JSONUtils.isNull(value)) {
                    if (value instanceof JSONArray) {
                        properties.put(key.toString(), getProperties((JSONArray) value));
                    } else if (value instanceof JSONObject && !((JSONObject) value).keySet().isEmpty()) {
                        properties.put(key.toString(), getProperties((JSONObject) value));
                    } else if ("{}".equals(obj.getString(key.toString()))) {
                        properties.put(key.toString(), new HashMap<String, Object>());
                    } else {
                        properties.put(key.toString(), obj.getString(key.toString()));
                    }
                } else {
                    properties.put(key.toString(), "");
                }
            }
        }
        return properties;
    }

    /**
     * Recursively call to get properties from json array
     * @return
     */
    private static Object[] getProperties(JSONArray arr) {
        Collection<Object> array = new ArrayList<Object>();
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                Object value = arr.get(i);
                if (!JSONUtils.isNull(value)) {
                    if (value instanceof JSONArray) {
                        array.add(getProperties((JSONArray) value));
                    } else {
                        array.add(getProperties((JSONObject) value));
                    }
                }
            }
        }
        return array.toArray();
    }
    
    public static String propertiesJsonLoadProcessing(String json) {
        //parse content
        if (json != null && json.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(json);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }

            try {
                if (!sList.isEmpty()) {
                    int count = 0;
                    for (String s : sList) {
                        json = json.replaceAll(StringUtil.escapeRegex(s), PASSWORD_PROTECTED_VALUE + count);
                        count++;
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(PropertyUtil.class.getName(), ex, "");
            }
        }
        
        return json;
    }
    
    public static String propertiesJsonStoreProcessing(String oldJson, String newJson) {
        Map<String, String> passwordProperty = new HashMap<String, String>();
        
        if (oldJson != null && !oldJson.isEmpty() && oldJson.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(oldJson);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }
            
            if (!sList.isEmpty()) {
                int count = 0;
                for (String s : sList) {
                    passwordProperty.put(SecurityUtil.ENVELOPE + PASSWORD_PROTECTED_VALUE + count + SecurityUtil.ENVELOPE, s);
                    count++;
                }
            }
        }
        
        if (newJson != null && !newJson.isEmpty() && (newJson.contains(SecurityUtil.ENVELOPE) || newJson.contains(PASSWORD_PROTECTED_VALUE))) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(newJson);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }
            
            Pattern pattern2 = Pattern.compile("\"("+StringUtil.escapeRegex(PASSWORD_PROTECTED_VALUE)+"[^\"]*)\"");
            Matcher matcher2 = pattern2.matcher(newJson);
            while (matcher2.find()) {
                sList.add(SecurityUtil.ENVELOPE + matcher2.group(1) + SecurityUtil.ENVELOPE);
                newJson = newJson.replaceAll(StringUtil.escapeRegex(matcher2.group(1)), SecurityUtil.ENVELOPE + matcher2.group(1) + SecurityUtil.ENVELOPE);
            }
            
            //For datalist binder initialization (getBuilderDataColumnList) 
            if (!newJson.contains("\"")) {
                sList.add(SecurityUtil.ENVELOPE + newJson + SecurityUtil.ENVELOPE);
                newJson = SecurityUtil.ENVELOPE + newJson + SecurityUtil.ENVELOPE;
            }
            
            try {
                if (!sList.isEmpty()) {
                    for (String s : sList) {
                        if (s.contains(PASSWORD_PROTECTED_VALUE)) {
                            newJson = newJson.replaceAll(StringUtil.escapeRegex(s), passwordProperty.get(s));
                        } else {
                            String tempS = s.replaceAll(SecurityUtil.ENVELOPE, "");
                            tempS = SecurityUtil.encrypt(tempS);

                            newJson = newJson.replaceAll(s, tempS);
                        }
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(PropertyUtil.class.getName(), ex, "");
            }
        }
        
        return newJson;
    }
}
