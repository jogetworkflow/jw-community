<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<div id="nav">
    <div id="nav-title">
        <jsp:include page="appTitle.jsp" flush="true" />
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="appSubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="addResource()"><fmt:message key="console.app.resources.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="main-body-content">
            <div id="resourcesTab">
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
                   checkboxButton1="general.method.label.delete"
                   checkboxCallback1="appResourceDelete"
                   searchItems="filter|Filter"
                   fields="['id','filesize','permissionClassLabel']"
                   column1="{key: 'id', label: 'console.app.resource.common.label.id', sortable: true}"
                   column2="{key: 'filesize', label: 'console.app.resource.common.label.filesize', sortable: true}"
                   column3="{key: 'permissionClassLabel', label: 'console.app.resource.common.label.permission', sortable: false}"
                   />
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonResourcesDataTable_searchTerm').hide();
    });

    <ui:popupdialog var="resourceCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/create"/>

    function addResource(){
        resourceCreateDialog.init();
    }

    function closeDialog() {
        resourceCreateDialog.close();
    }

    function appResourceDelete(selectedList){
        if (confirm('<fmt:message key="console.app.resource.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    filter(JsonResourcesDataTable, '&filter=', $('#JsonResourcesDataTable_searchCondition').val());
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/resource/delete', callback, 'ids='+selectedList);
        }
    }
    
    Template.init("#menu-apps", "#nav-app-resources");
</script>

<commons:footer />
