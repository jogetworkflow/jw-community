<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
    <c:choose>
        <c:when test="${param.builderMode eq 'true'}">
            <script type="text/javascript">
                parent.location.href = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/design/<c:out value="${builderDefinition.id}"/>";
            </script>
        </c:when>
        <c:otherwise>
            <div id="main-body-header">
                <fmt:message key="console.builder.create.label"><fmt:param value="${builder.objectLabel}"/></fmt:message>
            </div>
            <div id="main-body-content">
                <p>&nbsp;</p>
                <div id="main-body-message" class="align-center">
                    <h2><fmt:message key="console.builder.create.label.saved"><fmt:param value="${builder.objectLabel}"/></fmt:message></h2>
                    <p id="main-body-submessage"><fmt:message key="console.datalist.create.label.popupWarning"/></p>
                    <button onclick="launchBuilder()" class="form-button-large"><fmt:message key="console.builder.create.label.launch"><fmt:param value="${builder.label}"/></fmt:message></button>
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

                function launchBuilder() {
                    var builder = window.parent.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/design/<c:out value="${builderDefinition.id}"/>");
                    if (builder) {
                        closeDialog();
                    }
                }

                if (parent && parent.JsonDataTable) {
                    parent.JsonDataTable.refresh();
                }

                launchBuilder();
            </script>
        </c:otherwise>
    </c:choose>
<commons:popupFooter />



