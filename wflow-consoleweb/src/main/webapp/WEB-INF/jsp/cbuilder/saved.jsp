<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script type="text/javascript">
    if (parent && parent.CustomBuilder) {
        parent.CustomBuilder.ajaxRenderBuilder("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/design/<c:out value="${builderDefinition.id}"/>");
    }
</script>



