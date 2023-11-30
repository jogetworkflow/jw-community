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
    <div id="main-body-header">
        <fmt:message key="console.app.resources.create.label"/>
    </div>
    <div id="main-body-content">

        <form id="addResource" method="post" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/submit" class="form blockui" enctype="multipart/form-data">
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            
            <fieldset>
                <div class="form-row">
                    <label for="file" class="upload"><fmt:message key="console.app.import.label.selectFile"/></label>
                    <span class="form-input">
                        <input id="file" type="file" name="file"/>
                    </span>
                </div>
                <div class="form-buttons">
                    <input class="form-button" type="submit" value="<ui:msgEscHTML key="general.method.label.upload"/>" />
                    <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
                </div>
            </fieldset>
        </form>
    </div>
                
    <script>            
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>               
                    
<commons:popupFooter />
