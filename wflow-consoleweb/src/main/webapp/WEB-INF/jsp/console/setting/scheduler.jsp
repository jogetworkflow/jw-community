<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil,org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%=HostManager.isVirtualHostEnabled()%>" />

<commons:header />

<div id="nav">
	<div id="nav-title">
		<p>
			<i class="icon-cogs"></i>
			<fmt:message key='console.header.top.label.settings' />
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
	<div id="main-action">
		<ul id="main-action-buttons">
			<li><button onclick="onCreate()">
					<fmt:message key="console.setting.scheduler.create.label" />
				</button></li>
		</ul>
	</div>
	<div id="main-body">
		<ui:jsontable
			url="${pageContext.request.contextPath}/web/json/console/setting/scheduler/list?${pageContext.request.queryString}"
			var="JsonDataTable" 
			divToUpdate="schedulerContentList" 
			jsonData="data"
			rowsPerPage="10" 
			width="100%" 
			sort="jobName" desc="false"
			href="${pageContext.request.contextPath}/web/console/setting/scheduler/edit"
			hrefParam="id" 
			hrefQuery="false" 
			hrefDialog="true" 
			hrefDialogTitle=""
			checkbox="true"
			checkboxButton2="general.method.label.delete"
			checkboxCallback2="schedulerDetailsDelete"
			checkboxButton3="console.setting.scheduler.common.label.fireNow"
			checkboxCallback3="schedulerDetailsFireNow" 
			searchItems="jobName|Job Name"
			fields="['id','jobName','groupJobName','triggerName','groupTriggerName','jobClassName','modifiedate']"
			column1="{key: 'jobName', label: 'console.setting.scheduler.common.label.jobName', sortable: true}"
			column2="{key: 'groupJobName', label: 'console.setting.scheduler.common.label.groupJobName', sortable: false}" 
			column3="{key: 'triggerName', label: 'console.setting.scheduler.common.label.triggerName', sortable: false}"
			column5="{key: 'groupTriggerName', label: 'console.setting.scheduler.common.label.groupTriggerName', sortable: false}" 
			column6="{key: 'jobClassName', label: 'console.setting.scheduler.common.label.jobClassName', sortable: false}"
			column7="{key: 'modifiedate', label: 'console.setting.scheduler.common.label.modifiedate', sortable: false}"/>
	</div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/setting/scheduler/create"/>

    function onCreate(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    
	function schedulerDetailsDelete(selectedList) {
		if (confirm('<fmt:message key="console.setting.scheduler.delete.label.confirmation"/>')) {

			var callback = {
				success : function() {
					filter(JsonDataTable, '', '');
				}
			}
			var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/scheduler/delete', callback, 'ids=' + selectedList);
		}
	}
	
	function schedulerDetailsFireNow(selectedList) {
		if (confirm('<fmt:message key="console.setting.scheduler.fireNow.label.confirmation"/>')) {

			var callback = {
				success : function() {
					filter(JsonDataTable, '', '');
				}
			}
			var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/scheduler/firenow', callback, 'ids=' + selectedList);
		}
	}
</script>

<script>
	Template.init("", "#nav-setting-schedulerContent");
</script>

<commons:footer />
