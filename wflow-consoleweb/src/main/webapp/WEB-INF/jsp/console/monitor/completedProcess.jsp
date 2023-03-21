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
        <ul id="main-action-buttons">
            <li><button onclick="viewGraph()"><fmt:message key="console.monitoring.common.label.viewGraph"/></button></li>
            <li><button onclick="removeProcessInstance()"><fmt:message key="console.monitoring.common.label.removeInstance"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div class="row-content">
        <dl>
            <dt><fmt:message key="adminBar.label.app"/></dt>
            <dd><a target="_blank" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${appDef.appId}"/>/<c:out value="${appDef.version}"/>/builders"><c:out value="${appDef.name}"/> v<c:out value="${appDef.version}"/></a></dd>
            <dt><fmt:message key="console.app.process.common.label.name"/></dt>
            <dd><a target="_blank" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${wfProcess.packageId}"/>/<c:out value="${appDef.version}"/>/process/builder#<c:out value="${wfProcess.idWithoutVersion}"/>"><c:out value="${wfProcess.name}"/></a>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.recordId"/></dt>
            <dd><c:out value="${recordId}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.id"/></dt>
            <dd><c:out value="${wfProcess.instanceId}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.definitionId"/></dt>
            <dd><c:out value="${wfProcess.id}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.version"/></dt>
            <dd><c:out value="${wfProcess.version}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.state"/></dt>
            <dd><c:out value="${wfProcess.state}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.serviceLevelMonitor"/></dt>
            <dd><c:out value="${serviceLevelMonitor}" escapeXml="false"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.requester"/></dt>
            <dd><c:out value="${wfProcess.requesterId}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.startedTime"/></dt>
            <dd><ui:dateToString date="${trackWflowProcess.startedTime}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.limit"/></dt>
            <dd><c:out value="${trackWflowProcess.limit}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.dueDate"/></dt>
            <dd><ui:dateToString date="${trackWflowProcess.due}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.delay"/></dt>
            <dd><c:out value="${trackWflowProcess.delay}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.finishTime"/></dt>
            <dd><ui:dateToString date="${trackWflowProcess.finishTime}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.timeConsumingFromDateStarted"/></dt>
            <dd><c:out value="${trackWflowProcess.timeConsumingFromDateStarted}"/>&nbsp;</dd>
        </dl>
        </div>
        <div id="main-body-content-subheader">
            <fmt:message key="console.monitoring.common.label.activityList"/>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/monitor/activity/list?processId=${wfProcess.instanceId}"
                       var="JsonDataTable"
                       divToUpdate="activityList"
                       jsonData="data"
                       rowsPerPage="15"
                       width="100%"
                       sort="dateCreated"
                       desc="true"
                       href="${pageContext.request.contextPath}/web/console/monitor/${mode}/process/activity/view"
                       hrefParam="id"
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       fields="['id','name','serviceLevelMonitor']"
                       column1="{key: 'id', label: 'console.app.activity.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.app.activity.common.label.name', sortable: true}"
                       column3="{key: 'state', label: 'console.app.activity.common.label.state', sortable: false}"
                       column4="{key: 'dateCreated', label: 'console.app.activity.common.label.createdTime', sortable: true}"
                       column5="{key: 'serviceLevelMonitor', label: 'console.app.activity.common.label.serviceLevelMonitor', sortable: false, relaxed: true}"
                       />
    </div>
</div>

<script>
    function removeProcessInstance(){
         if (confirm('<ui:msgEscJS key="console.monitoring.common.label.removeProcess.confirm"/>')) {
            UI.blockUI(); 
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/monitor/${mode}';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/monitor/process/delete', callback, 'ids=${wfProcess.instanceId}');
        }
    }

    function viewGraph(){
        var url = '${pageContext.request.contextPath}/web/console/monitor/process/graph/${wfProcess.instanceId}';
        window.open(url);
    }
    
    Template.init("#menu-monitor", "#nav-monitor-${mode}");
</script>

<commons:footer />
