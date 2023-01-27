<%@page import="org.joget.commons.util.HostManager"%>
<%@include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
<li id="nav-setting-general"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/setting/general"><span class="nav-steps"><i class="fas fa-cog"></i></span><fmt:message key="console.header.submenu.label.setting.general"/></a></li>
<c:if test="${!isVirtualHostEnabled}"><li id="nav-setting-datasource"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/setting/datasource"><span class="nav-steps"><i class="fas fa-database"></i></span><fmt:message key="console.header.submenu.label.setting.datasource"/></a></li></c:if>
<li id="nav-setting-directory"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/setting/directory"><span class="nav-steps"><i class="fas fa-users-cog"></i></i></span><fmt:message key="console.header.submenu.label.setting.directory"/></a></li>
<li id="nav-setting-plugin"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/setting/plugin"><span class="nav-steps"><i class="fas fa-plug"></i></span><fmt:message key="console.header.submenu.label.setting.plugin"/></a></li>
<li id="nav-setting-message"><a class="nav-link" href="${pageContext.request.contextPath}/web/console/setting/message"><span class="nav-steps"><i class="fas fa-language"></i></span><fmt:message key="console.header.submenu.label.setting.message"/></a></li>

<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>