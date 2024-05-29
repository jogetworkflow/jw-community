<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.preload.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.js?build=<fmt:message key="build.number"/>"></script>
        <script>loadCSS("${pageContext.request.contextPath}/wro/common.css")</script>
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/v7.css?build=<fmt:message key="build.number"/>">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css?build=<fmt:message key="build.number"/>">  
        <script>
            $(function() {
                if (window.self !== window.top && $("#main-body-header").length > 0) {
                    var wh = $(window.self).height() - $("#main-body-header").outerHeight(true) - 40;
                    $("body.popupBody").css("width", "99.9%");
                    $("body.popupBody").css("padding-top", $("#main-body-header").outerHeight(true) + "px");
                    $("#main-body-content").css("height", wh + "px");
                }
            });
            UI.base = "${pageContext.request.contextPath}";
            ConnectionManager.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            ConnectionManager.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            JPopup.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            JPopup.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            UI.locale = "<%= AppUtil.getAppLocale()%>";
        </script>
    </head>
    <body class="popupBody"<c:if test="${not empty builderTheme and builderTheme ne 'classic'}"> builder-theme="${builderTheme}"</c:if>>
        <div id="main-body-header">
            &nbsp;
        </div>
        <div id="main-body-content">
            
        </div>    
        <script>
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#main-body-header";
            HelpGuide.show();
        </script>
        <jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
    </body>
</html>    
