<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><c:out value="${builder.objectLabel}"/>: <c:out value="${builderDefinition.name}"/> - <c:out value="${builder.label}"/></title>

        <c:set var="builderNumber"><fmt:message key="build.number"/></c:set>
        
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.ui.touch-punch.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch-formatters.min.js"></script>  
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/diff_match_patch_uncompressed.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/builderutil.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/cbuilder?type=${builder.objectName}&build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/cbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
        ${builder.getBuilderJS(pageContext.request.contextPath, builderNumber)}
        
        <link href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/css/cbuilder.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
        ${builder.getBuilderCSS(pageContext.request.contextPath, builderNumber)}
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatchhtml.css" />
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        
        <style>
            #builder-header:before {background:${builder.color};}
        </style>    
        <script type="text/javascript">
            $(document).ready(function () {
                CustomBuilder.saveUrl = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/<c:out value="${appVersion}"/>/cbuilder/<c:out value="${builder.objectName}"/>/save/<c:out value="${builderDefinition.id}"/>';
                CustomBuilder.previewUrl = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/<c:out value="${appVersion}"/>/cbuilder/<c:out value="${builder.objectName}"/>/preview/<c:out value="${builderDefinition.id}"/>';
                CustomBuilder.contextPath = '${pageContext.request.contextPath}';
                CustomBuilder.appId = '<c:out value="${appId}"/>';
                CustomBuilder.appVersion = '<c:out value="${appVersion}"/>';
                CustomBuilder.builderType = '<c:out value="${builder.objectName}"/>';
                CustomBuilder.builderLabel = '<c:out value="${builder.objectLabel}"/>';
                
                CustomBuilder.initConfig(${builder.builderConfig});
                CustomBuilder.initPropertiesOptions(${builder.propertyOptions});
                
                CustomBuilder.initBuilder(function() {
                    CustomBuilder.loadJson($("#cbuilder-json").val());
                });
            });

            window.onbeforeunload = function() {
                if(CustomBuilder.saveChecker != 0){
                    return '<ui:msgEscJS key="ubuilder.saveBeforeClose" />';
                }
            };
        </script>
    </head>

    <body id="customBuilder" class="<c:out value="${builder.objectName}"/>">
        <div id="builder-container">
            <div id="builder-header">
                <a class="reload" onclick="location.reload(true);"></a>
                <i class="fa-2x ${builder.icon}"></i>
                <div id="builder-logo"></div>
                <div id="builder-title"><c:out value="${builder.label}"/> <i><c:out value="${appDefinition.name}" /> v${appDefinition.version}: <c:out value="${builderDefinition.name}"/> <c:if test="${appDefinition.published}">(<fmt:message key="console.app.common.label.published"/>)</c:if></i></div>
                <%--<jsp:include page="/web/console/app/${appId}/${appVersion}/builder/navigator/u/${userviewId}" flush="true" />--%>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                        <ul id="builder-steps">
                        <li id="step-design" class="first-active active"><a href="#step-design-container"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.design"/></span><span class="subtitle"><fmt:message key="ubuilder.designUserview.description"/></span></span></a></li>
                        <li id="step-properties"><a href="#step-properties-container"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.properties"/></span><span class="subtitle"><fmt:message key="ubuilder.setting.description"/></span></span></a></li>
                        <li id="step-preview"><a onclick="CustomBuilder.preview();" title="<ui:msgEscHTML key="ubuilder.preview.tip"/>"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.preview"/></span><span class="subtitle"><fmt:message key="ubuilder.preview.description"/></span></span></a></li>
                        <li id="step-save" class="last-inactive save-disabled"><a onclick="CustomBuilder.mergeAndSave();" title="<ui:msgEscHTML key="ubuilder.save.tip"/>"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.save"/></span><span class="subtitle"><fmt:message key="ubuilder.save.description"/></span></span></a></li>
                    </ul>
                    <div id="builder-bg"></div>
                </div>
                <div id="builder-content">
                    <div id="step-properties-container">
                    </div>
                    <div id="step-design-container">
                        <div id="toolbar">
                            <ul>
                                <li id="tool-undo"><a class="undo-disabled" onclick="UserviewBuilder.undo();" title="<ui:msgEscHTML key="ubuilder.undo.disabled.tip"/>"><span><fmt:message key="ubuilder.undo.disabled"/></span></a></li>
                                <li id="tool-redo"><a class="redo-disabled" onclick="UserviewBuilder.redo();" title="<ui:msgEscHTML key="ubuilder.redo.disabled.tip"/>"><span><fmt:message key="ubuilder.redo.disabled"/></span></a></li>
                            </ul>
                            <div class="clear"></div>
                        </div>
                        <fieldset id="builder-palette">
                            <div id="builder-palette-top"></div>
                            <div id="builder-palette-body">
                                
                            </div>
                        </fieldset>

                        <div id="builder_canvas">    
                            ${builderHTML}
                        </div>
                            
                        <div class="clear"></div>
                        <div id="propertyEditor" class="menu-wizard-container" style="display:none;"></div>
                    
                        <div id="cbuilder-advanced">
                            <div id="cbuilder-info" style="display: none">
                                <form id="cbuilder-preview" action="?" target="_blank" method="post">
                                    <textarea id="cbuilder-json" name="json" cols="80" rows="10" style="font-size: smaller"><c:out value="${json}"/></textarea>
                                    <textarea id="cbuilder-json-original" name="json-original" cols="80" rows="10" style="display:none;"><c:out value="${json}"/></textarea>
                                    <textarea id="cbuilder-json-current" name="json-current" cols="80" rows="10" style="display:none;"><c:out value="${json}"/></textarea>
                                </form>
                                <button onclick="CustomBuilder.updateFromJson()"><fmt:message key="console.builder.update"/></button>
                            </div>
                        </div>

                    </div>
                    </div>
                </div>
            <div id="builder-footer">
                <fmt:message key="console.builder.footer"/>
            </div>
        </div>

        <div id="builder-message"></div>
        <div id="builder-screenshot"></div>
                                
        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#builder-bar";
            HelpGuide.key = "help.web.console.app.custom.builder.${builder.objectName}";
            HelpGuide.show();
        </script>
            
        <jsp:include page="/WEB-INF/jsp/console/apps/builder.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="elementId" value="${builderDefinition.id}"/>
            <jsp:param name="jsonForm" value="#cbuilder-info"/>
            <jsp:param name="builder" value="custom"/>
        </jsp:include>    
        <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appVersion}"/>
            <jsp:param name="webConsole" value="true"/>
            <jsp:param name="builderMode" value="true"/>
        </jsp:include>
            
    </body>
</html>