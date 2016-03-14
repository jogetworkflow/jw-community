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
            <li><button onclick="onEdit()"><fmt:message key="console.directory.group.edit.label"/></button></li>
            <li><button onclick="onDelete()"><fmt:message key="console.directory.group.delete.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <fieldset class="view">
            <legend><fmt:message key="console.directory.group.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.group.common.label.id"/></label>
                <span class="form-input"><c:out value="${group.id}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.group.common.label.name"/></label>
                <span class="form-input"><c:out value="${group.name}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.group.common.label.description"/></label>
                <span class="form-input"><c:out value="${group.description}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.group.common.label.organization"/></label>
                <span class="form-input"><a href="${pageContext.request.contextPath}/web/console/directory/org/view/${group.organization.id}"><c:out value="${group.organization.name}"/></a></span>
            </div>
        </fieldset>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.group.common.label.userList"/><span></div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/list?groupId=${group.id}&${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="userList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="username"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/user/view"
                       hrefParam="id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton1="console.directory.group.user.assign.label"
                       checkboxCallback1="assignUsers"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.group.user.unassign.label"
                       checkboxCallback2="unassignUsers"
                       searchItems="name|Name"
                       fields="['id','username','firstName','lastName','email']"
                       column1="{key: 'username', label: 'console.directory.user.common.label.username', sortable: true}"
                       column2="{key: 'firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                       column3="{key: 'lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                       column4="{key: 'email', label: 'console.directory.user.common.label.email', sortable: true}"
                       />
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager || group.readonly}">
            $('#main-action-buttons').remove();
            $('#JsonDataTable_userList-buttons').remove();
        </c:if>
    });
    
    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/group/edit/${group.id}"/>
    <ui:popupdialog var="popupDialog2" src="${pageContext.request.contextPath}/web/console/directory/group/${group.id}/user/assign/view"/>

    function onEdit(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
        popupDialog2.close();
    }

    function assignUsers(dummy){
        popupDialog2.init();
    }

    function unassignUsers(selectedIds){
         if (confirm('<fmt:message key="console.directory.group.user.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/group/view/${group.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/group/${group.id}/user/unassign', callback, 'ids='+ selectedIds);
        }
    }

    function onDelete(){
         if (confirm('<fmt:message key="console.directory.group.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/groups';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/group/delete', callback, 'ids=${group.id}');
        }
    }
</script>

<script>
    Template.init("#menu-users", "#nav-users-groups");
</script>

<commons:footer />


