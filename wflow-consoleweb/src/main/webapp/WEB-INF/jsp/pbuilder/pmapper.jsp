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
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link href="${pageContext.request.contextPath}/js/jquery/themes/ui-lightness/jquery-ui.custom.css" rel="stylesheet" type="text/css">
        <link href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/pbuilder/css/pbuilder.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.ui.touch-punch.js"></script>
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/html2canvas-0.4.1.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/jquery.plugin.html2canvas.js"></script>        
        <script src="${pageContext.request.contextPath}/pbuilder/js/rgbcolor.js"></script> 
        <script src="${pageContext.request.contextPath}/pbuilder/js/StackBlur.js"></script>
        <script src="${pageContext.request.contextPath}/pbuilder/js/canvg.js"></script> 
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/pbuilder?build=<fmt:message key="build.number"/>"></script>
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
                ProcessBuilder.Designer.editable = false;
                ProcessBuilder.Designer.isMapper = true;
                var loadCallback;
                <c:if test="${!empty param.processId}">
                    loadCallback = function() {
                        ProcessBuilder.Actions.viewProcess('<c:out value="${param.processId}"/>');
                    }
                </c:if>
                ProcessBuilder.ApiClient.load('<c:out value="${appId}"/>','<c:out value="${version}"/>', loadCallback);
            });   
        </script>
    </head>

    <body id="pbuilder" class="pmapper">
        <div id="builder-container">
            <div id="builder-header">
                <a class="reload" onclick="location.reload(true);"></a>
                <i class="fas fa-2x fa-link"></i>
                <div id="builder-logo"></div>
                <div id="builder-title"><fmt:message key="pbuilder.mapperTitle"/> <i> - <c:out value="${appDefinition.name}"/> v${appDefinition.version}</i></div>
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
                            <div id="header"></div>
                            <div id="viewport">
                                <div id="canvas"></div>
                            </div>
                            <div id="panel">
                                <div id="controls">                                    
                                    <a class="showallcontrol"><i class="fas fa-eye"></i> <fmt:message key="pbuilder.label.displayAllInfo"/> 
                                        <ul>
                                            <li class="showParticipant"><fmt:message key="pbuilder.label.participant"/></li>
                                            <li class="showActivity"><fmt:message key="pbuilder.label.activity"/></li>
                                            <li class="showTool"><fmt:message key="pbuilder.label.tool"/></li>
                                            <li class="showRoute"><fmt:message key="pbuilder.label.route"/></li>
                                            <li class="hideAll"><i class="fas fa-eye-slash"></i> <fmt:message key="pbuilder.label.hideAll"/></li>
                                        </ul>
                                    </a>
                                </div>
                            </div>
                        </div>                        

                    </div>                    
                </div>
            </div>
            <div id="builder-footer">
                <fmt:message key="pbuilder.footer"/> <fmt:message key="console.footer.label.revision"/>
            </div>
        </div>
        
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        
        <script src="${pageContext.request.contextPath}/web/console/i18n/advtool?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/advancedTool.js?build=<fmt:message key="build.number"/>"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/advancedTool.css?build=<fmt:message key="build.number"/>">
        <script type="text/javascript">
            $(document).ready(function(){
                AdvancedTools.initProcess({
                    contextPath : '${pageContext.request.contextPath}',
                    appId : '<c:out value="${appId}"/>',
                    appVersion : '<c:out value="${appDefinition.version}"/>'
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