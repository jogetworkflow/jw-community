<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<li id="nav-run-apps"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/run/apps"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.publishedApps"/></a></li>
<li id="nav-run-inbox"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/run/inbox"><span class="nav-steps">&nbsp;</span><fmt:message key="console.header.submenu.label.inbox"/></a></li>

<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>
