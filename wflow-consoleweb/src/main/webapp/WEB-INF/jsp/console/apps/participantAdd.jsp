<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
    <div id="main-body-header">
        <fmt:message key="console.process.config.label.mapParticipants"/> - <ui:stripTag html="${param.participantName}"/> <c:out value="(${participantId})" escapeXml="true" />
    </div>
    <div id="main-body-content" style="text-align: left">
        <div id="userTabView">
            <ul>
                <li class="selected">
                <c:if test="${participantId eq 'processStartWhiteList'}">
                        <a href="#role"><span><fmt:message key="console.process.config.label.mapParticipants.role"/></span></a></li><li>
                </c:if>
                <a href="#userGroup"><span><fmt:message key="console.process.config.label.mapParticipants.userGroup"/></span></a></li>
                <li><a href="#orgChart"><span><fmt:message key="console.process.config.label.mapParticipants.orgChart"/></span></a></li>
                <li><a href="#workflowVariableDiv"><span><fmt:message key="console.process.config.label.mapParticipants.variable"/></span></a></li>
                <li><a href="#plugin"><span><fmt:message key="console.process.config.label.mapParticipants.plugin"/></span></a></li>
            </ul>
            <div>
                <c:if test="${participantId eq 'processStartWhiteList'}">
                    <div id="role">
                        <form name="mapToRoleForm">
                            <div class="form-row">
                                <label style="vertical-align: top" for="workflowVariable"><fmt:message key="console.process.config.label.mapParticipants.role.selectARole"/></label>
                                <span class="form-input">
                                    <label for="roleAdmin">
                                        <input id="roleAdmin" type="radio" name="role" value="adminUser" checked="checked"> <fmt:message key="console.process.config.label.mapParticipants.role.adminUser"/>
                                    </label>
                                    <label for="roleUser">
                                        <input id="roleUser" type="radio" name="role" value="loggedInUser"> <fmt:message key="console.process.config.label.mapParticipants.role.loggedInUser"/>
                                    </label>
                                    <label for="roleEveryone">
                                        <input id="roleEveryone" type="radio" name="role" value="everyone"> <fmt:message key="console.process.config.label.mapParticipants.role.everyone"/>
                                    </label>
                                </span>
                            </div>
                            <div class="form-buttons">
                                <button type="button" onclick="submitRole()" value="Submit"><fmt:message key="general.method.label.submit"/></button>
                            </div>
                        </form>
                    </div>
                </c:if>
                <div id="userGroup">
                    <div id="userGroupTabView">
                        <ul>
                            <li class="selected"><a href="#group"><span><fmt:message key="console.process.config.label.mapParticipants.group"/></span></a></li>
                            <li><a href="#user"><span><fmt:message key="console.process.config.label.mapParticipants.user"/></span></a></li>
                        </ul>
                        <div>
                            <div id="group">
                                <div class="main-body-content-filter">
                                    <form>
                                    <fmt:message key="console.directory.group.filter.label.byOrganization"/>
                                    <select id="JsonGroupDataTable_filterbyOrg" onchange="filter(JsonGroupDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                                        <option value=""><fmt:message key="console.directory.group.empty.option.label"/></option>
                                    <c:forEach items="${organizations}" var="o">
                                        <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                                        <option value="${o.id}" ${selected}><c:out value="${o.name}"/></option>
                                    </c:forEach>
                                    </select>
                                    </form>
                                </div>
                                <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/group/list?${pageContext.request.queryString}"
                                               var="JsonGroupDataTable"
                                               divToUpdate="groupList"
                                               jsonData="data"
                                               rowsPerPage="10"
                                               width="100%"
                                               sort="name"
                                               desc="false"
                                               checkbox="true"
                                               checkboxButton1="general.method.label.submit"
                                               checkboxCallback1="submitGroups"
                                               searchItems="name|Name"
                                               fields="['id','name','description','organization.name']"
                                               column1="{key: 'id', label: 'console.directory.group.common.label.id', sortable: true}"
                                               column2="{key: 'name', label: 'console.directory.group.common.label.name', sortable: true}"
                                               column3="{key: 'description', label: 'console.directory.group.common.label.description', sortable: false}"
                                               column4="{key: 'organization.name', label: 'console.directory.group.common.label.organization', sortable: false}"
                                               />
                            </div>
                            <div id="user">
                                <div class="main-body-content-filter">
                                    <form>
                                    <fmt:message key="console.directory.user.filter.label.byOrganization"/>
                                    <select id="JsonUserDataTable_filterbyOrg" onchange="filter(JsonUserDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                                        <option value=""><fmt:message key="console.directory.user.empty.option.label"/></option>
                                    <c:forEach items="${organizations}" var="o">
                                        <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                                        <option value="${o.id}" ${selected}><c:out value="${o.name}"/></option>
                                    </c:forEach>
                                    </select>
                                    </form>
                                </div>
                                <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/user/list?${pageContext.request.queryString}"
                                               var="JsonUserDataTable"
                                               divToUpdate="userList"
                                               jsonData="data"
                                               rowsPerPage="10"
                                               width="100%"
                                               sort="username"
                                               desc="false"
                                               checkbox="true"
                                               checkboxButton1="general.method.label.submit"
                                               checkboxCallback1="submitUsers"
                                               searchItems="name|Name"
                                               fields="['id','username','firstName','lastName','email']"
                                               column1="{key: 'username', label: 'console.directory.user.common.label.username', sortable: true}"
                                               column2="{key: 'firstName', label: 'console.directory.user.common.label.firstName', sortable: true}"
                                               column3="{key: 'lastName', label: 'console.directory.user.common.label.lastName', sortable: true}"
                                               column4="{key: 'email', label: 'console.directory.user.common.label.email', sortable: true}"
                                               />
                            </div>
                        </div>
                    </div>
                </div>
                <div id="orgChart">
                    <div id="orgChartTabView">
                        <ul>
                            <li class="selected"><a href="#requester"><span><fmt:message key="console.process.config.label.mapParticipants.performer"/></span></a></li>
                            <li><a href="#hod"><span><fmt:message key="console.process.config.label.mapParticipants.hod"/></span></a></li>
                            <li><a href="#department"><span><fmt:message key="console.process.config.label.mapParticipants.department"/></span></a></li>
                        </ul>
                        <div>
                            <div id="requester">
                                <form name="requesterOrgChart">
                                    <div class="form-row">
                                    <label for="requester">
                                        <input id="requester" type="radio" name="participantType" value="requester" checked="checked"> <fmt:message key="console.process.config.label.mapParticipants.performer.performer"/>
                                    </label>
                                    </div>
                                    <div class="form-row">
                                    <label for="requesterHod">
                                        <input id="requesterHod" type="radio" name="participantType" value="requesterHod"> <fmt:message key="console.process.config.label.mapParticipants.performer.hod"/>
                                    </label>
                                    </div>
                                    <div class="form-row">
                                    <label for="requesterHodIgnoreReportTo">
                                        <input id="requesterHodIgnoreReportTo" type="radio" name="participantType" value="requesterHodIgnoreReportTo"> <fmt:message key="console.process.config.label.mapParticipants.performer.hod.ignoreReportTo"/>
                                    </label>
                                    </div>
                                    <div class="form-row">
                                    <label for="requesterSubordinates">
                                        <input id="requesterSubordinates" type="radio" name="participantType" value="requesterSubordinates"> <fmt:message key="console.process.config.label.mapParticipants.performer.subordinate"/>
                                    </label>
                                    </div>
                                    <div class="form-row">
                                    <label for="requesterDepartment">
                                        <input id="requesterDepartment" type="radio" name="participantType" value="requesterDepartment"> <fmt:message key="console.process.config.label.mapParticipants.performer.department"/>
                                    </label>
                                    </div>
                                    <div class="form-row">
                                        <label for="activity"><fmt:message key="console.process.config.label.mapParticipants.performer.activity"/></label>
                                        <span class="form-input">
                                            <select id="activity" name="activity">
                                                <option value=""><fmt:message key="console.process.config.label.mapParticipants.previousActivity"/></option>
                                                <c:forEach var="activity" items="${activityList}" varStatus="rowCounter">
                                                    <option value="<c:out value="${activity.id}"/>"><c:out value="${activity.name}"/></option>
                                                </c:forEach>
                                            </select>
                                        </span>
                                    </div>
                                    <div class="form-buttons">
                                        <button type="button" onclick="submitRequester()" value="Submit"><fmt:message key="general.method.label.submit"/></button>
                                    </div>
                                </form>
                            </div>
                            <div id="hod">
                                <div class="main-body-content-filter">
                                    <form>
                                    <fmt:message key="console.directory.group.filter.label.byOrganization"/>
                                    <select id="JsonHodDataTable_filterbyOrg" onchange="filter(JsonHodDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                                        <option value=""><fmt:message key="console.directory.dept.empty.option.label"/></option>
                                    <c:forEach items="${organizations}" var="o">
                                        <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                                        <option value="${o.id}" ${selected}><c:out value="${o.name}"/></option>
                                    </c:forEach>
                                    </select>
                                    </form>
                                </div>
                                <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/dept/list?${pageContext.request.queryString}"
                                               var="JsonHodDataTable"
                                               divToUpdate="departmentHodList"
                                               jsonData="data"
                                               rowsPerPage="10"
                                               width="100%"
                                               sort="name"
                                               desc="false"
                                               checkbox="true"
                                               checkboxButton1="general.method.label.submit"
                                               checkboxCallback1="submitHod"
                                               checkboxSelectSingle="true"
                                               searchItems="name|Name"
                                               fields="['id','name','description','organization.name']"
                                               column1="{key: 'id', label: 'console.directory.department.common.label.id', sortable: true}"
                                               column2="{key: 'name', label: 'console.directory.department.common.label.name', sortable: true}"
                                               column3="{key: 'description', label: 'console.directory.department.common.label.description', sortable: false}"
                                               column4="{key: 'organization.name', label: 'console.directory.department.common.label.organization', sortable: true}"
                                               />
                            </div>
                            <div id="department">
                                <div class="main-body-content-filter">
                                    <form>
                                    <fmt:message key="console.directory.group.filter.label.byOrganization"/>
                                    <select id="JsonDeptDataTable_filterbyOrg" onchange="filter(JsonDeptDataTable, '&orgId=', this.options[this.selectedIndex].value)">
                                        <option value=""><fmt:message key="console.directory.dept.empty.option.label"/></option>
                                    <c:forEach items="${organizations}" var="o">
                                        <c:set var="selected"><c:if test="${o.id == param.orgId}"> selected</c:if></c:set>
                                        <option value="${o.id}" ${selected}><c:out value="${o.name}"/></option>
                                    </c:forEach>
                                    </select>
                                    </form>
                                </div>
                                <ui:jsontable url="${pageContext.request.contextPath}/web/json/directory/admin/dept/list?${pageContext.request.queryString}"
                                               var="JsonDeptDataTable"
                                               divToUpdate="departmentList"
                                               jsonData="data"
                                               rowsPerPage="10"
                                               width="100%"
                                               sort="name"
                                               desc="false"
                                               checkbox="true"
                                               checkboxButton1="general.method.label.submit"
                                               checkboxCallback1="submitDepartment"
                                               checkboxSelectSingle="true"
                                               searchItems="name|Name"
                                               fields="['id','name','description','organization.name']"
                                               column1="{key: 'id', label: 'console.directory.department.common.label.id', sortable: true}"
                                               column2="{key: 'name', label: 'console.directory.department.common.label.name', sortable: true}"
                                               column3="{key: 'description', label: 'console.directory.department.common.label.description', sortable: false}"
                                               column4="{key: 'organization.name', label: 'console.directory.department.common.label.organization', sortable: true}"
                                               />
                            </div>
                        </div>
                    </div>
                </div>
                <div id="workflowVariableDiv">
                    <form name="workflowVariableForm">
                        <div class="form-row">
                            <label for="workflowVariable"><fmt:message key="console.app.process.common.label.variableId"/></label>
                            <span class="form-input">
                                <select id="workflowVariable" name="workflowVariable">
                                    <option></option>
                                    <c:forEach var="variable" items="${variableList}" varStatus="rowCounter">
                                        <option value="${variable.id}">${variable.id}</option>
                                    </c:forEach>
                                </select>
                            </span>
                        </div>
                        <div class="form-row">
                            <label style="vertical-align: top" for="workflowVariable"><fmt:message key="console.process.config.label.mapParticipants.variable.type"/></label>
                            <span class="form-input">
                                <label for="workflowVariableTypeGroup">
                                    <input id="workflowVariableTypeGroup" type="radio" name="workflowVariableType" value="group" checked="checked"> <fmt:message key="console.process.config.label.mapParticipants.variable.group"/>
                                </label>
                                <label for="workflowVariableTypeUser">
                                    <input id="workflowVariableTypeUser" type="radio" name="workflowVariableType" value="user"> <fmt:message key="console.process.config.label.mapParticipants.variable.user"/>
                                </label>
                                <label for="workflowVariableTypeDepartment">
                                    <input id="workflowVariableTypeDepartment" type="radio" name="workflowVariableType" value="department"> <fmt:message key="console.process.config.label.mapParticipants.variable.department"/>
                                </label>
                                <label for="workflowVariableTypeDepartmentHod">
                                    <input id="workflowVariableTypeDepartmentHod" type="radio" name="workflowVariableType" value="hod"> <fmt:message key="console.process.config.label.mapParticipants.variable.hod"/>
                                </label>
                            </span>
                        </div>
                        <div class="form-buttons">
                            <button type="button" onclick="submitWorkflowVariable()" value="Submit"><fmt:message key="general.method.label.submit"/></button>
                        </div>
                    </form>
                </div>
                <div id="plugin">
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?className=org.joget.workflow.model.ParticipantPlugin&${pageContext.request.queryString}"
                       var="JsonPluginDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       checkbox="true"
                       checkboxButton1="general.method.label.submit"
                       checkboxCallback1="submitPlugin"
                       checkboxSelectSingle="true"
                       fields="['id','name','description','version']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false}"
                       />
                </div>
            </div>
        </div>
    </div>
    <script>
        var tabView = new TabView('userTabView', 'top');
        tabView.init();

        var userGroupTabView = new TabView('userGroupTabView', 'top');
        userGroupTabView.init();

        var orgChartTabView = new TabView('orgChartTabView', 'top');
        orgChartTabView.init();

        $(document).ready(function(){
            $('#JsonGroupDataTable_searchTerm').hide();
            $('#JsonUserDataTable_searchTerm').hide();
            $('#JsonHodDataTable_searchTerm').hide();
            $('#JsonDeptDataTable_searchTerm').hide();

            <c:if test="${!isExtDirectoryManager}">
                $('#userGroupTabView #group .main-body-content-filter').hide();
                $('#userGroupTabView #user .main-body-content-filter').hide();
                $('#orgChartTabView #hod .main-body-content-filter').hide();
                $('#orgChartTabView #department .main-body-content-filter').hide();
                $('#JsonHodDataTable_departmentHodList-search').hide();
                $('#JsonDeptDataTable_departmentList-search').hide();
            </c:if>
        });

        function submitGroups(ids){
            var params = "param_value="+ids;
            post('group', params);
        }

        function submitUsers(ids){
            var params = "param_value="+ids;
            post('user', params);
        }

        function submitRequester(){
            var params = "param_value="+$('select#activity').val();
            post($('input[name=participantType]:checked').val() , params);
        }

        function submitHod(id){
            var params = "param_value="+id;
            post('hod', params);
        }

        function submitDepartment(id){
            var params = "param_value="+id;
            post('department', params);
        }

        function submitWorkflowVariable(){
            if($('#workflowVariable').val() != ''){
                var params = "param_value="+$('#workflowVariable').val()+','+$('input[name=workflowVariableType]:checked').val();
                post('workflowVariable', params);
            }
        }
        
        <c:if test="${participantId eq 'processStartWhiteList'}">
            function submitRole(){
                var role = $('input[name=role]:checked').val();
                if (role === "everyone") {
                    if (confirm('<fmt:message key="console.process.config.label.mapParticipants.submit.confirm"/>')) {
                        var callback = {
                            success : function(response) {
                                 parent.location.href = '<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}?tab=participantList&participantId=${participantId}"/>';
                            }
                        }
                        var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}/participant/${participantId}/remove"/>', callback, null);
                    }
                    return false;
                } else {
                    post('role', "param_value="+role);
                }
            }
        </c:if>

        function submitPlugin(id){
            document.location = '<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}/participant/${participantId}/plugin/configure"/>?value='+escape(id) + '&title=' + escape(" - <c:out value=" ${param.participantName} (${participantId})" escapeXml="true" />");
        }

        function post(type, params){
            if (confirm('<fmt:message key="console.process.config.label.mapParticipants.submit.confirm"/>')) {
                var callback = {
                    success : function(response) {
                        parent.location.href = '<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}"/>?tab=participantList&participantId=' + response;
                    }
                }
                var request = ConnectionManager.post('<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${processDefId}/participant/${participantId}/submit/"/>'+type, callback, params);
            }
            return false;
        }

        var org_filter = window.filter;
        var filter = function(jsonTable, url, value){
            var tempValue = "";
            if(jsonTable == JsonGroupDataTable){
                url = "&orgId=" + $('#JsonGroupDataTable_filterbyOrg').val();
                url += "&name=" + $('#JsonGroupDataTable_searchCondition').val();
            }else if(jsonTable == JsonUserDataTable){
                url = "&orgId=" + $('#JsonUserDataTable_filterbyOrg').val();
                url += "&name=" + $('#JsonUserDataTable_searchCondition').val();
            }else if(jsonTable == JsonHodDataTable){
                url = "&orgId=" + $('#JsonHodDataTable_filterbyOrg').val();
                url += "&name=" + $('#JsonHodDataTable_searchCondition').val();
            }else if(jsonTable == JsonDeptDataTable){
                url = "&orgId=" + $('#JsonDeptDataTable_filterbyOrg').val();
                url += "&name=" + $('#JsonDeptDataTable_searchCondition').val();
            }else{
                tempValue = value
            }

            org_filter(jsonTable, url, tempValue);
        };
     </script>
<commons:popupFooter />