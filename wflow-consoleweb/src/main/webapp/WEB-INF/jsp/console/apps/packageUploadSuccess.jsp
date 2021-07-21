<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:popupHeader />
    <div id="main-body-header">
        <fmt:message key="console.app.import.label.title"/>
    </div>
    <div id="main-body-content">
        <p>&nbsp;</p>
        <div id="main-body-message" class="align-center">
            <p id="main-body-submessage"><fmt:message key="console.form.create.label.popupWarning"/></p>
            <fmt:message key="abuilder.title" var="name" />
            <button onclick="launchAppBuilder()" class="btn btn-primary form-button-large"><fmt:message key="console.builder.create.label.launch"><fmt:param value="${name}"/></fmt:message></button>
        </div>
    </div>
    <script type="text/javascript">
        function launchAppBuilder() {
            var builder = window.parent.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/builders");
            if (builder) {
                parent.PopupDialog.closeDialog();
            }
        }
        
        <c:if test="${!isPublished}">
            if (top.CustomBuilder !== undefined) {
                top.CustomBuilder.ajaxRenderBuilder('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/builders');
                top.AdminBar.hideQuickOverlay();
            } else {
                launchAppBuilder();
            }
        </c:if>
        <c:if test="${isPublished}">
            if (parent && parent.AdminBar.showQuickOverlay) {
                parent.PopupDialog.closeDialog();
            }
            alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
            top.location = '${pageContext.request.contextPath}/web/desktop';
        </c:if>
    </script>
<commons:popupFooter />