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
    <div id="pluginDefault">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/pluginDefault/list?${pageContext.request.queryString}"
           var="JsonPluginDefaultDataTable"
           divToUpdate="pluginDefaultList"
           jsonData="data"
           rowsPerPage="15"
           width="100%"
           sort="id"
           desc="false"
           href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/config"
           hrefParam="id"
           hrefQuery="true"
           hrefDialog="true"
           hrefDialogTitle=""
           checkbox="${protectedReadonly != 'true'}"
           checkboxButton1="console.app.pluginDefault.create.label"
           checkboxCallback1="defaultPluginPropertiesCreate"
           checkboxOptional1="true"
           checkboxButton2="general.method.label.delete"
           checkboxCallback2="pluginDefaultDelete"
           searchItems="filter|Filter"
           fields="['id','pluginName','pluginDescription']"
           column1="{key: 'pluginName', label: 'console.plugin.label.name', sortable: true}"
           column2="{key: 'pluginDescription', label: 'console.plugin.label.description', sortable: true}"
           />
    </div>
    
    <script>
        <ui:popupdialog var="pluginDefaultCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/create"/>
    
        $(document).ready(function(){
            $('#JsonPluginDefaultDataTable').hide();
            <c:if test="${protectedReadonly == 'true'}">
                $(".ui-tabs-panel button").hide();
            </c:if>
        });
        
        function defaultPluginPropertiesCreate(){
            pluginDefaultCreateDialog.init();
        }
        
        function pluginDefaultDelete(selectedList){
            if (confirm('<ui:msgEscJS key="console.app.pluginDefault.delete.label.confirmation"/>')) {
               parent.UI.blockUI();
               var callback = {
                   success : function() {
                       reloadTable();
                       JsonPluginDefaultDataTable.clearSelectedRows();
                       parent.UI.unblockUI();
                   }
               }
               var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/pluginDefault/delete', callback, 'ids='+selectedList);
            }
        }
        function closeDialog() {
            pluginDefaultCreateDialog.close();
        }
        function reloadTable() {
            closeDialog();
            filter(JsonPluginDefaultDataTable, '&filter=', $('#JsonPluginDefaultDataTable_searchCondition').val());
        }   
    </script>
</div>  
<commons:popupFooter />
