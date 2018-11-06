<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="fa fa-dashboard"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="../console/monitor/subMenu.jsp" flush="true" />
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
        APMViewer.init('${pageContext.request.contextPath}');
    });
</script>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
    </div>
    <div id="main-body">
        <div class="apmviewer"></div>
    </div>
</div>

<script>
    Template.init("#menu-monitor", "#nav-monitor-apm");
</script>

<commons:footer />
