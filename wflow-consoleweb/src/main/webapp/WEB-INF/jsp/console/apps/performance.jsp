<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>
<%@ page import="org.joget.apm.APMUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:set var="isGlowrootAvailable" value="<%= APMUtil.isGlowrootAvailable() %>"/>
<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="${theme}"/>
<c:choose>
    <c:when test="${isGlowrootAvailable}">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/apmviewer.css?build=<fmt:message key="build.number"/>">    
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/echarts/echarts.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/moment/moment.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/apmviewer?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/apmviewer.js?build=<fmt:message key="build.number"/>"></script>
        <script>
            $(document).ready(function(){
                APMViewer.init('${pageContext.request.contextPath}', ${totalMemory}, ${maxHeap}, '<ui:msgEscJS key="console.header.browser.title"/>', ${isVirtualHostEnabled}, '${appId}');
            });
        </script>

        <div id="main-body-content">
            <div class="apmviewer"></div>
        </div>
    </c:when>
    <c:otherwise>
        <div id="main-body-content">
            <h3><fmt:message key='apm.unavailable'/></h3>
            <div><fmt:message key='apm.unavailable.instruction'/></div>
        </div>    
    </c:otherwise>
</c:choose>      
<commons:popupFooter />
