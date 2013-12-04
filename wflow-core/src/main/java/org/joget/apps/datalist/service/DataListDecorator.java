package org.joget.apps.datalist.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.CheckboxTableDecorator;
import org.displaytag.model.TableModel;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.tags.TableTagParameters;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;

/**
 * DisplayTag column decorator to modify columns e.g. format, add links, etc.
 */
public class DataListDecorator extends CheckboxTableDecorator {

    transient DataList dataList;
    // attributes from parent class to fix DisplayTag bug
    List checkedIds;
    String id;
    String fieldName;
    
    private int index = 0;

    @Override
    public void init(PageContext pageContext, Object decorated, TableModel tableModel) {
        super.init(pageContext, decorated, tableModel);

        this.dataList = (DataList) pageContext.findAttribute("dataList");

        // set values to fix DisplayTag bug later
        if (fieldName != null) {
            String[] params = pageContext.getRequest().getParameterValues(fieldName);
            checkedIds = params != null ? new ArrayList(Arrays.asList(params)) : new ArrayList(0); // used to fix DisplayTag bug
        } else {
            checkedIds = new ArrayList(0);
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
        buffer.append("<input type=\"checkbox\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/>");

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
        buffer.append("<input type=\"radio\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/>");

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

        // strip tags if media type is not HTML
        boolean renderHtml = column.isRenderHtml();
        if (renderHtml && text != null && !MediaTypeEnum.HTML.equals(tableModel.getMedia())) {
            text = StringUtil.stripAllHtmlTag(text);
        }

        // handle links
        DataListAction action = column.getAction();
        if (text != null && action != null && action.getHref() != null && action.getHref().trim().length() > 0 && MediaTypeEnum.HTML.equals(tableModel.getMedia())) {
            String href = action.getHref();
            String target = action.getTarget();
            String hrefParam = (action.getHrefParam() != null && action.getHrefParam().trim().length() > 0) ? action.getHrefParam() : "";
            String hrefColumn = (action.getHrefColumn() != null && action.getHrefColumn().trim().length() > 0) ? action.getHrefColumn() : "";
            String confirm = action.getConfirmation();
            String link = generateLink(href, target, hrefParam, hrefColumn, text.toString(), confirm);
            text = link;
        }

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
            for (DataListAction action : actions) {
                String label = StringUtil.stripHtmlRelaxed(action.getLinkLabel());
                String link = generateLink(action.getHref(), action.getTarget(), action.getHrefParam(), action.getHrefColumn(), label, action.getConfirmation());
                output += " " + link + " </td><td class=\"row_action\"> ";
            }
            output = output.substring(0, output.length() - 30);
        }
        return output;
    }

    protected DataListColumn findColumn(String columnName) {
        // get column, temporarily just iterate thru to find
        DataListColumn column = null;
        DataListColumn[] columns = dataList.getColumns();
        column = columns[index];
        if (index == columns.length - 1) {
            index = 0;
        } else {
            index++;
        }
        
        return column;
    }

    protected String generateLink(String href, String target, String hrefParam, String hrefColumn, String text, String confirmation) {
        // add links
        String link = href;
        String targetString = "";
        String confirmationString = "";

        if (link == null) {
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
                        } if (!link.contains("?")) {
                            if (!link.endsWith("/")) {
                                link += "/";
                            }
                            isValid = true;
                        }
                        
                        if (isValid) {
                            Object paramValue =evaluate(columns[i]);
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
            
            if (target != null && "popup".equalsIgnoreCase(target)) {
                if (confirmation == null) {
                    confirmation = "";
                }
                StringUtil.stripAllHtmlTag(confirmation);
                targetString = "onclick=\"return dlPopupAction(this, '" + confirmation + "')\"";
            } else {
                if (target != null && target.trim().length() > 0) {
                    targetString = " target=\"" + target + "\"";
                }
                if (confirmation != null && confirmation.trim().length() > 0) {
                    StringUtil.stripAllHtmlTag(confirmation);
                    confirmationString = " onclick=\"return confirm('" + confirmation + "')\"";
                }
            }
            link = "<a href=\"" + link + "\"" + targetString + confirmationString + ">" + text + "</a>";
        }
        return link;
    }

    protected String formatColumn(DataListColumn column, Object row, Object value) {
        Object result = value;
        
        // decrypt protected data 
        if (result != null && result instanceof String) {
            result = SecurityUtil.decrypt(result.toString());
            
            // sanitize output
            if (dataList.getDataListParam(TableTagParameters.PARAMETER_EXPORTTYPE) == null) {
                boolean renderHtml = column.isRenderHtml();
                if (renderHtml) {
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
        return text;
    }
}
