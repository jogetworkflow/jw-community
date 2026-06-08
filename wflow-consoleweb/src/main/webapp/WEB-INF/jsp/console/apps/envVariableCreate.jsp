<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="true"/>

    <script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror6/codemirror6-bundle.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/wro/codeMirror6.min.css" />

    <div id="main-body-header">
        <fmt:message key="console.app.envVariable.create.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createEnvVariable" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/envVariable/submit/create" method="POST" modelAttribute="environmentVariable" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.app.envVariable.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.id"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="id" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.remarks"/></label>
                    <span class="form-input"><form:textarea path="remarks" cssErrorClass="form-input-error" cols="60" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.value"/></label><br/>
                    <form:textarea path="value" cssErrorClass="form-input-error" rows="10" cols="60" style="display:none" />
                    <pre id="value_editor" name="value_editor" class="ace_editor"></pre>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        $(document).ready(function() {
            var isDark = false;
            //Set dark theme if dark theme mode is activated
            if ($('body').attr('builder-theme') === "dark") {
                isDark = true;
            }

            codeeditor = window.initCM6Editor($("#value_editor")[0], "", "", isDark);

            codeeditor.helpPanel = true;

            $("#value_editor").css({"width" : "50%"});
            $("#value_editor").find(".cm-editor").css({"min-height": "300px"});

            var textarea = $('textarea[name="value"]');
            codeeditor.setValue(textarea.val());

            codeeditor.on('change', function(){
                textarea.val(codeeditor.getValue());
            });

            var tooltip = $(" <i class=\"fas fa-info-circle\"></i>").attr('title', '<ui:msgEscJS key="console.codemirror.tooltipTitle"/>');

            $("#value_editor").siblings("label").append(" ").append(tooltip);
        });
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_-]+$/.test($("#id").val());
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<ui:msgEscJS key="console.app.envVariable.error.label.idInvalid"/>';
                    $("#id").focus();
                }
               UI.alert(alertString, {icon: 'error'});
            }else{
                $("#createEnvVariable").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
