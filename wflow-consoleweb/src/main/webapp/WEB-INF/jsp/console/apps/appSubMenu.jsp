<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apm.APMUtil"%>
<%@ page import="org.joget.apps.app.service.CustomBuilderUtil"%>

<li id="nav-app-forms"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/forms"><span class="nav-steps"><i class="fas fa-edit"></i></span><fmt:message key="console.header.submenu.label.formsAndUi"/></a></li>
<c:set var="hasBuilders" value="<%= CustomBuilderUtil.hasBuilders() %>"/>

<li id="nav-app-processes"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes"><span class="nav-steps"><i class="fas fa-th-list"></i></span><fmt:message key="console.header.submenu.label.processes"/></a></li>
<li id="nav-app-props"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/properties"><span class="nav-steps"><i class="fas fa-cog"></i></span><fmt:message key="console.header.submenu.label.properties"/></a></li>

<li class="divider"></li>
<c:if test="${hasBuilders}">
    <li id="nav-app-cbuilders"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilders"><span class="nav-steps"><i class="fas fa-tools"></i></span><fmt:message key="console.header.submenu.label.cbuilders"/></a></li>
</c:if>
<c:set var="isGlowrootAvailable" value="<%= APMUtil.isGlowrootAvailable() %>"/>
<c:if test="${isGlowrootAvailable}">
    <li id="nav-app-performance"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/performance"><span class="nav-steps"><i class="fas fa-tachometer-alt"></i></span><fmt:message key="apm.performance"/></a></li>
</c:if>
<li id="nav-app-logs"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/logs"><span class="nav-steps"><i class="fas fa-scroll"></i></span><fmt:message key="console.log.title"/></a></li>

<li id="nav-apps-link"><a class="nav-link" href="${pageContext.request.contextPath}/web/desktop/apps"><i class="fas fa-arrow-circle-left" style="font-size: 20px; width: 17px; margin-right: 6px;"></i> <fmt:message key="console.header.submenu.label.allApps"/></a></li>

<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>

