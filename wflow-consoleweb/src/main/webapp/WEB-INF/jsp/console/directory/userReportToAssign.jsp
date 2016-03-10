<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.user.reportTo.assign.label.title"/>
    </div>

    <div id="main-body-content">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.directory.employment.filter.label.byDepartment"/>
            <select id="JsonDataTable_filterbyDept" onchange="filter(JsonDataTable, '&deptId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.dept.empty.option.label"/></option>
            <c:forEach items="${departments}" var="d">
                <c:set var="selected"><c:if test="${d.id == param.deptId}"> selected</c:if></c:set>
                <option value="<c:out value="${d.id}"/>" ${selected}><c:out value="${d.name}"/></option>
            </c:forEach>
            </select>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <fmt:message key="console.directory.employment.filter.label.byGrade"/>
            <select id="JsonDataTable_filterbyGrade" onchange="filter(JsonDataTable, '&gradeId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.grade.empty.option.label"/></option>
            <c:forEach items="${grades}" var="g">
                <c:set var="selected"><c:if test="${g.id == param.gradeId}"> selected</c:if></c:set>
                <option value="<c:out value="${g.id}"/>" ${selected}><c:out value="${g.name}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/employment/list?orgId=${organizationId}&${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="employmentList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="user.username"
                   desc="false"
                   hrefParam="user.id"
                   hrefQuery="false"
                   hrefDialog="false"
                   hrefDialogWidth="600px"
                   hrefDialogHeight="400px"
                   hrefDialogTitle="Process Dialog"
                   checkbox="true"
                   checkboxId="user.id"
                   checkboxSelectSingle="true"
                   checkboxButton1="console.directory.user.reportTo.assign.label"
                   checkboxCallback1="assignReportTo"
                   checkboxButton2="general.method.label.cancel"
                   checkboxCallback2="closeDialog"
                   checkboxOptional2="true"
                   searchItems="name|Name"
                   fields="['user.id','user.username','user.firstName','user.lastName','employeeCode','role','deparment.name', 'grade.name']"
                   column1="{key: 'user.username', label: 'console.directory.user.common.label.username', sortable: true}"
                   column2="{key: 'user.firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                   column3="{key: 'user.lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                   column4="{key: 'employeeCode', label: 'console.directory.employment.common.label.employeeCode', sortable: true}"
                   column5="{key: 'role', label: 'console.directory.employment.common.label.role', sortable: true}"
                   column6="{key: 'department.name', label: 'console.directory.employment.common.label.department', sortable: true}"
                   column7="{key: 'grade.name', label: 'console.directory.employment.common.label.grade', sortable: true}"
                   />
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });

        function assignReportTo(selectedId){
             if (confirm('<fmt:message key="console.directory.user.reportTo.assign.label.confirmation"/>')) {
                var callback = {
                    success : function() {
                        parent.location.reload(true);
                    }
                }
                var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/directory/user/${id}/reportTo/assign/submit"/>', callback, 'userId='+ selectedId);
            }
        }

        function closeDialog(dummy) {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }

        var org_filter = window.filter;
        var filter = function(jsonTable, url, value){
            url = "&deptId=" + $('#JsonDataTable_filterbyDept').val();
            url += "&gradeId=" + $('#JsonDataTable_filterbyGrade').val();
            url += "&name=" + $('#JsonDataTable_searchCondition').val();
            org_filter(jsonTable, url, '');
        };
    </script>
<commons:popupFooter />
