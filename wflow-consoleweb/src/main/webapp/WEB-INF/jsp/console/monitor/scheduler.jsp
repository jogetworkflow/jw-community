<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp"%>
<%@ page import="org.joget.commons.util.HostManager"%>

<commons:header />

<div id="nav">
	<div id="nav-title">
		<p>
			<i class="icon-dashboard"></i>
			<fmt:message key='console.header.menu.label.monitor' />
		</p>
	</div>
	<div id="nav-body">
		<ul id="nav-list">
			<jsp:include page="subMenu.jsp" flush="true" />
		</ul>
	</div>
</div>

<div id="main">
	<div id="main-title"></div>
	<div id="main-action"></div>
	<div id="main-body">
		<div id="main-body-content">
			<ui:jsontable
				url="${pageContext.request.contextPath}/web/json/console/monitor/scheduler/list?${pageContext.request.queryString}"
				var="JsonDataTable" 
				divToUpdate="schedulerList" 
				jsonData="data"
				rowsPerPage="20" width="100%"
				href="${pageContext.request.contextPath}/web/console/monitor/scheduler/view"
				hrefParam="id" 
				hrefQuery="false" 
				hrefDialog="true"
				hrefDialogTitle="" 
				searchItems="jobName|Job Name"
				fields="['id','jobName', 'jobClassName', 'finishTime', 'jobStatus', 'message']"
				column1="{key: 'jobName', label: 'console.monitor.scheduler.common.label.jobName', sortable: false}"
				column2="{key: 'jobClassName', label: 'console.monitor.scheduler.common.label.jobClassName', sortable: false}"
				column3="{key: 'finishTime', label: 'console.monitor.scheduler.common.label.finishTime', sortable: false}"
				column4="{key: 'jobStatus', label: 'console.monitor.scheduler.common.label.jobStatus', sortable: false}"
				column5="{key: 'message', label: 'console.monitor.scheduler.common.label.message', sortable: false}" />
		</div>
	</div>
</div>

<script>
	Template.init("#menu-monitor", "#nav-monitor-scheduler");
</script>

<commons:footer />
