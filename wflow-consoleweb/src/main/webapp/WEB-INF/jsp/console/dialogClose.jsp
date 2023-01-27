<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
<c:choose>
    <c:when test="${!empty url}">
        if (parent && parent.AdminBar !== undefined && parent.AdminBar.isAdminBarOpen()) {
            parent.PopupDialog.closeDialog();
            parent.AdminBar.showQuickOverlay('<c:out value="${url}"/>');
        } else {
            if (parent != self) {
                parent.location.href="<c:out value="${url}"/>";
            } else {
                location.href="<c:out value="${url}"/>";
            }
        }
    </c:when>
    <c:otherwise>
        if (parent && parent.PopupDialog.closeDialog) {
            parent.PopupDialog.closeDialog();
        }
    </c:otherwise>
</c:choose>
</script>
