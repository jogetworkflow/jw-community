<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}">
    <head>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

        <link href="${pageContext.request.contextPath}/wro/common.css" rel="stylesheet" />
        <link href="${pageContext.request.contextPath}/js/bootstrap4/css/bootstrap.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/form.css" rel="stylesheet" />
        <script src="${pageContext.request.contextPath}/wro/common.preload.js"></script>
        <script src="${pageContext.request.contextPath}/wro/common.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/popper.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js" ></script>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/datalist8.css?build=<fmt:message key="build.number"/>" />
        <c:if test="${theme == 'dark'}">
            <link href="${pageContext.request.contextPath}/wro/darkTheme.css" rel="stylesheet" />
        </c:if>
        <style>
            html, body
            {
                width:100%;
                height:100%;
                height: auto !important;
                margin: 0 !important;
                padding: 0px;
                border: 0 !important;
            }
            body {
                padding: 25px !important;
            }
        </style>
        <script>
            UI.base = "${pageContext.request.contextPath}";
            UI.userview_app_id = '${appId}';
            UI.userview_id = 'BUILDER_PREVIEW';
            
            var _enableResponsiveTable = true;
        </script>    
    </head>

    <body<c:if test="${theme == 'dark'}"> class="dark-mode"</c:if>>
        <div id="preview">
            <c:if test="${!empty error}">
                <h3><fmt:message key="dbuilder.errorGenerating"/></h3>
                <div id="error">${error}</div>
            </c:if>

            <c:set scope="request" var="dataListId" value="${dataList.id}"/>

            <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />     

        </div>
        <script>
            $(function() {
                $("#preview input[type=submit], #preview .actions button").attr("disabled", "disabled");
                $("#preview tbody a").attr("onclick", "return false");
                $("#preview .actions button, #preview tbody a").click(function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                });
            });
        </script>
    </body>
</html>
