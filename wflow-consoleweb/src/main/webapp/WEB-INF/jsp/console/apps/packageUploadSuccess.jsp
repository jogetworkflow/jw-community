<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
<c:if test="${!isPublished}">
    if (top.CustomBuilder !== undefined) {
        top.CustomBuilder.ajaxRenderBuilder('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/builders');
        top.AdminBar.hideQuickOverlay();
    } else {
        parent.PopupDialog.closeDialog();
        window.open('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/builders');
    }
</c:if>
<c:if test="${isPublished}">
    if (parent && parent.AdminBar.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
    }
    alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
    top.location = '${pageContext.request.contextPath}/web/desktop';
</c:if>
</script>
