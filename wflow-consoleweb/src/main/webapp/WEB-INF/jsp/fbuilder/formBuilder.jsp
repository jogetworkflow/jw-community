<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
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
        <title><fmt:message key="adminBar.label.form"/>: <c:out value="${formDef.name}"/> - <fmt:message key="fbuilder.title"/></title>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/storage/jquery.html5storage.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/chosen/chosen.jquery.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/jquery.tinymce.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/fbuilder?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/fbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/wro/form_common.js?build=<fmt:message key="build.number"/>"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/form_common.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/chosen/chosen.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/fbuilder.css?build=<fmt:message key="build.number"/>" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/font-awesome4/css/font-awesome.min.css" />
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css?build=<fmt:message key="build.number"/>">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
            
        <script type="text/javascript">
            var viewForm = function() {
                // get form row id
                var primaryKey = $("#form-row-id").attr("value");
                if (!primaryKey || primaryKey.length == 0) {
                    primaryKey = "${primaryKey}";
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
                        FormBuilder.showMessage("<fmt:message key="fbuilder.saved"/>");
                        setTimeout(function(){ FormBuilder.showMessage(""); }, 2000);
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        alert("<fmt:message key="fbuilder.errorSaving"/> (" + textStatus + "): " + errorThrown);
                    }
                });
            }

            window.onbeforeunload = function() {
                if(!FormBuilder.isSaved()){
                    return "<fmt:message key="fbuilder.saveBeforeClose"/>";
                }
            };

            $(document).ready(function() {
                // add toggle json link
                $("#form-json-link").click(function() {
                    if ($("#form-info").css("display") != "block") {
                        $("#form-info").css("display", "block");
                    } else {
                        $("#form-info").css("display", "none");
                    }
                });

                // initialize the form
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
                    var elementProps = eval("(${element.propertyOptions})");
                    var elementTemplate = "${element.formBuilderTemplate}";
                    FormBuilder.initElementDefinition("${element.className}", elementProps, elementTemplate);
                } catch (e) {
                    alert("Error initializing ${element.name}:" + e);
                }
                </c:if>
            </c:forEach>
        </script>
    </head>
    <body id="formbuilder">

        <div id="builder-container">
            <div id="builder-header">
                <div id="builder-logo"></div>
                <div id="builder-title"><fmt:message key="fbuilder.title"/> <i> - <c:out value="${formDef.name}" /> (v${appDefinition.version})</i></div>
                <%--<jsp:include page="/web/console/app/${appId}/${appDefinition.version}/builder/navigator/f/${formId}" flush="true" />--%>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                    <ul id="builder-steps">
                        <li id="builder-step-design" class="first-active active" onclick="FormBuilder.showBuilder()"><a href="#"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.designForm"/> </span><span class="subtitle"><fmt:message key="fbuilder.designForm.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.showFormProperties()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.properties"/> </span><span class="subtitle"><fmt:message key="fbuilder.properties.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.previewForm()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.preview"/> </span><span class="subtitle"><fmt:message key="fbuilder.preview.description"/></span></span></a></li>
                        <li class="last-inactive"><a href="#" onclick="saveForm()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.save"/> </span><span class="subtitle"><fmt:message key="fbuilder.save.description"/></span></span></a></li>
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
                                                <div class="form-palette-element" element-class="${element.className}" element-property='${element.defaultPropertyValues}'>
                                                    <c:set var="elementIconPath" value="${element.formBuilderIcon}"/>
                                                    <c:if test="${empty elementIconPath}">
                                                        <c:set var="elementIconPath" value="/images/v3/builder/sidebar_element.gif"/>
                                                    </c:if>
                                                    <img src="${pageContext.request.contextPath}${elementIconPath}" border="0" align="left" />
                                                    <label>${element.i18nLabel}</label>
                                                </div>
                                            </li>
                                        </c:forEach>
                                        </ul>
                                    </c:forEach>
                                    </div>
                                </fieldset>
                            </td>
                            <td valign="top" style="padding-left: 175px;">
                                <fieldset id="form-canvas">
                                    <legend><fmt:message key="fbuilder.canvas"/></legend>
                                    <div class="form-container-div">
                                        ${elementHtml}
                                        <c:if test="${empty elementHtml}">
                                            <form id="form-container" class="form-container"></form>
                                        </c:if>
                                    </div>

                                    <p>&nbsp;</p>
                                    <a href="#" id="form-json-link" style="font-size: smaller" onclick="return false"><fmt:message key="console.builder.advanced"/></a>
                                    <div id="form-info" style="display: none">
                                        <form id="form-preview" target="_blank" action="" method="post">
                                            <textarea id="form-json" name="json" cols="80" rows="10" style="font-size: smaller"></textarea>
                                        </form>
                                        <button onclick="updateForm()"><fmt:message key="console.builder.update"/></button>
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
            
        <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="webConsole" value="true"/>
            <jsp:param name="builderMode" value="true"/>
        </jsp:include>
                        
    </body>
</html>