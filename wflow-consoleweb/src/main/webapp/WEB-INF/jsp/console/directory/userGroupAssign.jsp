<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.user.group.assign.label.title"/>
    </div>

    <div id="main-body-content">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.directory.group.filter.label.byOrganization"/>
            <select id="JsonDataTable_filterbyOrg" onchange="filter(JsonDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.group.empty.option.label"/></option>
            <c:forEach items="${organizations}" var="o">
                <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                <option value="<c:out value="${o.id}"/>" ${selected}><c:out value="${o.name}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/group/list?userId=${id}&inGroup=false&${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="groupList"
                   jsonData="data"
                   rowsPerPage="10"
                   width="100%"
                   sort="name"
                   desc="false"
                   hrefParam="id"
                   hrefQuery="false"
                   hrefDialog="false"
                   hrefDialogWidth="600px"
                   hrefDialogHeight="400px"
                   hrefDialogTitle="Process Dialog"
                   checkbox="true"
                   checkboxButton1="console.directory.user.group.assign.label"
                   checkboxCallback1="assignGroups"
                   checkboxButton2="general.method.label.cancel"
                   checkboxCallback2="closeDialog"
                   checkboxOptional2="true"
                   searchItems="name|Name"
                   fields="['id','name','description','organization.name']"
                   column1="{key: 'id', label: 'console.directory.group.common.label.id', sortable: true}"
                   column2="{key: 'name', label: 'console.directory.group.common.label.name', sortable: true}"
                   column3="{key: 'description', label: 'console.directory.group.common.label.description', sortable: false}"
                   column4="{key: 'organization.name', label: 'console.directory.group.common.label.organization', sortable: false}"
                   />
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });

        function assignGroups(selectedIds){
             if (confirm('<fmt:message key="console.directory.user.group.assign.label.confirmation"/>')) {
                var callback = {
                    success : function() {
                        parent.location.reload(true);
                    }
                }
                var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/directory/user/${id}/group/assign/submit"/>', callback, 'ids='+ selectedIds);
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
            url = "&orgId=" + $('#JsonDataTable_filterbyOrg').val();
            url += "&name=" + $('#JsonDataTable_searchCondition').val();
            org_filter(jsonTable, url, '');
        };
    </script>
<commons:popupFooter />
