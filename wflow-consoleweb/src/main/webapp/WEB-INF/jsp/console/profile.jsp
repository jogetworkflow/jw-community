<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.user.profile.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="profile" action="${pageContext.request.contextPath}/web/console/profile/submit" method="POST" commandName="user" cssClass="form">
            <form:hidden path="id"/>
            <form:hidden path="username"/>

            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.directory.user.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.username"/></label>
                    <span class="form-input">${user.username}</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.firstName"/></label>
                    <span class="form-input"><form:input path="firstName" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.lastName"/></label>
                    <span class="form-input"><form:input path="lastName" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.email"/></label>
                    <span class="form-input"><form:input path="email" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.password"/></label>
                    <span class="form-input"><form:password path="password" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.confirmPassword"/></label>
                    <span class="form-input"><form:password path="confirmPassword" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.timeZone"/></label>
                    <span class="form-input">
                        <form:select path="timeZone" cssErrorClass="form-input-error">
                            <form:options items="${timezones}"/>
                        </form:select>
                    </span>
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
            var valid = true;
            var alertString = "";
            if($("#firstName").val() == ""){
                alertString += '<fmt:message key="User.firstName[not.blank]"/>';
                valid = false;
            }
            if($("#password").val() != $("#confirmPassword").val()){
                if(alertString != ""){
                    alertString += '\n';
                }
                alertString += '<fmt:message key="console.directory.user.error.label.passwordNotMatch"/>';
                valid = false;
            }

            if(valid){
                $("#profile").submit();
            }else{
                alert(alertString);
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
