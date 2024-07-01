<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}"/>

    <script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/wro/codeMirror.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/wro/codeMirror.min.css" />

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
            codeeditor = CodeMirror(document.getElementById("value_editor"), {
                lineNumbers: true,
                mode: "text",
                autoRefresh:true,
                matchBrackets: true,
                theme: "default",
                gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
                lint: true,
                autoCloseTags: true,
                autoCloseBrackets: true,
                foldGutter: true,
                lint: true,
                lineWrapping: true,
                highlightSelectionMatches: {annotateScrollbar: true, minChars: 1},
                extraKeys: {
                    "Ctrl-F": function(cm) {
                        cm.execCommand("replace")
                        $('#value_editor').find(".CodeMirror-advanced-dialog").css({display: 'block'})
                        if (codeeditor.getOption("fullScreen")){
                            $('#value_editor').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: "0px", right: "0px", width: "320px", marginRight: "30px"})
                        }
                        else{
                            $('#value_editor').find(".CodeMirror-advanced-dialog").css({position:"sticky", top:"0px", width:"320px", zIndex:"10"});
                        }
                        $('#value_editor').css({overflow: "visible"})
                    },
                    "Ctrl-=": function(cm) {
                      modifyFontSize(true);
                    },
                    "Ctrl--": function(cm) {
                      modifyFontSize(false);
                    }
                  }
              });
            
            //Make the replace appear
            codeeditor.execCommand("replace");
            
            //Set height
            $('#value_editor').find(".CodeMirror-advanced-dialog").css({display: 'none'})
            $('#value_editor').find(".CodeMirror").css({"height":"auto", "minHeight": "175px"})
            $('#value_editor').find(".CodeMirror-scroll").css({"maxHeight":"100%", "minHeight":"175px"});

            //Set dark theme if dark theme mode is activated
            if ($('body').attr('builder-theme') === "dark") {
                codeeditor.setOption("theme", "ayu-mirage");
            }

            //Function to modify font size
            function modifyFontSize(increase){
                var wrapper = codeeditor.getWrapperElement();
                var currentSize = parseFloat(window.getComputedStyle(wrapper, null).getPropertyValue('font-size'));
                var newSize = increase ? currentSize + 2 : currentSize - 2;

                wrapper.style.fontSize = newSize + 'px';

                codeeditor.refresh()
            }

            // var editor = ace.edit("value_editor");
            var textarea = $('textarea[name="value"]');
            codeeditor.setValue(textarea.val());

            //Detect keydown for specific actions, such as f12 to toggle full screen mode, escape
            //to exit full scree mode, and F1 to toggle help panel
            $('#value_editor').on('keydown', function(event) {
                if (event.key === 'F12' || (event.key === 'Escape' && codeeditor.getOption("fullScreen"))){
                    event.preventDefault();
                    if (codeeditor.getOption("fullScreen")) {
                        codeeditor.setOption("fullScreen", false);
                        $('#value_editor').find(".CodeMirror-advanced-dialog").css({display: 'none'})
                        $('#value_editor').find(".CodeMirror").css({"height":"auto", "minHeight": "175px"})
                        $('#value_editor').find(".CodeMirror-scroll").css({"maxHeight":"100%", "minHeight":"175px"});
                        $(this).find(".CodeMirror-advanced-dialog .row.find button:last").click();
                        $(this).find(".CodeMirror-advanced-dialog").css({position:"sticky", top:"0px", width:"320px", zIndex:"10"});
                        $(this).find(".CodeMirror").css({left: "", top: ""})
                        event.stopPropagation();
                    }else{
                        codeeditor.setOption("fullScreen", true);
                        $('#value_editor').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: "0px", right: "0px", width: "320px", marginRight: "30px"});
                    }
                }
            });

            codeeditor.on('change', function(){
                textarea.val(codeeditor.getValue());
            });
        });
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_-]+$/.test($("#id").val());
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<ui:msgEscJS key="console.app.envVariable.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                alert(alertString);
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
