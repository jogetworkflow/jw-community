package org.joget.apps.datalist.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.displaytag.util.LookupUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.service.FormUtil;

/**
 * Hyperlink action for a datalist
 */
public class HyperlinkDataListAction extends DataListActionDefault {

    public String getName() {
        return "Data List Hyperlink Action";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Data List Hyperlink Action";
    }
    
    public String getLabel() {
        return "Hyperlink";
    }
    
    @Override
    public String getIcon() {
        return "<i class=\"fas fa-link\"></i>";
    }

    public String getLinkLabel() {
        String label = getPropertyString("label");
        if (label == null || label.isEmpty()) {
            label = "Hyperlink";
        }
        return label;
    }

    public String getHref() {
        return getPropertyString("href");
    }

    public String getTarget() {
        if ("iframe".equals(getPropertyString("target"))) {
            return getPropertyString("iframename");
        }
        return getPropertyString("target");
    }

    public String getHrefParam() {
        return getPropertyString("hrefParam");
    }

    public String getHrefColumn() {
        return getPropertyString("hrefColumn");
    }

    public String getConfirmation() {
        return getPropertyString("confirmation");
    }

    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        DataListActionResult result = new DataListActionResult();
        result.setType(DataListActionResult.TYPE_REDIRECT);
        String url = getHref();
        String hrefParam = getHrefParam();
        String hrefColumn = getHrefColumn();
        
        if (hrefParam != null && hrefColumn != null && !hrefColumn.isEmpty() && rowKeys != null && rowKeys.length > 0) {
            DataListCollection rows = dataList.getRows();
            String primaryKeyColumnName = dataList.getBinder().getPrimaryKeyColumnName();
        
            String[] params = hrefParam.split(";");
            String[] columns = hrefColumn.split(";");

            for (int i = 0; i < columns.length; i++) {
                if (columns[i] != null && !columns[i].isEmpty()) {
                    boolean isValid = false;
                    if (params.length > i && params[i] != null && !params[i].isEmpty()) {
                        if (url.contains("?")) {
                            url += "&";
                        } else {
                            url += "?";
                        }
                        url += params[i];
                        url += "=";
                        isValid = true;
                    } else {
                        if (!url.endsWith("/")) {
                            url += "/";
                        }
                        isValid = true;
                    }

                    if (isValid) {
                        for (String key : rowKeys) {
                            url += getValue(rows, primaryKeyColumnName, key, columns[i]) + ";";
                        }
                        url = url.substring(0, url.length() - 1);
                    }
                }
            }
        }
        
        result.setUrl(url);

        return result;
    }

    public String getPropertyOptions() {
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/hyperlinkDataListAction.json", null, true, "message/datalist/hyperlinkDataListAction");
        return json;
    }

    protected String getValue(DataListCollection rows, String primaryKeyColumnName, String key, String columnName) {
        String paramValue = "";
        
        try {
            if (primaryKeyColumnName != null && primaryKeyColumnName.equals(columnName)) {
                paramValue = key;
            } else {
                for (Object r : rows) {
                    Object id = LookupUtil.getBeanProperty(r, primaryKeyColumnName);
                    if (id != null && id.toString().equals(key)) {
                        paramValue = LookupUtil.getBeanProperty(r, columnName).toString();
                        break;
                    }
                }
            }
        
            return (paramValue != null) ? URLEncoder.encode(paramValue, "UTF-8") : null;
        } catch (Exception ex) {
            return paramValue;
        }
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}
