<%@ page isErrorPage="true" %>
<%@ page import="java.util.Date"%>
<%@ page import="org.joget.commons.util.LogUtil"%>
<%@ page import="org.joget.commons.util.ResourceBundleUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="java.util.Locale,org.springframework.web.servlet.support.RequestContextUtils,org.joget.commons.util.ResourceBundleUtil" %>
<% Locale locale = RequestContextUtils.getLocale(request); %>

<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>

<%
try {
    LogUtil.error(getClass().getName(), exception, exception.getMessage());
} catch(Throwable t) {
    // ignore
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}">
    <head>
        <title><%= ResourceBundleUtil.getMessage("console.header.top.title", locale) %></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/v3/joget.ico"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/js/fontawesome5/css/all.min.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/home/style.css"/>
    </head>
    <body class="page-body">
        <div id="page-container">
            <div id="logo">
                <a href="${pageContext.request.contextPath}/"><img src="${pageContext.request.contextPath}/home/logo.png" border="0" height="60" /></a>
            </div>
            <div id="icon"><i class="warning fas fa-exclamation-circle" aria-hidden="true"></i></div>
            <div id="content">
                <h1>
                <%= ResourceBundleUtil.getMessage("general.error.error500", locale) %>
                </h1>
                <p>&nbsp;</p>
                <%= ResourceBundleUtil.getMessage("general.error.error500Description", locale) %>
                <br><br>
                <ul style="text-align:left; display:inline-block">
                    <li><%= ResourceBundleUtil.getMessage("general.error.url", locale) %>: ${pageContext.errorData.requestURI}</li>
                    <li><%= ResourceBundleUtil.getMessage("general.error.date", locale) %>: <fmt:formatDate pattern="d MMM yyyy HH:mm:ss" value="<%= new Date() %>"/></li>
                    <%= ResourceBundleUtil.getMessage("general.error.errorDetails", locale) %>
                </ul>
                <p>&nbsp;</p>
            </div>
        </div>
    </body>
</html>
