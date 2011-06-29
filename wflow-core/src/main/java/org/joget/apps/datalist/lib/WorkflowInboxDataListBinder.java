package org.joget.apps.datalist.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.displaytag.properties.SortOrderEnum;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListBuilderProperty;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagedList;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

/**
 * Test implementation for a binder that retrieves workflow assignments for the current user.
 */
public class WorkflowInboxDataListBinder extends DataListBinderDefault implements PropertyEditable, PluginWebSupport {

    public static final String PROPERTY_FILTER = "appFilter";
    public static final String PROPERTY_FILTER_ALL = "all";
    public static final String PROPERTY_FILTER_APP = "app";
    public static final String PROPERTY_FILTER_PROCESS = "process";

    @Override
    public String getName() {
        return "Workflow Inbox Data Binder";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Retrieves workflow assignments for the current user.";
    }

    @Override
    public String getLabel() {
        return "Workflow Inbox Data Binder";
    }

    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{PROPERTY_FILTER, PROPERTY_FILTER_ALL, PROPERTY_FILTER_APP, PROPERTY_FILTER_PROCESS, appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/workflowInboxDataListBinder.json", arguments, true, "message/datalist/workflowInboxDataListBinder");
        return json;
    }

    public String getDefaultPropertyValues() {
        return "";
    }

    @Override
    public DataListBuilderProperty[] getBuilderProperties() {
        DataListBuilderProperty[] builderProps = {
            new DataListBuilderProperty("appFilter", "Assignments to Display", DataListBuilderProperty.TYPE_SELECTBOX, new String[]{"all", "current"}, null)
        };
        return builderProps;
    }

    @Override
    public DataListColumn[] getColumns() {
        Collection<DataListColumn> columns = new ArrayList<DataListColumn>();
        columns.add(new DataListColumn("activityDefId", "Activity Def ID", false));
        columns.add(new DataListColumn("activityId", "Activity ID", false));
        columns.add(new DataListColumn("activityName", "Activity Name", false));
        columns.add(new DataListColumn("processDefId", "Process Def ID", false));
        columns.add(new DataListColumn("processId", "Process ID", false));
        columns.add(new DataListColumn("processName", "Process Name", false));
        columns.add(new DataListColumn("dateCreated", "Date Created", false));
        columns.add(new DataListColumn("dueDate", "Due Date", false));
        return columns.toArray(new DataListColumn[0]);
    }

    @Override
    public String getPrimaryKeyColumnName() {
        String primaryKey = "activityId";
        return primaryKey;
    }

    @Override
    public DataListCollection getData(DataList dataList, Properties properties, String filterName, String filterValue, String sort, Boolean desc, int start, int rows) {
        DataListCollection resultList = new DataListCollection();

        // determine filter
        String packageId = null;
        String processDefId = null;
        Properties props = getProperties();
        if (props != null) {
            String appFilter = props.getProperty(PROPERTY_FILTER);
            if (PROPERTY_FILTER_APP.equals(appFilter)) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                if (appDef != null) {
                    PackageDefinition packageDef = appDef.getPackageDefinition();
                    if (packageDef != null) {
                        packageId = packageDef.getId();
                    }
                }
            } else if (PROPERTY_FILTER_PROCESS.equals(appFilter)) {
                String processId = props.getProperty("processId");
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                if (appDef != null) {
                    ApplicationContext ac = AppUtil.getApplicationContext();
                    AppService appService = (AppService) ac.getBean("appService");
                    WorkflowProcess process = appService.getWorkflowProcessForApp(appDef.getId(), appDef.getVersion().toString(), processId);
                    processDefId = process.getId();
                }
            }
        }

        // set default sorting
        if (sort == null || sort.trim().isEmpty()) {
            sort = "dateCreated";
            desc = Boolean.TRUE;
        }

        // get assignments
        WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        PagedList<WorkflowAssignment> assignmentList = workflowManager.getAssignmentPendingAndAcceptedList(packageId, processDefId, null, sort, desc, start, rows);
        Integer total = assignmentList.getTotal();

        // set results
        resultList.addAll(assignmentList);
        resultList.setObjectsPerPage(rows);
        resultList.setFullListSize(total);
        resultList.setSortCriterion(sort);
        if (desc != null) {
            if (desc.booleanValue()) {
                resultList.setSortDirection(SortOrderEnum.DESCENDING);
            } else {
                resultList.setSortDirection(SortOrderEnum.ASCENDING);
            }
        }
        return resultList;
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Properties properties, String filterName, String filterValue) {
        WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        int count = workflowManager.getAssignmentSize(null, null, null);
        return count;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("getProcesses".equals(action)) {
            String appId = request.getParameter("appId");
            String appVersion = request.getParameter("appVersion");
            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                AppService appService = (AppService) ac.getBean("appService");
                WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
                AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                Long packageVersion = (packageDefinition != null) ? packageDefinition.getVersion() : new Long(1);
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());

                Map<String, String> empty = new HashMap<String, String>();
                empty.put("value", "");
                empty.put("label", "");
                jsonArray.put(empty);

                for (WorkflowProcess p : processList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", p.getIdWithoutVersion());
                    option.put("label", p.getName());
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Run Process's options Error!");
            }
        }
    }
}
