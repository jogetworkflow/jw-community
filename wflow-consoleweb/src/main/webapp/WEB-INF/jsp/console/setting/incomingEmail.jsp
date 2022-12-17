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
			<li>
			    <button onclick="onCreate()">
			        <fmt:message key="console.setting.incomingEmail.create.label" />
                </button>
            </li>
		</ul>
	</div>
	<div id="main-body">
		<ui:jsontable
			url="${pageContext.request.contextPath}/web/json/console/setting/incomingEmail/list?${pageContext.request.queryString}"
			var="JsonDataTable" 
			divToUpdate="incomingEmailContentList"
			jsonData="data"
			rowsPerPage="10" 
			width="100%" 
			sort="priority" desc="false"
			href="${pageContext.request.contextPath}/web/console/setting/incomingEmail/edit"
			hrefParam="id" 
			hrefQuery="false" 
			hrefDialog="true" 
			hrefDialogTitle=""
			checkbox="true"
			checkboxButton2="general.method.label.delete"
			checkboxCallback2="incomingEmailDelete"
			searchItems="host|username"
			fields="['id','priority', 'username','protocol','host','port','folder']"
			column1="{key: 'priority', label: 'console.setting.incomingEmail.common.label.priority', sortable: true}"
			column2="{key: 'username', label: 'console.setting.incomingEmail.common.label.username', sortable: true}"
			column3="{key: 'protocol', label: 'console.setting.incomingEmail.common.label.protocol', sortable: true}"
			column5="{key: 'host', label: 'console.setting.incomingEmail.common.label.host', sortable: true}"
			column6="{key: 'port', label: 'console.setting.incomingEmail.common.label.port', sortable: true}"
			column7="{key: 'folder', label: 'console.setting.incomingEmail.common.label.folder', sortable: true}"/>
	</div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/setting/incomingEmail/create"/>

    function onCreate(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    
	function incomingEmailDelete(selectedList) {
		if (confirm('<fmt:message key="console.setting.incomingEmail.delete.label.confirmation"/>')) {

			var callback = {
				success : function() {
					filter(JsonDataTable, '', '');
				}
			}
			var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/incomingEmail/delete', callback, 'ids=' + selectedList);
		}
	}

</script>

<script>
	Template.init("", "#nav-setting-incomingEmailContent");
</script>

<commons:footer />
