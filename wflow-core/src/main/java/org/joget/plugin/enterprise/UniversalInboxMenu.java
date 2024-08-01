package org.joget.plugin.enterprise;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListQueryParam;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.lib.InboxMenu;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

public class UniversalInboxMenu extends InboxMenu implements PluginWebSupport {
    private DataList cacheDataList = null;

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Universal Inbox";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-inbox\"></i>";
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    public String getName() {
        return "Universal Inbox Menu";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
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
    public String getDecoratedMenu() {
        String menuItem = null;
        boolean showRowCount = Boolean.valueOf(getPropertyString("rowCount")).booleanValue();
        if (showRowCount) {
            int rowCount = 0;
            
            if (!"true".equalsIgnoreCase(getRequestParameterString("isBuilder"))) {
                rowCount = getDataTotalRowCount();
            }

            // sanitize label
            String label = getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }

            // generate menu link
            menuItem = "<a href=\"" + getUrl() + "\" class=\"menu-link default\"><span>" + label + "</span> <span class='pull-right badge rowCount'>" + rowCount + "</span></a>";
        }
        return menuItem;
    }

    @Override
    protected DataList getDataList() {
        if (cacheDataList == null) {
            // get datalist
            ApplicationContext ac = AppUtil.getApplicationContext();
            DataListService dataListService = (DataListService) ac.getBean("dataListService");
            String target = "_self";
            String embed = "";
            if ("true".equalsIgnoreCase(getPropertyString("showPopup"))) {
                target = "popup";
                embed = "&embed=true";
            }
            String json = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/universalInboxMenuListJson.json", new String[]{embed, target}, true, "message/userview/universalInboxMenu");
            cacheDataList = dataListService.fromJson(json);
        }
        return cacheDataList;
    }

    @Override
    protected DataListCollection getRows(DataList dataList) {
        try {
            DataListCollection resultList = new DataListCollection();
            DataListQueryParam param = dataList.getQueryParam(null, null);

            // get assignments
            WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
            Collection<WorkflowAssignment> assignmentList = workflowManager.getAssignmentListLite(null, null, null, null, param.getSort(), param.getDesc(), param.getStart(), param.getSize());

            String format = AppUtil.getAppDateFormat();
            for (WorkflowAssignment assignment : assignmentList) {
                Map data = new HashMap();
                data.put("processId", assignment.getProcessId());
                data.put("processRequesterId", assignment.getProcessRequesterId());
                data.put("activityId", assignment.getActivityId());
                data.put("processName", assignment.getProcessName());
                data.put("activityName", assignment.getActivityName());
                data.put("processVersion", assignment.getProcessVersion());
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(assignment.getDateCreated(), null, format));
                data.put("acceptedStatus", assignment.isAccepted());
                data.put("dueDate", assignment.getDueDate() != null ? TimeZoneUtil.convertToTimeZone(assignment.getDueDate(), null, format) : "-");
                data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(assignment.getServiceLevelValue()));

                // set results
                resultList.add(data);
            }
            return resultList;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getDataTotalRowCount() {
        WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        int count = 0;
        count = workflowManager.getAssignmentSize(null, null, null);
        return count;
    }

    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{PROPERTY_FILTER, PROPERTY_FILTER_ALL, PROPERTY_FILTER_PROCESS, appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/universalInboxMenu.json", arguments, true, "message/userview/universalInboxMenu");
        return json;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
                
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
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(null, null);

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
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
