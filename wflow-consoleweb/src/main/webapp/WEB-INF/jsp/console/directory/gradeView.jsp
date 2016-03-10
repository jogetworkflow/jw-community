<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-group"></i> <fmt:message key='console.header.menu.label.users'/></p>
        <p><fmt:message key="console.directory.org.common.label"/>: <span class="nav-subtitle"><c:out value="${grade.organization.name}"/></span></p>
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
            <li><button onclick="onEdit()"><fmt:message key="console.directory.grade.edit.label"/></button></li>
            <li><button onclick="onDelete()"><fmt:message key="console.directory.grade.delete.label"/></button></li>
            <li><button onclick="assignUsers()"><fmt:message key="console.directory.grade.user.assign.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <fieldset class="view">
            <legend><fmt:message key="console.directory.grade.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.org.common.label"/></label>
                <span class="form-input"><a href="${pageContext.request.contextPath}/web/console/directory/org/view/${grade.organization.id}"><c:out value="${grade.organization.name}"/></a></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.grade.common.label.id"/></label>
                <span class="form-input"><c:out value="${grade.id}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.grade.common.label.name"/></label>
                <span class="form-input"><c:out value="${grade.name}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.grade.common.label.description"/></label>
                <span class="form-input"><c:out value="${grade.description}"/></span>
            </div>
        </fieldset>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.grade.common.label.employmentList"/><span></div>
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
                </form>
            </div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/employment/list?gradeId=${grade.id}&${pageContext.request.queryString}"
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
                       checkboxButton1="console.directory.grade.user.assign.label"
                       checkboxCallback1="assignUsers"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.grade.user.unassign.label"
                       checkboxCallback2="unassignUsers"
                       searchItems="name|Name"
                       fields="['user.id','user.username','user.firstName','user.lastName','employeeCode','role','department.name']"
                       column1="{key: 'user.username', label: 'console.directory.user.common.label.username', sortable: true}"
                       column2="{key: 'user.firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                       column3="{key: 'user.lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                       column4="{key: 'employeeCode', label: 'console.directory.employment.common.label.employeeCode', sortable: true}"
                       column5="{key: 'role', label: 'console.directory.employment.common.label.role', sortable: true}"
                       column6="{key: 'department.name', label: 'console.directory.employment.common.label.department', sortable: true}"
                       />
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonUserDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager || grade.readonly}">
            $('#main-action-buttons').remove();
            $('#JsonUserDataTable_userList-buttons').remove();
        </c:if>
    });
    
    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/grade/edit/${grade.id}?orgId=${grade.organization.id}"/>
    <ui:popupdialog var="popupDialog2" src="${pageContext.request.contextPath}/web/console/directory/grade/${grade.id}/user/assign/view"/>

    function onEdit(){
        popupDialog.init();
    }

    function assignUsers(dummy){
        popupDialog2.init();
    }

    function closeDialog() {
        popupDialog.close();
        popupDialog2.close();
    }

    function onDelete(){
         if (confirm('<fmt:message key="console.directory.grade.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/org/view/${grade.organization.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/grade/delete', callback, 'ids=${grade.id}');
        }
    }

    function unassignUsers(selectedList){
         if (confirm('<fmt:message key="console.directory.grade.user.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/grade/view/${grade.id}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/grade/${grade.id}/user/unassign', callback, 'ids='+selectedList);
        }
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        if(jsonTable == JsonUserDataTable){
            url = "&deptId=" + $('#JsonUserDataTable_filterbyDept').val();
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


