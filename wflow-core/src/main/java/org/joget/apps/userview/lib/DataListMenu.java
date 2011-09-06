package org.joget.apps.userview.lib;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class DataListMenu extends UserviewMenu implements PluginWebSupport {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "List";
    }

    @Override
    public String getIcon() {
        return "/plugin/org.joget.apps.userview.lib.DataListMenu/images/grid_icon.gif";
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    public String getName() {
        return "Data List Menu";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/dataListMenu.json", arguments, true, "message/userview/dataListMenu");
        return json;
    }

    @Override
    public String getDecoratedMenu() {
        String menuItem = null;
        boolean showRowCount = Boolean.valueOf(getPropertyString("rowCount")).booleanValue();
        if (showRowCount) {
            // get datalist and row count
            DataList dataList = getDataList();
            if (dataList != null) {
                int rowCount = dataList.getSize();

                // generate menu link
                menuItem = "<a href=\"" + getUrl() + "\" class=\"menu-link default\"><span>" + getPropertyString("label") + "</span> <span class='rowCount'>(" + rowCount + ")</span></a>";
            }
        }
        return menuItem;
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("getOptions".equals(action)) {
            String appId = request.getParameter("appId");
            String appVersion = request.getParameter("appVersion");
            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                AppService appService = (AppService) ac.getBean("appService");
                AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
                Collection<DatalistDefinition> datalistDefList = appDef.getDatalistDefinitionList();

                Map<String, String> empty = new HashMap<String, String>();
                empty.put("value", "");
                empty.put("label", "");
                jsonArray.put(empty);

                for (DatalistDefinition d : datalistDefList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", d.getId());
                    option.put("label", d.getName());
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Datalist's options Error!");
            }
        }
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getJspPage() {
        // get data list
        DataList dataList = getDataList();

        // set data list
        setProperty("dataList", dataList);
        return "userview/plugin/datalist.jsp";
    }

    protected DataList getDataList() throws BeansException {
        // get datalist
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        DataListService dataListService = (DataListService) ac.getBean("dataListService");
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) ac.getBean("datalistDefinitionDao");
        String id = getPropertyString("datalistId");
        AppDefinition appDef = appService.getAppDefinition(getRequestParameterString("appId"), getRequestParameterString("appVersion"));
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(id, appDef);
        DataList dataList = dataListService.fromJson(datalistDefinition.getJson());
        
        if (getPropertyString(Userview.USERVIEW_KEY_NAME) != null && getPropertyString(Userview.USERVIEW_KEY_NAME).trim().length() > 0) {
            dataList.addBinderProperty(Userview.USERVIEW_KEY_NAME, getPropertyString(Userview.USERVIEW_KEY_NAME));
        }
        if (getKey() != null && getKey().trim().length() > 0) {
            dataList.addBinderProperty(Userview.USERVIEW_KEY_VALUE, getKey());
        }
        
        return dataList;
    }
}
