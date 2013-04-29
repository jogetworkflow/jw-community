<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ tag import="org.joget.commons.util.HostManager"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

</div>

<div id="footer">
    <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=footer" />
    <c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
    <c:if test="${isVirtualHostEnabled}">
        <div id="footer-profile">
            <fmt:message key="console.header.top.label.profile"/>: <%= HostManager.getCurrentProfile() %>
        </div>
    </c:if>
</div>

<style>
<%= WorkflowUtil.getSystemSetupValue("customCss") %>
</style>

<script>
    if (window.parent != self && window.parent.name != "quickOverlayFrame") {
        $("#header, #footer, #adminBar").hide();
        $("#container, #nav").css("top", "0px");
    } else {
        $("#header, #footer, #adminBar").show();
    }
</script>

<script type="text/javascript">
    HelpGuide.base = "${pageContext.request.contextPath}"
    HelpGuide.attachTo = "#home-container, #login-container, #main";
    HelpGuide.show();
</script>

</body>
</html>
