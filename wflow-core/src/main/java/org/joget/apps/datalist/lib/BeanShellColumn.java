package org.joget.apps.datalist.lib;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListDisplayColumnDefault;

public class BeanShellColumn extends DataListDisplayColumnDefault {

    @Override
    public String getName() {
        return "BeanShellColumn";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Executes standard Java syntax to display a cell value";
    }

    @Override
    public String getColumnHeader() {
        return getPropertyString("label");
    }

    @Override
    public String getRowValue(Object row, int index) {
        String script = getPropertyString("script");
        if (!script.isEmpty()) {
            Map properties = new HashMap();
            properties.put("column", this);
            properties.put("datalist", getDatalist());
            properties.put("row", row);
            properties.put("index", index);

            Object result = AppPluginUtil.executeScript(script, properties);
            if (result != null) {
                return result.toString();
            }
        }
        return "";
    }

    @Override
    public Boolean isRenderHtml() {
        return false;
    }

    @Override
    public String getLabel() {
        return "BeanShell";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/beanShellColumn.json", null, true, null);
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-code\"></i>";
    }
}