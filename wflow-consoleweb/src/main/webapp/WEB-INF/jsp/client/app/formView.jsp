<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<c:choose>
    <c:when test="${activityForm.type == 'EXTERNAL'}">
        <style>
            #assignmentExternalForm {
                border: 0px;
                width: 100%;
                height: 400px;
            }
        </style>
        <iframe id="assignmentExternalForm" src="${activityForm.formUrl}appId=${appDef.id}&appVersion=${appDef.version}&processId=${assignment.processId}&activityId=${assignment.activityId}&processVersion=${assignment.processVersion}&processRequesterId=${assignment.processRequesterId}&username=<%= WorkflowUtil.getCurrentUsername() %>" frameborder="0" style="${activityForm.formIFrameStyle}"></iframe>
    </c:when>
    <c:otherwise>

        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.5.2.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css" />

        <c:if test="${rightToLeft == 'true'}">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form_rtl.css" />
        </c:if>
        
        <script type="text/javascript">
            $(document).ready(function() {
                // add toggle json link
                $("#form-json-link").click(function() {
                    if ($("#form-info").css("display") != "block") {
                        $("#form-info").css("display", "block");
                    } else {
                        $("#form-info").css("display", "none");
                    }
                });
            });
        </script>

        <fieldset id="form-canvas">

            <c:if test="${submitted && errorCount > 0}">
                <div class="form-message"><fmt:message key="client.app.run.process.label.validationError" /></div>
            </c:if>
            <c:if test="${submitted && errorCount <= 0}">
                <div class="form-message"><fmt:message key="client.app.run.process.label.formSubmitted" /></div>
                <c:if test="${closeDialog}">
                <script type="text/javascript">
                    if (parent && parent.PopupDialog && parent.PopupDialog.closeDialog) {
                        parent.PopupDialog.closeDialog();
                    }else if (opener) {
                        window.close();
                    }
                    if (parent && parent.closeDialog) {
                        parent.closeDialog();
                    } 
                </script>
                </c:if>
            </c:if>
            <c:out value="${formHtml}" escapeXml="false" />
            
        </fieldset>

    </c:otherwise>
</c:choose>

