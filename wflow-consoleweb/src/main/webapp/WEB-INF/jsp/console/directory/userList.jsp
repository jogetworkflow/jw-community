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
            <li><button onclick="onCreate()"><fmt:message key="console.directory.user.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.directory.user.filter.label.byOrganization"/>
            <select id="JsonDataTable_filterbyOrg" onchange="filter(JsonDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.user.empty.option.label"/></option>
            <c:forEach items="${organizations}" var="o">
                <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                <option value="<c:out value="${o.id}"/>" ${selected}><c:out value="${o.name}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/list?${pageContext.request.queryString}"
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
                       checkboxButton2="general.method.label.delete"
                       checkboxCallback2="deleteUser"
                       searchItems="name|Name"
                       fields="['id','username','firstName','lastName','email']"
                       column1="{key: 'username', label: 'console.directory.user.common.label.username', sortable: true}"
                       column2="{key: 'firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                       column3="{key: 'lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                       column4="{key: 'email', label: 'console.directory.user.common.label.email', sortable: true}"
                       column5="{key: 'active', label: 'console.directory.user.common.label.status', sortable: true}"
                       />

    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager}">
            $('#main-action-buttons').remove();
            $('#JsonDataTable_userList-buttons').remove();
        </c:if>
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/user/create"/>

    function onCreate(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    function deleteUser(selectedList){
         if (confirm('<fmt:message key="console.directory.user.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/users';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/user/delete', callback, 'ids='+selectedList);
        }
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        url = "&orgId=" + $('#JsonDataTable_filterbyOrg').val();
        url += "&name=" + $('#JsonDataTable_searchCondition').val();
        org_filter(jsonTable, url, '');
    };
</script>

<script>
    Template.init("#menu-users", "#nav-users-users");
</script>

<commons:footer />
