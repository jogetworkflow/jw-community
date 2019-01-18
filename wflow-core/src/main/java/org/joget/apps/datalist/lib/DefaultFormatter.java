package org.joget.apps.datalist.lib;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormRow;
import org.joget.commons.util.LogUtil;
import org.springframework.context.ApplicationContext;

public class DefaultFormatter extends DataListColumnFormatDefault {

    @Override
    public String getName() {
        return "Default Formatter";
    }

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        String formDefId = getPropertyString("formDefId");
        String field = getPropertyString("field");

        if (!formDefId.isEmpty() && !field.isEmpty() && value != null) {
            try {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                ApplicationContext ac = AppUtil.getApplicationContext();
                AppService appService = (AppService) ac.getBean("appService");
                if (formDefId != null) {
                    String tableName = appService.getFormTableName(appDef, formDefId);

                    FormDataDao dao = (FormDataDao) ac.getBean("formDataDao");

                    FormRow formRow = dao.load(formDefId, tableName, value.toString());

                    if (formRow != null && formRow.getCustomProperties() != null && formRow.getProperty(field) != null) {
                        value = formRow.getProperty(field);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(this.getClass().getName(), e, "Get Foreign Key Error!");
            }
        }

        String result = (value != null) ? value.toString() : null;

        if (result != null) {
            String formatting = getPropertyString("formatting");
            if (!formatting.isEmpty()) {
                String formats[] = formatting.split(";");

                for (String format : formats) {
                    result = "<" + format + ">" + result + "</" + format + ">";
                }
            }

            String size = getPropertyString("size");
            String color = getPropertyString("color");

            if (!size.isEmpty() || !color.isEmpty()) {
                String style = "";

                if (!size.isEmpty()) {
                    style += "font-size:" + size + ";";
                }
                if (!color.isEmpty()) {
                    style += "color:" + color + ";";
                }

                result = "<span style=\"" + style + "\">" + result + "</span>";
            }
        }
        return result;
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Default Formatter";
    }

    public String getLabel() {
        return "Default Formatter";
    }

    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/defaultFormatter.json", arguments, true, "message/datalist/defaultFormatter");
        return json;
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}
