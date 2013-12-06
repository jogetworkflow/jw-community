<%@tag import="org.joget.commons.util.HostManager"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<div id="guide">
    <div id="guide-title">
        Guide
    </div>
    <div id="guide-body">
        <ul id="guide-list">
            <li><input type="checkbox" checked disabled> Setup Users
                <ul class="guide-sublist">
                    <li><input type="checkbox" checked disabled> Define Org Chart</li>
                    <li><input type="checkbox" checked disabled> Define Groups</li>
                    <li><input type="checkbox" checked disabled> Define Users</li>
                </ul>
            </li>
            <li><input type="checkbox" checked disabled> Design App
                <ul class="guide-sublist">
                    <li><input type="checkbox" checked disabled> Design Processes
                        <ul class="guide-sub-sublist">
                            <li><input type="checkbox" checked disabled> Map Activities</li>
                            <li><input type="checkbox" checked disabled> Map Tools</li>
                            <li><input type="checkbox" checked disabled> Map Participants</li>
                        </ul>
                    </li>
                    <li><input type="checkbox" checked disabled> Design Forms</li>
                    <li><input type="checkbox" checked disabled> Design Userviews</li>
                </ul>
            </li>
            <li><input type="checkbox" checked disabled> Run App</li>
        </ul>
    </div>
</div>

</div>

<div id="footer">
    <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=footer" />
    <%--
    <c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
    <c:if test="${isVirtualHostEnabled}">
        <div id="footer-profile">
            <fmt:message key="console.header.top.label.profile"/>: <%= HostManager.getCurrentProfile() %>
        </div>
    </c:if>
    --%>
</div>

<script type="text/javascript">
    HelpGuide.base = "${pageContext.request.contextPath}"
    HelpGuide.attachTo = "#home-container, #login-container, #main";
    HelpGuide.show();
</script>

</body>
</html>
