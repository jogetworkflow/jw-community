<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.MobileUtil"%>

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
        <iframe id="assignmentExternalForm" src="<c:out value="${activityForm.formUrl}appId=${appDef.id}&appVersion=${appDef.version}&processId=${assignment.processId}&activityId=${assignment.activityId}&processVersion=${assignment.processVersion}&processRequesterId=${assignment.processRequesterId}"/>&username=<%= WorkflowUtil.getCurrentUsername() %>" frameborder="0" style="<c:out value="${activityForm.formIFrameStyle}"/>"></iframe>
    </c:when>
    <c:otherwise>

        <c:set var="mobileView" value="<%= MobileUtil.isMobileView() %>"/>
        <c:if test="${!mobileView}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/form_common.css?build=<fmt:message key="build.number"/>" />
            <script type="text/javascript" src="${pageContext.request.contextPath}/wro/form_common.js?build=<fmt:message key="build.number"/>"></script>
            
            <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form_rtl.css?build=<fmt:message key="build.number"/>" />
            </c:if>
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
                <c:if test="${!stay && submitted && errorCount <= 0}">
                    <div class="form-message"><fmt:message key="client.app.run.process.label.formSubmitted" /></div>
                    <c:if test="${!empty closeDialog && closeDialog}">
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

