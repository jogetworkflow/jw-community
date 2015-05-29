<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.app.envVariable.create.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createEnvVariable" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/envVariable/submit/create" method="POST" commandName="environmentVariable" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.app.envVariable.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.id"/></label>
                    <span class="form-input"><form:input path="id" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.value"/></label>
                    <span class="form-input"><form:textarea path="value" cssErrorClass="form-input-error" rows="10" cols="60" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.envVariable.common.label.remarks"/></label>
                    <span class="form-input"><form:textarea path="remarks" cssErrorClass="form-input-error" cols="60" /></span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_-]+$/.test($("#id").attr("value"));
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<fmt:message key="console.app.envVariable.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                alert(alertString);
            }else{
                $("#createEnvVariable").submit();
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
