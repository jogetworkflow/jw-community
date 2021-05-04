<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
<c:choose>
    <c:when test="${!empty url}">
        if ('<c:out value="${url}"/>'.indexOf('/builders') !== -1) {
            if (top.CustomBuilder !== undefined) {
                top.CustomBuilder.ajaxRenderBuilder('<c:out value="${url}"/>');
                top.AdminBar.hideQuickOverlay();
            } else {
                parent.PopupDialog.closeDialog();
                window.open('<c:out value="${url}"/>');
            }
        } else if (parent && parent.AdminBar !== undefined && parent.AdminBar.isAdminBarOpen()) {
            parent.PopupDialog.closeDialog();
            parent.AdminBar.showQuickOverlay('<c:out value="${url}"/>');
        } else if (parent && parent.reloadTable) {
            if(parent.PopupDialog) {
                parent.PopupDialog.closeDialog();
            }
            parent.reloadTable();
        } else {
            if (parent != self) {
                parent.location.href="<c:out value="${url}"/>";
            } else {
                location.href="<c:out value="${url}"/>";
            }
        }
    </c:when>
    <c:when test="${!empty script}">
        ${script}
    </c:when>   
    <c:otherwise>
        if (parent && parent.PopupDialog.closeDialog) {
            parent.PopupDialog.closeDialog();
        }
    </c:otherwise>
</c:choose>
</script>
