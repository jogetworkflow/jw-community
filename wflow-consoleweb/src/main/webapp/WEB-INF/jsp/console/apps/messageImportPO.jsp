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
        <fmt:message key="console.app.message.import.po.label.title"/>
    </div>

    <div id="main-body-content">
        <c:if test="${errorMessage != null}">
            <div class="form-error">
                ${errorMessage}
            </div>
        </c:if>

        <c:if test="${errorList != null}">
            <div class="form-error">
                <div style="borderx: 1px dashed #aaa; backgroundx: #e5e5e5; padding: 0.5em">
                <c:forEach var="error" items="${errorList}">
                    <div style="border-bottomx: 1px solid #ccc; margin-bottom: 5px">${error}</div>
                </c:forEach>
                </div>
            </div>
        </c:if>

        <form id="importPO" method="post" action="${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/message/importpo/submit?containerId=<c:out value="${containerId}" escapeXml="true" />&columnId=<c:out value="${columnId}" escapeXml="true" />&lang=<c:out value="${lang}" escapeXml="true" />" class="form blockui" enctype="multipart/form-data">
            <div class="form-row">
                <label for="localeFile" class="upload"><fmt:message key="console.app.message.import.po.label.poFile"/></label>
                <span class="form-input">
                    <input id="localeFile" type="file" name="localeFile"/>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="console.app.message.import.po.label.upload"/>" />
            </div>
        </form>
    </div>
<commons:popupFooter />
