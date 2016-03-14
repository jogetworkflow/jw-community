<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-group"></i> <fmt:message key='console.header.menu.label.users'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="directorySubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="onCreate()"><fmt:message key="console.directory.org.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/organization/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="organizationList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/org/view"
                       hrefParam="id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton2="general.method.label.delete"
                       checkboxCallback2="deleteOrganization"
                       searchItems="name|Organization Name"
                       fields="['id','name','description']"
                       column1="{key: 'id', label: 'console.directory.org.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.org.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.org.common.label.description', sortable: false}"
                       />

    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager}">
            $('#main-action-buttons').remove();
            $('#JsonDataTable_organizationList-buttons').remove();
        </c:if>
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/org/create"/>

    function onCreate(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    function deleteOrganization(selectedList){
         if (confirm('<fmt:message key="console.directory.org.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/orgs';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/org/delete', callback, 'ids='+selectedList);
        }
    }
</script>

<script>
    Template.init("#menu-users", "#nav-users-orgchart");
</script>

<commons:footer />
