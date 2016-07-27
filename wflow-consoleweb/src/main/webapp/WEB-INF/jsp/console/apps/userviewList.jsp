<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

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
            <li><button onclick="userviewCreate()"><fmt:message key="console.userview.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/userview/list?${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="userviewList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="id"
                   desc="false"
                   href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builder/"
                   hrefParam="id"
                   hrefDialogWindowName="_blank"
                   hrefQuery="false"
                   hrefDialog="true"
                   hrefDialogTab="true"
                   hrefDialogTitle="Userview Dialog"
                   checkbox="true"
                   checkboxButton1="general.method.label.delete"
                   checkboxCallback1="userviewDelete"
                   searchItems="filter|Filter"
                   fields="['id','name','description','dateCreated','dateModified']"
                   column1="{key: 'id', label: 'console.userview.common.label.id', sortable: true}"
                   column2="{key: 'name', label: 'console.userview.common.label.name', sortable: true}"
                   column3="{key: 'name', label: 'console.userview.common.label.description', sortable: false}"
                   column4="{key: 'dateCreated', label: 'console.userview.common.label.dateCreated', sortable: false}"
                   column5="{key: 'dateModified', label: 'console.userview.common.label.dateModified', sortable: false}"
                   />

    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();
    });

    <ui:popupdialog var="userviewCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/create"/>
    function userviewCreate(){
        userviewCreateDialog.init();
    }
    function userviewDelete(selectedList){
        if (confirm('<fmt:message key="console.userview.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/userviews';
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/userview/delete', callback, 'ids='+selectedList);
        }
    }
    Template.init("#menu-apps", "#nav-app-userviews");
</script>

<commons:footer />
