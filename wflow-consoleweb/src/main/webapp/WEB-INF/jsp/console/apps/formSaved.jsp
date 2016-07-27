<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
    <c:choose>
        <c:when test="${param.builderMode eq 'true'}">
            <script type="text/javascript">
                parent.location.href = "${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appDefinition.version}/form/builder/<c:out value="${formId}"/>";
            </script>
        </c:when>
        <c:otherwise>
            <div id="main-body-header">
                <fmt:message key="console.form.create.label.title"/>
            </div>
            <div id="main-body-content">
                <p>&nbsp;</p>
                <div id="main-body-message">
                    <fmt:message key="console.form.create.label.saved"/>
                    <p id="main-body-submessage"><fmt:message key="console.form.create.label.popupWarning"/></p>
                    <button onclick="launchFormBuilder()" class="form-button-large"><fmt:message key="console.form.create.label.launch"/></button>
                </div>
            </div>
            <script type="text/javascript">
                function closeDialog() {
                    if("<c:out value="${activityDefId}"/>" != ""){
                        var parentUrlQueryString = parent.location.search;
                        if(parentUrlQueryString == '')
                            parent.location.href = parent.location.href + "?tab=activityList&activityDefId=<c:out value="${activityDefId}"/>";
                        else{
                            if(parentUrlQueryString.indexOf('tab') == -1)
                                parent.location.href = parent.location.href + "&tab=activityList&activityDefId=<c:out value="${activityDefId}"/>";
                            else{

                                parent.location.href = parent.location.href.replace(parentUrlQueryString, '') + "?tab=activityList&activityDefId=<c:out value="${activityDefId}"/>";
                            }
                        }
                    }else{
                        if (parent && parent.PopupDialog.closeDialog) {
                            parent.PopupDialog.closeDialog();
                        }
                        if (parent && parent.refreshNavigator) {
                            parent.refreshNavigator();
                        }
                    }
                    return false;
                }

                function launchFormBuilder() {
                    var formBuilder = window.open("${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appDefinition.version}/form/builder/<c:out value="${formId}"/>");
                    if (formBuilder) {
                        closeDialog();
                    }
                }

                if (parent && parent.JsonDataTable) {
                    parent.JsonDataTable.refresh();
                }

                launchFormBuilder();
            </script>
        </c:otherwise>
    </c:choose>
<commons:popupFooter />



