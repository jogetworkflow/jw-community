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
        return "3.0.0";
    }

    public String getDescription() {
        return "Data List Hyperlink Action";
    }
    
    public String getLabel() {
        return "Data List Hyperlink Action";
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
        String columnName = getHrefColumn();

        if (columnName != null && columnName.trim().length() > 0) {
            DataListCollection rows = dataList.getRows();
            String primaryKeyColumnName = dataList.getBinder().getPrimaryKeyColumnName();
            if (rows != null) {
                if (getHrefParam() != null && getHrefParam().trim().length() > 0) {
                    if (url.contains("?")) {
                        url += "&";
                    } else {
                        url += "?";
                    }
                    url += getHrefParam();
                    url += "=";
                } else {
                    if (!url.endsWith("/")) {
                        url += "/";
                    }
                }

                for (String key : rowKeys) {
                    url += getValue(rows, primaryKeyColumnName, key, columnName) + ";";
                }
                url = url.substring(0, url.length() - 1);
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
