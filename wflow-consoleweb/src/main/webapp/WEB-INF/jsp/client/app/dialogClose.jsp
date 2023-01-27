<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
<c:choose>
    <c:when test="${!empty url}">
        if (parent != self) {
            parent.location.href="${url}";
        } else {
            location.href="${url}";
        }
    </c:when>
    <c:otherwise>
        if (parent && parent.PopupDialog && parent.PopupDialog.closeDialog) {
            parent.PopupDialog.closeDialog();
        }
    </c:otherwise>
</c:choose>
</script>
