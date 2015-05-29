<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:include page="header.jsp" flush="true" />

<div id="nav">
    <div id="nav-title">
        <a href="#">Home</a> &gt; Setup Users
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <li>
                <ul class="nav-sublist">

                    <li><input type="checkbox" checked disabled /><a id="nav-users-orgchart" class="nav-link" href="orgchart.jsp">i. Organization Chart</a></li>
                    <li><input type="checkbox" checked disabled /><a id="nav-users-groups" class="nav-link" href="groups.jsp">ii. Groups</a></li>
                    <li><input type="checkbox" disabled /><a id="nav-users-users" class="nav-link" href="users.jsp">iii. Users</a></li>
                </ul>
            </li>
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button>Create New User</button></li>
        </ul>
    </div>
    <div id="main-body">

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="userList"
                       jsonData="data"
                       rowsPerPage="10"
                       sort="username"
                       desc="false"
                       width="100%"
                       href="${pageContext.request.contextPath}/web/directory/admin/user/view"
                       hrefParam="id"
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="true"
                       checkboxButton2="Delete"
                       checkboxCallback2="removeUser"
                       searchItems="username|Username/First Name"
                       fields="['id', 'username','firstName','lastName']"
                       column1="{key: 'username', label: 'Username', sortable: true}"
                       column2="{key: 'firstName', label: 'First Name', sortable: false}"
                       column3="{key: 'lastName', label: 'Last Name', sortable: false}"
                       />

    </div>
    <div id="footer">
        Footer
    </div>
</div>

<script>
    Template.init("#menu-users", "#nav-users-users");
</script>

<jsp:include page="footer.jsp" flush="true" />
