<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.message.create.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createMessage" action="${pageContext.request.contextPath}/web/console/setting/message/submit/create" method="POST" modelAttribute="message" cssClass="form blockui">
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
                    <label for="field1"><fmt:message key="console.setting.message.common.label.key"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="key" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.locale"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:select path="locale" cssErrorClass="form-input-error"><form:options items="${localeList}"/></form:select></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.message"/></label>
                    <span class="form-input"><form:textarea path="message" cssErrorClass="form-input-error" /></span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_]+$/.test($("#messageKey").val());
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<ui:msgEscJS key="console.setting.message.error.label.idInvalid"/>';
                    $("#key").focus();
                }
                alert(alertString);
            }else{
                $("#createMessage").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
