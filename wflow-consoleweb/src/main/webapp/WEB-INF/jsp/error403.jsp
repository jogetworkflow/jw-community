<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page isErrorPage="true" %>
<%@ page import="java.util.Locale,org.springframework.web.servlet.support.RequestContextUtils,org.joget.commons.util.ResourceBundleUtil" %>
<% Locale locale = RequestContextUtils.getLocale(request); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title><%= ResourceBundleUtil.getMessage("console.header.top.title", locale) %></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/v3/joget.ico"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/home/style.css"/>
    </head>
    <body>
        <div id="container">
            <div id="logo">
                <a href="${pageContext.request.contextPath}/"><img src="${pageContext.request.contextPath}/home/logo.png" border="0" height="60" /></a>
            </div>
            <div id="title">
                <%= ResourceBundleUtil.getMessage("general.error.error403", locale) %>
            </div>
            <div id="content">
                <br><br>
                <%= ResourceBundleUtil.getMessage("general.error.error403Description", locale) %>
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                <p>
                    <a class="content-link" href="${pageContext.request.contextPath}/"><%= ResourceBundleUtil.getMessage("general.error.backToHome", locale) %></a>
                </p>
            </div>
            <div id="footer">
                <%= ResourceBundleUtil.getMessage("console.builder.footer", locale) %>
            </div>
        </div>
    </body>
</html>
