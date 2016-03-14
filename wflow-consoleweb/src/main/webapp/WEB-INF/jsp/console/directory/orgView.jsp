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
            <li><button onclick="onEdit()"><fmt:message key="console.directory.org.edit.label"/></button></li>
            <li><button onclick="onDelete()"><fmt:message key="console.directory.org.delete.label"/></button></li>
            <li><button onclick="onCreateDepartment()"><fmt:message key="console.directory.department.create.label"/></button></li>
            <li><button onclick="onCreateGrade()"><fmt:message key="console.directory.grade.create.label"/></button></li>
            <li><button onclick="assignUsers()"><fmt:message key="console.directory.org.user.assign.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <fieldset class="view">
            <legend><fmt:message key="console.directory.org.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.org.common.label.id"/></label>
                <span class="form-input"><c:out value="${organization.id}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.org.common.label.name"/></label>
                <span class="form-input"><c:out value="${organization.name}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.org.common.label.description"/></label>
                <span class="form-input"><c:out value="${organization.description}"/></span>
            </div>
        </fieldset>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.org.view.label.departmentList"/><span></div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/dept/list?orgId=${organization.id}&${pageContext.request.queryString}"
                       var="JsonDeptDataTable"
                       divToUpdate="departmentList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/dept/view"
                       hrefParam="id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton1="console.directory.department.create.label"
                       checkboxCallback1="onCreateDepartment"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.department.delete.label"
                       checkboxCallback2="deleteDepartment"
                       searchItems="name|Name"
                       fields="['id','name','description','parent.name']"
                       column1="{key: 'id', label: 'console.directory.department.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.department.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.department.common.label.description', sortable: false}"
                       column4="{key: 'parent.name', label: 'console.directory.department.common.label.parentDepartment', sortable: false}"
                       />
        </div>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.org.view.label.gradeList"/><span></div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/grade/list?orgId=${organization.id}&${pageContext.request.queryString}"
                       var="JsonGradeDataTable"
                       divToUpdate="gradeList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/grade/view"
                       hrefParam="id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton1="console.directory.grade.create.label"
                       checkboxCallback1="onCreateGrade"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.grade.delete.label"
                       checkboxCallback2="deleteGrade"
                       searchItems="name|Name"
                       fields="['id','name','description']"
                       column1="{key: 'id', label: 'console.directory.grade.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.grade.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.grade.common.label.description', sortable: false}"
                       />
        </div>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.org.common.label.employmentList"/><span></div>
            <div id="main-body-content-filter">
                <form>
                <fmt:message key="console.directory.employment.filter.label.byDepartment"/>
                <select id="JsonUserDataTable_filterbyDept" onchange="filter(JsonUserDataTable, '&deptId=', this.options[this.selectedIndex].value)">
                    <option></option>
                <c:forEach items="${departments}" var="d">
                    <c:set var="selected"><c:if test="${d.id == param.deptId}"> selected</c:if></c:set>
                    <option value="<c:out value="${d.id}"/>" ${selected}><c:out value="${d.name}"/></option>
                </c:forEach>
                </select>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <fmt:message key="console.directory.employment.filter.label.byGrade"/>
                <select id="JsonUserDataTable_filterbyGrade" onchange="filter(JsonUserDataTable, '&gradeId=', this.options[this.selectedIndex].value)">
                    <option></option>
                <c:forEach items="${grades}" var="g">
                    <c:set var="selected"><c:if test="${g.id == param.gradeId}"> selected</c:if></c:set>
                    <option value="<c:out value="${g.id}"/>" ${selected}><c:out value="${g.name}"/></option>
                </c:forEach>
                </select>
                </form>
            </div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/employment/list?orgId=${organization.id}&${pageContext.request.queryString}"
                       var="JsonUserDataTable"
                       divToUpdate="userList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="user.username"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/user/view"
                       hrefParam="user.id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton1="console.directory.org.user.assign.label"
                       checkboxCallback1="assignUsers"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.org.user.unassign.label"
                       checkboxCallback2="unassignUsers"
                       searchItems="name|Name"
                       fields="['user.id','user.username','user.firstName','user.lastName','employeeCode','role','department.name','grade.name']"
                       column1="{key: 'user.username', label: 'console.directory.user.common.label.username', sortable: true}"
                       column2="{key: 'user.firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                       column3="{key: 'user.lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                       column4="{key: 'employeeCode', label: 'console.directory.employment.common.label.employeeCode', sortable: true}"
                       column5="{key: 'role', label: 'console.directory.employment.common.label.role', sortable: true}"
                       column6="{key: 'department.name', label: 'console.directory.employment.common.label.department', sortable: true}"
                       column7="{key: 'grade.name', label: 'console.directory.employment.common.label.grade', sortable: true}"
                       />
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDeptDataTable_searchTerm').hide();
        $('#JsonGradeDataTable_searchTerm').hide();
        $('#JsonUserDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager || organization.readonly}">
            $('#main-action-buttons').remove();
            $('#JsonDeptDataTable_departmentList-buttons').remove();
            $('#JsonGradeDataTable_gradeList-buttons').remove();
            $('#JsonUserDataTable_userList-buttons').remove();
        </c:if>
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/org/edit/${organization.id}"/>
    <ui:popupdialog var="popupDialog2" src="${pageContext.request.contextPath}/web/console/directory/dept/create?orgId=${organization.id}"/>
    <ui:popupdialog var="popupDialog3" src="${pageContext.request.contextPath}/web/console/directory/grade/create?orgId=${organization.id}"/>
    <ui:popupdialog var="popupDialog4" src="${pageContext.request.contextPath}/web/console/directory/org/${organization.id}/user/assign/view"/>

    function onEdit(){
        popupDialog.init();
    }

    function onCreateDepartment(dummy){
        popupDialog2.init();
    }

    function onCreateGrade(dummy){
        popupDialog3.init();
    }

    function assignUsers(dummy){
        popupDialog4.init();
    }

    function closeDialog() {
        popupDialog.close();
        popupDialog2.close();
        popupDialog3.close();
        popupDialog4.close();
    }

    function onDelete(){
         if (confirm('<fmt:message key="console.directory.org.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/orgs';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/org/delete', callback, 'ids=${organization.id}');
        }
    }

    function deleteDepartment(selectedList){
         if (confirm('<fmt:message key="console.directory.department.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/org/view/${organization.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/dept/delete', callback, 'ids='+selectedList);
        }
    }

    function deleteGrade(selectedList){
         if (confirm('<fmt:message key="console.directory.grade.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/org/view/${organization.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/grade/delete', callback, 'ids='+selectedList);
        }
    }

    function unassignUsers(selectedList){
         if (confirm('<fmt:message key="console.directory.org.user.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/org/view/${organization.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/org/${organization.id}/user/unassign', callback, 'ids='+selectedList);
        }
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        if(jsonTable == JsonUserDataTable){
            url = "&deptId=" + $('#JsonUserDataTable_filterbyDept').val();
            url += "&gradeId=" + $('#JsonUserDataTable_filterbyGrade').val();
            url += "&name=" + $('#JsonUserDataTable_searchCondition').val();
            org_filter(jsonTable, url, '');
        }else{
            org_filter(jsonTable, url, value);
        }
    };
</script>

<script>
    Template.init("#menu-users", "#nav-users-orgchart");
</script>

<commons:footer />

