<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">

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
            <li><button onclick="abortProcessInstance()"><fmt:message key="console.monitoring.running.label.abortProcess"/></button></li>
            <li><button onclick="removeProcessInstance()"><fmt:message key="console.monitoring.common.label.removeInstance"/></button></li>
            <li><button onclick="reevaluateProcessInstance()"><fmt:message key="console.monitoring.running.label.reevaluate"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <dl>
            <dt><fmt:message key="console.app.process.common.label.id"/></dt>
            <dd>${wfProcess.instanceId}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.definitionId"/></dt>
            <dd>${wfProcess.id}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.version"/></dt>
            <dd>${wfProcess.version}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.name"/></dt>
            <dd><a target="_blank" href="${pageContext.request.contextPath}/web/console/app/${wfProcess.packageId}//processes/${wfProcess.idWithoutVersion}">${wfProcess.name}</a>&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.state"/></dt>
            <dd>${wfProcess.state}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.serviceLevelMonitor"/></dt>
            <dd>${serviceLevelMonitor}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.startedTime"/></dt>
            <dd>${trackWflowProcess.startedTime}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.limit"/></dt>
            <dd>${trackWflowProcess.limit}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.dueDate"/></dt>
            <dd>${trackWflowProcess.due}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.delay"/></dt>
            <dd>${trackWflowProcess.delay}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.finishTime"/></dt>
            <dd>${trackWflowProcess.finishTime}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.timeConsumingFromDateCreated"/></dt>
            <dd>${trackWflowProcess.timeConsumingFromDateCreated}&nbsp;</dd>
            <dt><fmt:message key="console.app.process.common.label.timeConsumingFromDateStarted"/></dt>
            <dd>${trackWflowProcess.timeConsumingFromDateStarted}&nbsp;</dd>
        </dl>
        <div id="main-body-content-subheader">
            <fmt:message key="console.monitoring.common.label.activityList"/>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/monitor/activity/list?processId=${wfProcess.instanceId}"
                       var="JsonDataTable"
                       divToUpdate="activityList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="id"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/monitor/running/process/activity/view"
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
                       column4="{key: 'serviceLevelMonitor', label: 'console.app.activity.common.label.serviceLevelMonitor', sortable: false}"
                       />
    </div>
</div>

<script>
    function removeProcessInstance(){
         if (confirm('<fmt:message key="console.monitoring.common.label.removeProcess.confirm"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/monitor/running';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/monitor/process/delete', callback, 'ids=${wfProcess.instanceId}');
        }
    }

    function viewGraph(){
        var url = '${pageContext.request.contextPath}/web/console/monitor/process/viewGraph/${wfProcess.instanceId}';
        window.open(url);
    }

    function abortProcessInstance(){
        if (confirm('<fmt:message key="console.monitoring.running.label.abortProcess.confirm"/>')) {
            var callback = {
                success : function() {
                    alert("<fmt:message key='console.monitoring.running.label.abortProcess.success'/>");
                    document.location = '${pageContext.request.contextPath}/web/console/monitor/running';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/monitor/running/process/abort/${wfProcess.instanceId}', callback, '');
        }
    }

    function reevaluateProcessInstance(){
        if (confirm('<fmt:message key="console.monitoring.running.label.reevaluate.confirm"/>')) {
            var callback = {
                success : function() {
                    alert("<fmt:message key='console.monitoring.running.label.reevaluate.success'/>");
                    document.location = document.location;
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/monitor/running/process/reevaluate/${wfProcess.instanceId}', callback, '');
        }
    }

    Template.init("#menu-monitor", "#nav-monitor-running");
</script>

<commons:footer />
