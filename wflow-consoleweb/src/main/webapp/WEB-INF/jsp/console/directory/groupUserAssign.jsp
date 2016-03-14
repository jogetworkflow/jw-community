<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.group.user.assign.label.title"/>
    </div>

    <div id="main-body-content">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/notInGroup/list?groupId=${id}&${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="userList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="username"
                   desc="false"
                   hrefParam="id"
                   hrefQuery="false"
                   hrefDialog="false"
                   hrefDialogWidth="600px"
                   hrefDialogHeight="400px"
                   hrefDialogTitle="Process Dialog"
                   checkbox="true"
                   checkboxButton1="console.directory.group.user.assign.label"
                   checkboxCallback1="assignUsers"
                   checkboxButton2="general.method.label.cancel"
                   checkboxCallback2="closeDialog"
                   searchItems="name|Name"
                   fields="['id','username','firstName','lastName','email']"
                   column1="{key: 'username', label: 'console.directory.user.common.label.username', sortable: true}"
                   column2="{key: 'firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                   column3="{key: 'lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                   column4="{key: 'email', label: 'console.directory.user.common.label.email', sortable: true}"
                   />
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });

        function assignUsers(selectedIds){
             if (confirm('<fmt:message key="console.directory.group.user.assign.label.confirmation"/>')) {
                var callback = {
                    success : function() {
                        parent.location.reload(true);
                    }
                }
                var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/directory/group/${id}/user/assign/submit"/>', callback, 'ids='+ selectedIds);
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
