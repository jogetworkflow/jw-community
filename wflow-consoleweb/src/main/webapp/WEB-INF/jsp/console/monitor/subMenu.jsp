<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<li id="nav-monitor-running"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/running"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.runningProcesses"/></a></li>
<li id="nav-monitor-completed"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/completed"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.completedProcesses"/></a></li>
<li id="nav-monitor-audit"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/audit"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.auditTrail"/></a></li>
<%--<li id="nav-monitor-sla"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/monitor/sla"><span class="nav-steps">&nbsp;</span><fmt:message key="wflowAdmin.sla.list.label.title"/></a></li>--%>
