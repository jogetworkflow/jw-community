<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${!isAnonymous}">
    <c:redirect url="/web/console/home"/>
</c:if>

<commons:header />

<style>
#login-container {
    -moz-box-shadow: 0 1px 3px #BFBFBF;
    background-color: #FFFFFF;
    border: 1px solid #E9E9E9;
    margin: 0 auto;
    padding: 70px 20px;
    width: 90%;
}
#login-box {
    -moz-border-radius: 8px;
    -webkit-border-radius: 8px;
    background-color: #F3F9E0;
    border: 1px solid #E9E9E9;
    margin: 0 auto;
    padding: 60px 20px;
    text-align: center;
    width: 500px;
}
#login-box td, #login-box .input {
    font-family: 'Times New Roman', serif;
    font-size: 16px;
    color: #757575;
    display: block;
    text-align: left;
}
#login-box .input {
    width: 250px;
    height: 20px;
    font-size: 18px;
    color: #535353;
    padding: 5px;
}
#login-box tr {
    margin-top: 15px;
    display: block;
}
#login-box h1 {
    color: #617722;
    font-family: Georgia,"Times New Roman",Times,serif;
    font-size: 22px;
    font-weight: normal;
}
#login-box .buttons {
    text-align: right;
    font-size: 18px;
}
#login-box .buttons input {
    font-size: 16px;
}
</style>

<div id="login-container">
    <span id="main-action-help" style="display:none"></span>
    <div id="login-box">
        <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=login" flush="true" />
        <div>
            <c:if test="${!empty param.login_error}">
                <div id="main-body-message" class="form-errors">
                    <fmt:message key="console.login.label.loginError" /> <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
                </div>
            </c:if>

            <form id="loginForm" name="loginForm" action="<c:url value='/j_spring_security_check'/>" method="POST">
                <table align="center">
                    <tr><td><fmt:message key="console.login.label.username" /> </td><td><input class="input" type='text' id='j_username' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
                    <tr><td><fmt:message key="console.login.label.password" /> </td><td><input class="input" type='password' id='j_password' name='j_password'></td></tr>
                    <tr><td></td><td class="buttons"><input name="submit" class="form-button" type="submit" value="<fmt:message key="console.login.label.login" />" /></td></tr>
                </table>
            </form>

            <script type="text/javascript">
                $(document).ready(
                    function() {
                        $("#j_username").focus();
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




