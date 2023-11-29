package org.joget.apps.datalist.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.displaytag.pagination.SmartListHelper;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.properties.TableProperties;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.Anchor;
import org.displaytag.util.DefaultHref;
import org.joget.apps.app.service.AppUtil;
import static org.joget.apps.datalist.model.DataList.CHECKBOX_PREFIX;
import org.joget.apps.datalist.service.DataListDecorator;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.joget.apps.datalist.service.JsonUtil;
import org.joget.commons.util.LogUtil;

public abstract class DataListTemplate extends ExtDefaultPlugin implements PropertyEditable {
    private DataList datalist;
    private DataListDecorator decorator;
    private Map<String, String> styles = new HashMap<String, String>();
    private Set<String> styleKeys = new HashSet<String>();
    
    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
        this.decorator = new DataListDecorator(datalist);
    }
    
    public abstract String getTemplate();
    
    public String getTemplate(Map data, String templatePath, String translationPath) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        if (data == null) {
            data = new HashMap();
        }
        
        data.put("element", this);

        String content = pluginManager.getPluginFreeMarkerTemplate(data, getClassName(), templatePath, translationPath);
        return content;
    }
    
    public String render() {
        styles.put("MOBILE_STYLE", "");
        styles.put("TABLET_STYLE", "");
        styles.put("STYLE", "");
        String template = getTemplate();
        
        setProperty("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        template = fillTemplateProps(template);
        template = fillData(template, null);
        template = clean(template);
        
        return template + renderPagination();
    }
    
    public Map<String, String> getStyles() {
        return styles;
    }
    
    public String renderPagination() {
        int size = getDatalist().getSize();
        int pageSize = getDatalist().getPageSize();
        String pageValue = getDatalist().getDataListParamString(TableTagParameters.PARAMETER_PAGE);
        int page = 1;
        
        try {
            page = Integer.parseInt(pageValue);
        } catch(NumberFormatException e) {}
        
        
        TableProperties props = TableProperties.getInstance(WorkflowUtil.getHttpServletRequest());
        SmartListHelper listHelper = new SmartListHelper(getDatalist().getRows(), size, pageSize, props, true);
        listHelper.setCurrentPage(page);
        
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String url = "?";
        String qs = request.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            url += qs;
        }
        
        DefaultHref href = new DefaultHref(url);
        href.removeParameter("OWASP_CSRFTOKEN");
        
        String pageNav = listHelper.getPageNavigationBar(href, getDatalist().getDataListEncodedParamName("p"));
        
        String exportLinks = "";
        if (!getDatalist().getNoExport()) {
            if (MediaTypeEnum.getSize() == 4) {
                MediaTypeEnum.registerMediaType("pdf");
            }
            Iterator iterator = MediaTypeEnum.iterator();

            while (iterator.hasNext()){
                MediaTypeEnum currentExportType = (MediaTypeEnum) iterator.next();

                if (props.getAddExport(currentExportType)){

                    if (!exportLinks.isEmpty()){
                        exportLinks += props.getExportBannerSeparator();
                    }

                    href.addParameter(getDatalist().getDataListEncodedParamName(TableTagParameters.PARAMETER_EXPORTTYPE), currentExportType.getCode());

                    // export marker
                    href.addParameter(TableTagParameters.PARAMETER_EXPORTING, "1");

                    Anchor anchor = new Anchor(href, props.getExportLabel(currentExportType));
                    exportLinks += anchor.toString();
                }
            }
            exportLinks = MessageFormat.format(props.getExportBanner(), new String[]{exportLinks});
        }
        
        return "<div class=\"template_pagelinks\">" + pageNav + exportLinks + "</div>";
    }
    
    public String fillTemplateProps(String template) {
        //find normal variables
        Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String replace = matcher.group(0);
            String key = matcher.group(1);
            if (getProperties().containsKey(key)){
                String value = getPropertyString(key);
                if (key.startsWith("is") && (value.isEmpty() || value.equalsIgnoreCase("true"))) { //to support simple if true checking
                    continue;
                }
                template = template.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(value));
            } else if (key.startsWith("list.")) { //to support populate list properties such as id
                String skey = key.substring(5);
                if (getDatalist() != null && getDatalist().getProperties().containsKey(skey)) {
                    String value = getDatalist().getPropertyString(skey);
                    if (skey.startsWith("is") && (value.isEmpty() || value.equalsIgnoreCase("true"))) {
                        continue;
                    }
                    template = template.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(value));
                }
            }
        }
        return template;
    }
    
    public String clean(String template) {
        //find template variables
        Pattern pattern = Pattern.compile("\\{\\{([a-zA-Z0-9-_]+)(|.+?)\\}\\}([\\s\\S]+?)\\{\\{\\1\\}\\}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String replace = matcher.group(0);
            template = template.replaceAll(StringUtil.escapeRegex(replace), "");
        }
        
        //find normal variables
        Pattern pattern2 = Pattern.compile("\\{\\{([a-zA-Z0-9-_]+)(|.+?)\\}\\}");
        Matcher matcher2 = pattern2.matcher(template);

        while (matcher2.find()) {
            String replace = matcher2.group(0);
            template = template.replaceAll(StringUtil.escapeRegex(replace), "");
        }
        
        return template;
    }
    
    public String fillData(String template, Object data) {
        //find template variables
        Pattern pattern = Pattern.compile("\\{\\{([a-zA-Z0-9-_]+)(|.+?)\\}\\}(([\\s\\S]+?)\\{\\{\\1\\}\\}|)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String replace = matcher.group(0);
            String key = matcher.group(1);
            String props = matcher.group(2);
            String childtemplate = matcher.group(4);
            
            String value = "";
            if ("rows".equals(key)) {
                DataListCollection rows = getDatalist().getRows();
                value += fillRows(rows, childtemplate);
                applyStyles("", key, props, getProperties());
            } else if ("rowActions".equals(key) || key.startsWith("rowAction_")) {
                value += fillDatalistObjects(key, props, childtemplate, (Object[]) getDatalist().getRowActionPlaceholder(key), data);
            } else if ("columns".equals(key) || key.startsWith("column_")) {
                value += fillDatalistObjects(key, props, childtemplate, (Object[]) getDatalist().getColumnPlaceholder(key), data);
            } else if ("selector".equals(key)) {
                value += populateSelector(props, childtemplate, data);
            } else if (key.startsWith("is") && getProperties().containsKey(key)) { //to support simple if true checking
                if (getPropertyString(key).equalsIgnoreCase("true")) {
                    value = childtemplate;
                } 
            } else {
                continue;
            }
            template = template.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(value));
        }
        
        if (data != null) {
            //find normal variables
            Pattern pattern2 = Pattern.compile("\\{\\{([a-zA-Z0-9-_]+)(|.+?)\\}\\}");
            Matcher matcher2 = pattern2.matcher(template);

            while (matcher2.find()) {
                String replace = matcher2.group(0);
                String key = matcher2.group(1);
                String props = matcher2.group(2);
                String value = "";
                if ("body".equals(key)) {
                    if (data instanceof DataListColumn) {
                        DataListColumn col = (DataListColumn) data;
                        value = decorator.formatColumn(col, decorator.getCurrentRowObject(), decorator.evaluate(col.getName()));
                    } else if (data instanceof DataListAction) {
                        DataListAction a = (DataListAction) data;
                        value = decorator.generateLink(a);
                    } else {
                        value = (String) DataListService.evaluateColumnValueFromRow(data, key);
                    }
                } else {
                    value = (String) DataListService.evaluateColumnValueFromRow(data, key);
                }
                if (value != null) {
                    template = template.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(value));
                }
            }
        }
        
        return template;
    }
    
    public String fillRows(DataListCollection rows, String childtemplate) {
        String value = "";
        if (rows != null && !rows.isEmpty()) {
            int index = 0;
            for (Object r : rows) {
                decorator.initRow(r, index, index);
                value += fillData(childtemplate, r);
                index++;
            }
        }
        return value;
    }
    
    public String populateSelector(String props, String template, Object data) {
        String html = "";
        if (!getDatalist().getCheckboxPosition().equals(DataList.CHECKBOX_POSITION_NO)) {
            String key = getDatalist().getBinder().getPrimaryKeyColumnName();
            String param = getDatalist().getDataListEncodedParamName(CHECKBOX_PREFIX + key);
            String value = (String) DataListService.evaluateColumnValueFromRow(data, key);
            String type = "radio";
            String id = type + "_" + param + "_" + value;
            String header = "";
            if (!getDatalist().getSelectionType().equals(DataList.SELECTION_TYPE_SINGLE)) {
                type = "checkbox";
                header = "<label><input type='checkbox' onclick='toggleAll(this)'/><i></i></label>";
            }
            String body = "<label><input type=\""+type+"\" name=\"" + StringUtil.escapeString(param, StringUtil.TYPE_XML, null) + "\" ";
            body += "id=\"" + StringUtil.escapeString(id, StringUtil.TYPE_XML, null) + "\" ";
            body += "value=\"" + StringUtil.escapeString(value, StringUtil.TYPE_XML, null) + "\"/><i></i></label>";
            
            if (template != null && !template.isEmpty()) {
                Map<String, String> obj = new HashMap<String, String>();
                obj.put("body", body);
                obj.put("id", StringUtil.escapeString(id, StringUtil.TYPE_XML, null));
                obj.put("name", StringUtil.escapeString(param, StringUtil.TYPE_XML, null));
                obj.put("value", StringUtil.escapeString(value, StringUtil.TYPE_XML, null));
                obj.put("type", StringUtil.escapeString(type, StringUtil.TYPE_XML, null));
                obj.put("header", header);
                html = fillData(template, obj);
            } else {
                html = body;
            }
            html = applyStyles(html, "selector", props, null);
        }
        return html;
    }
    
    public String fillDatalistObjects(String key, String props, String template, Object[] objects, Object data) {
        if (objects == null || objects.length == 0) {
            return "";
        }
        
        String replace = "";
        String childtemplate = "";
        String value = "";
        
        if (template == null) {
            template = "";
        }
        
        String innerKey = StringUtil.escapeRegex(key.substring(0, key.length() -1));
        if (template.contains("{{"+innerKey+"}}") || template.contains("{{"+innerKey+" ")) {
            Pattern pattern = Pattern.compile("\\{\\{"+innerKey+"(|.+?)\\}\\}([\\s\\S]+?)\\{\\{"+innerKey+"\\}\\}", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(template);

            while (matcher.find()) {
                replace = matcher.group(0);
                childtemplate = matcher.group(2);
                break;
            }
            
            if (childtemplate.isEmpty()) {
                pattern = Pattern.compile("\\{\\{"+innerKey+"(|.+?)\\}\\}");
                matcher = pattern.matcher(template);

                while (matcher.find()) {
                    replace = matcher.group(0);
                    break;
                }
            }
        } else {
            replace = template;
            childtemplate = template;
        }
        
        if (childtemplate.isEmpty()) {
            childtemplate = "{{body}}";
        }
        if (objects != null) {
            for (Object o : objects) {
                String temp = fillData(childtemplate, o);
                temp = applyStyles(temp, key, props, o);
                value += " " + temp;
            }
        }
        template = template.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(value));
        
        return template;
    }
    
    public String applyStyles(String html, String key, String props, Object data) {
        html = html.trim();
        
        String cssClass = "ph_" + key + " ";
        String attrs = "";
        String stylesStr = "";
        String id = "";
        
        String styleProps = "";
        Map properties = null;
        
        if (props != null && !props.isEmpty()) {
            Pattern pattern = Pattern.compile("([a-zA-Z0-9-_]+)=\"(.+?)\"");
            Matcher matcher = pattern.matcher(props);
            while (matcher.find()) {
                String attr = matcher.group(0);
                String name = matcher.group(1);
                String value = matcher.group(2);
                if (name.startsWith("attr-")) {
                    if (name.equals("attr-class")) {
                        cssClass += value + " ";
                    } else if (name.equals("attr-style")) {
                        stylesStr += value;
                        if (!stylesStr.endsWith(";")) {
                            stylesStr += ";";
                        }
                    } else {
                        attrs += attr.substring(5) + " ";
                    }
                } else if (name.equals("data-cbuilder-style")) {
                    styleProps = value;
                }
            }
        }
        
        if (data != null) {
            if (data instanceof DataListColumn) {
                DataListColumn c = (DataListColumn) data;
                if (c.getStyle() != null && !c.getStyle().isEmpty()) {
                    stylesStr += c.getStyle();
                    if (!stylesStr.endsWith(";")) {
                        stylesStr += ";";
                    }
                }

                id = c.getPropertyString("id");
                cssClass += c.getPropertyString("id") + " ";
                if (c.isHidden()) {
                    cssClass += " column-hidden ";
                }
                if (html.startsWith("<th")) {
                    cssClass += "column_header column_" + c.getName().replaceAll(StringUtil.escapeRegex("."), StringUtil.escapeRegex("_")) + " header_"+id+" ";
                    if (c.getHeaderAlignment() != null && !c.getHeaderAlignment().isEmpty()) {
                        cssClass += c.getHeaderAlignment() + " ";
                    }
                } else {
                    cssClass += "column_body column_" + c.getName().replaceAll(StringUtil.escapeRegex("."), StringUtil.escapeRegex("_")) + " body_"+id+" ";
                    if (c.getAlignment() != null && !c.getAlignment().isEmpty()) {
                        cssClass += c.getAlignment() + " ";
                    }
                }
                if (c.getWidth() != null && !c.getWidth().isEmpty()) {
                    stylesStr += "width:"+c.getWidth()+";max-width:"+c.getWidth()+";";
                }
                properties = c.getProperties();
            } else if (data instanceof DataListAction) {
                DataListAction a = (DataListAction) data;
                id = a.getPropertyString("id");
                cssClass += a.getPropertyString("id") + " ";
                if (html.startsWith("<th")) {
                    cssClass += "rowaction_header header_"+id+" ";
                } else {
                    cssClass += "rowaction_body body_"+id+" ";
                }
                properties = a.getProperties();
            } else {
                properties = (Map) data;
            }
        }
        
        Pattern pattern = Pattern.compile("^<([a-zA-Z0-9]+)(| .+?)>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String replace = matcher.group(0);
            String tag = matcher.group(1);
            String tagAttrs = matcher.group(2);
            String result = replace;
            
            if (tagAttrs.contains("class=\"")) {
                result = result.replace("class=\"", "class=\"" + cssClass);
            } else if (tagAttrs.contains("class='")) {
                result = result.replace("class='", "class='" + cssClass);
            } else {
                attrs += "class=\""+cssClass+"\" ";
            }
            if (tagAttrs.contains("style=\"")) {
                result = result.replace("style=\"", "style=\"" + stylesStr);
            } else if (tagAttrs.contains("style='")) {
                result = result.replace("style='", "style='" + stylesStr);
            } else {
                attrs += "style=\""+stylesStr+"\" ";
            }
            if (!attrs.isEmpty()) {
                result = result.replace("<"+tag, "<"+tag+" "+attrs);
            }
            html = html.replace(replace, result);
        }

        // make the styleKey unique to the currently styled element
        // so that styling applies to all required elements instead of only the first one
        if (!id.isEmpty()) {
            key += "_" + id;
        }

        if (properties != null && !styleKeys.contains(key)) {
            if (styleProps.isEmpty()) {
                styleProps = "[{}]";
                if (data instanceof DataListAction) {
                    styleProps = "[{}, {prefix : 'link', class : 'a'}]";
                }
            }
            try {
                JSONArray arr = new JSONArray(styleProps);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String prefix = "";
                    String styleClass = "";

                    if (o.has("prefix")) {
                        prefix = o.getString("prefix");
                    }
                    if (!prefix.isEmpty()) {
                        prefix += "-";
                    }
                    if (o.has("class")) {
                        styleClass = o.getString("class");
                    }
                    
                    String listStyle = styleClass;
                    String listPrefix = prefix;
                    if (!html.isEmpty()) {
                        if (listStyle.contains(" ")) {
                            listStyle = listStyle.replaceFirst(" ", StringUtil.escapeRegex(".ph_"+key+" "));
                        } else {
                            listStyle += ".ph_" + key;
                        }
                        listPrefix = key + "-" + listPrefix;
                    }
                    JsonUtil.generateBuilderProperties(getDatalist().getProperties(), new String[]{listPrefix});
                    DataList.generateStyle(styles, getDatalist().getProperties(), ".dataList#dataList_"+datalist.getId()+" "+listStyle, listPrefix.toUpperCase().replace("-", "_"));
                    
                    if (!id.isEmpty()) {
                        if (styleClass.contains(" ")) {
                            styleClass = styleClass.replaceFirst(" ", StringUtil.escapeRegex("."+id+" "));
                        } else {
                            styleClass += "." + id;
                        }
                    }
                    JsonUtil.generateBuilderProperties(properties, new String[]{prefix});
                    DataList.generateStyle(styles, properties, ".dataList#dataList_"+datalist.getId()+" "+styleClass, prefix.toUpperCase().replace("-", "_"));
                }
                styleKeys.add(key);
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
        
        return html;
    }
}
