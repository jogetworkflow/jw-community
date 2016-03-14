<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String css = request.getContextPath() + "/css/v5.css";
    String temp = WorkflowUtil.getSystemSetupValue("css");
    String customCss = WorkflowUtil.getSystemSetupValue("customCss");
    if(temp != null && temp.length() > 0)
        css = temp;
%>
<c:set var="css" value="<%= css %>"/>
<c:set var="customCss" value="<%= customCss %>"/>

    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="<c:out value="${css}"/>?build=<fmt:message key="build.number"/>">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css?build=<fmt:message key="build.number"/>">
    <style>
        <c:out value="${customCss}"/>
    </style>
