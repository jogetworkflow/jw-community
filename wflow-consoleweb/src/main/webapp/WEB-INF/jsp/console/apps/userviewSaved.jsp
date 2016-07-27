<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
    <c:choose>
        <c:when test="${param.builderMode eq 'true'}">
            <script type="text/javascript">
                parent.location.href = "${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/userview/builder/<c:out value="${userviewDefinition.id}"/>";
            </script>
        </c:when>
        <c:otherwise>
            <div id="main-body-header">
                <fmt:message key="console.userview.create.label.title"/>
            </div>
            <div id="main-body-content">
                <p>&nbsp;</p>
                <div id="main-body-message">
                    <fmt:message key="console.userview.create.label.saved"/>
                    <p id="main-body-submessage"><fmt:message key="console.userview.create.label.popupWarning"/></p>
                    <button onclick="launchUserviewBuilder()" class="form-button-large"><fmt:message key="console.userview.create.label.launch"/></button>
                </div>
            </div>
            <script type="text/javascript">
                function closeDialog() {
                    if (parent && parent.PopupDialog.closeDialog) {
                        parent.PopupDialog.closeDialog();
                    }
                    if (parent && parent.refreshNavigator) {
                        parent.refreshNavigator();
                    }
                    return false;
                }

                function launchUserviewBuilder() {
                    var userviewBuilder = window.open("${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/userview/builder/<c:out value="${userviewDefinition.id}"/>");
                    if (userviewBuilder) {
                        closeDialog();
                    }
                }

                if (parent && parent.JsonDataTable) {
                    parent.JsonDataTable.refresh();
                }

                launchUserviewBuilder();
            </script>
        </c:otherwise>
    </c:choose>
<commons:popupFooter />



