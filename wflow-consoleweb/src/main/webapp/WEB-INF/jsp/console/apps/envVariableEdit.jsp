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
    
    <div id="main-body-header">
        <fmt:message key="console.app.envVariable.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createEnvVariable" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/envVariable/submit/edit" method="POST" modelAttribute="environmentVariable" cssClass="form blockui">
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
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.id"/></label>
                    <span class="form-input"><c:out value="${environmentVariable.id}"/><input id="id" type="hidden" value="<c:out value="${environmentVariable.id}"/>" name="id"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="setting.plugin.hashVariable"/></label>
                    <span class="form-input">#appVariable.<c:out value="${environmentVariable.id}"/>#</span>
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
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="general.method.label.save"/>"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        $(document).ready(function() {
            var editor = ace.edit("value_editor");
            var textarea = $('textarea[name="value"]');
            editor.getSession().setValue(textarea.val());
            editor.getSession().setTabSize(4);
            if ($('body').attr('builder-theme') === "dark") {
                editor.setTheme("ace/theme/vibrant_ink");
            } else {
                editor.setTheme("ace/theme/textmate");
            }
            editor.getSession().setMode("ace/mode/text");
            editor.setAutoScrollEditorIntoView(true);
            editor.setOption("maxLines", 1000000); //unlimited, to fix the height issue
            editor.setOption("minLines", 10);
            editor.resize();
            editor.getSession().on('change', function(){
                textarea.val(editor.getSession().getValue());
            });
        });
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
