package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.service.DataListDecorator;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

public class DataList {

    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAXIMUM_PAGE_SIZE = 100000;
    public static final String ACTION_POSITION_TOP_LEFT = "topLeft";
    public static final String ACTION_POSITION_TOP_RIGHT = "topRight";
    public static final String ACTION_POSITION_BOTTOM_LEFT = "bottomLeft";
    public static final String ACTION_POSITION_BOTTOM_RIGHT = "bottomRight";
    public static final String ACTION_POSITION_BOTH_LEFT = "bothLeft";
    public static final String ACTION_POSITION_BOTH_RIGHT = "bothRight";
    public static final String CHECKBOX_POSITION_LEFT = "left";
    public static final String CHECKBOX_POSITION_RIGHT = "right";
    public static final String CHECKBOX_POSITION_BOTH = "both";
    public static final String CHECKBOX_POSITION_NO = "no";
    public static final String SELECTION_TYPE_SINGLE = "single";
    public static final String SELECTION_TYPE_MULTIPLE = "multiple";
    public static final String PARAMETER_FILTER_PREFIX = "fn_";
    public static final String PARAMETER_PAGE_SIZE = "ps";
    public static final String PARAMETER_ACTION = "ac";
    public static final String CHECKBOX_PREFIX = "checkbox_";
    public static final String ORDER_DESCENDING_VALUE = "1";
    public static final String ORDER_ASCENDING_VALUE = "2";
    public static final String DATALIST_ROW_ACTION = "rowAction";
    public static final String DATALIST_ACTION = "action";
    public static final String DEFAULT_PAGE_SIZE_LIST = "10,20,30,40,50,100";
    private String id;
    private String name;
    private String description;
    private DataListFilter[] filters;
    private DataListAction[] actions;
    private DataListAction[] rowActions;
    private DataListColumn[] columns;
    private String[] filterTemplates;
    private DataListBinder binder;
    private DataListCollection rows;
    private DataListActionResult actionResult;
    private Integer size;
    private Integer pageSize;
    private int defaultPageSize = 0;
    private String pageSizeList;
    private String defaultSortColumn;
    private String defaultOrder;
    private boolean useSession = false;
    private boolean allowUpdateSession = true;
    private boolean reloadRequired = false;
    private boolean resetSessionPageValue = false;
    private boolean showPageSizeSelector = true;
    private String sessionKeyPrefix = "";
    private String actionPosition = ACTION_POSITION_BOTTOM_LEFT;
    private String checkboxPosition = CHECKBOX_POSITION_LEFT;
    private String selectionType = SELECTION_TYPE_MULTIPLE;
    private Collection<DataListFilterQueryObject> dataListFilterQueryObjectList = new ArrayList<DataListFilterQueryObject>();
    private boolean filterQueryBuild = false;

    //Required when using session
    public void init() {
        try {
            rows = getRows();
            size = getSize();
        } catch(Exception e) {
            LogUtil.error(DataList.class.getName(), e, "");
        }

        if (isReloadRequired()) {
            actionResult = new DataListActionResult();
            actionResult.setType(DataListActionResult.TYPE_REDIRECT);
            String queryString = WorkflowUtil.getHttpServletRequest().getQueryString();
            String queryString2 = "";
            if (getDataListParamString(TableTagParameters.PARAMETER_SORT) != null) {
                queryString2 += getDataListEncodedParamName(TableTagParameters.PARAMETER_SORT) + "=" + getDataListParamString(TableTagParameters.PARAMETER_SORT) + "&";
            }
            if (getDataListParamString(TableTagParameters.PARAMETER_ORDER) != null) {
                queryString2 += getDataListEncodedParamName(TableTagParameters.PARAMETER_ORDER) + "=" + getDataListParamString(TableTagParameters.PARAMETER_ORDER) + "&";
            }
            if (getDataListParamString(TableTagParameters.PARAMETER_PAGE) != null) {
                queryString2 += getDataListEncodedParamName(TableTagParameters.PARAMETER_PAGE) + "=" + getDataListParamString(TableTagParameters.PARAMETER_PAGE) + "&";
            }
            if (getDataListParamString(PARAMETER_PAGE_SIZE) != null) {
                queryString2 += getDataListEncodedParamName(PARAMETER_PAGE_SIZE) + "=" + getDataListParamString(PARAMETER_PAGE_SIZE) + "&";
            }
            actionResult.setUrl("?" + StringUtil.mergeRequestQueryString(queryString, queryString2));
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActionPosition() {
        return actionPosition;
    }

    public void setActionPosition(String actionPosition) {
        this.actionPosition = actionPosition;
    }

    public DataListAction[] getActions() {
        return actions;
    }

    public void setActions(DataListAction[] actions) {
        this.actions = actions;
    }

    public DataListBinder getBinder() {
        return binder;
    }

    public void setBinder(DataListBinder binder) {
        this.binder = binder;
    }

    public String getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(String selectionType) {
        this.selectionType = selectionType;
    }

    public String getCheckboxPosition() {
        return checkboxPosition;
    }

    public void setCheckboxPosition(String checkboxPosition) {
        this.checkboxPosition = checkboxPosition;
    }

    public DataListColumn[] getColumns() {
        return columns;
    }

    public void setColumns(DataListColumn[] columns) {
        this.columns = columns;
    }

    public DataListFilter[] getFilters() {
        return filters;
    }

    public void setFilters(DataListFilter[] filters) {
        this.filters = filters;
    }

    public String getId() {
        if (id != null) {
            return id;
        } else {
            return "";
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDefaultPageSize() {
        if (defaultPageSize == 0) {
            try {
                defaultPageSize = Integer.parseInt(ResourceBundleUtil.getMessage("dbuilder.defaultPageSize"));
            } catch (NumberFormatException e) {
                defaultPageSize = DEFAULT_PAGE_SIZE;
            }
        }
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public String getDefaultSortColumn() {
        return defaultSortColumn;
    }

    public void setDefaultSortColumn(String defaultSortColumn) {
        this.defaultSortColumn = defaultSortColumn;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            String pageSizeParam = getDataListParamString(PARAMETER_PAGE_SIZE);
            if (pageSizeParam != null) {
                pageSize = Integer.parseInt(pageSizeParam);
            } else {
                pageSize = getDefaultPageSize();
            }
        }
        return pageSize;
    }

    public DataListAction[] getRowActions() {
        if (getBinder() != null) {
            String key = getBinder().getPrimaryKeyColumnName();
            String keyParam = getDataListEncodedParamName(CHECKBOX_PREFIX + key);
            for (int i = 0; i <  rowActions.length; i++) {
                DataListAction r = rowActions[i];
                if (r.getHref() == null || (r.getHref() != null && r.getHref().isEmpty())) {
                    r.setProperty("href", "?" + getActionParamName() + "=" + r.getPropertyString("id"));
                    r.setProperty("target", "_self");
                    r.setProperty("hrefParam", keyParam);
                    r.setProperty("hrefColumn", key);
                }
                rowActions[i] = r;
            }
        }
        
        return rowActions;
    }

    public void setRowActions(DataListAction[] rowActions) {
        this.rowActions = rowActions;
    }

    public boolean isUseSession() {
        return useSession;
    }

    public void setUseSession(boolean useSession) {
        this.useSession = useSession;
    }

    public boolean isAllowUpdateSession() {
        return allowUpdateSession;
    }

    public void setAllowUpdateSession(boolean allowUpdateSession) {
        this.allowUpdateSession = allowUpdateSession;
    }

    public boolean isReloadRequired() {
        return reloadRequired;
    }

    public void setReloadRequired(boolean reloadRequired) {
        this.reloadRequired = reloadRequired;
    }

    public String getSessionKeyPrefix() {
        return sessionKeyPrefix;
    }

    public boolean isResetSessionPageValue() {
        return resetSessionPageValue;
    }

    public void setResetSessionPageValue(boolean resetSessionPageValue) {
        this.resetSessionPageValue = resetSessionPageValue;
    }

    public void setSessionKeyPrefix(String sessionKeyPrefix) {
        this.sessionKeyPrefix = sessionKeyPrefix;
    }

    public void setRows(DataListCollection rows) {
        this.rows = rows;
    }

    public DataListQueryParam getQueryParam(Integer customSize, Integer customStart) {
        DataListQueryParam param = new DataListQueryParam();

        String page = getDataListParamString(TableTagParameters.PARAMETER_PAGE);
        String order = getDataListParamString(TableTagParameters.PARAMETER_ORDER);
        String sort = getDataListParamString(TableTagParameters.PARAMETER_SORT);
        
        // determine start and size
        Integer recordSize = getPageSize();
        Integer start = 0;
        
        if (page != null && page.trim().length() > 0 && getSize() <= ((Integer.parseInt(page)-1) * recordSize)) {
            page = null;
        }
        
        if (getDataListParam(TableTagParameters.PARAMETER_EXPORTTYPE) != null && getDataListParam(TableTagParameters.PARAMETER_EXPORTING) != null) {
            // exporting, set full list
            page = "1";
            setPageSize(DataList.MAXIMUM_PAGE_SIZE);
            recordSize = DataList.MAXIMUM_PAGE_SIZE;
        } else if (customSize != null) {
            recordSize = customSize;
        }
        if (customStart != null) {
            start = customStart;
        } else if (page != null && page.trim().length() > 0 && recordSize != null) {
            start = (Integer.parseInt(page) - 1) * recordSize;
        }

        // determine sort column & order direction
        String sortColumn = null;
        Boolean desc = false;
        if (sort != null && !sort.trim().isEmpty()) {
            int sortIndex = Integer.parseInt(sort);

            if (getCheckboxPosition().equals(CHECKBOX_POSITION_LEFT) || getCheckboxPosition().equals(CHECKBOX_POSITION_BOTH)) {
                sortIndex = sortIndex - 1;
            }

            DataListColumn[] columns = getColumns();
            if (sortIndex < columns.length) {
                sortColumn = columns[sortIndex].getName();
            }
        } else if (getDefaultSortColumn() != null && !getDefaultSortColumn().isEmpty()) {
            sortColumn = getDefaultSortColumn();
            if (ORDER_DESCENDING_VALUE.equals(getDefaultOrder())) {
                desc = true;
            }
        }
        if (ORDER_DESCENDING_VALUE.equals(order)) {
            desc = true;
        }

        param.setSort(sortColumn);
        param.setDesc(desc);
        param.setSize(recordSize);
        param.setStart(start);

        return param;
    }

    public DataListCollection getRows() {
        if (rows == null) {
            rows = getRows(null, null);
        }
        return rows;
    }

    public DataListCollection getRows(Integer customSize, Integer customStart) {
        try {
            if (getBinder() != null) {
                DataListQueryParam param = getQueryParam(customSize, customStart);
                return getBinder().getData(this, getBinder().getProperties(), getFilterQueryObjects(), param.getSort(), param.getDesc(), param.getStart(), param.getSize());
            }
        } catch (Exception e) {
            LogUtil.error(DataList.class.getName(), e, "Error retrieving binder rows");
            throw new RuntimeException(e);
        }
        return null;
    }

    public int getSize() {
        if (size == null) {
            try {
                if (getBinder() != null) {
                    size = getBinder().getDataTotalRowCount(this, getBinder().getProperties(), getFilterQueryObjects());
                } else {
                    size = 0;
                }
            } catch (Exception e) {
                LogUtil.error(DataList.class.getName(), e, "Error retrieving binder row count");
                size = 0;
            }
        }
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
    
    public DataListDecorator getPrimaryKeyDecorator() {
        if (getBinder() != null) {
            String key = getBinder().getPrimaryKeyColumnName();
            DataListDecorator decorator = new DataListDecorator();
            decorator.setId(key);
            decorator.setFieldName(getDataListEncodedParamName(CHECKBOX_PREFIX + key));
            return decorator;
        }
        return new DataListDecorator();
    }

    public void setActionResult(DataListActionResult actionResult) {
        this.actionResult = actionResult;
    }

    public DataListActionResult getActionResult() {
        if (actionResult != null) {
            return actionResult;
        }
        
        if (isUseSession()) {
            init();
        }

        // get parameter
        String actionParamValue = getDataListParamString(PARAMETER_ACTION);
        if (actionParamValue != null) {
            String[] selectedKeys = getSelectedKeys();
            // find action
            for (DataListAction action : getActions()) {
                String actionId = action.getPropertyString("id");
                if (actionParamValue.equals(actionId)) {
                    // invoke action
                    actionResult = action.executeAction(this, selectedKeys);
                    break;
                }
            }
            
            //look from row action as well
            for (DataListAction action : getRowActions()) {
                String actionId = action.getPropertyString("id");
                if (actionParamValue.equals(actionId)) {
                    // invoke action
                    actionResult = action.executeAction(this, selectedKeys);
                    break;
                }
            }
            
        }
        return actionResult;
    }

    public DataListFilterQueryObject[] getFilterQueryObjects() {
        if (!filterQueryBuild) {
            DataListFilter[] filterList = getFilters();
            if (filterList != null) {
                for (int i = 0; i < filterList.length; i++) {
                    DataListFilter filter = filterList[i];
                    DataListFilterQueryObject temp = filter.getType().getQueryObject(this, filter.getName());
                    if (temp != null) {
                        temp.setOperator(filter.getOperator());
                        dataListFilterQueryObjectList.add(temp);
                    }
                }
            }
            filterQueryBuild = true;
        }
        return dataListFilterQueryObjectList.toArray(new DataListFilterQueryObject[dataListFilterQueryObjectList.size()]);
    }
    
    public void addFilterQueryObject(DataListFilterQueryObject filterQueryObject) {
        dataListFilterQueryObjectList.add(filterQueryObject);
    }

    public String[] getDataListParam(String paramName) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String param = getDataListEncodedParamName(paramName);
        String[] values = request.getParameterValues(param);
        if (isUseSession() && !(TableTagParameters.PARAMETER_EXPORTTYPE.equals(paramName) || PARAMETER_ACTION.equals(paramName) || paramName.startsWith(CHECKBOX_PREFIX))) {
            String sessionKey = "session_" + getId() + "_" + getSessionKeyPrefix() + "_" + param;
            HttpSession session = request.getSession(true);

            if (values != null) {
                if (isAllowUpdateSession()) {
                    String[] sessionValues = (String[]) session.getAttribute(sessionKey);
                    if (!Arrays.equals(values, sessionValues) && (paramName.startsWith(PARAMETER_FILTER_PREFIX) || PARAMETER_PAGE_SIZE.equals(paramName))) {
                        setResetSessionPageValue(true);
                    }

                    if (isResetSessionPageValue() && TableTagParameters.PARAMETER_PAGE.equals(paramName)) {
                        values[0] = "1";
                    }

                    session.setAttribute(sessionKey, values);
                }
            } else {
                values = (String[]) session.getAttribute(sessionKey);
                if ((values != null && values.length > 0) && isAllowUpdateSession() && (TableTagParameters.PARAMETER_SORT.equals(paramName) || TableTagParameters.PARAMETER_ORDER.equals(paramName) || TableTagParameters.PARAMETER_PAGE.equals(paramName) || PARAMETER_PAGE_SIZE.equals(paramName))) {
                    setReloadRequired(true);
                }
            }
        }
        return values;
    }

    public String getDataListParamString(String paramName) {
        String[] values = getDataListParam(paramName);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    public String[] getFilterTemplates() {
        if (filterTemplates == null) {
            Collection<String> templates = new ArrayList<String>();

            //reset page value when filter is submit
            templates.add("<input type='hidden' id='" + getDataListEncodedParamName(TableTagParameters.PARAMETER_PAGE) + "' name='" + getDataListEncodedParamName(TableTagParameters.PARAMETER_PAGE) + "' value='1'/>");
            
            if (isShowPageSizeSelector()) {
                templates.add(getPageSizeSelectorTemplate());
            }

            DataListFilter[] filterList = getFilters();
            for (int i = 0; i < filterList.length; i++) {
                String label = filterList[i].getLabel();
                templates.add(filterList[i].getType().getTemplate(this, filterList[i].getName(), label));
            }
            filterTemplates = (String[]) templates.toArray(new String[0]);
        }
        return filterTemplates;
    }

    public void addBinderProperty(String key, Object value) {
        if (getBinder() != null) {
            getBinder().getProperties().put(key, value);
        }
    }

    public void addDataListAction(String className, String type, Map properties) {
        try {
            Collection<DataListAction> actionList = new ArrayList<DataListAction>();
            if (DATALIST_ROW_ACTION.equals(type) && getRowActions().length > 0) {
                actionList.addAll(Arrays.asList(getRowActions()));
            } else if (DATALIST_ACTION.equals(type) && getActions().length > 0) {
                actionList.addAll(Arrays.asList(getActions()));
            }

            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            DataListAction action = (DataListAction) pluginManager.getPlugin(className);
            action.setProperties(properties);
            actionList.add(action);

            if (DATALIST_ROW_ACTION.equals(type) && actionList.size() > 0) {
                setRowActions((DataListAction[]) actionList.toArray(new DataListAction[actionList.size()]));
            } else if (DATALIST_ACTION.equals(type) && actionList.size() > 0) {
                setActions((DataListAction[]) actionList.toArray(new DataListAction[actionList.size()]));
            }

        } catch (Exception e) {
            LogUtil.error(DataList.class.getName(), e, "");
        }
    }

    public String[] getSelectedKeys() {
        String[] selectedKeys = new String[0];

        // get selected IDs
        if (getBinder() != null) {
            String key = getBinder().getPrimaryKeyColumnName();
            String[] values = getDataListParam(CHECKBOX_PREFIX + key);
            return values;
        }
        return selectedKeys;
    }

    public String getDataListEncodedParamName(String name) {
        if (!TableTagParameters.PARAMETER_EXPORTING.equals(name)) {
            return new ParamEncoder(getId()).encodeParameterName(name);
        } else {
            return name;
        }
    }

    public boolean isShowPageSizeSelector() {
        return showPageSizeSelector;
    }

    public void setShowPageSizeSelector(boolean showPageSizeSelector) {
        this.showPageSizeSelector = showPageSizeSelector;
    }

    public String getPageSizeList() {
        if (pageSizeList == null || (pageSizeList != null && pageSizeList.isEmpty())) {
            pageSizeList = DEFAULT_PAGE_SIZE_LIST;
        }
        return pageSizeList;
    }

    public void setPageSizeList(String pageSizeList) {
        this.pageSizeList = pageSizeList;
    }
    
    public String getActionParamName() {
        return getDataListEncodedParamName(PARAMETER_ACTION);
    }

    private String getPageSizeSelectorTemplate() {
        String template = "<select id='" + getDataListEncodedParamName(PARAMETER_PAGE_SIZE) + "' name='" + getDataListEncodedParamName(PARAMETER_PAGE_SIZE) + "'>";
        String value = getPageSize().toString();
            
        String[] list = getPageSizeList().split(",");

        for (String o : list) {
            String selected = "";
            if (o.equals(value)) {
                selected = " selected='selected'";
            }
            template += "<option value='" + o + "'" + selected + ">" + o + "</option>";
        }

        template += "</select>";
        return template;
    }
}
