<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="${theme}"/>
<div id="main-body-content">
    <div id="resources">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/resource/list?${pageContext.request.queryString}"
            var="JsonResourcesDataTable"
            divToUpdate="ResourcesList"
            jsonData="data"
            rowsPerPage="15"
            width="100%"
            sort="id"
            desc="false"
            href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/permission"
            hrefParam="id"
            hrefQuery="true"
            hrefDialog="true"
            hrefDialogTitle=""
            checkbox="true"
            checkboxButton1="console.app.resources.create.label"
            checkboxCallback1="addResource"
            checkboxOptional1="true"
            checkboxButton2="general.method.label.delete"
            checkboxCallback2="appResourceDelete"
            searchItems="filter|Filter"
            fields="['id','filesize','permissionClassLabel']"
            column1="{key: 'id', label: 'console.app.resource.common.label.id', sortable: true}"
            column2="{key: 'filesize', label: 'console.app.resource.common.label.filesize', sortable: true}"
            column3="{key: 'permissionClassLabel', label: 'console.app.resource.common.label.permission', sortable: false}"
            />
    </div>
    
    <script>
        <ui:popupdialog var="resourceCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/create"/>
    
        $(document).ready(function(){
            $('#JsonResourcesDataTable_searchTerm').hide();
            <c:if test="${protectedReadonly == 'true'}">
                $(".ui-tabs-panel button").hide();
            </c:if>
        });
        
        function addResource(){
            resourceCreateDialog.init();
        }

        function appResourceDelete(selectedList){
            if (confirm('<ui:msgEscJS key="console.app.resource.delete.label.confirmation"/>')) {
                parent.UI.blockUI();
                var callback = {
                    success : function() {
                        filter(JsonResourcesDataTable, '&filter=', $('#JsonResourcesDataTable_searchCondition').val());
                        JsonResourcesDataTable.clearSelectedRows();
                        parent.UI.unblockUI();
                    }
                }
                var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/resource/delete', callback, 'ids='+selectedList);
            }
        }
        
        function closeDialog() {
            resourceCreateDialog.close();
        }
        function reloadTable() {
            closeDialog();
            filter(JsonResourcesDataTable, '&filter=', $('#JsonResourcesDataTable_searchCondition').val());
        }
    </script>
</div>  
<commons:popupFooter />
