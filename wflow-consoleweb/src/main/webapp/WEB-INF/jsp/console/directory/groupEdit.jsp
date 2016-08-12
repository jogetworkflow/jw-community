<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.group.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="creategroup" action="${pageContext.request.contextPath}/web/console/directory/group/submit/edit" method="POST" commandName="group" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.directory.group.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.group.common.label.id"/></label>
                    <span class="form-input"><c:out value="${group.id}"/><input id="id" type="hidden" value="<c:out value="${group.id}"/>" name="id"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.group.common.label.name"/></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.group.common.label.description"/></label>
                    <span class="form-input"><form:textarea path="description" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.group.common.label.organization"/></label>
                    <span class="form-input">
                        <form:select path="organizationId" cssErrorClass="form-input-error">
                            <option value=""></option>
                            <form:options items="${organizations}" itemValue="id" itemLabel="name"/>
                        </form:select>
                    </span>
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
