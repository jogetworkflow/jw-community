<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="console.monitoring.common.label.viewGraph"/>: ${wfProcess.instanceId}</title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/pbuilder/css/pbuilder.css" rel="stylesheet" />
        <script src="${pageContext.request.contextPath}/js/JSONError.js"></script>
        <script src="${pageContext.request.contextPath}/js/JSON.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.10.3.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/html2canvas-0.4.1.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.plugin.html2canvas.js"></script>        
        <script src="${pageContext.request.contextPath}/pbuilder/js/rgbcolor.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/StackBlur.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/canvg.js"></script> 
        <script src="${pageContext.request.contextPath}/web/console/i18n/peditor"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js"></script>
        <script src="${pageContext.request.contextPath}/js/json/util.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/undomanager.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.format.js"></script> 
        <script src="${pageContext.request.contextPath}/web/console/i18n/pbuilder"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/pbuilder.js"></script>
        <script>
            $(function() {
                //init ApiClient base url (add to support different context path)
                ProcessBuilder.ApiClient.baseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.ApiClient.designerBaseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.Designer.setZoom(1);
                ProcessBuilder.Designer.editable = false;
                var xpdl = $("#xpdl").val();
                if (xpdl && xpdl !== '') {
                    ProcessBuilder.Designer.init(xpdl);
                    ProcessBuilder.Designer.setZoom(0.7);
                    ProcessBuilder.Actions.viewProcess('<c:out value="${wfProcess.idWithoutVersion}"/>');
                    
                    var selectedNodes = new Array();
                    <c:forEach var="activityId" items="${runningActivityIds}">
                    selectedNodes.push("<c:out value="${activityId}"/>");
                    </c:forEach>
                    for (var i=0; i<selectedNodes.length; i++) {
                        $("#node_" + selectedNodes[i]).addClass("node_active");
                    }
                }
            });
        </script>
    </head>

    <body id="pviewer">
        <div id="pviewer-container">
            <div id="viewport">
                <div id="canvas"></div>
            </div>
            <div id="panel">
                <div id="controls">                                    
                    <a href="#" onclick="ProcessBuilder.Designer.setZoom(0.7); return false"><i class="icon-zoom-out"></i> </a> 
                    <a href="#" onclick="ProcessBuilder.Designer.setZoom(1.0); return false"><i class="icon-zoom-in"></i></a> | 
                    <a href="#" onclick="$('#config').toggle(); return false"><i class="icon-cog"></i> <fmt:message key="pbuilder.label.debug"/></a>
                </div>
                <div id="config">
                    <form method="POST">
                        <textarea id="xpdl" name="xpdl" rows="12" cols="30"><c:out value="${xpdl}" escapeXml="true"/></textarea>
                        <br />
                        <input type="hidden" name="editable" value="false"/>
                    </form>
                </div>
            </div>        
        </div>

        <div id="builder-message"></div>
        <div id="builder-screenshot"></div>
        
    </body>
</html>