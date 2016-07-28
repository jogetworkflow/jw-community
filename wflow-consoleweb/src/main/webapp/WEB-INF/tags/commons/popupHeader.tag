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
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        
        <c:if test="${!empty userviewThemeCss}">
                ${userviewThemeCss}
        </c:if>
        <script>
            $(function() {
                if (window.self !== window.top) {
                    var wh = $(window.self).height() - 50;
                    $("body.popupBody").css("width", "99.9%");
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
