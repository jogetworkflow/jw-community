<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.apps.userview.model.Userview"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
<% response.setHeader("P3P", "CP=\"This is not a P3P policy\""); %>

<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${!isAnonymous}">
    <c:set var="redirectUrl" scope="request" value="/web/"/>
    <c:choose>
        <c:when test="${embed}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}embed/userview/${appId}/${userview.properties.id}/"/>
        </c:when>
        <c:otherwise>
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}userview/${appId}/${userview.properties.id}/"/>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${!empty key}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}"/>
        </c:when>
        <c:otherwise>
            <c:set var="key" scope="request" value="<%= Userview.USERVIEW_KEY_EMPTY_VALUE %>"/>
            <c:if test="${!empty menuId}">
                <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}"/>
            </c:if>    
        </c:otherwise>    
    </c:choose>
    <c:if test="${!empty menuId}">
        <c:set var="redirectUrl" scope="request" value="${redirectUrl}/${menuId}"/>
    </c:if>
    <c:set var="qs"><ui:decodeurl value="${queryString}"/></c:set>
    <c:redirect url="${redirectUrl}?${qs}"/>
</c:if>

<html lang="${lang}">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title>
            <c:set var="html">
                ${userview.properties.name} &nbsp;&gt;&nbsp;
                <c:if test="${!empty userview.current}">
                    ${userview.current.properties.label}
                </c:if>
            </c:set>
            <ui:stripTag html="${html}"/>  
        </title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        
        <script type="text/javascript">
            ${userview.setting.theme.javascript}
            UI.base = "${pageContext.request.contextPath}";    
            UI.userview_app_id = '<c:out value="${appId}"/>';
            UI.userview_id = '${userview.properties.id}';
        </script>

        <script type="text/javascript">
            ${userview.setting.theme.javascript}
        </script>

        <link href="${pageContext.request.contextPath}/css/userview.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />

        <style type="text/css">
            ${userview.setting.theme.css}
            <c:if test="${!empty userview.setting.theme.properties.loginBackground}">
                #login{background-size:cover; background-image:url('${userview.setting.theme.properties.loginBackground}');}
            </c:if>
        </style>
    </head>

    <body id="login" class="<c:if test="${embed}">embeded</c:if><c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}"> rtl</c:if>">
        <div id="page">
            <div id="header">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <div id="header-inner">${userview.setting.theme.header}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="header-info">
                            <div id="header-name">
                                <a href="${pageContext.request.contextPath}/web/userview/<c:out value="${appId}"/>/${userview.properties.id}" id="header-link"><span id="name">${userview.properties.name}</span></a>
                            </div>
                            <div id="header-description">
                                <span id="description">${userview.properties.description}</span>
                            </div>
                            <div class="clear"></div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div id="header-message">
                    <div id="header-welcome-message">
                        <span id="welcomeMessage">${userview.properties.welcomeMessage}</span>
                    </div>
                    <div id="header-logout-text">
                        
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
            <div id="main">
                <c:if test="${!empty userview.setting.theme.pageTop}">
                    ${userview.setting.theme.pageTop}
                </c:if>
                <div id="content">
                    <c:if test="${!empty param.login_error}">
                        <div id="main-body-message" class="form-errors">
                            ${SPRING_SECURITY_LAST_EXCEPTION.message}
                        </div>
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty userview.setting.theme.properties.loginPageTop}">
                            ${userview.setting.theme.properties.loginPageTop}
                        </c:when>
                        <c:when test="${!empty userview.setting.properties.loginPageTop}">
                            ${userview.setting.properties.loginPageTop}
                        </c:when>
                    </c:choose>    
                    <form id="loginForm" name="loginForm" action="<c:url value='/j_spring_security_check'/>" method="POST">
                        <table align="center">
                            <tr><td><fmt:message key="ubuilder.login.username" />: </td><td><input type='text' id='j_username' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
                            <tr><td><fmt:message key="ubuilder.login.password" />:</td><td><input type='password' id='j_password' name='j_password'></td></tr>
                            <tr><td>&nbsp;</td><td><input name="submit" class="form-button" type="submit" value="<ui:msgEscHTML key="ubuilder.login" />" /></td></tr>
                            <tr><td colspan="2">
                                <%= DirectoryUtil.getLoginFormFooter() %>
                            </td></tr>
                        </table>
                    </form>
                    <c:choose>
                        <c:when test="${!empty userview.setting.theme.properties.loginPageBottom}">
                            ${userview.setting.theme.properties.loginPageBottom}
                        </c:when>
                        <c:when test="${!empty userview.setting.properties.loginPageBottom}">
                            ${userview.setting.properties.loginPageBottom}
                        </c:when>
                    </c:choose>         
                    <script type="text/javascript">
                        $(document).ready(
                            function() {
                                $("#j_username").focus();
                                
                                $("#loginForm input[name='submit']").click(function(){
                                    $.blockUI({ css: { 
                                        border: 'none', 
                                        padding: '15px', 
                                        backgroundColor: '#000', 
                                        '-webkit-border-radius': '10px', 
                                        '-moz-border-radius': '10px', 
                                        opacity: .3, 
                                        color: '#fff' 
                                    }, message : "<h1><ui:msgEscJS key="form.form.message.wait" /></h1>" }); 
                                    return true;
                                });
                            }
                        );
                    </script>
                </div>
                <div class="clear"></div>
                <c:if test="${!empty userview.setting.theme.pageBottom}">
                    ${userview.setting.theme.pageBottom}
                </c:if>
            </div>
            <div id="footer">
                <c:choose>
                    <c:when test="${!empty userview.setting.theme.footer}">
                        <div id="footer-inner">${userview.setting.theme.footer}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="footer-message">
                            <span id="footerMessage">${userview.properties.footerMessage}</span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <%= AppUtil.getSystemAlert() %>                  
    </body>
</html>
