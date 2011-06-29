package org.joget.apps.datalist.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Hyperlink action for a datalist
 */
public class HyperlinkDataListAction extends DataListActionDefault implements PropertyEditable {

    @Override
    public String getName() {
        return "Data List Hyperlink";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Data List Hyperlink Action";
    }

    @Override
    public String getLabel() {
        String label = getProperty("label");
        if (label == null || label.isEmpty()) {
            label = "Hyperlink";
        }
        return label;
    }

    @Override
    public String getHref() {
        return getProperty("href");
    }

    @Override
    public String getTarget() {
        return getProperty("target");
    }

    @Override
    public String getHrefParam() {
        return getProperty("hrefParam");
    }

    @Override
    public String getHrefColumn() {
        return getProperty("hrefColumn");
    }

    @Override
    public String getConfirmation() {
        return getProperty("confirmation");
    }

    @Override
    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        DataListActionResult result = new DataListActionResult();
        result.setType(DataListActionResult.TYPE_REDIRECT);
        String url = getHref();
        String columnName = getHrefColumn();

        if (columnName != null && columnName.trim().length() > 0) {
            Form form = getSelectedForm();
            if (form != null) {
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
                    url += getValue(form, key, columnName) + ";";
                }
                url = url.substring(0, url.length() - 1);
            }
        }

        result.setUrl(url);

        return result;
    }

    @Override
    public String getPropertyOptions() {
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/hyperlinkDataListAction.json", null, true, "message/datalist/hyperlinkDataListAction");
        return json;
    }

    @Override
    public String getDefaultPropertyValues() {
        return "{label:'Hyperlink'}";
    }

    protected Form getSelectedForm() {
        Form form = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        String formDefId = getProperties().getProperty("formDefId");
        if (formDefId != null) {
            form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formDefId, null, null, null, null, null, null);
        }
        return form;
    }

    protected String getValue(Form form, String key, String columnName) {
        FormDataDao formDataDao = (FormDataDao) FormUtil.getApplicationContext().getBean("formDataDao");
        FormRow row = formDataDao.load(form, key);

        String paramValue = "";
        if (FormUtil.PROPERTY_ID.equals(columnName)) {
            paramValue = row.getId();
        } else {
            paramValue = (String) row.getCustomProperties().get(columnName);
        }

        try {
            return (paramValue != null) ? URLEncoder.encode(paramValue, "UTF-8") : null;
        } catch (UnsupportedEncodingException ex) {
            return paramValue;
        }
    }
}
