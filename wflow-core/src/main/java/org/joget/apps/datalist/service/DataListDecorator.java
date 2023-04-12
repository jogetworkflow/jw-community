package org.joget.apps.datalist.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.CheckboxTableDecorator;
import org.displaytag.model.TableModel;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.tags.TableTagParameters;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListDisplayColumnProxy;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.mozilla.javascript.Scriptable;

/**
 * DisplayTag column decorator to modify columns e.g. format, add links, etc.
 */
public class DataListDecorator extends CheckboxTableDecorator {

    transient DataList dataList;
    // attributes from parent class to fix DisplayTag bug
    List checkedIds;
    String id;
    String fieldName;
    Boolean listRenderHtml = true;
    
    private int index = 0;

    public DataListDecorator() {
    }
    
    public DataListDecorator(DataList dataList) {
        this.dataList = dataList;
    }
    
    @Override
    public void init(PageContext pageContext, Object decorated, TableModel tableModel) {
        super.init(pageContext, decorated, tableModel);

        if (this.dataList == null) {
            this.dataList = (DataList) pageContext.findAttribute("dataList");
        }

        // set values to fix DisplayTag bug later
        if (fieldName != null) {
            String[] params = pageContext.getRequest().getParameterValues(fieldName);
            checkedIds = params != null ? new ArrayList(Arrays.asList(params)) : new ArrayList(0); // used to fix DisplayTag bug
        } else {
            checkedIds = new ArrayList(0);
        }
        
        String disableListRenderHtml = WorkflowUtil.getSystemSetupValue("disableListRenderHtml");
        if (disableListRenderHtml != null && disableListRenderHtml.equals("true")) {
            listRenderHtml = false;
        } 
    }

    @Override
    public void setId(String id) {
        // Override this method to fix DisplayTag bug
        super.setId(id);
        this.id = id;
    }

    @Override
    public void setFieldName(String fieldName) {
        // Override this method to fix DisplayTag bug
        super.setFieldName(fieldName);
        this.fieldName = fieldName;
    }

    @Override
    public String getCheckbox() {
        // Override this method to fix DisplayTag bug
        String evaluatedId = "";
        boolean checked = false;
        if (id != null) {
            evaluatedId = ObjectUtils.toString(evaluate(id));
            checked = checkedIds.contains(evaluatedId);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<label><input type=\"checkbox\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/><i></i></label>");

        return buffer.toString();
    }
    
    public String getRadio() {
        // Override this method to fix DisplayTag bug
        String evaluatedId = "";
        boolean checked = false;
        if (id != null) {
            evaluatedId = ObjectUtils.toString(evaluate(id));
            checked = checkedIds.contains(evaluatedId);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<label><input type=\"radio\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/><i></i></label>");

        return buffer.toString();
    }

    /**
     * Decorator method for a column to display links. TODO: formatting?
     * @param columnName
     * @return
     */
    public Object getColumn(String columnName) {
        Object row = getCurrentRowObject();
        Object columnValue = evaluate(columnName);
        DataListColumn column = findColumn(columnName);

        // handle formatting
        String text = formatColumn(column, row, columnValue);

        return text;
    }

    /**
     * Decorator method to display row actions as links
     * @return
     */
    public Object getActions() {
        String output = "";
        DataListAction[] actions = dataList.getRowActions();
        if (actions != null) {
            int i = 0;
            for (DataListAction action : actions) {
                String link = generateLink(action);
                
                if ("true".equals(dataList.getPropertyString("rowActionsMode")) || "dropdown".equals(dataList.getPropertyString("rowActionsMode"))) {
                    if (!link.isEmpty()) {
                        output += " <span class=\"row_action rowaction_body body_"+action.getPropertyString("id")+" " + action.getPropertyString("BUILDER_GENERATED_CSS") + "\">" + link + "</span> ";
                    }
                } else {
                    if (i > 0) {
                        String lastClass = "";
                        if (i == actions.length - 1) {
                            lastClass = "footable-last-column row_action_last";
                        }
                        output += "</td><td class=\"row_action rowaction_body "+lastClass+" body_"+action.getPropertyString("id")+" " + action.getPropertyString("BUILDER_GENERATED_CSS") + "\">";
                    }
                    
                    output += " <span class=\"row_action_inner\">" + link + "</span> ";
                }
                
                i++;
            }
        }
        
        if ("dropdown".equals(dataList.getPropertyString("rowActionsMode")) && !output.isEmpty()) {
            String btnStyle = actions[0].getPropertyString("link-css-display-type");
            if (btnStyle.isEmpty()) {
                btnStyle = "btn btn-sm btn-primary";
            }
            String label = dataList.getPropertyString("rowActionsDropdownLabel");
            if (label.isEmpty()) {
                label = ResourceBundleUtil.getMessage("dbuilder.rowActionsDropdownLabel.default");
            }
            output = output.replaceAll(StringUtil.escapeRegex("btn btn-sm btn-"), StringUtil.escapeRegex("xbtn xbtn-sm xbtn-")); //remove btn style
            output = "<div class=\"dropdown rowActionsDropdown\"><a data-toggle=\"dropdown\" class=\""+btnStyle+"\" href=\"javascript:;\">"+label+" <i class=\"fas fa-chevron-down\"></i></a><div class=\"dropdown-menu dropdown-menu-left rowActions\">"+output+"</div></div>";
        }
        
        return output;
    }
    
    protected DataListColumn findColumn(String columnName) {
        boolean skipHidden = false;
        String export =  dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTING);
        String exportType = dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTTYPE);
        if (("1".equals(export) && exportType != null && (exportType.equals("1") || exportType.equals("2") || exportType.equals("3") || exportType.equals("5")))) {
            skipHidden = true;
        }
        
        // get column, temporarily just iterate thru to find
        DataListColumn column = null;
        DataListColumn[] columns = dataList.getColumns();
        column = columns[index];
        if (index == columns.length - 1) {
            index = 0;
        } else {
            index++;
        }
        if (!column.getName().equals(columnName) && ((skipHidden && column.isHidden()) || (!column.isHidden() && "true".equals(column.getPropertyString("exclude_export"))))) {
            column = findColumn(columnName);
        }
        
        return column;
    }
    
    public String generateLink(DataListAction action) {
        String label = StringUtil.stripHtmlRelaxed(action.getLinkLabel());
        String link = "";
        if (isRowActionVisible(action)) {
            String linkCss = action.getPropertyString("cssClasses");

            if (!action.getPropertyString("BUILDER_GENERATED_LINK_CSS").isEmpty()) {
                linkCss = action.getPropertyString("BUILDER_GENERATED_LINK_CSS");
            }

            linkCss += " link_" + action.getPropertyString("id");

            link = generateLink(action.getHref(), action.getTarget(), action.getHrefParam(), action.getHrefColumn(), label, action.getConfirmation(), linkCss);
        }
        return link;
    }

    protected String generateLink(String href, String target, String hrefParam, String hrefColumn, String text, String confirmation) {
        return generateLink(href, target, hrefParam, hrefColumn, text, confirmation, "");
    }
    
    protected String generateLink(String href, String target, String hrefParam, String hrefColumn, String text, String confirmation, String cssClasses) {
        return generateLink(getCurrentRowObject(), href, target, hrefParam, hrefColumn, text, confirmation, cssClasses);
    }
    
    public static String generateLink(Object row, String href, String target, String hrefParam, String hrefColumn, String text, String confirmation, String cssClasses) {
        // add links
        String link = href;
        String targetString = "";
        String confirmationString = "";
        String arialLabel= "";

        if (link == null || text == null || text.isEmpty()) {
            link = text;
        } else {
            if (hrefParam != null && hrefColumn != null && !hrefColumn.isEmpty()) {
                String[] params = hrefParam.split(";");
                String[] columns = hrefColumn.split(";");
                
                for (int i = 0; i < columns.length; i++ ) {
                    if (columns[i] != null && !columns[i].isEmpty()) {
                        boolean isValid = false;
                        if (params.length > i && params[i] != null && !params[i].isEmpty()) {
                            if (link.contains("?")) {
                                link += "&";
                            } else {
                                link += "?";
                            }
                            link += StringEscapeUtils.escapeHtml(params[i]);
                            link += "=";
                            isValid = true;
                        } else if (!link.contains("?")) {
                            if (!link.endsWith("/")) {
                                link += "/";
                            }
                            isValid = true;
                        }
                        
                        if (isValid) {
                            Object paramValue = DataListService.evaluateColumnValueFromRow(row, columns[i]);
                            if (paramValue == null) {
                                paramValue = StringEscapeUtils.escapeHtml(columns[i]);
                            }
                            try {
                                link += (paramValue != null) ? URLEncoder.encode(paramValue.toString(), "UTF-8") : null;
                            } catch (UnsupportedEncodingException ex) {
                                link += paramValue;
                            }
                        }
                    }
                }
            }
            
            if (link.contains("{") && link.contains("}")) {
                Pattern pattern = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");
                Matcher matcher = pattern.matcher(link);

                while (matcher.find()) {
                    String replace = matcher.group(0);
                    String column = matcher.group(1);
                    
                    Object value = DataListService.evaluateColumnValueFromRow(row, column);
                    if (value != null) {
                        String temp = StringUtil.escapeString(value.toString(), StringUtil.TYPE_URL, null);
                        link = link.replaceAll(StringUtil.escapeRegex(replace), StringUtil.escapeRegex(temp));
                    }
                }
            }
            
            if (target != null && "popup".equalsIgnoreCase(target)) {
                if (confirmation == null) {
                    confirmation = "";
                }
                confirmation = StringUtil.stripAllHtmlTag(confirmation);
                targetString = "onclick=\"return dlPopupAction(this, '" + StringUtil.escapeString(confirmation, StringUtil.TYPE_JAVASCIPT, null) + "')\"";
            } else if (target != null && "post".equalsIgnoreCase(target)) {
                if (confirmation == null) {
                    confirmation = "";
                }
                confirmation = StringUtil.stripAllHtmlTag(confirmation);
                targetString = "onclick=\"return dlPostAction(this, '" + StringUtil.escapeString(confirmation, StringUtil.TYPE_JAVASCIPT, null) + "')\"";
            } else {
                if (target != null && target.trim().length() > 0) {
                    targetString = " target=\"" + target + "\"";
                }
                if (confirmation != null && confirmation.trim().length() > 0) {
                    confirmation = StringUtil.stripAllHtmlTag(confirmation);
                    confirmationString = " onclick=\"return confirm('" + StringUtil.escapeString(confirmation, StringUtil.TYPE_JAVASCIPT, null) + "')\"";
                }
            }
            if (StringUtil.stripAllHtmlTag(text).isEmpty()) {
                arialLabel = " aria-label=\"link\"";
            }
            link = "<a href=\"" + link + "\"" + targetString + confirmationString + arialLabel + " class=\""+cssClasses+"\">" + text + "</a>";
            }
        return link;
    }

    public String formatColumn(DataListColumn column, Object row, Object value) {
        Object result = value;
        
        if (column instanceof DataListDisplayColumnProxy) {
            result = ((DataListDisplayColumnProxy) column).getRowValue(row, getViewIndex());
        }
        
        // decrypt protected data 
        if (result != null && result instanceof String) {
            result = SecurityUtil.decrypt(result.toString());
            
            // sanitize output
            String export =  dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTING);
            String exportType = dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTTYPE);
            if (!("1".equals(export) && exportType != null && (exportType.equals("1") || exportType.equals("2") || exportType.equals("3") || exportType.equals("5")))) {
                if (isRenderHtml(column)) {
                    result = StringUtil.stripHtmlRelaxed(result.toString());
                } else {
                    result = StringEscapeUtils.escapeHtml(result.toString());
                }
            }
        }

        Collection<DataListColumnFormat> formats = column.getFormats();
        if (formats != null) {
            for (DataListColumnFormat format : formats) {
                if (format != null) {
                    result = format.format(dataList, column, row, result);
                }
            }
        }

        String text = (result != null) ? result.toString() : null;
        
        // strip tags if media type is not HTML
        if (text != null) {
            if (tableModel != null && !MediaTypeEnum.HTML.equals(tableModel.getMedia())) {
                text = StringUtil.stripAllHtmlTag(text, false);
                text = StringEscapeUtils.unescapeXml(text);
            } else {
                if (text.equals(StringUtil.stripAllHtmlTag(text, false))) { //check it is not html text
                    text = StringUtil.escapeString(text, StringUtil.TYPE_NL2BR, null);
                }
            }
        }

        // handle links
        DataListAction action = dataList.getColumnAction(column);
        if (text != null && action != null && action.getHref() != null && action.getHref().trim().length() > 0 && (tableModel == null || MediaTypeEnum.HTML.equals(tableModel.getMedia()))) {
            String href = action.getHref();
            String target = action.getTarget();
            String hrefParam = (action.getHrefParam() != null && action.getHrefParam().trim().length() > 0) ? action.getHrefParam() : "";
            String hrefColumn = (action.getHrefColumn() != null && action.getHrefColumn().trim().length() > 0) ? action.getHrefColumn() : "";
            String confirm = action.getConfirmation();
            String link = generateLink(href, target, hrefParam, hrefColumn, text.toString(), confirm, action.getPropertyString("cssClasses"));
            text = link;
        }
        
        return text;
    }
    
    @Override
    public Object evaluate(String propertyName) {
        return DataListService.evaluateColumnValueFromRow(getCurrentRowObject(), propertyName);
    }
    
    protected boolean isRowActionVisible(DataListAction rowAction) {
        boolean visible = true;
        
        Object[] rules = (Object[]) rowAction.getProperty("rules");
        if (rules != null && rules.length > 0) {
            String visibleRules = "";
            
            for (Object o : rules) {
                Map ruleMap = (HashMap) o;
                
                String join = ruleMap.get("join").toString();
                String fieldId = ruleMap.get("field").toString();
                String operator = ruleMap.get("operator").toString();
                String compareValue = ruleMap.get("value").toString();
                Object tempValue = evaluate(fieldId);
                String value = (tempValue != null)?tempValue.toString():"";
                
                if (!visibleRules.isEmpty() && !visibleRules.endsWith("(") && !")".equals(fieldId)) {
                    if ("OR".equals(join)) {
                        visibleRules += " || ";
                    } else {
                        visibleRules += " && ";
                    }
                }
                if (!")".equals(fieldId)) {
                    visibleRules += " ";
                }
                if ("(".equals(fieldId) || ")".equals(fieldId) ) {
                    visibleRules += fieldId;
                } else {
                    boolean result = false;
                    if (value != null) {
                        if ("=".equals(operator) && compareValue.equals(value)) {
                            result = true;
                        } else if ("<>".equals(operator) && !compareValue.equals(value)) {
                            result = true;
                        } else if (">".equals(operator) || "<".equals(operator) || ">=".equals(operator) || "<=".equals(operator)) {
                            try {
                                double valueNum = Double.parseDouble(value);
                                double compareValueNum = Double.parseDouble(compareValue);

                                if (">".equals(operator) && valueNum > compareValueNum) {
                                    result = true;
                                } else if ("<".equals(operator) && valueNum < compareValueNum) {
                                    result = true;
                                } else if (">=".equals(operator) && valueNum >= compareValueNum) {
                                    result = true;
                                } else if ("<=".equals(operator) && valueNum <= compareValueNum) {
                                    result = true;
                                }
                            } catch (NumberFormatException e) {}
                        } else if ("LIKE".equals(operator) && value.toLowerCase().contains(compareValue.toLowerCase())) {
                            result = true;
                        } else if ("NOT LIKE".equals(operator) && !value.toLowerCase().contains(compareValue.toLowerCase())) {
                            result = true;
                        } else if ("IN".equals(operator) || "NOT IN".equals(operator)) {
                            String[] compareValues = compareValue.split(";");
                            List<String> compareValuesList = new ArrayList<String>(Arrays.asList(compareValues));
                            if ("IN".equals(operator) && compareValuesList.contains(value)) {
                                result = true;
                            } else if ("NOT IN".equals(operator) && !compareValuesList.contains(value)) {
                                result = true;
                            }
                        } else if ("IS TRUE".equals(operator) || "IS FALSE".equals(operator)) {
                            try {
                                boolean valueBoolean = Boolean.parseBoolean(value);

                                if ("IS TRUE".equals(operator) && valueBoolean) {
                                    result = true;
                                } else if ("IS FALSE".equals(operator) && !valueBoolean) {
                                    result = true;
                                }
                            } catch(Exception e) {}
                        } else if ("IS NOT EMPTY".equals(operator)) {
                            result = true;
                        } else if ("REGEX".equals(operator) || "NOT REGEX".equals(operator)) {
                            if (value.matches(StringEscapeUtils.unescapeJavaScript(compareValue))) {
                                if ("REGEX".equals(operator)) {
                                    result = true;
                                }
                            } else {
                                if ("NOT REGEX".equals(operator)) {
                                    result = true;
                                }
                            }
                        }
                    } else if ("IS EMPTY".equals(operator)) {
                        result = true;
                    }
                    
                    visibleRules += result;
                }
            }
            
            org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
            Scriptable scope = cx.initStandardObjects(null);
            try {
                visible = (Boolean) cx.evaluateString(scope, visibleRules, "", 1, null);
            } finally {
                org.mozilla.javascript.Context.exit();
            }
        }
        
        return visible;
    }
    
    protected boolean isRenderHtml(DataListColumn column) {
        if (column != null && column.isRenderHtml() != null) {
            return column.isRenderHtml();
        } else {
            return listRenderHtml;
        }
    }
}
