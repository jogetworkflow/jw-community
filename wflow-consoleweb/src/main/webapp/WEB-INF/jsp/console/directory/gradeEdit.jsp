<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.grade.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createGrade" action="${pageContext.request.contextPath}/web/console/directory/grade/submit/edit?orgId=${organization.id}" method="POST" modelAttribute="grade" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.directory.grade.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.grade.common.label.id"/></label>
                    <span class="form-input"><c:out value="${grade.id}"/><input id="id" type="hidden" value="<c:out value="${grade.id}"/>" name="id"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.grade.common.label.name"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.grade.common.label.description"/></label>
                    <span class="form-input"><form:textarea path="description" cssErrorClass="form-input-error" /></span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>" onclick="validateField()"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function validateField(){
            var valid = true;
            var alertString = "";
            if($("[name=description]").val().length > 255){
                alertString += '<ui:msgEscJS key="console.directory.group.error.label.descriptionLimit"/>';
                valid = false;
            }
            
            if(valid){
                $("#createGrade").submit();
            } else {
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
