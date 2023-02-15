<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.directory.user.edit.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createUser" action="${pageContext.request.contextPath}/web/console/directory/user/submit/edit" method="POST" modelAttribute="user" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <c:out value="${error}"/><br/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.directory.user.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.username"/></label>
                    <span class="form-input"><c:out value="${user.username}"/><input id="id" type="hidden" value="<c:out value="${user.id}"/>" name="id"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.firstName"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="firstName" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.lastName"/></label>
                    <span class="form-input"><form:input path="lastName" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.email"/></label>
                    <span class="form-input"><form:input path="email" cssErrorClass="form-input-error" autocomplete="off"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.password"/></label>
                    <span class="form-input"><form:password path="password" cssErrorClass="form-input-error" autocomplete="off"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.confirmPassword"/></label>
                    <span class="form-input"><form:password path="confirmPassword" cssErrorClass="form-input-error" autocomplete="off"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.role"/></label>
                    <span class="form-input">
                        <form:select path="roles" cssErrorClass="form-input-error" size="4" multiple="true">
                            <form:options items="${roles}" itemValue="id" itemLabel="name"/>
                        </form:select>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.timeZone"/></label>
                    <span class="form-input">
                        <form:select path="timeZone" cssErrorClass="form-input-error">
                            <form:options items="${timezones}"/>
                        </form:select>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.status"/></label>
                    <span class="form-input">
                        <form:select path="active" cssErrorClass="form-input-error">
                            <form:options items="${status}"/>
                        </form:select>
                    </span>
                </div> 
            </fieldset>
            <fieldset>
                <legend><fmt:message key="console.directory.employment.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.employeeCode"/></label>
                    <span class="form-input">
                        <input id="employeeCode" name="employeeCode" type="text" value="<c:out value="${employeeCode}"/>"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.role"/></label>
                    <span class="form-input">
                        <input id="employeeRole" name="employeeRole" type="text" value="<c:out value="${employeeRole}"/>"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.startDate"/></label>
                    <span class="form-input">
                        <input id="employeeStartDate" name="employeeStartDate" type="text" value="<c:out value="${employeeStartDate}"/>"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.endDate"/></label>
                    <span class="form-input">
                        <input id="employeeEndDate" name="employeeEndDate" type="text" value="<c:out value="${employeeEndDate}"/>"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.department"/></label>
                    <span class="form-input">
                        <table>
                            <tr class="template" style="display:none;">
                                <td class="delete">
                                    <a class="delete"><i class="fas fa-minus-circle"></i><a>
                                </td>
                                <td class="org">
                                    <select name="employeeDeptOrganization" disabled="true">
                                        <option value=""><fmt:message key="console.directory.employment.common.label.organization"/></option>
                                        <c:forEach items="${organizations}" var="org">
                                            <option value="<c:out value="${org.id}"/>"><c:out value="${org.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </td>
                                <td class="dept">
                                    <select name="employeeDepartment" disabled="true">
                                        <option value=""><fmt:message key="console.directory.employment.common.label.department"/></option>
                                    </select>
                                </td>
                                <td class="isHod">
                                    <input type="hidden" name="employeeDepartmentHod" value="" disabled="true"/>
                                    <label><input value="true" type="checkbox" disabled="true"/><fmt:message key="console.directory.employment.common.label.hod"/></label>
                                </td>
                            </tr>
                            <c:if test="${!empty employments}">
                                <c:forEach items="${employments}" var="e" >
                                    <c:if test="${!empty e.organizationId || !empty e.departmentId}">
                                        <tr class="row">
                                            <td class="delete">
                                                <a class="delete"><i class="fas fa-minus-circle"></i><a>
                                            </td>
                                            <td class="org">
                                                <select name="employeeDeptOrganization">
                                                    <option value=""><fmt:message key="console.directory.employment.common.label.organization"/></option>
                                                    <c:forEach items="${organizations}" var="org">
                                                        <option value="<c:out value="${org.id}"/>" <c:if test="${org.id eq e.organizationId}">selected</c:if>><c:out value="${org.name}"/></option>
                                                    </c:forEach>
                                                </select>
                                            </td>
                                            <td class="dept">
                                                <select name="employeeDepartment" data-value="<c:out value="${e.departmentId}"/>">
                                                    <option value=""><fmt:message key="console.directory.employment.common.label.department"/></option>
                                                </select>
                                            </td>
                                            <td class="isHod">
                                                <input type="hidden" name="employeeDepartmentHod" value="<c:if test="${!empty e.hods}">true</c:if>"/>
                                                <label><input value="true" type="checkbox" <c:if test="${!empty e.hods}">checked</c:if>/> <fmt:message key="console.directory.employment.common.label.hod"/></label>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </table> 
                        <a class="add addDept"><i class="fas fa-plus-circle"></i></a>        
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.employment.common.label.grade"/></label>
                    <span class="form-input">
                        <table>
                            <tr class="template" style="display:none;">
                                <td class="delete">
                                    <a class="delete"><i class="fas fa-minus-circle"></i><a>
                                </td>
                                <td class="org">
                                    <select name="employeeGradeOrganization" disabled="true">
                                        <option value=""><fmt:message key="console.directory.employment.common.label.organization"/></option>
                                        <c:forEach items="${organizations}" var="org">
                                            <option value="<c:out value="${org.id}"/>"><c:out value="${org.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </td>
                                <td class="grade">
                                    <select name="employeeGrade" disabled="true">
                                        <option value=""><fmt:message key="console.directory.employment.common.label.grade"/></option>
                                    </select>
                                </td>
                            </tr>
                            <c:set var="displayed" value="" />
                            <c:if test="${!empty employments}">
                                <c:forEach items="${employments}" var="e" >
                                    <c:if test="${!empty e.gradeId}">
                                        <c:set var="check" value="${e.gradeId};" />
                                        <c:if test="${!fn:contains(displayed, check)}">
                                            <tr class="row">
                                                <td class="delete">
                                                    <a class="delete"><i class="fas fa-minus-circle"></i><a>
                                                </td>
                                                <td class="org">
                                                    <select name="employeeGradeOrganization">
                                                        <option value=""><fmt:message key="console.directory.employment.common.label.organization"/></option>
                                                        <c:forEach items="${organizations}" var="org">
                                                            <option value="<c:out value="${org.id}"/>" <c:if test="${org.id eq e.organizationId}">selected</c:if>><c:out value="${org.name}"/></option>
                                                        </c:forEach>
                                                    </select>
                                                </td>
                                                <td class="grade">
                                                    <select name="employeeGrade" data-value="<c:out value="${e.gradeId}"/>">
                                                        <option value=""><fmt:message key="console.directory.employment.common.label.grade"/></option>
                                                    </select>
                                                </td>
                                            </tr>
                                            <c:set var="displayed" value="${displayed}${e.grade.id};" />
                                        </c:if>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </table> 
                        <a class="add addGrade"><i class="fas fa-plus-circle"></i></a>        
                    </span>
                </div>                
            </fieldset>
            ${userFormFooter} 
            <div class="form-buttons">
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        var orgs = {};
        
        $(document).ready(function(){
            $("table").on("click", "a.delete", function(){
               $(this).closest("tr").remove();
               return false;
            });
            
            $("a.add").on("click", function(){
               var table = $(this).closest(".form-input").find("table");
               var template = $(table).find("tr.template").html();
               $(table).append("<tr class=\"row\">"+template+"</tr>");
               $(table).find("tr:last").find("select, input").removeAttr("disabled");
               return false;
            });
            
            loadDepartmentAndGradeOption();

            $('table').on("change", "tr.row td.org select", function(){
                updateDepartmentOrGradeOption(this);
            });
            
            $('table').on("change", "tr.row td.isHod input[type=checkbox]", function(){
                var hidden = $(this).closest("td").find("input[type=hidden]");
                if ($(this).is(":checked")) {
                    $(hidden).val("true");
                } else {
                    $(hidden).val("");
                }
            });
        });

        function validateField(){
            var valid = true;
            var alertString = "";
            if($("[name=password]").val() != $("[name=confirmPassword]").val()){
                alertString += '<ui:msgEscJS key="console.directory.user.error.label.passwordNotMatch"/>';
                valid = false;
            }

            if(valid){
                $("#createUser").submit();
            }else{
                alert(alertString);
            }
        }

        function loadDepartmentAndGradeOption(){
            $('tr.row td.org select').each(function(){
                if ($(this).val() !== "" && orgs[$(this).val()] === undefined) {
                    orgs[$(this).val()] = {};
                }
            });
            
            for (var property in orgs) {
                if (orgs.hasOwnProperty(property)) {
                    retrieveOptions(property);
                }
            }
        }
        
        function retrieveOptions(orgId) {
            if (orgs[orgId] === undefined || orgs[orgId].departments === undefined) {
                ConnectionManager.get('${pageContext.request.contextPath}/web/json/directory/admin/user/deptAndGrade/options', {
                    success : function(data) {
                        var obj = eval('(' + data + ')');
                        orgs[orgId] = obj;
                        $('tr.row td.org select').each(function(){
                            if ($(this).val() === orgId) {
                                updateDepartmentOrGradeOption(this);
                            }
                        });
                    }
                }, 'rnd=' + new Date().valueOf().toString() + '&orgId='+orgId);
            }
        }
        
        function updateDepartmentOrGradeOption(select) {
            var orgId = $(select).val();
            if (orgs[orgId] === undefined || orgs[orgId].departments === undefined) {
                retrieveOptions(orgId);
            } else {
                var td = $(select).closest("tr").find("td.grade, td.dept");
                var options = [];
                if ($(td).hasClass("grade") && Array.isArray(orgs[orgId].grades)) {
                    options = orgs[orgId].grades;
                } else if ($(td).hasClass("dept") && Array.isArray(orgs[orgId].departments)) {
                    options = orgs[orgId].departments;
                }
                
                var field = $(td).find("select");
                var dvalue = $(field).data("value");
                if (dvalue === undefined || dvalue === null) {
                    dvalue = "";
                }
                $(field).find("option:not([value=''])").remove();
                for(var i=0; i < options.length; i++){
                    if (options[i].id !== "") {
                        $(field).append('<option value="' + UI.escapeHTML(options[i].id) + '">' + UI.escapeHTML(options[i].name) + '</option>');
                    }
                }
                $(field).val(dvalue);
            }
        }
        
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }

        Calendar.show("employeeStartDate");
        Calendar.show("employeeEndDate");
    </script>
<commons:popupFooter />
