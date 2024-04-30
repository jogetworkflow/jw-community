<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.message.export.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createMessage" action="${pageContext.request.contextPath}/web/console/setting/message/export/submit" method="POST" modelAttribute="message" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.export.label.locale"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:select path="locale" cssErrorClass="form-input-error"><form:options items="${localeList}"/></form:select></span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="console.setting.message.export.label.export"/>"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
       $(document).ready(function() {
            $("#createMessage").submit(function(event) {
                closeDialog();
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