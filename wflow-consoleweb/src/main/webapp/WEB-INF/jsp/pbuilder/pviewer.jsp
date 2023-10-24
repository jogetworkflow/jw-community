<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>

<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="console.monitoring.common.label.viewGraph"/>: ${wfProcess.instanceId}</title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />

        <link id="favicon" rel="alternate icon" href="${pageContext.request.contextPath}/images/favicon.ico" /> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/builder/builder.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/pbuilder/css/pbuilder.css?build=<fmt:message key="build.number"/>">
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        <style>
            #builder_canvas.process_graph {top: 0 !important; margin: 0 !important; left: 0px !important; width: 100vw !important; max-width: none !important; height: 100vh !important;}
        </style>    
    </head>
    <body id="cbuilder" class="no-right-panel">
        <div id="builder_canvas" class="process_graph">
        </div>
        <textarea id="json" name="json" rows="12" cols="30" style="display:none;"><c:out value="${json}" escapeXml="true"/></textarea>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/builderutil.js"></script>
        <script>
            /*** Handle jQuery plugin naming conflict between jQuery UI and Bootstrap ***/
            $.widget.bridge('uibutton', $.ui.button);
            $.widget.bridge('uitooltip', $.ui.tooltip);
        </script>
        <script src="${pageContext.request.contextPath}/web/console/i18n/advtool?build=<fmt:message key="build.number"/>"></script>
        <script data-cbuilder-script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/cbuilder?type=process&build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/builder/builder.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/pbuilder.js?build=<fmt:message key="build.number"/>"></script>
        <script data-cbuilder-script>
            $(function () {
                CustomBuilder.contextPath = '${pageContext.request.contextPath}';
                        
                var selectedNodes = new Array();
                <c:forEach var="activityId" items="${runningActivityIds}">
                    selectedNodes.push("<c:out value="${activityId}"/>");
                </c:forEach>
                var json = $("#json").val();
                ProcessBuilder.loadGraph(json, '<c:out value="${wfProcess.idWithoutVersion}"/>', selectedNodes);
            });
        </script>
    </body>
</html>
