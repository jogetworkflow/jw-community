<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css" />

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
                    } else if (parent && parent.closeDialog) {
                        parent.closeDialog();
                    } else if (opener) {
                        window.close();
                    }
                </script>
                </c:if>
            </c:if>
            <c:out value="${formHtml}" escapeXml="false" />
            
        </fieldset>
