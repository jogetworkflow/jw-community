<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-group"></i> <fmt:message key='console.header.menu.label.users'/></p>
        <p><fmt:message key="console.directory.org.common.label"/>: <span class="nav-subtitle"><c:out value="${department.organization.name}"/></span></p>
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
            <li><button onclick="onEdit()"><fmt:message key="console.directory.department.edit.label"/></button></li>
            <li><button onclick="onDelete()"><fmt:message key="console.directory.department.delete.label"/></button></li>
            <li><button onclick="onCreateSubDepartment()"><fmt:message key="console.directory.department.create.label.createSubDepartment"/></button></li>
            <li><button onclick="onSetHOD()"><fmt:message key="console.directory.department.hod.set.label"/></button></li>
            <c:if test="${!empty hod}">
                <li><button onclick="onRemoveHOD()"><fmt:message key="console.directory.department.hod.remove.label"/></button></li>
            </c:if>
            <li><button onclick="assignUsers()"><fmt:message key="console.directory.department.user.assign.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <fieldset class="view">
            <legend><fmt:message key="console.directory.department.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.org.common.label"/></label>
                <span class="form-input"><a href="${pageContext.request.contextPath}/web/console/directory/org/view/${department.organization.id}"><c:out value="${department.organization.name}"/></a></span>
            </div>
            <c:if test="${!empty department.parent}">
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.department.common.label.parentDepartment"/></label>
                    <span class="form-input"><a href="${pageContext.request.contextPath}/web/console/directory/dept/view/${department.parent.id}"><c:out value="${department.parent.name}"/></a></span>
                </div>
            </c:if>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.department.common.label.id"/></label>
                <span class="form-input"><c:out value="${department.id}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.department.common.label.name"/></label>
                <span class="form-input"><c:out value="${department.name}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.department.common.label.description"/></label>
                <span class="form-input"><c:out value="${department.description}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.department.common.label.hod"/></label>
                <span class="form-input">
                    <c:if test="${!empty hod}">
                        <a href="${pageContext.request.contextPath}/web/console/directory/user/view/${hod.id}."><c:out value="${hod.username}"/></a>
                    </c:if>
                </span>
            </div>
        </fieldset>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.department.common.label.subDepartmentList"/><span></div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/subdept/list?deptId=${department.id}&${pageContext.request.queryString}"
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
                       checkboxButton1="console.directory.department.create.label.createSubDepartment"
                       checkboxCallback1="onCreateSubDepartment"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.department.delete.label.deleteSubDepartment"
                       checkboxCallback2="deleteDepartment"
                       searchItems="name|Name"
                       fields="['id','name','description']"
                       column1="{key: 'id', label: 'console.directory.department.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.department.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.department.common.label.description', sortable: false}"
                       />
        </div>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.department.common.label.employmentList"/><span></div>
            <div id="main-body-content-filter">
                <form>
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
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/employment/list?deptId=${department.id}&${pageContext.request.queryString}"
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
                       checkboxButton1="console.directory.department.user.assign.label"
                       checkboxCallback1="assignUsers"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.department.user.unassign.label"
                       checkboxCallback2="unassignUsers"
                       searchItems="name|Name"
                       fields="['user.id','user.username','user.firstName','user.lastName','employeeCode','role','grade.name']"
                       column1="{key: 'user.username', label: 'console.directory.user.common.label.username', sortable: true}"
                       column2="{key: 'user.firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                       column3="{key: 'user.lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                       column4="{key: 'employeeCode', label: 'console.directory.employment.common.label.employeeCode', sortable: true}"
                       column5="{key: 'role', label: 'console.directory.employment.common.label.role', sortable: true}"
                       column6="{key: 'grade.name', label: 'console.directory.employment.common.label.grade', sortable: true}"
                       />
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDeptDataTable_searchTerm').hide();
        $('#JsonUserDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager || department.readonly}">
            $('#main-action-buttons').remove();
            $('#JsonDeptDataTable_departmentList-buttons').remove();
            $('#JsonUserDataTable_userList-buttons').remove();
        </c:if>
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/dept/edit/${department.id}?orgId=${department.organization.id}"/>
    <ui:popupdialog var="popupDialog2" src="${pageContext.request.contextPath}/web/console/directory/dept/create?orgId=${department.organization.id}&parentId=${department.id}"/>
    <ui:popupdialog var="popupDialog3" src="${pageContext.request.contextPath}/web/console/directory/dept/${department.id}/hod/set/view"/>
    <ui:popupdialog var="popupDialog4" src="${pageContext.request.contextPath}/web/console/directory/dept/${department.id}/user/assign/view"/>

    function onEdit(){
        popupDialog.init();
    }

    function onCreateSubDepartment(dummy){
        popupDialog2.init();
    }

    function closeDialog() {
        popupDialog.close();
        popupDialog2.close();
        popupDialog3.close();
        popupDialog4.close();
    }

    function onSetHOD(){
        popupDialog3.init();
    }

    function assignUsers(dummy){
        popupDialog4.init();
    }

    function onRemoveHOD(){
        if (confirm('<fmt:message key="console.directory.department.hod.remove.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/dept/view/${department.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/dept/${department.id}/hod/remove', callback, 'userId=<c:if test="${!empty hod}">${hod.id}</c:if>');
        }
    }

    function onDelete(){
        if (confirm('<fmt:message key="console.directory.department.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/org/view/${department.organization.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/dept/delete', callback, 'ids=${department.id}');
        }
    }

    function deleteDepartment(selectedList){
        if (confirm('<fmt:message key="console.directory.department.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/dept/view/${department.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/dept/delete', callback, 'ids='+selectedList);
        }
    }

    function unassignUsers(selectedList){
         if (confirm('<fmt:message key="console.directory.department.user.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/dept/view/${department.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/dept/${department.id}/user/unassign', callback, 'ids='+selectedList);
        }
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        if(jsonTable == JsonUserDataTable){
            url = "&gradeId=" + $('#JsonUserDataTable_filterbyGrade').val();
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



