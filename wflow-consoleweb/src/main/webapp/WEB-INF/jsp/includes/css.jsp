<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String css = request.getContextPath() + "/css/v3.css";
    String temp = WorkflowUtil.getSystemSetupValue("css");
    if(temp != null && temp.length() > 0)
        css = temp;
%>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/joget/joget.ico"/>
    <link rel="stylesheet" type="text/css" href="<%= css %>?build=<fmt:message key="build.number"/>">
    <style>
        <%= WorkflowUtil.getSystemSetupValue("customCss") %>
    </style>
