<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<style>
#home-container {
    -moz-box-shadow: 0 1px 3px #BFBFBF;
    background-color: #FFFFFF;
    border: 1px solid #E9E9E9;
    margin: 0 auto;
    padding: 70px 20px;
    width: 90%;
}
#home-box {
    -moz-border-radius: 8px;
    -webkit-border-radius: 8px;
    background-color: #F3F9E0;
    border: 1px solid #E9E9E9;
    margin: 0 auto;
    padding: 20px 20px;
    width: 600px;
    position: relative;
}
#home-box td, #home-box input {
    font-family: 'PT Sans', Arial;
    font-size: 16px;
    color: #535353;
}
#home-box h3 {
    color: #617722;
    font-family: Georgia,"Times New Roman",Times,serif;
    font-size: 18px;
    font-weight: bold;
}
.welcome-box {
    background-colorx: #F3F9E0;
    borderx: 1px solid #E9E9E9;
    padding: 10px;
    -moz-border-radius: 10px;
    -webkit-border-radius: 10px;
    font-size: 14px;
    line-height: 24px;
    color: #454545;
    width: 42%;
    float: left;
    margin: 0px 20px 20px 0px;
}
x.welcome-box h3 {
    color: #617722;
    font-size: 14pt;
}
.welcome-clear {
    clear: both;
}
</style>

<div id="home-container">
    <div id="home-box">
        <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=home" flush="true" />
        <div class="welcome-box">
            <h3><fmt:message key="console.home.getStarted.title"/></h3>
            <p>
                <ul>
                <c:if test="${isAdmin}">
                    <li><a href="${pageContext.request.contextPath}/web/console/directory/orgs"><fmt:message key="console.header.menu.label.users"/></a></li>
                    <li><a href="#" onclick="appCreate();return false"><fmt:message key="console.header.menu.label.apps"/></a></li>
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
</script>

<commons:footer />
