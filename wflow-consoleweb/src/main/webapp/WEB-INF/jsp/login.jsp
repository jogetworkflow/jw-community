<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.apps.app.service.MobileUtil"%>

<%
if (!MobileUtil.isMobileDisabled() && MobileUtil.isMobileUserAgent(request)) {
    pageContext.setAttribute("mobileUserAgent", Boolean.TRUE);
}
%>
<c:if test="${mobileUserAgent && (empty cookie['desktopSite'].value || cookie['desktopSite'].value != 'true')}">
    <c:redirect url="/web/mlogin"/>
</c:if>

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${!isAnonymous}">
    <c:redirect url="/web/console/home"/>
</c:if>

<commons:header id="login" />

<div id="login-container">
    <span id="main-action-help" style="display:none"></span>
    <div id="login-box">
        <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=login" flush="true" />
        <div>
            <c:if test="${!empty param.login_error}">
                <div id="main-body-message" class="form-errors">
                    ${SPRING_SECURITY_LAST_EXCEPTION.message}
                </div>
            </c:if>

            <form id="loginForm" name="loginForm" action="<c:url value='/j_spring_security_check'/>" method="POST">
                <table align="center">
                    <tr><td><fmt:message key="console.login.label.username" /> </td><td><input class="input" type='text' id='j_username' name='j_username' value='<c:if test="${!empty param.login_error && empty param.login_openid}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
                    <tr><td><fmt:message key="console.login.label.password" /> </td><td><input class="input" type='password' id='j_password' name='j_password'></td></tr>
                    <tr><td></td><td class="buttons"><input name="submit" class="form-button" type="submit" value="<fmt:message key="console.login.label.login" />" /></td></tr>
                    <tr><td colspan="2">
                        <%= DirectoryUtil.getLoginFormFooter() %>
                    </td></tr>
                </table>
            </form>
                    
            <script type="text/javascript">
                $(document).ready(
                    function() {
                        $("#loginForm #j_username").focus();
                    }
                );
            </script>
        </div>
    </div>
</div>

<script>
    Template.init("#menu-login", "#nav-home-login");
</script>

<commons:footer />




