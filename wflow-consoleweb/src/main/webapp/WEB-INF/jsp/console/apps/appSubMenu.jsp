<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<li id="nav-app-processes"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes"><span class="nav-steps">1</span> <fmt:message key="console.header.submenu.label.processes"/></a></li>
<li id="nav-app-forms"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms"><span class="nav-steps">2</span> <fmt:message key="console.header.submenu.label.forms"/></a></li>
<li id="nav-app-lists"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalists"><span class="nav-steps">3</span> <fmt:message key="console.header.submenu.label.lists"/></a></li>
<li id="nav-app-userviews"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userviews"><span class="nav-steps">4</span> <fmt:message key="console.header.submenu.label.userview"/></a></li>
<li id="nav-app-props"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/properties"><span class="nav-steps">5</span> <fmt:message key="console.header.submenu.label.properties"/></a></li>
