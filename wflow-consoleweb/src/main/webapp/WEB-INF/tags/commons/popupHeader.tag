<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ attribute name="title" %>
<%@ attribute name="bodyCssClass" required="false"%>
<%@ attribute name="builderTheme" required="false"%>
<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>

<c:set var="userviewThemeCss" value="<%= AppUtil.getUserviewThemeCss() %>"/>
<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>
<c:set var="systemTheme" value='<%= AppUtil.getSystemTheme() %>'/>
<c:if test="${empty title}"><c:set var="title"><fmt:message key="console.header.browser.title"/></c:set></c:if>

<c:if test="${param.__u_ eq '_builder_dark_mode' or param.__u_ eq 'BUILDER_PREVIEW_DARK'}">
    <c:set var="bodyCssClass" value="${bodyCssClass} dark-mode"/>
</c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}" system-theme="${systemTheme}">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><c:out value="${title}"/></title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/fonts/inter/css/font.css" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <c:choose>
            <c:when test="${!empty userviewThemeCss}">
                ${userviewThemeCss}
            </c:when>
            <c:otherwise>
                <c:if test="${builderTheme eq 'true'}">
                    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
                </c:if>
            
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/v7.css?build=<fmt:message key="build.number"/>">
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css?build=<fmt:message key="build.number"/>">
            </c:otherwise>
        </c:choose>    
        <script>
            UI.base = "${pageContext.request.contextPath}";
            <c:choose>
                <c:when test="${!empty param.__a_ and !empty param.__u_}">
                    UI.userview_app_id = '<c:out value="${param.__a_}"/>';
                    UI.userview_id = '<c:out value="${param.__u_}"/>';
                    $(document).ready(function() {
                        UI.initThemeParams();
                        if (window.self !== window.top && $("#main-body-header").length > 0) {
                            if ($("#main-body-header").is(":visible")) {
                                $("body.popupBody").css("padding-top", $("#main-body-header").outerHeight(true) + "px");
                            }
                        }
                    });
                </c:when>
                <c:otherwise>
                    $(function() {
                        if (window.self !== window.top && $("#main-body-header").length > 0) {
                            $("body.popupBody").css("width", "99.9%");
                            $("body.popupBody").css("padding-top", $("#main-body-header").outerHeight(true) + "px");
                            $("body.popupBody").css("height", "calc(100vh - " + $("#main-body-header").outerHeight(true) + "px)");
                            $("body.popupBody").css("overflow", "auto");
                        }
                    });
                </c:otherwise>    
            </c:choose>
        </script>
    </head>
    <body class="popupBody ${bodyCssClass}"
    <c:if test="${empty param.__a_ or empty param.__u_}">
        <c:choose>
            <c:when test="${not empty builderTheme}">
                builder-theme="${systemTheme}"
            </c:when>
            <c:otherwise>
                system-theme="${systemTheme}"
            </c:otherwise>
        </c:choose>
    </c:if>            
    >
