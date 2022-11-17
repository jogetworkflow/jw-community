<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ attribute name="title" %>

<c:set var="userviewThemeCss" value="<%= AppUtil.getUserviewThemeCss() %>"/>

<c:if test="${empty title}"><c:set var="title"><fmt:message key="console.header.browser.title"/></c:set></c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><c:out value="${title}"/></title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <c:choose>
            <c:when test="${!empty userviewThemeCss}">
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/userview_popup.css?build=<fmt:message key="build.number"/>">
                ${userviewThemeCss}
            </c:when>
            <c:otherwise>
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/kecak_v3.css?build=<fmt:message key="build.number"/>">
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css?build=<fmt:message key="build.number"/>">
            </c:otherwise>
        </c:choose>    
        <script>
            $(function() {
                if (window.self !== window.top) {
                    var wh = $(window.self).height() - $("#main-body-header").outerHeight(true) - 40;
                    $("body.popupBody").css("width", "99.9%");
                    $("body.popupBody").css("padding-top", $("#main-body-header").outerHeight(true) + "px");
                    $("#main-body-content").css("height", wh + "px");
                }
            });
            UI.base = "${pageContext.request.contextPath}";
            <c:if test="${!empty param.__a_}">
                UI.userview_app_id = '<c:out value="${param.__a_}"/>';
                UI.userview_id = '<c:out value="${param.__u_}"/>';
                $(document).ready(function() {
                    UI.initThemeParams();
                });
            </c:if>
        </script>
    </head>
    <body class="popupBody">
