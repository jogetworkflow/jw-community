<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="pbuilder.label.screenshot"/>: ${wfProcess.id}</title>

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
                ProcessBuilder.ApiClient.baseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.ApiClient.designerBaseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.Designer.contextPath = "${pageContext.request.contextPath}";
                ProcessBuilder.Designer.setZoom(1);
                ProcessBuilder.Designer.editable = false;
                var xpdl = $("#xpdl").val();
                if (xpdl && xpdl !== '') {
                    ProcessBuilder.Designer.init(xpdl, "<c:out value="${wfProcess.idWithoutVersion}"/>");
                    var processDefId = "<c:out value="${wfProcess.id}"/>";
                    var saveUrl = ProcessBuilder.ApiClient.designerBaseUrl + "/web/console/app/<c:out value="${appId}"/>/process/builder/screenshot/submit?processDefId=" + encodeURIComponent(processDefId);
                    var screenshotCallback = function(imgData) {
                        var image = new Blob([imgData], {type : 'text/plain'});
                        var params = new FormData();
                        params.append("xpdlimage", image);
                        $.ajax({ 
                            type: "POST", 
                            url: saveUrl,
                            data: params,
                            cache: false,
                            processData: false,
                            contentType: false,
                            beforeSend: function (request) {
                               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                            },
                            success: function() {
                                <c:if test="${!empty callback}">
                                    if (parent && parent.<c:out value="${callback}"/>) {
                                        parent.<c:out value="${callback}"/>();
                                    }
                                </c:if>
//                                    alert("Saved " + processDefId);
                            },
                            error: function(e) {
                                <c:if test="${!empty callback}">
                                    if (parent && parent.<c:out value="${callback}"/>) {
                                        parent.<c:out value="${callback}"/>();
                                    }
                                </c:if>
//                                alert("Error saving " + processDefId);
                            },
                            complete: function() {
//                                alert("Screenshot saved");
                            }
                        });
                    };
                    var showDialog = (parent === window);
                    ProcessBuilder.Designer.screenshot(screenshotCallback, showDialog);                    
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
        
        <jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
    </body>
</html>