<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<div id="main-body-header">
    <fmt:message key="console.monitoring.running.label.reassign"/>
</div>

<div id="main-body-content" style="text-align: left">
    <p id="userToReplace">
        <label><fmt:message key="console.monitoring.running.label.reassign.select"/></label>
        <select id="replaceUser" name="replaceUser">
            <c:forEach var="assignmentUser" items="${trackWflowActivity.assignmentUsers}" varStatus="index">
                <option value="<c:out value="${assignmentUser}"/>"><c:out value="${assignmentUser}"/></option>
            </c:forEach>
        </select>
    </p>
    <div id="user">
        <div class="main-body-content-filter">
            <form>
            <fmt:message key="console.directory.user.filter.label.byOrganization"/>
            <select id="userDataTable_filterbyOrg" onchange="filter(userDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.user.empty.option.label"/></option>
                <c:forEach items="${organizations}" var="o">
                    <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                    <option value="${o.id}" ${selected}><c:out value="${o.name}"/></option>
                </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/list?${pageContext.request.queryString}"
                      var="userDataTable"
                      divToUpdate="userList"
                      jsonData="data"
                      rowsPerPage="10"
                      width="100%"
                      height="200"
                      sort="firstName"
                      desc="false"
                      href=""
                      hrefParam="username"
                      hrefSuffix="."
                      hrefQuery="false"
                      hrefDialog="false"
                      hrefDialogWidth="600px"
                      hrefDialogHeight="400px"
                      hrefDialogTitle=""
                      checkbox="true"
                      checkboxSelectSingle="true"
                      checkboxId="username"
                      checkboxButton1="general.method.label.submit"
                      checkboxCallback1="submitUser"
                      checkboxSelection="true"
                      checkboxSelectionTitle="Selected Users"
                      searchItems="name|Username/First Name"
                      fields="['id','username','firstName','lastName']"
                      column1="{key: 'username', label: 'console.directory.user.common.label.username', sortable: true}"
                      column2="{key: 'firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                      column3="{key: 'lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                      />
    </div>
</div>
<script>
    $(document).ready(function(){
        $('#userDataTable_searchTerm').hide();
    });
    function submitUser(username){
        if(username.length > 0){
            if (confirm("<fmt:message key="console.monitoring.running.label.reassign.confirm"/>")) {
                var callback = {
                    success : function() {
                       parent.location.reload(true);
                    }
                }
                var replaceUser = $('#replaceUser').val();
                if($('#replaceUser option[value="'+username+'"]').length > 0){
                    alert('<fmt:message key="console.monitoring.running.label.reassign.error"/>');
                }else{
                    var params = "username=" + username + "&state=<c:out value="${state}"/>&processDefId=<c:out value="${processDefId}"/>&activityId=<c:out value="${activityId}"/>&processId=<c:out value="${processId}"/>&replaceUser=" + escape(replaceUser);
                    ConnectionManager.post('${pageContext.request.contextPath}/web/json/monitoring/running/activity/reassign', callback, params);
                }
            }
        }
    }
    
    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        url = "&orgId=" + $('#userDataTable_filterbyOrg').val();
        url += "&name=" + $('#userDataTable_searchCondition').val();
        org_filter(jsonTable, url, '');
    };
</script>

<commons:popupFooter />
