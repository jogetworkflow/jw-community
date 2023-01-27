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
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.apps.userview.model.UserviewTheme;
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
    private String injectedHTML;
    private DataListBinder binder;
    private DataListCollection rows;
    private DataListActionResult actionResult;
    private Integer size;
    private Integer total;
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
    private boolean disableQuickEdit = false;
    private boolean showDataWhenFilterSet = false;
    private boolean disableResponsive = false;
    private boolean responsiveSearchPopup = false;
    private String responsiveJson = "";
    private Boolean considerFilterWhenGetTotal = null;
    private Map<String, String[]> requestParamMap = null;
    private boolean isAuthorized = true;
    private String unauthorizedMsg = null;

    private UserviewPermission permission = null;

    private UserviewTheme theme;

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
            if (queryString == null) {
                queryString = "";
            }
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
            actionResult.setUrl("?"+ StringUtil.mergeRequestQueryString(queryString, queryString2));
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
        return Arrays.stream(actions)
                .filter(DataListAction::isPermitted)
                .toArray(DataListAction[]::new);
    }

    public void setActions(DataListAction[] actions) {
        if (actions != null && actions.length > 0) {
            for (DataListAction a : actions) {
                if (a instanceof DataListActionDefault) {
                    ((DataListActionDefault) a).setDatalist(this);
                }
            }
        }
        this.actions = actions;
    }

    public DataListBinder getBinder() {
        return binder;
    }

    public void setBinder(DataListBinder binder) {
        if (binder != null && binder instanceof DataListBinderDefault) {
            ((DataListBinderDefault) binder).setDatalist(this);
        }
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
        if (columns != null && columns.length > 0) {
            for (DataListColumn c : columns) {
                if (c.getFormats() != null && !c.getFormats().isEmpty()) {
                    for (DataListColumnFormat f : c.getFormats()) {
                        if (f instanceof DataListColumnFormatDefault) {
                            ((DataListColumnFormatDefault) f).setDatalist(this);
                        }
                    }
                }
                if (c.getAction() != null && c.getAction() instanceof DataListActionDefault) {
                    ((DataListActionDefault) c.getAction()).setDatalist(this);
                }
            }
        }
        
        this.columns = columns;
    }

    public DataListFilter[] getFilters() {
        return filters;
    }

    public void setFilters(DataListFilter[] filters) {
        if (filters != null && filters.length > 0) {
            for (DataListFilter f : filters) {
                if (f.getType() != null && f.getType() instanceof DataListFilterTypeDefault) {
                    ((DataListFilterTypeDefault) f.getType()).setDatalist(this);
                }
            }
        }
        
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
            String queryString = "";
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request !=null) {
                queryString = request.getQueryString();
                if (queryString == null) {
                    queryString = "";
                }
            }
            for (int i = 0; i <  rowActions.length; i++) {
                DataListAction r = rowActions[i];
                if (r.getHref() == null || (r.getHref() != null && r.getHref().isEmpty())) {
                    r.setProperty("href", "?" + StringUtil.mergeRequestQueryString(queryString, getActionParamName() + "=" + r.getPropertyString("id")));
                    if (r.getTarget() == null || (r.getTarget() != null && r.getTarget().isEmpty())) {
                        r.setProperty("target", "_self");
                    }
                    if (r.getHrefParam() == null || (r.getHrefParam() != null && r.getHrefParam().isEmpty())) {
                        r.setProperty("hrefParam", keyParam);
                    }
                    if (r.getHrefColumn() == null || (r.getHrefColumn() != null && r.getHrefColumn().isEmpty())) {
                        r.setProperty("hrefColumn", key);
                    }
                }
                rowActions[i] = r;
            }
        }

        return Arrays.stream(rowActions)
                .filter(DataListAction::isPermitted)
                .toArray(DataListAction[]::new);
    }
    
    public DataListAction getColumnAction(DataListColumn column) {
        DataListAction action = column.getAction();
        if (getBinder() != null && action != null) {
            String key = getBinder().getPrimaryKeyColumnName();
            String keyParam = getDataListEncodedParamName(CHECKBOX_PREFIX + key);
            if (action.getHref() == null || (action.getHref() != null && action.getHref().isEmpty())) {
                String queryString = WorkflowUtil.getHttpServletRequest().getQueryString();
                if (queryString == null) {
                    queryString = "";
                }
                action.setProperty("href", "?" + StringUtil.mergeRequestQueryString(queryString, getActionParamName() + "=" + "ca_" + column.getPropertyString("id")));
                if (action.getTarget() == null || (action.getTarget() != null && action.getTarget().isEmpty())) {
                    action.setProperty("target", "_self");
                }
                if (action.getHrefParam() == null || (action.getHrefParam() != null && action.getHrefParam().isEmpty())) {
                    action.setProperty("hrefParam", keyParam);
                }
                if (action.getHrefColumn() == null || (action.getHrefColumn() != null && action.getHrefColumn().isEmpty())) {
                    action.setProperty("hrefColumn", key);
                }
            }
        }
        return action;
    }

    public void setRowActions(DataListAction[] rowActions) {
        if (rowActions != null && rowActions.length > 0) {
            for (DataListAction a : rowActions) {
                if (a instanceof DataListActionDefault) {
                    ((DataListActionDefault) a).setDatalist(this);
                }
            }
        }
        
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
        if (sessionKeyPrefix == null || sessionKeyPrefix.isEmpty()) {
            sessionKeyPrefix = AppUtil.processHashVariable("#request.requestURI#", null, null, null);
        }
        
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
        
        try {
            if (page != null && page.trim().length() > 0 && getSize() <= ((Integer.parseInt(page)-1) * recordSize)) {
                page = null;
            }
        } catch(NumberFormatException e) {
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
        if (isReturnNoDataWhenFilterNotSet()) {
            return null;
        }
        
        if (rows == null) {
            rows = getRows(null, null);
        }
        return rows;
    }

    public DataListCollection getRows(Integer customSize, Integer customStart) {
        try {
            if (getBinder() != null) {
                //force get total before get rows to bypass additional filter
                getTotal();
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
        if (isReturnNoDataWhenFilterNotSet()) {
            return 0;
        }
        
        if (size == null) {
            try {
                if (getBinder() != null) {
                    //force get total before get size to bypass additional filter
                    if (!isConsiderFilterWhenGetTotal()) {
                        getTotal();
                    }
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
    
    public int getTotal() {
        if (isConsiderFilterWhenGetTotal()) {
            return getSize();
        }
        
        if (total == null) {
            try {
                if (getBinder() != null) {
                    filterQueryBuild = true;
                    total = getBinder().getDataTotalRowCount(this, getBinder().getProperties(), getFilterQueryObjects());
                    filterQueryBuild = false;
                } else {
                    total = 0;
                }
            } catch (Exception e) {
                LogUtil.error(DataList.class.getName(), e, "Error retrieving binder row count");
                total = 0;
            }
        }
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
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
            
            for (DataListAction action : getRowActions()) {
                String actionId = action.getPropertyString("id");
                if (actionParamValue.equals(actionId)) {
                    // invoke action
                    actionResult = action.executeAction(this, selectedKeys);
                    break;
                }
            }
            
            //look from column action as well
            if (actionParamValue.startsWith("ca_column_")) {
                for (DataListColumn column : columns) {
                    DataListAction action = column.getAction();
                    if (action != null && actionParamValue.equals("ca_"+column.getPropertyString("id"))) {
                        // invoke action
                        actionResult = action.executeAction(this, selectedKeys);
                        break;
                    }
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
        String[] values = null; 
        if (requestParamMap != null) {
            values = requestParamMap.get(param);
        } else {
            if(request!=null){
            values = request.getParameterValues(param);
            }
        }
        if (isUseSession() && !(TableTagParameters.PARAMETER_EXPORTING.equals(paramName) || TableTagParameters.PARAMETER_EXPORTTYPE.equals(paramName) || PARAMETER_ACTION.equals(paramName) || paramName.startsWith(CHECKBOX_PREFIX))) {
            if(request!=null){
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
                templates.add("<span class=\"filter-cell\">"+getPageSizeSelectorTemplate()+"</span>");
            }

            DataListFilter[] filterList = getFilters();
            for (int i = 0; i < filterList.length; i++) {
                String cssClass = "";
                if (filterList[i].isHidden()) {
                    cssClass = "hidden-filter";
                }
                String label = filterList[i].getLabel();
                templates.add("<span class=\"filter-cell "+cssClass+"\">"+filterList[i].getType().getTemplate(this, filterList[i].getName(), label)+"</span>");
            }
            filterTemplates = (String[]) templates.toArray(new String[0]);
        }
        return filterTemplates;
    }
    
    public String getInjectedHTML() {
        if (injectedHTML == null) {
            injectedHTML = "";

            // find action
            for (DataListAction action : getActions()) {
                if (action instanceof DataListPluginExtend) {
                    String temp = ((DataListPluginExtend) action).getHTML(this);
                    if (temp != null) {
                        injectedHTML += temp;
                    }
                }
            }
            
            for (DataListAction action : getRowActions()) {
                if (action instanceof DataListPluginExtend) {
                    String temp = ((DataListPluginExtend) action).getHTML(this);
                    if (temp != null) {
                        injectedHTML += temp;
                    }
                }
            }
            
            for (DataListColumn column : columns) {
                DataListAction action = column.getAction();
                if (action != null && action instanceof DataListPluginExtend) {
                    String temp = ((DataListPluginExtend) action).getHTML(this);
                    if (temp != null) {
                        injectedHTML += temp;
                    }
                }
                if (column.getFormats() != null) {
                    for (DataListColumnFormat f : column.getFormats()) {
                        if (f != null && f instanceof DataListPluginExtend) {
                            String temp = ((DataListPluginExtend) f).getHTML(this);
                            if (temp != null) {
                                injectedHTML += temp;
                            }
                        }
                    }
                }
            }
        }
        return injectedHTML;
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
        if (name != null && name.contains(" ")) {
            name = name.replaceAll(" ", "__");
        }
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
            boolean isInteger = true;
            try {
                Integer.parseInt(o); 
            } catch (Exception e) {
                isInteger = false;
            }
            if (isInteger) {
                template += "<option value='" + o + "'" + selected + ">" + o + "</option>";
            }
        }

        template += "</select>";
        return template;
    }

    public boolean isDisableQuickEdit() {
        return disableQuickEdit;
    }

    public void setDisableQuickEdit(boolean disableQuickEdit) {
        this.disableQuickEdit = disableQuickEdit;
    }

    public boolean isShowDataWhenFilterSet() {
        return showDataWhenFilterSet;
    }

    public void setShowDataWhenFilterSet(boolean showDataWhenFilterSet) {
        this.showDataWhenFilterSet = showDataWhenFilterSet;
    }
    
    public boolean isReturnNoDataWhenFilterNotSet() {
        if (isShowDataWhenFilterSet() && (getFilterQueryObjects() == null || getFilterQueryObjects().length == 0)) {
            return true;
        }
        return false;
    }

    public Boolean isConsiderFilterWhenGetTotal() {
        if (considerFilterWhenGetTotal == null) {
            return "true".equals(ResourceBundleUtil.getMessage("dbuilder.menu.counter.considerFilters"));
        }
        return considerFilterWhenGetTotal;
    }

    public void setConsiderFilterWhenGetTotal(Boolean considerFilterWhenGetTotal) {
        this.considerFilterWhenGetTotal = considerFilterWhenGetTotal;
    }

    public boolean isDisableResponsive() {
        return disableResponsive;
    }

    public void setDisableResponsive(boolean disableResponsive) {
        this.disableResponsive = disableResponsive;
    }

    public boolean isResponsiveSearchPopup() {
        return responsiveSearchPopup;
    }

    public void setResponsiveSearchPopup(boolean responsiveSearchPopup) {
        this.responsiveSearchPopup = responsiveSearchPopup;
    }
    
    public String getResponsiveJson() {
        return responsiveJson;
    }

    public void setResponsiveJson(String responsiveJson) {
        this.responsiveJson = responsiveJson;
    }
    
    /**
     * Retrieve current request map
     * @return 
     */
    public Map<String, String[]> getRequestParamMap() {
        return requestParamMap;
    }

    /**
     * Set current request map
     * @return 
     */
    public void setRequestParamMap(Map<String, String[]> requestParamMap) {
        this.requestParamMap = requestParamMap;
    }

    /**
     * Method to clear filter
     */
    public void clearFilter() {
        filterQueryBuild = false;
        dataListFilterQueryObjectList = new ArrayList<DataListFilterQueryObject>();
    }

    public boolean isIsAuthorized() {
        return isAuthorized;
    }

    public void setIsAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public String getUnauthorizedMsg() {
        return unauthorizedMsg;
    }

    public void setUnauthorizedMsg(String unauthorizedMsg) {
        this.unauthorizedMsg = unauthorizedMsg;
    }
    
    public boolean getNoExport() {
        boolean noExport = true;
        if (getColumns() != null) {
            for (DataListColumn column : getColumns()) {
                boolean isExport = true;
                if ((column.isHidden() && !"true".equalsIgnoreCase(column.getPropertyString("include_export")))
                        || (!column.isHidden() && "true".equalsIgnoreCase(column.getPropertyString("exclude_export")))) {
                    isExport = false;
                }
                if (isExport) {
                    noExport = false;
                    break;
                }
            }
        }
        return noExport;
    }

    /**
     * Set permission * * @param permission
     */
    public void setPermission(UserviewPermission permission) {
        this.permission = permission;
    }

    /**
     * Get permission * * @return
     */
    public UserviewPermission getPermission() {
        return permission;
    }

    public UserviewTheme getTheme() {
        return theme;
    }

    public void setTheme(UserviewTheme theme) {
        this.theme = theme;
    }
}
