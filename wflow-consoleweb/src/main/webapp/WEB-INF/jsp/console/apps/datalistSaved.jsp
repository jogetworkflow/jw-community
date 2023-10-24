<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script type="text/javascript">
    if (parent && parent.CustomBuilder) {
        var handled = false;
        var iframe = window.parent.document.getElementById('navCreateNewDialog');
        if (iframe) {
            var $ = window.parent['jQuery'];
            var field = $(iframe).data('field');
            if (field) {
                field.addNewOption('<c:out value="${datalistDefinition.id}"/>', '<c:out value="${datalistDefinition.name}"/>');
                parent.JPopup.hide('navCreateNewDialog', false);
                handled = true;
            }
        }
        
        if (!handled){
            parent.CustomBuilder.ajaxRenderBuilder("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builder/<c:out value="${datalistDefinition.id}"/>");
        }
    }
</script>



