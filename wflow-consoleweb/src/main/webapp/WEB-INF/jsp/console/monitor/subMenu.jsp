<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.LogUtil"%>
<%@ page import="org.joget.commons.util.HostManager"%>
<%@ page import="org.joget.apm.APMUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>

<li id="nav-monitor-running"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/running"><span class="nav-steps"><i class="fas fa-play"></i></span><fmt:message key="console.header.submenu.label.runningProcesses"/></a></li>
<c:set var="hasNonArchivedProcessData" value="<%= AppUtil.hasNonArchivedProcessData() %>"/>
<c:set var="isArchivedProcessDataModeEnabled" value="<%= AppUtil.isArchivedProcessDataModeEnabled() %>"/>
<c:if test="${hasNonArchivedProcessData}">
    <li id="nav-monitor-completed"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/completed"><span class="nav-steps"><i class="fas fa-stop"></i></span><fmt:message key="console.header.submenu.label.completedProcesses"/></a></li>
</c:if>
<c:if test="${isArchivedProcessDataModeEnabled}">    
    <li id="nav-monitor-archived"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/archived"><span class="nav-steps"><i class="fas fa-stop"></i></span><fmt:message key="console.header.submenu.label.archivedProcesses"/></a></li>
</c:if>
<li id="nav-monitor-audit"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/audit"><span class="nav-steps"><i class="fas fa-shoe-prints fa-rotate-270"></i></span><fmt:message key="console.header.submenu.label.auditTrail"/></a></li>
<c:set var="isTomcat" value="<%= LogUtil.isDeployInTomcat() %>"/>
<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
<c:if test="${isTomcat && !isVirtualHostEnabled}">
    <li id="nav-monitor-log"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/logs"><span class="nav-steps"><i class="fas fa-scroll"></i></span><fmt:message key="console.header.submenu.label.logs"/></a></li>
</c:if>
<li id="nav-monitor-slog"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/slogs"><span class="nav-steps"><i class="fas fa-scroll"></i></span><fmt:message key="console.log.mtitle"/></a></li>
<li id="nav-governance"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/governance"><span class="nav-steps"><i class="fas fa-check-circle"></i></span><fmt:message key="console.governance.healthCheck"/></a></li>
<%--<li id="nav-monitor-sla"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/sla"><span class="nav-steps">&nbsp;</span><fmt:message key="wflowAdmin.sla.list.label.title"/></a></li>--%>
<c:set var="isGlowrootAvailable" value="<%= APMUtil.isGlowrootAvailable() %>"/>
<c:if test="${isGlowrootAvailable}">
<li id="nav-monitor-apm"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/apm"><span class="nav-steps"><i class="fas fa-tachometer-alt"></i></span><fmt:message key="apm.performance"/></a></li>
</c:if>
<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>
