<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="adminBar.label.form"/>: <c:out value="${formDef.name}"/> - <fmt:message key="fbuilder.title"/></title>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.ui.touch-punch.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch-formatters.min.js"></script>  
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/diff_match_patch_uncompressed.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/builderutil.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/fbuilder?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/fbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/form_common.js?build=<fmt:message key="build.number"/>"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/form_common.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/fbuilder.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatchhtml.css" />
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
            
        <script type="text/javascript">
            var viewForm = function() {
                // get form row id
                var primaryKey = $("#form-row-id").val();
                if (!primaryKey || primaryKey.length == 0) {
                    primaryKey = "<c:out value="${primaryKey}"/>";
                }

                var form = $('#form-preview');
                form.attr("action", "${pageContext.request.contextPath}/web/fbuilder/form/view/<c:out value="${formId}"/>/" + primaryKey);
                $('#form-preview').submit();
                return false;
            };

            var updateForm = function() {
                var securityToken = ConnectionManager.tokenName + "=" + ConnectionManager.tokenValue;
                var form = $('#form-preview');
                form.attr("action", "?" + securityToken);
                form.attr("target", "");
                $('#form-preview').submit();
                return false;
            };

            var saveForm = function() {
                var json = FormBuilder.generateJSON();
                var saveUrl = "${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appDefinition.version}/form/<c:out value="${formId}"/>/update";
                $.ajax({
                    type: "POST",
                    data: {"json": json },
                    url: saveUrl,
                    dataType : "text",
                    beforeSend: function (request) {
                       request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                    },
                    success: function(response) {
                        FormBuilder.originalJson = FormBuilder.generateJSON();
                        FormBuilder.showMessage('<ui:msgEscJS key="fbuilder.saved"/>');
                        setTimeout(function(){ FormBuilder.showMessage(""); }, 2000);
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        alert('<ui:msgEscJS key="fbuilder.errorSaving"/> (' + textStatus + '): ' + errorThrown);
                    }
                });
            }

            window.onbeforeunload = function() {
                if(!FormBuilder.isSaved()){
                    return '<ui:msgEscJS key="fbuilder.saveBeforeClose"/>';
                }
            };

            $(document).ready(function() {
                let lockSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + '${pageContext.request.contextPath}/web/websocket/app/<c:out value="${appId}"/>/${appDefinition.version}/plugin/org.kecak.apps.app.lib.UrlLockSocket');

                lockSocket.onmessage = function(event) {
                    let text = event.data;
                    if(text) {
                        alert(text);
                    }
                };

                lockSocket.onopen = () => lockSocket.send('/web/fbuilder/app/<c:out value="${appId}"/>/${appDefinition.version}/form/<c:out value="${formId}"/>');

                // initialize the form
                FormBuilder.appId = '<c:out value="${appId}"/>';
                FormBuilder.appVersion = '<c:out value="${appDefinition.version}"/>';
                FormBuilder.contextPath = '${pageContext.request.contextPath}';
                FormBuilder.formPreviewUrl = '/web/fbuilder/app/<c:out value="${appId}"/>/${appDefinition.version}/form/<c:out value="${formId}"/>/preview/';
                FormBuilder.elementPreviewUrl = '/web/fbuilder/app/<c:out value="${appId}"/>/${appDefinition.version}/form/<c:out value="${formId}"/>/element/preview';
                FormBuilder.init("<c:out value="${formId}"/>");

                <c:if test="${empty elementHtml}">
                // clear the form
                FormBuilder.clear();

                setTimeout(function() {
                    // test to programatically add a new section and column
                    var section = FormBuilder.addSection();
                }, 0);
                </c:if>
            });

            <c:forEach items="${palette.editableElementList}" var="element">
                <c:if test="${!empty element.propertyOptions}">
                try {
                    <c:set var="initScript">                    
                        var elementProps = ${PropertyUtil.injectHelpLink(element.helpLink, element.propertyOptions)};
                        var elementTemplate = "<c:out value="${fn:replace(element.formBuilderTemplate, '\"', '\\\\\"')}" escapeXml="false"/>";
                        FormBuilder.initElementDefinition("${element.className}", elementProps, elementTemplate);
                    </c:set>
                    <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                    eval("${initScript}");
                } catch (e) {
                    if (console && console.log) {
                        console.log("Error initializing ${element.className} : " + e);
                    }
                }
                </c:if>
            </c:forEach>
        </script>
    </head>
    <body id="formbuilder">

        <div id="builder-container">
            <div id="builder-header">
                <a class="reload" onclick="location.reload(true);"></a>
                <i class="fas fa-2x fa-file-alt"></i>
                <div id="builder-logo"></div>
                <div id="builder-title"><fmt:message key="fbuilder.title"/> <i><c:out value="${appDefinition.name}" /> v${appDefinition.version}: <c:out value="${formDef.name}" /> <c:if test="${appDefinition.published}">(<fmt:message key="console.app.common.label.published"/>)</c:if></i></div>
                <%--<jsp:include page="/web/console/app/${appId}/${appDefinition.version}/builder/navigator/f/${formId}" flush="true" />--%>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                    <ul id="builder-steps">
                        <li id="builder-step-design" class="first-active active" onclick="FormBuilder.showBuilder()"><a href="#"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.designForm"/> </span><span class="subtitle"><fmt:message key="fbuilder.designForm.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.showFormProperties()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.properties"/> </span><span class="subtitle"><fmt:message key="fbuilder.properties.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.previewForm()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.preview"/> </span><span class="subtitle"><fmt:message key="fbuilder.preview.description"/></span></span></a></li>
                        <li class="last-inactive"><a href="#" onclick="FormBuilder.mergeAndSave()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.save"/> </span><span class="subtitle"><fmt:message key="fbuilder.save.description"/></span></span></a></li>
                        <jsp:include page="extend.jsp" flush="true" />
                    </ul>
                    <div id="builder-bg"></div>
                </div>
                <div id="builder-content">

                    <table>
                        <tr>
                            <td width="0" valign="top">
                                <fieldset id="builder-palette">
                                    <div id="builder-palette-top"></div>
                                    <div id="builder-palette-body">
                                    <c:forEach items="${palette.elementCategoryMap}" var="categoryRow">
                                        <c:set var="category" value="${categoryRow.key}"/>
                                        <c:set var="elementList" value="${categoryRow.value}"/>
                                        <h3>${category}</h3>
                                        <ul>
                                        <c:forEach items="${elementList}" var="element">
                                            <li>
                                                <div class="form-palette-element builder-palette-element" element-class="${element.className}" element-property='<c:out escapeXml="true" value="${element.defaultPropertyValues}" />' data-icon='<c:out escapeXml="true" value="${element.formBuilderIcon}" />'>
                                                    <label>${element.i18nLabel}</label>
                                                </div>
                                            </li>
                                        </c:forEach>
                                        </ul>
                                    </c:forEach>
                                    </div>
                                </fieldset>
                                <script>
                                    FormBuilder.populatePaletteIcons();
                                </script>    
                            </td>
                            <td valign="top" style="padding-left: 185px;">
                                <fieldset id="form-canvas">
                                    <legend><fmt:message key="fbuilder.canvas"/></legend>
                                    <div class="form-container-div">
                                        ${elementHtml}
                                        <c:if test="${empty elementHtml}">
                                            <form id="form-container" class="form-container"></form>
                                        </c:if>
                                    </div>

                                    <p>&nbsp;</p>
                                    <div id="form-info" style="display: none">
                                        <form id="form-preview" target="_blank" action="" method="post">
                                            <textarea id="form-json" name="json" cols="80" rows="10" style="font-size: smaller"></textarea>
                                            <textarea id="form-json-original" name="json-original" cols="80" rows="10" style="display:none;"></textarea>
                                            <textarea id="form-json-current" name="json-current" cols="80" rows="10" style="display:none;"></textarea>
                                        </form>
                                        <button onclick="FormBuilder.updateForm()"><fmt:message key="console.builder.update"/></button>    
                                    </div>
                                </fieldset>
                            </td>
                        </tr>
                    </table>
                    <div class="form-clear"></div>
                </div>
            </div>
            <div id="builder-footer">
                <fmt:message key="console.builder.footer"/>
            </div>
        </div>
        
        <div id="builder-message"></div>
            
        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#builder-bar";
            HelpGuide.key = "help.web.console.app.form.builder";
//            HelpGuide.definition = [{
//                    id: "start",
//                    title: "Form Builder",
//                    description: "Drag elements from here to the canvas on the right.",
//                    buttons: [{name: "Done", onclick: HelpGuide.hide},{name: "Hide Hints", onclick: HelpGuide.disable}],
//                    attachTo: "#builder-palette",
//                    position: 3,
//                    show: true
//                }
//            ]
            HelpGuide.show();
        </script>
            
        <jsp:include page="/WEB-INF/jsp/console/apps/builder.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="elementId" value="${formId}"/>
            <jsp:param name="jsonForm" value="#form-info"/>
            <jsp:param name="builder" value="form"/>
        </jsp:include>
        <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="webConsole" value="true"/>
            <jsp:param name="builderMode" value="true"/>
        </jsp:include>
                        
    </body>
</html>