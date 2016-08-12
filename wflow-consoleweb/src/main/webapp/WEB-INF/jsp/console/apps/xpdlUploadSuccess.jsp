<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
    if (parent && parent.AdminBar.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
        parent.AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/processes')
    } else {
        parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/processes';
    }
</script>