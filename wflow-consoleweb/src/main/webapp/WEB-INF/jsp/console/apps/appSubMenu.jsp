<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<li id="nav-app-forms"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms"><span class="nav-steps">1</span> <fmt:message key="console.header.submenu.label.formsAndUi"/></a></li>
<li id="nav-app-processes"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes"><span class="nav-steps">2</span> <fmt:message key="console.header.submenu.label.processes"/></a></li>
<li id="nav-app-props"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/properties"><span class="nav-steps">3</span> <fmt:message key="console.header.submenu.label.properties"/></a></li>
<li id="nav-apps-link"><a class="nav-link" href="${pageContext.request.contextPath}/web/desktop/apps"><i class="icon-circle-arrow-left" style="font-size: 20px; width: 17px; margin-right: 6px;"></i> <fmt:message key="console.header.submenu.label.allApps"/></a></li>
