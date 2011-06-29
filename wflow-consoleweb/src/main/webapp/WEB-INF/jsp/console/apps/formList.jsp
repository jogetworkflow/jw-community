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
            <li><button onclick="formCreate()"><fmt:message key="console.form.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/forms?${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="formList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="name"
                   desc="false"
                   href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/builder/"
                   hrefParam="id"
                   hrefDialogWindowName="_blank"
                   hrefQuery="false"
                   hrefDialog="true"
                   hrefDialogTab="true"
                   hrefDialogTitle="Form Dialog"
                   checkbox="true"
                   checkboxButton1="general.method.label.delete"
                   checkboxCallback1="formDelete"
                   searchItems="name|Form Name"
                   fields="['id','name','dateCreated','dateModified']"
                   column1="{key: 'name', label: 'console.form.common.label.name', sortable: true}"
                   column2="{key: 'tableName', label: 'console.form.common.label.tableName', sortable: false}"
                   column3="{key: 'dateCreated', label: 'console.form.common.label.dateCreated', sortable: false}"
                   column4="{key: 'dateModified', label: 'console.form.common.label.dateModified', sortable: false}"
                   />

    </div>
</div>

<script>
    <ui:popupdialog var="formCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/create"/>
    function formCreate(){
        formCreateDialog.init();
    }
    function formDelete(selectedList){
        if (confirm('<fmt:message key="console.form.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms';
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/delete', callback, 'formId='+selectedList);
        }
    }
    Template.init("#menu-apps", "#nav-app-forms");
</script>

<commons:footer />
