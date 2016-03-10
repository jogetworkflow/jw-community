<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
<c:if test="${!isPublished}">
    if (parent && parent.AdminBar.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
        parent.AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms')
    } else {
        parent.location = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms';
    }
</c:if>
<c:if test="${isPublished}">
    if (parent && parent.AdminBar.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
    }
    alert("<fmt:message key="appCenter.label.appInstalled"/>");
    top.location = '${pageContext.request.contextPath}/web/desktop';
</c:if>
</script>
