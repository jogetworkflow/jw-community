<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.message.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createMessage" action="${pageContext.request.contextPath}/web/console/setting/message/submit/edit" method="POST" commandName="message" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.setting.message.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.key"/></label>
                    <span class="form-input"><c:out value="${message.key}"/><input id="id" type="hidden" value="<c:out value="${message.id}"/>" name="id"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.locale"/></label>
                    <span class="form-input"><c:out value="${message.locale}"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.message"/></label>
                    <span class="form-input"><form:textarea path="message" cssErrorClass="form-input-error" /></span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="general.method.label.save"/>"/>
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
