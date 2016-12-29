<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${!empty appId}">
    <c:set var="redirectUrl" scope="request" value=""/>
    <c:choose>
        <c:when test="${embed}">
            <c:set var="redirectUrl" scope="request" value="/web/embed/mobile/${appId}/${userview.properties.id}/landing"/>
        </c:when>
        <c:otherwise>
            <c:set var="redirectUrl" scope="request" value="/web/mobile/${appId}/${userview.properties.id}/landing"/>
        </c:otherwise>
    </c:choose>
    <c:if test="${!empty key && key ne '______'}">
        <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}/landing"/>
    </c:if>
    <c:if test="${!empty menuId}">
        <c:set var="redirectUrl" scope="request" value="${redirectUrl}/${menuId}"/>
    </c:if>
</c:if>
<c:if test="${!isAnonymous && !empty appId}">
    <c:redirect url="${redirectUrl}"/>
</c:if>
<c:if test="${empty redirectUrl}">
    <c:set var="redirectUrl" scope="request" value="/web/mobile"/>
</c:if>
<!DOCTYPE html>
<html class="ui-mobile" manifest="${pageContext.request.contextPath}/web/mobilecache/<c:out value="${appId}"/>/${userview.properties.id}">
    <head>
        <title>
            <c:set var="html">
                ${userview.properties.name}
                <c:if test="${!empty userview.current}">
                     &nbsp;&gt;&nbsp; ${userview.current.properties.label}
                </c:if>
            </c:set>
            <ui:stripTag html="${html}"/>
        </title>
        <jsp:include page="mScripts.jsp" flush="true"/>
    </head>
    <body class="ui-mobile-viewport">

        <div id="userview" data-role="page" data-url="userview" tabindex="0" style="min-height: 377px; ">

            <div data-role="header" data-position="fixed" role="banner" style="top: 0px; ">
                <c:if test="${!empty appId && !empty userview.properties.id}">
                    <c:set var="homePath" value="${appId}/${userview.properties.id}"/>
                </c:if>
                <a href="${pageContext.request.contextPath}/web/mobile/${homePath}" data-icon="home" data-direction="reverse"><fmt:message key="console.header.menu.label.home"/></a>
                <h1 class="ui-title" tabindex="0" role="heading" aria-level="1">
                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <ui:stripTag html="${userview.setting.theme.header}"/>
                    </c:when>
                    <c:otherwise>
                        <ui:stripTag html="${userview.properties.name}"/>
                    </c:otherwise>
                </c:choose>                    
                </h1>
            </div>
                
            <div data-role="content" class="ui-content" role="main">
                    <c:if test="${!empty param.login_error}">
                        <div id="main-body-message" class="form-errors">
                            <fmt:message key="ubuilder.loginError" /> <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
                        </div>
                    </c:if>
                    <c:set var="loginUrl"><c:url value='${redirectUrl}'/></c:set>
                    <form id="loginForm" name="loginForm" target="_self" action="${loginUrl}" method="POST">
                        <table align="center">
                            <tr><td><fmt:message key="console.login.label.username" />: </td><td><input type='text' id='j_username' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
                            <tr><td><fmt:message key="console.login.label.password" />:</td><td><input type='password' id='j_password' name='j_password'></td></tr>
                            <tr><td>&nbsp;</td><td><input name="submit" class="form-button" type="submit" value="<fmt:message key="console.login.label.login" />" /></td></tr>
                            <tr><td colspan="2">
                                <%= DirectoryUtil.getLoginFormFooter() %>
                            </td></tr>
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

        <div class="ui-loader" style="top: 332px; "><h1><fmt:message key="mobile.apps.loading"/></h1></div>
        <%= AppUtil.getSystemAlert() %> 
        <jsp:include page="mFooter.jsp" flush="true" />   
    </body>    
</html>


