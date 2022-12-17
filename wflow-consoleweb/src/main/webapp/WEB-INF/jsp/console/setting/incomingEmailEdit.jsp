<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.incomingEmail.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="editIncomingEmail" action="${pageContext.request.contextPath}/web/console/setting/incomingEmail/submit/edit" method="POST" commandName="incomingEmail" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.setting.incomingEmail.common.label.details"/></legend>
                <div class="form-row">
                    <label for="id"><fmt:message key="console.setting.scheduler.common.label.id"/></label>
                    <span class="form-input"><c:out value="${incomingEmail.id}"/><input id="id" type="hidden" value="${incomingEmail.id}" name="id" /></span>
                </div>
                <div class="form-row">
                    <label for="priority"><fmt:message key="console.setting.incomingEmail.common.label.priority"/></label>
                    <span class="form-input"><form:input path="priority" cssErrorClass="form-input-error" size="30" /> *</span>
                </div>
                <div class="form-row">
                    <label for="username"><fmt:message key="console.setting.incomingEmail.common.label.username"/></label>
                    <span class="form-input"><form:input path="username" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="password"><fmt:message key="console.setting.incomingEmail.common.label.password"/></label>
                    <span class="form-input"><form:input path="password" cssErrorClass="form-input-error" size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="protocol"><fmt:message key="console.setting.incomingEmail.common.label.protocol"/></label>
                    <span class="form-input"><form:input path="protocol" cssErrorClass="form-input-error" size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="host"><fmt:message key="console.setting.incomingEmail.common.label.host"/></label>
                    <span class="form-input"><form:input path="host" cssErrorClass="form-input-error" size="30" /> *</span>
                </div>
                <div class="form-row">
                    <label for="port"><fmt:message key="console.setting.incomingEmail.common.label.port"/></label>
                    <span class="form-input"><form:input path="port" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="folder"><fmt:message key="console.setting.incomingEmail.common.label.folder"/></label>
                    <span class="form-input"><form:input path="folder" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="active"><fmt:message key="console.setting.incomingEmail.common.label.active"/></label>
                    <span class="form-input"><form:checkbox path="active" cssErrorClass="form-input-error"  size="30"/></span>
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
                    let validators = {
                        priority: {
                            pattern: /^[0-9]+$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.priorityInvalid"/>'
                        },

                        username: {
                            pattern: /^[0-9a-zA-Z_\-@.]+$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.usernameInvalid"/>'
                        },

                        protocol: {
                            pattern: /^imaps?$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.protocolInvalid"/>'
                        },

                        host: {
                            pattern: /^[0-9a-zA-Z_\-.]+$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.hostInvalid"/>'
                        },

                        port: {
                            pattern: /^[0-9]+$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.portInvalid"/>'
                        },
                        folder: {
                            pattern: /^\w+$/,
                            message: '<fmt:message key="console.setting.incomingEmail.error.label.folderInvalid"/>'
                        }
                    };

                    let submit = true;
                	for(let field in validators) {
                	    let validator = validators[field];
                	    let pattern = validator.pattern;
                	    let $field = $('#' + field);
                	    let value = $field.val();
                	    let test = pattern.test(value);
                	    if(!test) {
                	        submit = false;
                	        let message = validator.message;
                	        $field.focus();
                	        alert(message);
                	        break;
                	    }
                	}

                	if(submit) {
                	    $("form#editIncomingEmail").submit();
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
