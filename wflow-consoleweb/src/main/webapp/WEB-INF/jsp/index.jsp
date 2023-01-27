<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<c:set var="username" value="<%= WorkflowUtil.getCurrentUsername() %>"/>
<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>

<c:set var="homeUrl" value="${pageContext.request.contextPath}/web/login"/>
<c:if test="${!isAnonymous}">
    <c:set var="homeUrl" value="${pageContext.request.contextPath}/web/client/assignment/inbox"/>
    <c:if test="${isAdmin}">
        <c:set var="homeUrl" value="${pageContext.request.contextPath}/web/admin/package/upload"/>
    </c:if>
</c:if>

<html>
<head>
<title></title>
<meta http-equiv="REFRESH" content="0;url=${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${homeUrl}"></HEAD>
<body>
</body>
</html>