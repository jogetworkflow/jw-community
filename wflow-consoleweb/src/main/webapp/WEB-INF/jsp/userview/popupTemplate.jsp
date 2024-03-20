<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<c:set var="userviewThemeCss" value="<%= AppUtil.getUserviewThemeCss() %>"/>
<c:set var="appLocale" value="<%= AppUtil.getAppLocale() %>"/>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE html>
<html lang="${appLocale}">
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
            UI.locale = "${appLocale}";
            
            <c:if test="${rightToLeft == 'true' || fn:startsWith(appLocale, 'ar') == true}">
                UI.rtl = true;
                $(document).ready(function(){
                    $("body").addClass("rtl");
                    $(".row-content").append("<div style=\"clear:both\"></div>");
                });
            </c:if>
        </script>
        
        <c:if test="${!empty userviewThemeCss}">
            ${userviewThemeCss}
        </c:if>
        
    </head>

    <body class="popupBody">
        ${content}
    </body>
</html>
