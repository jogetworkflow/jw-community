<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="webconsole" />

<div id="home-container">
    <div id="home-box">
        <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=home" flush="true" />
        <div class="welcome-box">
            <h3><fmt:message key="console.home.getStarted.title"/></h3>
            <p>
                <ul>
                <c:if test="${isAdmin}">
                    <li><a href="${pageContext.request.contextPath}/web/console/directory/orgs"><fmt:message key="console.header.menu.label.users"/></a></li>
                    <li><a href="${pageContext.request.contextPath}/web/desktop/apps"><fmt:message key="console.header.menu.label.apps"/></a></li>
                    <li><a href="${pageContext.request.contextPath}/web/console/run/apps"><fmt:message key="console.header.menu.label.run"/></a></li>
                    <li><a href="${pageContext.request.contextPath}/web/console/monitor/running"><fmt:message key="console.header.menu.label.monitor"/></a></li>
                </c:if>
                <c:if test="${!isAdmin}">
                    <li><a href="${pageContext.request.contextPath}/web/console/run/apps"><fmt:message key="console.header.submenu.label.publishedApps"/></a></li>
                    <li><a href="${pageContext.request.contextPath}/web/console/run/processes"><fmt:message key="console.header.submenu.label.publishedProcesses"/></a></li>
                    <li><a href="${pageContext.request.contextPath}/web/console/run/inbox"><fmt:message key="console.header.submenu.label.inbox"/></a></li>
                </c:if>
                </ul>
            </p>
        </div>

        <div class="welcome-box">
            <p>
                <jsp:include page="welcome.jsp" flush="true" />
            </p>
        </div>

        <div class="welcome-clear"></div>
    </div>
</div>

<script>
    Template.init("#menu-home", "#nav-home-welcome");

<c:if test="${isAdmin}">
    HelpGuide.key = "help.web.console.home.admin";
</c:if>
<c:if test="${!isAdmin}">
    HelpGuide.key = "help.web.console.home.user";
</c:if>
    
    if (AdminBar.showQuickOverlay) {
        HelpGuide.key = "help.none";
    }    
</script>

<commons:footer />
