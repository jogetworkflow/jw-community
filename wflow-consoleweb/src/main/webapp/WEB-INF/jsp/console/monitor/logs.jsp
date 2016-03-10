<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

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
        <c:if test="${!isVirtualHostEnabled}">    
        <div id="main-body-content">
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/monitor/logs/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="logsList"
                       jsonData="data"
                       rowsPerPage="20"
                       width="100%"
                       href="${pageContext.request.contextPath}/web/console/monitor/log/"
                       hrefDialogWindowName="_blank"
                       hrefParam="filename"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       fields="['filename', 'filesize', 'date']"
                       column1="{key: 'filename', label: 'console.monitoring.logs.label.filename', sortable: false}"
                       column2="{key: 'filesize', label: 'console.monitoring.logs.label.filesize', sortable: false}"
                       column3="{key: 'date', label: 'console.monitoring.logs.label.date', sortable: false}"
                       />
        </div>
        </c:if>
    </div>
</div>

<script>
    Template.init("#menu-monitor", "#nav-monitor-log");
</script>

<commons:footer />
