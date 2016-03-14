<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-dashboard"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
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

        <div id="main-body-content">
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/workflow/audittrail/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="auditTrailList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="timestamp"
                       desc="true"
                       href=""
                       hrefParam="id"
                       hrefQuery="false"
                       hrefDialog="false"
                       fields="['username', 'clazz','method','message','timestamp']"
                       column1="{key: 'timestamp', label: 'console.monitoring.audittrail.label.timestamp', sortable: true}"
                       column2="{key: 'username', label: 'console.monitoring.audittrail.label.username', sortable: true}"
                       column3="{key: 'method', label: 'console.monitoring.audittrail.label.method', sortable: true}"
                       column4="{key: 'message', label: 'console.monitoring.audittrail.label.message', sortable: false}"
                       column5="{key: 'clazz', label: 'console.monitoring.audittrail.label.clazz', sortable: true}"
                       />
        </div>
    </div>
</div>

<script>
    Template.init("#menu-monitor", "#nav-monitor-audit");
</script>

<commons:footer />
