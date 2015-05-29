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
            <li><button onclick="onCreate()"><fmt:message key="console.directory.group.create.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.directory.group.filter.label.byOrganization"/>
            <select id="JsonDataTable_filterbyOrg" onchange="filter(JsonDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                <option value=""><fmt:message key="console.directory.group.empty.option.label"/></option>
            <c:forEach items="${organizations}" var="o">
                <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                <option value="<c:out value="${o.id}"/>" ${selected}><c:out value="${o.name}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/group/list?${pageContext.request.queryString}"
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
                       checkboxButton2="general.method.label.delete"
                       checkboxCallback2="deleteGroup"
                       searchItems="name|Name"
                       fields="['id','name','description','organization.name']"
                       column1="{key: 'id', label: 'console.directory.group.common.label.id', sortable: true}"
                       column2="{key: 'name', label: 'console.directory.group.common.label.name', sortable: true}"
                       column3="{key: 'description', label: 'console.directory.group.common.label.description', sortable: false}"
                       column4="{key: 'organization.name', label: 'console.directory.group.common.label.organization', sortable: false}"
                       />

    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isCustomDirectoryManager}">
            $('#main-action-buttons').remove();
            $('#JsonDataTable_groupList-buttons').remove();
        </c:if>
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/directory/group/create"/>

    function onCreate(){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    function deleteGroup(selectedList){
         if (confirm('<fmt:message key="console.directory.group.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/directory/groups';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/directory/group/delete', callback, 'ids='+selectedList);
        }
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){       
        url = "&orgId=" + $('#JsonDataTable_filterbyOrg').val(); 
        url += "&name=" + $('#JsonDataTable_searchCondition').val();
        org_filter(jsonTable, url, '');
    };
</script>

<script>
    Template.init("#menu-users", "#nav-users-groups");
</script>

<commons:footer />
