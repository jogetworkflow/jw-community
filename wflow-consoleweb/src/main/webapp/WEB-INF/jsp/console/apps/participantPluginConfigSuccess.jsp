<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
    parent.location.href = "<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}?tab=participantList&participantId=${participantId}"/>";
</script>