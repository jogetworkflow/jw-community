<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<c:if test="${!cssJspInRequest}">
    <c:set var="cssJspInRequest" scope="request" value="true"/>
    <%
        String css = WorkflowUtil.getSystemSetupValue("css");
        String customCss = WorkflowUtil.getSystemSetupValue("customCss");
    %>
    <c:set var="css" value="<%= css %>"/>
    <c:set var="customCss" value="<%= customCss %>"/>
    <c:if test="${!empty css}">
    <link rel="stylesheet" type="text/css" href="<c:out value="${css}"/>?build=<fmt:message key="build.number"/>">
    </c:if>
    <style>
    <c:out value="${customCss}"/>
    </style>
</c:if>