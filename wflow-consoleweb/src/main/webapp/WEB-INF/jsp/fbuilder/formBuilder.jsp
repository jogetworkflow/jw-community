<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title><fmt:message key="fbuilder.title"/> - ${formDef.name}</title>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/jquery.tinymce.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/fbuilder"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/fbuilder.core.js"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/fbuilder.css" />
        <script type="text/javascript">
            var viewForm = function() {
                // get form row id
                var primaryKey = $("#form-row-id").attr("value");
                if (!primaryKey || primaryKey.length == 0) {
                    primaryKey = "${primaryKey}";
                }

                var form = $('#form-preview');
                form.attr("action", "${pageContext.request.contextPath}/web/fbuilder/form/view/${formId}/" + primaryKey);
                $('#form-preview').submit();
                return false;
            };

            var updateForm = function() {
                var form = $('#form-preview');
                form.attr("action", "?");
                form.attr("target", "");
                $('#form-preview').submit();
                return false;
            };

            var saveForm = function() {
                if (confirm("<fmt:message key="fbuilder.save.confirm"/>")) {
                    var json = FormBuilder.generateJSON();
                    var saveUrl = "${pageContext.request.contextPath}/web/console/app/${appId}/${appDefinition.version}/form/${formId}/update";
                    $.ajax({
                        type: "POST",
                        data: {"json": json },
                        url: saveUrl,
                        success: function(response) {
                            alert("<fmt:message key="fbuilder.saved"/>");
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            alert("<fmt:message key="fbuilder.errorSaving"/> (" + textStatus + "): " + errorThrown);
                        }
                    });
                }
            }

            window.onbeforeunload = function() {
                return "<fmt:message key="fbuilder.saveBeforeClose"/>";
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
                FormBuilder.formPreviewUrl = '/web/fbuilder/app/${appId}/${appDefinition.version}/form/preview/';
                FormBuilder.init("${formId}");

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
    <body>

        <div id="builder-container">
            <div id="builder-header">
                <img alt="logo" width="107" height="38" src="${pageContext.request.contextPath}/images/v3/builder/logo.png" align="left" /> <div id="builder-title"><fmt:message key="fbuilder.title"/></div>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                    <ul id="builder-steps">
                        <li id="builder-step-design" class="first-active active" onclick="FormBuilder.showBuilder()"><a href="#"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.designForm"/> </span><span class="subtitle"><fmt:message key="fbuilder.designForm.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.showFormProperties()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.properties"/> </span><span class="subtitle"><fmt:message key="fbuilder.properties.description"/></span></span></a></li>
                        <li id="builder-step-properties"><a href="#" onclick="FormBuilder.previewForm()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.preview"/> </span><span class="subtitle"><fmt:message key="fbuilder.preview.description"/></span></span></a></li>
                        <li class="last-inactive"><a href="#" onclick="saveForm()"><span class="steps-bg"><span class="title"><fmt:message key="fbuilder.save"/> </span><span class="subtitle"><fmt:message key="fbuilder.save.description"/></span></span></a></li>
                    </ul>
                </div>
                <div id="builder-content">

                    <table>
                        <tr>
                            <td width="200" valign="top">
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
                                                <div class="form-palette-element" element-class="${element.className}" element-property="${element.defaultPropertyValues}">
                                                    <c:set var="elementIconPath" value="${element.formBuilderIcon}"/>
                                                    <c:if test="${empty elementIconPath}">
                                                        <c:set var="elementIconPath" value="/images/v3/builder/sidebar_element.gif"/>
                                                    </c:if>
                                                    <img src="${pageContext.request.contextPath}${elementIconPath}" border="0" align="left" />
                                                    <label>${element.label}</label>
                                                </div>
                                            </li>
                                        </c:forEach>
                                        </ul>
                                    </c:forEach>
                                    </div>
                                </fieldset>
                            </td>
                            <td valign="top">
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

    </body>
</html>
