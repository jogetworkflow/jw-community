<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<c:set var="userviewThemeCss" value="<%= AppUtil.getUserviewThemeCss() %>"/>

<!DOCTYPE html>
<html >
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.preload.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.js?build=<fmt:message key="build.number"/>"></script>
        <script>loadCSS("${pageContext.request.contextPath}/wro/common.css")</script>
    
        <script>
            UI.base = "${pageContext.request.contextPath}";
            ConnectionManager.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            ConnectionManager.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            JPopup.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            JPopup.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            UI.locale = "<%= AppUtil.getAppLocale() %>";
        </script>
        
        <c:if test="${!empty userviewThemeCss}">
            ${userviewThemeCss}
        </c:if>
        
    </head>

    <body class="popupBody">
        ${content}
    </body>
</html>
