<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="${theme}"/>
<style>
    #main {margin: 0 !important; padding: 0!important;}
    .form-row {padding-top: 25px; padding-left: 20px;}
</style>    
<jsp:include page="../log/log.jsp" flush="true" />
<commons:popupFooter />
