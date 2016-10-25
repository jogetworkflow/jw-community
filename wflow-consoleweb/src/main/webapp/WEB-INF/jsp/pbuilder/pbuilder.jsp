<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="console.header.submenu.label.processes"/>: <c:out value="${appDefinition.name}"/></title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        
        <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" />
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css?build=<fmt:message key="build.number"/>">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link href="${pageContext.request.contextPath}/js/jquery/themes/ui-lightness/jquery-ui-1.10.3.custom.css" rel="stylesheet" type="text/css">
        <link href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/js/chosen/chosen.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/pbuilder/css/pbuilder.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
        <script src="${pageContext.request.contextPath}/js/JSONError.js"></script>
        <script src="${pageContext.request.contextPath}/js/JSON.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.10.3.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script src="${pageContext.request.contextPath}/js/chosen/chosen.jquery.js"></script>
        <script src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
        <script src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/html2canvas-0.4.1.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.plugin.html2canvas.js"></script>        
        <script src="${pageContext.request.contextPath}/pbuilder/js/rgbcolor.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/StackBlur.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/canvg.js"></script> 
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/pbuilder?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/undomanager.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.format.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/pbuilder.js?build=<fmt:message key="build.number"/>"></script>
        <script>
            $(function() {
                //init ApiClient base url (add to support different context path)
                ProcessBuilder.ApiClient.appName = "<c:out value="${appDefinition.name}"/>";
                ProcessBuilder.ApiClient.baseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.ApiClient.designerBaseUrl = "${pageContext.request.contextPath}";
                ProcessBuilder.Designer.contextPath = "${pageContext.request.contextPath}";
                ProcessBuilder.Designer.setZoom(1.0);
                <c:if test="${param.editable == 'false'}">
                    ProcessBuilder.Designer.editable = false;
                </c:if>
                <c:if test="${param.autoValidate == 'false'}">
                    ProcessBuilder.Designer.autoValidate = false;
                </c:if>
                <c:choose>
                <c:when test="${!empty appId && empty param.xpdl}">
                    var loadCallback;
                    <c:if test="${!empty param.processId}">
                        loadCallback = function() {
                            ProcessBuilder.Actions.viewProcess('<c:out value="${param.processId}"/>');
                        }
                    </c:if>
                    ProcessBuilder.ApiClient.load('<c:out value="${appId}"/>','<c:out value="${version}"/>', loadCallback);
                </c:when>
                <c:otherwise>
                    var xpdl = $("#xpdl").val();
                    if (xpdl && xpdl !== '') {
                        ProcessBuilder.Designer.init(xpdl);
                    }
                </c:otherwise>
                </c:choose>
                $(window).bind('beforeunload', function() {
                    if (ProcessBuilder.Designer.isModified()) {
                        return '<fmt:message key="pbuilder.label.modifiedPrompt"/>';
                    }
                });
                // check for web designer
                var designerUrl = "<%= AppUtil.getDesignerWebBaseUrl() %>/designer/webstart.jsp";
                $.ajax({
                    type: 'GET',
                    url: designerUrl,
                    cache: false,
                    success: function(data) {
                        $("#launchDesigner").show();
                    },
                    error: function(data) {
                    }
                });
            });
            function launchDesigner(){
            <%
                String designerwebBaseUrl = AppUtil.getDesignerWebBaseUrl();
                String locale = "en";
                if (WorkflowUtil.getSystemSetupValue("systemLocale") != null && WorkflowUtil.getSystemSetupValue("systemLocale").length() > 0) {
                    locale = WorkflowUtil.getSystemSetupValue("systemLocale");
                }
            %>
                var base = '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}';
                var url = base + "${pageContext.request.contextPath}/web/console/app/${appId}/${appDefinition.version}/package/xpdl";
                var path = base + '${pageContext.request.contextPath}';
                <c:set var="sessionId" value="${cookie.JSESSIONID.value}"/>
                <c:if test="${empty sessionId}"><c:set var="sessionId" value="${pageContext.request.session.id}"/></c:if>
                document.location = '<%= designerwebBaseUrl%>/designer/webstart.jsp?url=' + encodeURIComponent(url) + '&path=' + encodeURIComponent(path) + '&appId=${appId}&appVersion=${appDefinition.version}&locale=<%= locale%>&username=${username}&domain=${pageContext.request.serverName}&port=${pageContext.request.serverPort}&context=${pageContext.request.contextPath}&session=<c:out value="${sessionId}"/>&tokenName=<%= SecurityUtil.getCsrfTokenName() %>&tokenValue=<%= SecurityUtil.getCsrfTokenValue(request) %>';
            }            
        </script>
    </head>

    <body id="pbuilder">
        <div id="builder-container">
            <div id="builder-header">
                <div id="builder-logo"></div>
                <div id="builder-title"><fmt:message key="pbuilder.title"/> <i> - <c:out value="${appDefinition.name}"/> (v${appDefinition.version})</i></div>
                <div id="deploy-button" class="last-inactive"><a id="deploy" href="#" onclick="ProcessBuilder.ApiClient.deploy(); return false"><span class="steps-bg"><span class="title"> <i class="icon-cloud-upload"></i> <fmt:message key="pbuilder.label.deploy"/></span></span></a></div>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                        <ul id="builder-steps">
                    </ul>
                    <div id="builder-bg"></div>
                </div>
                <div id="builder-content">
                    <div id="step-design-container">
                        
                        <div id="designer-container">
                            <div id="header"><div id="header_title"><strong><fmt:message key="pbuilder.title"/></strong></div></div>
                            <div id="viewport">
                                <div id="canvas"></div>
                            </div>
                            <div id="panel">
                                <div id="controls">                                    
                                    <a href="#" onclick="ProcessBuilder.Actions.undo(); return false" class="action-undo"><i class="icon-undo"></i> <fmt:message key="pbuilder.label.undo"/></a> | 
                                    <a href="#" onclick="ProcessBuilder.Actions.redo(); return false" class="action-redo"><i class="icon-repeat"></i> <fmt:message key="pbuilder.label.redo"/></a> | 
                                    <a href="#" onclick="ProcessBuilder.Designer.setZoom(0.7); return false"><i class="icon-zoom-out"></i> </a> 
                                    <a href="#" onclick="ProcessBuilder.Designer.setZoom(1.0); return false"><i class="icon-zoom-in"></i></a> | 
                                    <!--<a href="#" onclick="ProcessBuilder.ApiClient.saveScreenshots(function(){}, true); return false"><i class="icon-camera"></i> <fmt:message key="pbuilder.label.screenshot"/></a> |--> 
                                    <!--<a href="#" onclick="ProcessBuilder.Designer.validate(); return false"><i class="icon-check"></i> <fmt:message key="pbuilder.label.validate"/></a> | -->
                                    <!--<a href="#" onclick="ProcessBuilder.ApiClient.list(); return false"><i class="icon-cloud-download"></i> <fmt:message key="pbuilder.label.load"/></a> |-->
                                    <span id="launchDesigner"><a href="#" onclick="launchDesigner(); return false"><i class="icon-upload"></i> <fmt:message key="console.process.config.label.launchDesigner"/></a> |</span>
                                    <a href="#" onclick="$('#config').toggle(); return false"><i class="icon-cog"></i> <fmt:message key="pbuilder.label.debug"/></a>
                                </div>
                                <div id="config">
                                    <form method="POST" action="?">
                                        <textarea id="xpdl" name="xpdl" rows="12" cols="30"><c:if test="${empty param.xpdl}"><jsp:include page="resources/default.xpdl"/></c:if><c:if test="${!empty param.xpdl}"><c:out value="${param.xpdl}" escapeXml="true"/></c:if></textarea>
                                        <br />
                                        <!--
                                        <input type="checkbox" name="editable" value="false" <c:if test="${param.editable == 'false'}">checked</c:if> /> <fmt:message key="pbuilder.label.readonly"/>
                                        <br />
                                        <input type="checkbox" name="autoValidate" value="false" <c:if test="${param.autoValidate == 'false'}">checked</c:if> /> <fmt:message key="pbuilder.label.disableAutoValidate"/>
                                        <br />
                                        -->
                                        <input type="submit" value="<fmt:message key="pbuilder.label.update"/>" />
                                    </form>
                                </div>
                            </div>
                            <c:if test="${param.editable != 'false'}">
                                <div id="palette">
                                    <div class="palette_participant participant">
                                        <div class="participant_handle">
                                            <div class="participant_label"><fmt:message key="pbuilder.label.participant"/></div>
                                        </div>
                                    </div>                
                                    <div class="palette_node activity">
                                        <div class="node_label"><fmt:message key="pbuilder.label.activity"/></div>
                                    </div>                
                                    <div class="palette_node tool">
                                        <div class="node_label"><fmt:message key="pbuilder.label.tool"/></div>
                                    </div>
                                    <div class="palette_node route">
                                        <div class="node_label"><fmt:message key="pbuilder.label.route"/></div>
                                        <div class="node_route"></div>
                                    </div>
                                    <div class="palette_node subflow">
                                        <div class="node_label"><fmt:message key="pbuilder.label.subflow"/></div>
                                    </div>
                                    <div class="palette_node palette_start">
                                        <fmt:message key="pbuilder.label.start"/>
                                    </div>
                                    <div class="palette_node palette_end">
                                        <fmt:message key="pbuilder.label.end"/>
                                    </div>
                                </div>
                            </c:if>
                        </div>                        

                    </div>                    
                </div>
            </div>
            <div id="builder-footer">
                <fmt:message key="pbuilder.footer"/> <fmt:message key="console.footer.label.revision"/>
            </div>
        </div>

        <div id="builder-message"></div>
        <div id="builder-screenshot"></div>
        
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        
        <script>
            $(function() {
                $('#builder-steps li').click(function(){
                    var div = $(this).find('a').attr('href');
                    if($(div).length > 0){
                        $('#builder-steps li').removeClass("active");
                        $('#builder-steps li').removeClass("next");
                        $(this).addClass("active");
                        $(this).prev().addClass("next");
                        $('#builder-content').children().hide();
                        $(div).show();
                        if (div === "#step-design-container") {
                            $("#palette").dialog("open");
                        } else {
                            $("#palette").dialog("close");
                        }
                    }
                    return false;
                });
            });
        </script>

        <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="webConsole" value="true"/>
            <jsp:param name="builderMode" value="true"/>
        </jsp:include>
        
    </body>
</html>