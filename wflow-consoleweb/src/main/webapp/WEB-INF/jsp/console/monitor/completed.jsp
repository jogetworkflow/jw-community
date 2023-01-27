<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-tachometer-alt"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
    </div>
    <div id="main-body">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.monitoring.common.label.filterByApp"/>
            <select id="JsonDataTable_filterbyApp"  onchange="filter(JsonDataTable, '&appId=', this.options[this.selectedIndex].value)">
                <option></option>
            <c:forEach items="${appDefinitionList}" var="app">
                <c:set var="selected"><c:if test="${app.id == param.appId}"> selected</c:if></c:set>
                <option value="<c:out value="${app.id}"/>" ${selected}><c:out value="${app.name}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/monitor/completed/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="processList"
                       jsonData="data"
                       rowsPerPage="15"
                       width="100%"
                       sort="createdTime"
                       desc="true"
                       href="${pageContext.request.contextPath}/web/console/monitor/completed/process/view"
                       hrefParam="id"
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="true"
                       checkboxButton1="general.method.label.delete"
                       checkboxCallback1="removeProcessInstances"
                       searchItems="processId|console.app.process.common.label.id, processName|console.app.process.common.label.name, version|console.app.process.common.label.version,recordId|console.app.process.common.label.recordId,requester|console.app.process.common.label.requester"
                       fields="['id', 'version', 'name', 'state', 'startedTime', 'due', 'serviceLevelMonitor']"
                       column1="{key: 'id', label: 'console.app.process.common.label.id', sortable: true}"
                       column2="{key: 'startedTime', label: 'console.app.process.common.label.startedTime', sortable: true}"
                       column3="{key: 'name', label: 'console.app.process.common.label.name', sortable: true}"
                       column4="{key: 'requesterId', label: 'console.app.process.common.label.requester', sortable: false}"
                       column5="{key: 'version', label: 'console.app.process.common.label.version', sortable: false}"
                       column6="{key: 'state', label: 'console.app.process.common.label.state', sortable: false}"
                       column7="{key: 'due', label: 'console.app.process.common.label.dueDate', sortable: false}"
                       column8="{key: 'serviceLevelMonitor', label: 'console.app.process.common.label.serviceLevelMonitor', sortable: false, relaxed: true}"
                       column9="{key: 'recordId', label: 'console.app.process.common.label.recordId', sortable: false}"
                       />
    </div>
</div>

<script>
    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        url = "&appId=" + $('#JsonDataTable_filterbyApp').val();
        if($('#JsonDataTable_searchCondition').val() != ""){
            url += "&" + $('#JsonDataTable_searchTerm').val() +"=" + encodeURI($('#JsonDataTable_searchCondition').val().trim());
        }
        org_filter(jsonTable, url, '');
    };

    function removeProcessInstances(selectedList){
         if (confirm('<ui:msgEscJS key="console.monitoring.common.label.removeProcess.confirm"/>')) {
            UI.blockUI(); 
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/monitor/completed';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/monitor/process/delete', callback, 'ids='+selectedList);
        }
    }

    Template.init("#menu-monitor", "#nav-monitor-completed");
</script>

<commons:footer />
