package org.joget.apps.userview.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class DataListMenu extends UserviewMenu implements PwaOfflineValidation {
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
        return "<i class=\"fas fa-table\"></i>";
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    public String getName() {
        return "Data List Menu";
    }

    public String getVersion() {
        return "5.0.0";
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
                int rowCount = dataList.getTotal();

                // sanitize label
                String label = getPropertyString("label");
                if (label != null) {
                    label = StringUtil.stripHtmlRelaxed(label);
                }
            
                // generate menu link
                menuItem = "<a href=\"" + getUrl() + "\" class=\"menu-link default\"><span>" + label + "</span> <span class='pull-right badge rowCount'>" + rowCount + "</span></a>";
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
                            if (ac.getUrl().startsWith("?")) {
                                ac.setUrl(getUrl() + ac.getUrl());
                            }
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
    
    @Override
    public String getOfflineOptions() {
        String options = super.getOfflineOptions();
        options += ", {name : 'cacheListAction', label : '@@userview.offline.cacheListAction@@', type : 'checkbox', options : [{value : 'true', label : ''}]}";
        options += ", {name : 'cacheAllLinks', label : '@@userview.offline.cacheList@@', type : 'checkbox', options : [{value : 'true', label : ''}]}";
        
        return options;
    }
    
    @Override
    public Set<String> getOfflineCacheUrls() {
        if ("true".equalsIgnoreCase(getPropertyString("enableOffline"))) {
            Set<String> urls = super.getOfflineCacheUrls();
            
            if ("true".equalsIgnoreCase(getPropertyString("cacheListAction")) || "true".equalsIgnoreCase(getPropertyString("cacheAllLinks"))) {
                DataList dataList = getDataList();
                urls.addAll(UserviewUtil.getDatalistCacheUrls(dataList, "true".equalsIgnoreCase(getPropertyString("cacheListAction")), "true".equalsIgnoreCase(getPropertyString("cacheAllLinks"))));
            }
            
            return urls;
        }
        return null;
    }
    
    @Override
    public Map<WARNING_TYPE, String[]> validation() {
        boolean checkAction = false;
        if ("true".equalsIgnoreCase(getPropertyString("cacheListAction")) || "true".equalsIgnoreCase(getPropertyString("cacheAllLinks"))) {
            checkAction = true;
        }
        if (!DataListService.pwaOfflineValidation(AppUtil.getCurrentAppDefinition(), getPropertyString("datalistId"), checkAction)) {
            Map<WARNING_TYPE, String[]> warning = new HashMap<WARNING_TYPE, String[]>();
            warning.put(WARNING_TYPE.NOT_SUPPORTED, new String[]{ResourceBundleUtil.getMessage("pwa.listContainsElementNotCompatible")});
            return warning;
        }
        return null;
    }
}