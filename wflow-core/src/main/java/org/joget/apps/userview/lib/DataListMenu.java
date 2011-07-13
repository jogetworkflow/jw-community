package org.joget.apps.userview.lib;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.map.ListOrderedMap;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.service.DataListDecorator;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class DataListMenu extends UserviewMenu implements PluginWebSupport {

    public static final String PREFIX_SELECTED = "selected_";
    public static final String PREFIX_BINDER_PROPERTY = "binder_";

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
        return "Datalist";
    }

    public String getVersion() {
        return "1.0.0";
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

    public String getDefaultPropertyValues() {
        return "";
    }

    @Override
    public String getDecoratedMenu() {
        String menuItem = null;
        boolean showRowCount = Boolean.valueOf(getPropertyString("rowCount")).booleanValue();
        if (showRowCount) {
            // get datalist and row count
            DataList dataList = getDataList();
            DataListBinder binder = dataList.getBinder();
            if (binder != null) {
                Properties binderProperties = getBinderProperties(dataList);
                int rowCount = binder.getDataTotalRowCount(dataList, binderProperties, null, null);

                // generate menu link
                String menuItemId = getPropertyString("customId");
                if (menuItemId == null || menuItemId.trim().isEmpty()) {
                    menuItemId = getPropertyString("id");
                }
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
        String action = getRequestParameterString("action");
        if (action != null && !action.trim().isEmpty()) {
            DataListActionResult result = handleAction();
            if (result != null) {
                setProperty("actionResult", result);
            }
        }

        viewList();
        return "userview/plugin/datalist.jsp";
    }

    protected void viewList() {
        // get parameters
        String id = getPropertyString("datalistId");
        String sortParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_SORT);
        String orderParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_ORDER);
        String pageParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_PAGE);
        String filterNameParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_NAME);
        String filterValueParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_VALUE);
        String filterOptionParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_OPTION);
        String exportParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_EXPORTTYPE);
        String sort = getRequestParameterString(sortParam);
        String order = getRequestParameterString(orderParam);
        String filterName = getRequestParameterString(filterNameParam);
        String filterValue = getRequestParameterString(filterValueParam);
        String filterOption = getRequestParameterString(filterOptionParam);
        String export = getRequestParameterString(exportParam);

        try {
            // get data list
            //DataList dataList = dataListService.getDataList(id);
            DataList dataList = getDataList();

            // parse JSON from request if available
            //dataList = parseFromJsonParameter(map, dataList, id, request);

            // determine sort column
            String sortColumn = null;
            if (sort != null && !sort.trim().isEmpty()) {
                int sortIndex = Integer.parseInt(sort) - 1;
                DataListColumn[] columns = dataList.getColumns();
                if (sortIndex < columns.length) {
                    sortColumn = columns[sortIndex].getName();
                }
            }

            // determine order
            String dir = null;
            if ("2".equals(order)) {
                dir = "asc";
            } else if ("1".equals(order)) {
                dir = "desc";
            }

            // determine start and size
            String page = getRequestParameterString(pageParam);
            int pageSize = dataList.getPageSize();
            int start = 0;
            if (export == null) {
                if (page != null && page.trim().length() > 0) {
                    start = (Integer.parseInt(page) - 1) * pageSize;
                }
            } else {
                // exporting, set full list
                pageSize = DataList.MAXIMUM_PAGE_SIZE;
            }

            // determine filter name and value
            if (filterValue == null || filterValue.trim().length() == 0) {
                if (filterOption != null && filterOption.trim().length() > 0) {
                    StringTokenizer st = new StringTokenizer(filterOption, DataListFilter.FILTER_OPTION_DELIMITER);
                    if (st.hasMoreTokens()) {
                        filterName = st.nextToken();
                        filterValue = (st.hasMoreTokens()) ? st.nextToken() : "";
                    }
                }
            }

            // set filter param names
            setProperty("filterNameParam", filterNameParam);
            setProperty("filterValueParam", filterValueParam);
            setProperty("filterOptionParam", filterOptionParam);
            setProperty("filterNameParamValue", filterName);
            setProperty("filterValueParamValue", filterValue);
            setProperty("filterOptionParamValue", filterOption);

            // set data list
            setProperty("dataList", dataList);

            DataListBinder binder = dataList.getBinder();
            if (binder == null) {
                String message = "No binder or colums specified";
                setProperty("error", message);
                return;
            }
            Properties binderProperties = getBinderProperties(dataList);

            // set data rows
            Boolean desc = null;
            if (dir != null) {
                desc = ("desc".equals(dir)) ? Boolean.TRUE : Boolean.FALSE;
            }
            DataListCollection rows = binder.getData(dataList, binderProperties, filterName, filterValue, sortColumn, desc, start, pageSize);
            setProperty("dataListRows", rows);
            setProperty("dataListSize", rows.getFullListSize());
            setProperty("dataListPageSize", pageSize);

            // set filters
            Collection<DataListFilter> filterList = new ArrayList<DataListFilter>();
            DataListFilter[] filters = dataList.getFilters();
            if (filters != null && filters.length > 0) {
                filterList.addAll(Arrays.asList(filters));
            }
            Map<String, String> textfieldFilterMap = new ListOrderedMap(); // filter with open options (textfield)
            Map<String, String> selectBoxFilterMap = new ListOrderedMap(); // filter with fixed options (selectbox)
            for (DataListFilter filter : filterList) {
                if (filter.getOptions() != null) {
                    // add selectbox option header
                    selectBoxFilterMap.put(DataListFilter.FILTER_HEADER, filter.getLabel());
                    String[][] options = filter.getOptions();
                    for (int i = 0; i < options.length; i++) {
                        String key = options[i][0];
                        String value = options[i][1];
                        selectBoxFilterMap.put(filter.getName() + DataListFilter.FILTER_OPTION_DELIMITER + key, value);
                    }
                } else {
                    textfieldFilterMap.put(filter.getName(), filter.getLabel());
                }
            }
            setProperty("textfieldFilterMap", textfieldFilterMap);
            setProperty("selectBoxFilterMap", selectBoxFilterMap);

            // set checkbox
            String key = binder.getPrimaryKeyColumnName();
            DataListDecorator decorator = new DataListDecorator();
            decorator.setId(key);
            decorator.setFieldName(PREFIX_SELECTED + key);
            setProperty("decorator", decorator);

        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message += "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            setProperty("error", message);
        }
    }

    protected Properties getBinderProperties(DataList dataList) {
        Properties binderProperties = dataList.getBinder().getProperties();
        if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null && getKey().trim().length() > 0) {
            String extraCondition = "";

            if (binderProperties.getProperty("extraCondition") != null && binderProperties.getProperty("extraCondition").toString().trim().length() > 0) {
                extraCondition = binderProperties.getProperty("extraCondition").toString() + " AND ";
            }

            if (FormUtil.PROPERTY_ID.equals(getPropertyString("keyName")) || FormUtil.PROPERTY_DATE_CREATED.equals(getPropertyString("keyName")) || FormUtil.PROPERTY_DATE_MODIFIED.equals(getPropertyString("keyName"))) {
                extraCondition += getPropertyString("keyName") + " = '" + getKey() + "'";
            } else {
                extraCondition += FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + getPropertyString("keyName") + " = '" + getKey() + "'";
            }

            binderProperties.put("extraCondition", extraCondition);
        }
        return binderProperties;
    }

    protected DataListActionResult handleAction() {
        DataListActionResult result = null;
        // get parameter
        String actionParamValue = getRequestParameterString("action");
        if (actionParamValue != null) {
            // get selected keys
            DataList dataList = getDataList();
            String[] selectedKeys = getSelectedKeys();
            if (selectedKeys != null) {
                // find action
                DataListAction[] actions = dataList.getActions();
                for (DataListAction action : actions) {
                    String actionId = action.getProperties().getProperty("id");
                    if (actionParamValue.equals(actionId)) {
                        // invoke action
                        result = action.executeAction(dataList, selectedKeys);
                        break;
                    }
                }
            }
        }
        return result;
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
        return dataList;
    }

    protected String[] getSelectedKeys() {
        String[] selectedKeys = new String[0];
        DataList dataList = getDataList();

        // get selected IDs
        Collection<String> selectedKeyList = new ArrayList<String>();
        DataListBinder binder = dataList.getBinder();
        String key = binder.getPrimaryKeyColumnName();
        Object values = getRequestParameter(PREFIX_SELECTED + key);
        if (values instanceof String[]) {
            String[] valuesStr = (String[]) values;
            for (String value : valuesStr) {
                if (!selectedKeyList.contains(value)) {
                    selectedKeyList.add(value);
                }
            }
        } else if (values instanceof String) {
            selectedKeyList.add((String) values);
        }

        selectedKeys = (String[]) selectedKeyList.toArray(new String[0]);
        return selectedKeys;
    }
}
