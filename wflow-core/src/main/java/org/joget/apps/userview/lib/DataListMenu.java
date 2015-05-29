package org.joget.apps.userview.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class DataListMenu extends UserviewMenu {
    private DataList cacheDataList = null;

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

                // sanitize label
                String label = getPropertyString("label");
                if (label != null) {
                    label = StringUtil.stripHtmlRelaxed(label);
                }
            
                // generate menu link
                menuItem = "<a href=\"" + getUrl() + "\" class=\"menu-link default\"><span>" + label + "</span> <span class='rowCount'>(" + rowCount + ")</span></a>";
            }
        }
        return menuItem;
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getJspPage() {
        try {
            // get data list
            DataList dataList = getDataList();

            if (dataList != null) {
                //overide datalist result to use userview result
                DataListActionResult ac = dataList.getActionResult();
                if (ac != null) {
                    if (ac.getMessage() != null && !ac.getMessage().isEmpty()) {
                        setAlertMessage(ac.getMessage());
                    }
                    if (ac.getType() != null && DataListActionResult.TYPE_REDIRECT.equals(ac.getType()) &&
                            ac.getUrl() != null && !ac.getUrl().isEmpty()) {
                        if ("REFERER".equals(ac.getUrl())) {
                            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                            if (request != null && request.getHeader("Referer") != null) {
                                setRedirectUrl(request.getHeader("Referer"));
                            } else {
                                setRedirectUrl("REFERER");
                            }
                        } else {
                            setRedirectUrl(ac.getUrl());
                        }
                    }
                }

                // set data list
                setProperty("dataList", dataList);
            } else {
                setProperty("error", "Data List \"" + getPropertyString("datalistId") + "\" not exist.");
            }
        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message += "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            setProperty("error", message);
        }    
        return "userview/plugin/datalist.jsp";
    }

    protected DataList getDataList() throws BeansException {
        if (cacheDataList == null) {
            // get datalist
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            DataListService dataListService = (DataListService) ac.getBean("dataListService");
            DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) ac.getBean("datalistDefinitionDao");
            String id = getPropertyString("datalistId");
            AppDefinition appDef = appService.getAppDefinition(getRequestParameterString("appId"), getRequestParameterString("appVersion"));
            DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(id, appDef);

            if (datalistDefinition != null) {
                cacheDataList = dataListService.fromJson(datalistDefinition.getJson());

                if (getPropertyString(Userview.USERVIEW_KEY_NAME) != null && getPropertyString(Userview.USERVIEW_KEY_NAME).trim().length() > 0) {
                    cacheDataList.addBinderProperty(Userview.USERVIEW_KEY_NAME, getPropertyString(Userview.USERVIEW_KEY_NAME));
                }
                if (getKey() != null && getKey().trim().length() > 0) {
                    cacheDataList.addBinderProperty(Userview.USERVIEW_KEY_VALUE, getKey());
                }

                cacheDataList.setActionPosition(getPropertyString("buttonPosition"));
                cacheDataList.setSelectionType(getPropertyString("selectionType"));
                cacheDataList.setCheckboxPosition(getPropertyString("checkboxPosition"));
            }
        }
        return cacheDataList;
    }
}