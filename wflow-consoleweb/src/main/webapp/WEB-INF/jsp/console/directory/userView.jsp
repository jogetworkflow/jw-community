<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-group"></i> <fmt:message key='console.header.menu.label.users'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="directorySubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="onEdit()"><fmt:message key="console.directory.user.edit.label"/></button></li>
            <li><button onclick="onDelete()"><fmt:message key="console.directory.user.delete.label"/></button></li>
            <li><button onclick="assignReportTo()"><fmt:message key="console.directory.user.reportTo.assign.label"/></button></li>
            <c:if test="${!empty employment.employmentReportTo && !empty employment.employmentReportTo.reportTo}">
                <li><button onclick="unassignReportTo()"><fmt:message key="console.directory.user.reportTo.unassign.label"/></button></li>
            </c:if>
            <li><button onclick="assignGroups()"><fmt:message key="console.directory.user.group.assign.label"/></button></li>
        </ul>
        <c:if test="${!empty addOnButtons}">
            ${addOnButtons}
        </c:if>
    </div>
    <div id="main-body">
        <fieldset class="view">
            <legend><fmt:message key="console.directory.user.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.username"/></label>
                <span class="form-input"><c:out value="${user.username}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.firstName"/></label>
                <span class="form-input"><c:out value="${user.firstName}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.lastName"/></label>
                <span class="form-input"><c:out value="${user.lastName}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.email"/></label>
                <span class="form-input"><c:out value="${user.email}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.role"/></label>
                <span class="form-input"><c:out value="${roles}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.timeZone"/></label>
                <span class="form-input"><c:out value="${user.timeZoneLabel}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.user.common.label.status"/></label>
                <span class="form-input">
                    <c:choose>
                        <c:when test="${user.active == 1}">
                            <fmt:message key="console.directory.user.common.label.status.active"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="console.directory.user.common.label.status.inactive"/>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
        </fieldset>
        <fieldset class="view">
            <legend><fmt:message key="console.directory.employment.common.label.details"/></legend>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.employeeCode"/></label>
                <span class="form-input"><c:out value="${employment.employeeCode}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.role"/></label>
                <span class="form-input"><c:out value="${employment.role}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.department"/></label>
                <span class="form-input">
                    <c:if test="${!empty employment.department}">
                        <a href="${pageContext.request.contextPath}/web/console/directory/dept/view/${employment.department.id}"><c:out value="${employment.department.name}"/></a>
                    </c:if>
                </span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.grade"/></label>
                <span class="form-input">
                    <c:if test="${!empty employment.grade}">
                        <a href="${pageContext.request.contextPath}/web/console/directory/grade/view/${employment.grade.id}"><c:out value="${employment.grade.name}"/></a>
                    </c:if>
                </span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.organization"/></label>
                <span class="form-input">
                    <c:if test="${!empty employment.organization}">
                        <a href="${pageContext.request.contextPath}/web/console/directory/org/view/${employment.organization.id}"><c:out value="${employment.organization.name}"/></a>
                    </c:if>
                </span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.startDate"/></label>
                <span class="form-input"><c:out value="${employment.startDate}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.endDate"/></label>
                <span class="form-input"><c:out value="${employment.endDate}"/></span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.hod"/></label>
                <span class="form-input">
                    <c:choose>
                        <c:when test="${!empty employment.hods}">
                            <fmt:message key="console.directory.employment.common.label.hod.yes"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="console.directory.employment.common.label.hod.no"/>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="form-row">
                <label for="field1"><fmt:message key="console.directory.employment.common.label.reportTo"/></label>
                <span class="form-input">
                    <c:if test="${!empty employment.employmentReportTo && !empty employment.employmentReportTo.reportTo && !empty employment.employmentReportTo.reportTo.user}">
                        <a href="${pageContext.request.contextPath}/web/console/directory/user/view/${employment.employmentReportTo.reportTo.user.id}."><c:out value="${employment.employmentReportTo.reportTo.user.firstName} ${employment.employmentReportTo.reportTo.user.lastName}"/></a>
                    </c:if>
                </span>
            </div>
        </fieldset>
        <div class="view">
            <div class="main-body-content-subheader"><span><fmt:message key="console.directory.user.common.label.groupList"/><span></div>
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/group/list?userId=${user.id}&${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="groupList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/directory/group/view"
                       hrefParam="id"
                       hrefSuffix="."
                       hrefQuery="false"
                       hrefDialog="false"
                       hrefDialogWidth="600px"
                       hrefDialogHeight="400px"
                       hrefDialogTitle="Process Dialog"
                       checkbox="${!isCustomDirectoryManager}"
                       checkboxButton1="console.directory.user.group.assign.label"
                       checkboxCallback1="assignGroups"
                       checkboxOptional1="true"
                       checkboxButton2="console.directory.user.group.unassign.label"
                       checkboxCallback2="unassignGroups"
                       searchItems="name|Name"
                       fields="['id','name','description','organization.name']"
                       column1="{key: 'id', label: 'console.directory.group.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.group.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.group.common.label.description', sortable: false}"
                       column4="{key: 'organization.name', label: 'console.directory.group.common.label.organization', sortable: false}"
                       />
        </div>
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager || user.readonly}">
            $('#main-action-buttons').remove();
            $('#JsonDataTable_groupList-buttons').remove();
        </c:if>
    });
    
    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/user/edit/${user.id}."/>
    <ui:popupdialog var="popupDialog2" src="${pageContext.request.contextPath}/web/console/directory/user/${user.id}/group/assign/view"/>
    <ui:popupdialog var="popupDialog3" src="${pageContext.request.contextPath}/web/console/directory/user/${user.id}/reportTo/assign/view"/>

    function onEdit(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
        popupDialog2.close();
        popupDialog3.close();
    }

    function onDelete(){
         if (confirm('<fmt:message key="console.directory.user.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/users';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/user/delete', callback, 'ids=${user.id}');
        }
    }

    function assignGroups(dummy){
        popupDialog2.init();
    }

    function assignReportTo(){
        popupDialog3.init();
    }

    function unassignReportTo(){
         if (confirm('<fmt:message key="console.directory.user.reportTo.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/user/view/${user.id}.';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/user/${user.id}/reportTo/unassign', callback, '');
        }
    }

    function unassignGroups(selectedIds){
         if (confirm('<fmt:message key="console.directory.user.group.unassign.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/user/view/${user.id}.';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/user/${user.id}/group/unassign', callback, 'ids='+ selectedIds);
        }
    }
</script>

<script>
    Template.init("#menu-users", "#nav-users-users");
</script>

<commons:footer />


