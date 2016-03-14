<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ tag import="org.joget.commons.util.HostManager"%>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

    </div>
</div>

<div id="footer">
    <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=footer" />
    <%--
    <c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
    <c:if test="${false && isVirtualHostEnabled}">
        <div id="footer-profile">
            <fmt:message key="console.header.top.label.profile"/>: <%= HostManager.getCurrentProfile() %>
        </div>
    </c:if>
    --%>
</div>

<jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
    <jsp:param name="webConsole" value="true"/>
    <jsp:param name="appControls" value="true"/>
</jsp:include>

<style>
<%= WorkflowUtil.getSystemSetupValue("customCss") %>
</style>

<script>
    if (window.parent !== self && window.parent.name !== "quickOverlayFrame") {
        $("#main-header, #main-menu, #header, #footer, #adminBar, #beta").hide();
        $("#container, #nav, #menu-popup").css("top", "0px");
    } else {
        $("#main-header, #header, #footer, #adminBar, #beta").show();
    }
</script>

<script type="text/javascript">
    HelpGuide.base = "${pageContext.request.contextPath}"
    HelpGuide.attachTo = "#home-container, #login-container, #main";
    HelpGuide.show();
</script>

<%= AppUtil.getSystemAlert() %>

</body>
</html>
