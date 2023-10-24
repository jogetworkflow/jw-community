<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script type="text/javascript">
    if (parent && parent.CustomBuilder) {
        var handled = false;
        var iframe = window.parent.document.getElementById('navCreateNewDialog');
        if (iframe) {
            var $ = window.parent['jQuery'];
            var field = $(iframe).data('field');
            if (field) {
                field.addNewOption('<c:out value="${builderDefinition.id}"/>', '<c:out value="${builderDefinition.name}"/>');
                parent.JPopup.hide('navCreateNewDialog', false);
                handled = true;
            }
        }
        
        if (!handled){
            parent.CustomBuilder.ajaxRenderBuilder("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/design/<c:out value="${builderDefinition.id}"/>");
        }
    }
</script>



