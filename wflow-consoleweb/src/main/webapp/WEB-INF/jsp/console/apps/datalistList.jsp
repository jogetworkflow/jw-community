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
            <li><button onclick="datalistCreate()"><fmt:message key="console.datalist.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/datalist/list?${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="datalistList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="id"
                   desc="false"
                   href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builder/"
                   hrefParam="id"
                   hrefDialogWindowName="_blank"
                   hrefQuery="false"
                   hrefDialog="true"
                   hrefDialogTab="true"
                   hrefDialogTitle="Datalist Dialog"
                   checkbox="true"
                   checkboxButton1="general.method.label.delete"
                   checkboxCallback1="datalistDelete"
                   searchItems="filter|Filter"
                   fields="['id','name','description','dateCreated','dateModified']"
                   column1="{key: 'id', label: 'console.datalist.common.label.id', sortable: true}"
                   column2="{key: 'name', label: 'console.datalist.common.label.name', sortable: true}"
                   column3="{key: 'name', label: 'console.datalist.common.label.description', sortable: false}"
                   column4="{key: 'dateCreated', label: 'console.datalist.common.label.dateCreated', sortable: false}"
                   column5="{key: 'dateModified', label: 'console.datalist.common.label.dateModified', sortable: false}"
                   />

    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();
    });

    <ui:popupdialog var="datalistCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/create"/>
    function datalistCreate(){
        datalistCreateDialog.init();
    }
    function datalistDelete(selectedList){
        if (confirm('<fmt:message key="console.datalist.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalists';
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/delete', callback, 'ids='+selectedList);
        }
    }
    Template.init("#menu-apps", "#nav-app-lists");
</script>

<commons:footer />
