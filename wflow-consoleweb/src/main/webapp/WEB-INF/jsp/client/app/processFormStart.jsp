<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>
 
<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}"/>

    <c:if test="${not empty theme and theme ne 'classic'}">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
    </c:if>
        
<div id="main-body-header"><c:out value="${process.name}" escapeXml="true"/></div>

<div id="main-body-content">
    <jsp:include page="formView.jsp" flush="true" />
</div>

<commons:popupFooter />
