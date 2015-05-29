<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.LogUtil"%>
<%@ page import="org.joget.commons.util.HostManager"%>

<li id="nav-monitor-running"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/running"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.runningProcesses"/></a></li>
<li id="nav-monitor-completed"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/completed"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.completedProcesses"/></a></li>
<li id="nav-monitor-audit"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/audit"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.auditTrail"/></a></li>
<c:set var="isTomcat" value="<%= LogUtil.isDeployInTomcat() %>"/>
<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
<c:if test="${isTomcat && !isVirtualHostEnabled}">
    <li id="nav-monitor-log"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/logs"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.logs"/></a></li>
</c:if>
<%--<li id="nav-monitor-sla"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/sla"><span class="nav-steps">&nbsp;</span><fmt:message key="wflowAdmin.sla.list.label.title"/></a></li>--%>
