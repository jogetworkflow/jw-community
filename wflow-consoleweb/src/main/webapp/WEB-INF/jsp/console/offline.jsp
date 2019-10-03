<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page isErrorPage="true" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title><fmt:message key="pwa.pageUnavailableOffline.title"/></title>
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/v3/joget.ico"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/home/style.css"/>
    </head>
    <body>
        <div id="container">
            <div id="logo">
                <img src="${pageContext.request.contextPath}/home/logo.png" border="0" height="60" />
            </div>
            <div id="title">
                <fmt:message key="pwa.pageUnavailableOffline.title"/>
            </div>
            <div id="content">
                <br><br>
                <fmt:message key="pwa.pageUnavailableOffline.desc"/>
                <br><br>
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                <p>
                    <a class="content-link" href="javascript:window.history.back()">&laquo;<fmt:message key="pwa.pageUnavailableOffline.goBack"/></a>
                </p>
            </div>
        </div>
    </body>
</html>