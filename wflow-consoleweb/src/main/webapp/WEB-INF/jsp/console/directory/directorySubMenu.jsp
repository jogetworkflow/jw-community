<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<li id="nav-users-orgchart"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/directory/orgs"><span class="nav-steps"><i class="fas fa-sitemap"></i></span><fmt:message key="console.header.submenu.label.organization"/></a></li>
<li id="nav-users-groups"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/directory/groups"><span class="nav-steps"><i class="fas fa-users"></i></span><fmt:message key="console.header.submenu.label.groups"/></a></li>
<li id="nav-users-users"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/directory/users"><span class="nav-steps"><i class="fas fa-user"></i></span><fmt:message key="console.header.submenu.label.users"/></a></li>

<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>