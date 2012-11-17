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
        <title><fmt:message key="ubuilder.title"/> - ${userview.name}</title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/jquery.tinymce.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/ubuilder?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ubuilder.core.js?build=<fmt:message key="build.number"/>"></script>

        <link href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/css/jquery-ui-1.8.5.custom.css" rel="stylesheet" type="text/css" />
        <link href="${pageContext.request.contextPath}/css/ubuilder.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />

        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css?build=<fmt:message key="build.number"/>">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
            
        <script type="text/javascript">
            $(document).ready(function () {
                UserviewBuilder.tinymceUrl = '${pageContext.request.contextPath}/js/tiny_mce/tiny_mce.js';
                UserviewBuilder.saveUrl = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builderSave/';
                UserviewBuilder.previewUrl = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builderPreview/';
                UserviewBuilder.contextPath = '${pageContext.request.contextPath}';

                UserviewBuilder.initSettingPropertyOptions(${setting.propertyOptions});
                UserviewBuilder.initCategoryPropertyOptions(${category.propertyOptions});

                <c:forEach items="${menuTypeCategories}" var="categoryRow">
                    <c:set var="category" value="${categoryRow.key}"/>
                    <c:set var="elementList" value="${categoryRow.value}"/>
                    <c:forEach items="${elementList}" var="element">
                        <c:set var="propertyOptions" value="${element.propertyOptions}"/>
                        <c:if test="${empty propertyOptions}">
                            <c:set var="propertyOptions" value="''"/>
                        </c:if>
                        UserviewBuilder.initMenuType('${category}', '${element.className}', '${element.label}', '${element.icon}',${propertyOptions});
                    </c:forEach>
                </c:forEach>

                UserviewBuilder.loadUserview('${userviewId}', ${json});
                UserviewBuilder.initBuilder();

                // add toggle json link
                $("#userview-json-link").click(function() {
                    if ($("#userview-info").css("display") != "block") {
                        $("#userview-info").css("display", "block");
                    } else {
                        $("#userview-info").css("display", "none");
                    }
                });
            });

            window.onbeforeunload = function() {
                if(UserviewBuilder.saveChecker != 0){
                    return "<fmt:message key="ubuilder.saveBeforeClose" />";
                }
            };
        </script>
    </head>

    <body id="userviewbuilder">
        <div id="builder-container">
            <div id="builder-header">
                <img alt="logo" width="68" height="24" src="${pageContext.request.contextPath}/images/v3/builder/logo.png" align="left" /> <div id="builder-title"><fmt:message key="ubuilder.title"/></div>
                <jsp:include page="/web/console/app/${appId}/${appVersion}/builder/navigator/u/${userviewId}" flush="true" />
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                        <ul id="builder-steps">
                        <li id="step-design" class="first-active active"><a href="#step-design-container"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.designUserview"/></span><span class="subtitle"><fmt:message key="ubuilder.designUserview.description"/></span></span></a></li>
                        <li id="step-setting"><a href="#step-setting-container"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.setting"/></span><span class="subtitle"><fmt:message key="ubuilder.setting.description"/></span></span></a></li>
                        <li id="step-preview"><a onclick="UserviewBuilder.preview();" title="<fmt:message key="ubuilder.preview.tip"/>"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.preview"/></span><span class="subtitle"><fmt:message key="ubuilder.preview.description"/></span></span></a></li>
                        <li id="step-save" class="last-inactive save-disabled"><a onclick="UserviewBuilder.save();" title="<fmt:message key="ubuilder.save.disabled.tip"/>"><span class="steps-bg"><span class="title"><fmt:message key="ubuilder.save"/></span><span class="subtitle"><fmt:message key="ubuilder.save.description"/></span></span></a></li>
                    </ul>
                    <div id="builder-bg"></div>
                </div>
                <div id="builder-content">
                    <div id="step-design-container">
                        <div id="toolbar">
                            <ul>
                                <li id="tool-undo"><a class="undo-disabled" onclick="UserviewBuilder.undo();" title="<fmt:message key="ubuilder.undo.disabled.tip"/>"><span><fmt:message key="ubuilder.undo.disabled"/></span></a></li>
                                <li id="tool-redo"><a class="redo-disabled" onclick="UserviewBuilder.redo();" title="<fmt:message key="ubuilder.redo.disabled.tip"/>"><span><fmt:message key="ubuilder.redo.disabled"/></span></a></li>
                            </ul>
                            <div class="clear"></div>
                        </div>
                        <fieldset id="builder-palette">
                            <legend><fmt:message key="ubuilder.palette"/></legend>
                            <div id="builder-palette-top"></div>
                            <div id="builder-palette-body">

                            </div>
                        </fieldset>

                        <fieldset id="canvas">
                            <legend><fmt:message key="ubuilder.canvas"/></legend>
                            <div class="page">
                                <div id="userview-header">
                                    <div id="header-info">
                                        <div id="header-name" class="editable-info">
                                            <span id="name" class="editable"></span>
                                        </div>
                                        <div id="header-description" class="editable-info">
                                            <span id="description" class="editable"></span>
                                        </div>
                                        <div class="clear"></div>
                                    </div>
                                    <div id="header-message">
                                        <div id="header-welcome-message" class="editable-info">
                                            <span id="welcomeMessage" class="editable"></span>
                                        </div>
                                        <div id="header-logout-text" class="editable-info">
                                            <span id="logoutText" class="editable"></span>
                                        </div>
                                        <div class="clear"></div>
                                    </div>
                                </div>
                                <div id="userview-main">
                                    <div id="userview-sidebar" class="sidebar">

                                    </div>
                                    <div id="userview-content">
                                        <span>&lt;<fmt:message key="ubuilder.content"/>&gt;</span>
                                    </div>
                                    <div class="clear"></div>
                                </div>
                                <div id="userview-footer">
                                    <div id="footer-message" class="editable-info">
                                        <span id="footerMessage" class="editable"></span>
                                    </div>
                                </div>
                            </div>
                        </fieldset>
                        <div class="clear"></div>
                        <div id="propertyEditor" class="menu-wizard-container" style="display:none;"></div>
                    
                        <div id="userview-advanced">
                            <a href="#" id="userview-json-link" style="font-size: smaller" onclick="return false"><fmt:message key="console.builder.advanced"/></a>
                            <div id="userview-info" style="display: none">
                                <form id="userview-preview" action="?" target="_blank" method="post">
                                    <textarea id="userview-json" name="json" cols="80" rows="10" style="font-size: smaller">${json}</textarea>
                                </form>
                                <button onclick="UserviewBuilder.updateFromJson()"><fmt:message key="console.builder.update"/></button>
                            </div>
                        </div>

                    </div>

                    <div id="step-setting-container" style="display:none;">

                    </div>
                </div>
            </div>
            <div id="builder-footer">
                <fmt:message key="console.builder.footer"/>
            </div>
        </div>

        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#builder-bar";
            HelpGuide.key = "help.web.console.app.userview.builder";
            HelpGuide.show();
        </script>
            
    </body>
</html>