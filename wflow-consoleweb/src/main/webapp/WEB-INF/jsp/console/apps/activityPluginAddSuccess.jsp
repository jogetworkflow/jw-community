<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
    window.location = "<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}/activity/${activityDefId}/plugin/configure"/>?title=<c:out value="${param.title}" escapeXml="true" />";
</script>


