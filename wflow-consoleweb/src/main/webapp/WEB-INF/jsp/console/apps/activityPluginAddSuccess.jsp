<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
    window.location = "<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}/${activityType}/${activityDefId}/plugin/configure"/>?title=<c:out value="${param.title}" escapeXml="true" />&param_tab=<c:out value="${param.tab}" escapeXml="true" />&pluginname=${pluginName}";
</script>


