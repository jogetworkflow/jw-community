<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.org.user.assign.label.title"/>
    </div>

    <div id="main-body-content">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/employment/notInOrganization/list?orgId=${id}&${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="employmentList"
                   jsonData="data"
                   rowsPerPage="15"
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
                   checkboxButton1="console.directory.org.user.assign.label"
                   checkboxCallback1="assignUsers"
                   checkboxButton2="general.method.label.cancel"
                   checkboxCallback2="closeDialog"
                   checkboxOptional2="true"
                   searchItems="name|Name"
                   fields="['user.id','user.username','user.firstName','user.lastName','employeeCode','role']"
                   column1="{key: 'user.username', label: 'console.directory.user.common.label.username', sortable: true}"
                   column2="{key: 'user.firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                   column3="{key: 'user.lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                   column4="{key: 'employeeCode', label: 'console.directory.employment.common.label.employeeCode', sortable: true}"
                   column5="{key: 'role', label: 'console.directory.employment.common.label.role', sortable: true}"
                   />
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });

        function assignUsers(selectedIds){
             if (confirm('<ui:msgEscJS key="console.directory.org.user.assign.label.confirmation"/>')) {
                UI.blockUI(); 
                var callback = {
                    success : function() {
                        UI.unblockUI();
                        parent.location.reload(true);
                    }
                }
                var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/directory/org/${id}/user/assign/submit"/>', callback, 'ids='+ selectedIds);
            }
        }

        function closeDialog(dummy) {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
