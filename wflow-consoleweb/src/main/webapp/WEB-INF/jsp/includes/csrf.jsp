<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${!csrfInitialized}">
<script src="${pageContext.request.contextPath}/csrf"></script>
<c:set var="csrfInitialized" scope="request" value="true"/>
</c:if>