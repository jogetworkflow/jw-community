<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script type="text/javascript">
    if (parent && parent.CustomBuilder) {
        parent.CustomBuilder.ajaxRenderBuilder("${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/userview/builder/<c:out value="${userviewDefinition.id}"/>");
    }
</script>



