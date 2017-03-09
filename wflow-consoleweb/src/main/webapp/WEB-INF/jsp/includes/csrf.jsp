<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${!csrfInitialized}">
<script src="${pageContext.request.contextPath}/csrf"></script>
<script src="${pageContext.request.contextPath}/wro/csrfguard.min.js"></script>
<c:set var="csrfInitialized" scope="request" value="true"/>
</c:if>