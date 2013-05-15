<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String css = request.getContextPath() + "/css/v3.css";
    String temp = WorkflowUtil.getSystemSetupValue("css");
    if(temp != null && temp.length() > 0)
        css = temp;
%>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/v3/joget.ico"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="<%= css %>?build=<fmt:message key="build.number"/>">
