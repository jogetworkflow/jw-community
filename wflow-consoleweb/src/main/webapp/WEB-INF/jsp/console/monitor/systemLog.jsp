<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-tachometer-alt"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>
        
<jsp:include page="../log/log.jsp" flush="true" />

<script>
    Template.init("#menu-monitor", "#nav-monitor-slog");
</script>

<commons:footer />
