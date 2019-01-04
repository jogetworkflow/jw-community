<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
<div id="nav">
    <div id="nav-title">
        <jsp:include page="appTitle.jsp" flush="true" />
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="appSubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>
        
<jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/apmviewer.css?build=<fmt:message key="build.number"/>">    
<script type="text/javascript" src="${pageContext.request.contextPath}/js/echarts/echarts.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment/moment.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/apmviewer?build=<fmt:message key="build.number"/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/apmviewer.js?build=<fmt:message key="build.number"/>"></script>
<script>
    $(document).ready(function(){
        APMViewer.init('${pageContext.request.contextPath}', ${totalMemory}, ${maxHeap}, '<fmt:message key="console.header.browser.title"/>', ${isVirtualHostEnabled}, '${appId}');
    });
</script>

<div id="main">
    <div id="main-body">
        <div id="main-body-content">
            <div class="apmviewer"></div>
        </div>
    </div>
</div>

<script>
    Template.init("#menu-apps", "#nav-app-performance");
</script>    


<commons:footer />
