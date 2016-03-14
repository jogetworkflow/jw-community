<%@ page isErrorPage="true" %>
<%@ page import="java.util.Date"%>
<%@ page import="org.joget.commons.util.LogUtil"%>
<%@ page import="org.joget.commons.util.ResourceBundleUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%
try {
    LogUtil.error(getClass().getName(), exception, exception.getMessage());
} catch(Throwable t) {
    // ignore
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title>Joget Workflow</title>
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
                System Error
            </div>
            <div id="content" style="font-size:13px">
                <br><br>
                Sorry, an unintended error has occurred.
                Please contact support with the following information:
                <br><br>
                <ul style="text-align:left; display:inline-block">
                    <li><%= ResourceBundleUtil.getMessage("console.footer.label.revision") %></li>
                    <li>URL: ${pageContext.errorData.requestURI}</li>
                    <li>Date: <fmt:formatDate pattern="d MMM yyyy HH:mm:ss" value="<%= new Date() %>"/></li>
                    <li>Description of the steps needed to reproduce the issue</li>
                    <li>Copy of the relevant log files which are stored in the logs directory</li>
                    <li>Screenshot(s) showing the problem if possible</li>
                    <li>Sample app that produces the issue if possible</li>
                </ul>
                <p>&nbsp;</p>
            </div>
            <div id="footer">
                <a href="http://www.joget.com">&copy; Joget Workflow - Joget Inc</a>
            </div>
        </div>
    </body>
</html>
