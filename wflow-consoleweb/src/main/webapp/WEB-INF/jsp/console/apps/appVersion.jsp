<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />

<div id="main-body-header">
    <fmt:message key="console.app.dev.settings"/>
</div>
<div id="main-body-content">

    <div id="appTabView">
        <ul>
            <li class="selected"><a href="#appVersionDiv"><span><fmt:message key="console.app.version.label.title"/></span></a></li>
            <li><a href="#appDevSettings"><span><fmt:message key="console.app.dev.admin.settings"/></span></a></li>
            <c:if test="${!isGitDisabled}"><li><a href="#appGitSettings"><span><fmt:message key="console.app.dev.git.configuration"/></span></a></li></c:if>
        </ul>
        <div>
            <div id="appVersionDiv">   
                <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/version/list?${pageContext.request.queryString}"
                              var="JsonDataTable"
                              divToUpdate="appVersionList"
                              jsonData="data"
                              rowsPerPage="15"
                              width="100%"
                              sort="version"
                              desc="true"
                              hrefParam="version"
                              hrefDialogWindowName="_blank"
                              hrefQuery="false"
                              hrefDialog="false"
                              hrefDialogTab="false"
                              hrefDialogTitle="Form Dialog"
                              checkbox="true"
                              checkboxId="version"
                              checkboxSelectSingle="true"
                              checkboxButton1="console.app.version.label.newVersion"
                              checkboxCallback1="newVersion"
                              checkboxOptional1="true"
                              checkboxButton2="console.app.version.label.publish"
                              checkboxCallback2="publishVersion"
                              checkboxButton3="console.app.version.label.unpublish"
                              checkboxCallback3="unpublishVersion"
                              checkboxButton4="console.app.version.label.view"
                              checkboxCallback4="viewVersion"
                              checkboxButton5="general.method.label.delete"
                              checkboxCallback5="deleteVersion"
                              fields="['version','published','description','dateCreated','dateModified']"
                              column1="{key: 'version', label: 'console.app.common.label.version', sortable: true}"
                              column2="{key: 'published', label: 'console.app.common.label.published', sortable: true, relaxed: true}"
                              column3="{key: 'description', label: 'console.app.common.label.description', sortable: false}"                   
                              column4="{key: 'dateCreated', label: 'console.app.common.label.dateCreated', sortable: true}"
                              column5="{key: 'dateModified', label: 'console.app.common.label.dateModified', sortable: true}"
                              />
                <script>
                    function showLoading() {
                        $.blockUI({ css: { 
                            border: 'none', 
                            padding: '15px', 
                            backgroundColor: '#000', 
                            '-webkit-border-radius': '10px', 
                            '-moz-border-radius': '10px', 
                            opacity: .3, 
                            color: '#fff' 
                        }, message : "<i class='icon-spinner icon-spin icon-2x fas fa-spinner fa-spin fa-2x'></i>" });                    
                    }
                    
                    function newVersion(version){
                        if (confirm('<ui:msgEscJS key="console.app.version.label.newVersion.confirm"/>')) {
                            showLoading();
                            var callback = {
                                success : function() {
                                    parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/_/forms';
                                }
                            }
                            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/version/new?version='+version, callback, '');
                        }
                    }

                    function deleteVersion(version){
                        if (version != '' && confirm('<ui:msgEscJS key="console.app.delete.label.confirm"/>')) {
                            showLoading();
                            var callback = {
                                success : function() {
                                    document.location.reload(true);
                                }
                            }
                            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/delete', callback, '');
                        }
                    }

                    function publishVersion(version){
                        if (version != '' && confirm('<ui:msgEscJS key="console.app.publish.label.confirm"/>')) {
                            showLoading();
                            var callback = {
                                success : function() {
                                    document.location.reload(true);
                                    parent.location.href = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/forms';
                                }
                            }
                            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/publish', callback, '');
                        }
                    }

                    function unpublishVersion(version){
                        if (version != '' && confirm('<ui:msgEscJS key="console.app.unpublish.label.confirm"/>')) {
                            showLoading();
                            var callback = {
                                success : function() {
                                    document.location.reload(true);
                                    parent.location.reload(true);
                                }
                            }
                            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/unpublish', callback, '');
                        }
                    }

                    function viewVersion(version){
                        if (version != '') {
                            showLoading();
                            parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/forms';
                        }
                    }
                    function adjustPropertySize(height) {
                        return height - 60;
                    }
                </script>                
                
            </div>
            <div id="appDevSettings">        

                <c:if test="${upload}">
                    <div class="form-message form-success"><fmt:message key="console.app.dev.updated" /></div>
                </c:if>
                <c:if test="${!empty errors}">
                    <span class="form-errors" style="display:block">
                        <c:forEach items="${errors}" var="error">
                            <fmt:message key="${error}"/>
                        </c:forEach>
                    </span>
                </c:if>

                <div id="propertyEditor" class="pluginConfig menu-wizard-container">

                </div>
                <form id="propertiesForm" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/dev/submit" class="form blockui" method="POST" style="display:none">
                    <input id="properties" name="properties" type="hidden" value=""/>
                    <input id="id" name="id" type="hidden" value="${appResource.id}"/>
                </form>
                <script>
                    function saveProperties(container, properties) {
                        $("#properties").val(JSON.encode(properties));
                        $("#propertiesForm").submit();
                    }

                    function savePropertiesFailed(container, returnedErrors) {
                        var errorMsg = '<ui:msgEscJS key="console.plugin.label.youHaveFollowingErrors"/>:\n';
                        for (key in returnedErrors) {
                            if (returnedErrors[key].fieldName === undefined || returnedErrors[key].fieldName === "") {
                                errorMsg += returnedErrors[key].message + '\n';
                            } else {
                                errorMsg += returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message + '\n';
                            }
                        }
                        alert(errorMsg);
                    }

                    function cancel(container) {
                        if (parent && parent.PopupDialog.closeDialog) {
                            parent.PopupDialog.closeDialog();
                        }
                        return false;
                    }

                    $(document).ready(function () {
                        var prop = ${properties};

                        var options = {
                            contextPath: '${pageContext.request.contextPath}',
                            propertiesDefinition: [{
                                    title: '<ui:msgEscJS key="console.app.dev.admin.settings"/>',
                                    properties: [{
                                            name: 'orgId',
                                            label: '<ui:msgEscJS key="userview.userpermission.selectOrg"/>',
                                            type: 'selectbox',
                                            options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getOrgs'
                                        },
                                        {
                                            name: 'ROLE_ADMIN',
                                            label: '<ui:msgEscJS key="userview.userpermission.selectUsers"/>',
                                            type: 'multiselect',
                                            size: '10',
                                            options_ajax_on_change: 'orgId',
                                            options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getUsers'
                                        },
                                        {
                                            name: 'ROLE_ADMIN_GROUP',
                                            label: '<ui:msgEscJS key="userview.grouppermission.selectGroups"/>',
                                            type: 'multiselect',
                                            size: '10',
                                            options_ajax_on_change: 'orgId',
                                            options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.GroupPermission/service?action=getGroups'
                                        }]
                                }],
                            propertyValues: prop,
                            cancelCallback: cancel,
                            showCancelButton: true,
                            saveCallback: saveProperties,
                            saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><ui:msgEscJS key="${submitLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.submit"/></c:otherwise></c:choose>',
                            cancelButtonLabel: '<ui:msgEscJS key="general.method.label.cancel"/>',
                            closeAfterSaved: false,
                            validationFailedCallback: savePropertiesFailed,
                            adjustSize : adjustPropertySize
                        }
                        $('.menu-wizard-container').propertyEditor(options);
                    });
                </script>

            </div>
            <c:if test="${!isGitDisabled}">
            <div id="appGitSettings" >        

                <c:if test="${upload}">
                    <div class="form-message form-success"><fmt:message key="console.app.dev.updated" /></div>
                </c:if>
                <c:if test="${!empty errors}">
                    <span class="form-errors" style="display:block">
                        <c:forEach items="${errors}" var="error">
                            <fmt:message key="${error}"/>
                        </c:forEach>
                    </span>
                </c:if>

                <div id="propertyEditorGit" class="pluginConfig menu-wizard-container-git">

                </div>
                <form id="propertiesFormGit" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/dev/submit" class="form blockui" method="POST" style="display:none">
                    <input id="propertiesGit" name="properties" type="hidden" value=""/>
                    <input id="id" name="id" type="hidden" value="${appResource.id}"/>
                </form>
                <script>
                    function savePropertiesGit(container, properties) {
                        $("#propertiesGit").val(JSON.encode(properties));
                        $("#propertiesFormGit").submit();
                    }

                    $(document).ready(function () {
                        var prop = ${properties};

                        var options = {
                            contextPath: '${pageContext.request.contextPath}',
                            propertiesDefinition: [{
                                    title: '<ui:msgEscJS key="console.app.dev.git.configuration"/>',
                                    properties: [{
                                            name: 'gitUri',
                                            label: '<ui:msgEscJS key="console.app.dev.git.uri"/>',
                                            type: 'textfield'
                                        },
                                        {
                                            name: 'gitUsername',
                                            label: '<ui:msgEscJS key="console.app.dev.git.username"/>',
                                            type: 'textfield'
                                        },
                                        {
                                            name: 'gitPassword',
                                            label: '<ui:msgEscJS key="console.app.dev.git.password"/>',
                                            type: 'password'
                                        },
                                        {
                                            label: '<ui:msgEscJS key="console.app.dev.git.deployment"/>',
                                            type: 'header'
                                        },
                                        {
                                            name: 'gitConfigExcludeCommit',
                                            label: '<ui:msgEscJS key="console.app.dev.git.configExcludeCommit"/>',
                                            type: 'checkbox',
                                            options: [{
                                                    value: 'true',
                                                    label: ''
                                                }]
                                        },
                                        {
                                            name: 'gitConfigPull',
                                            label: '<ui:msgEscJS key="console.app.dev.git.configPull"/>',
                                            type: 'checkbox',
                                            options: [{
                                                    value: 'true',
                                                    label: ''
                                                }]
                                        },
                                        {
                                            name : 'gitConfigAutoSync',
                                            label : '<ui:msgEscJS key="console.app.dev.git.configAutoSync"/>',
                                            type : 'checkbox',
                                            options : [{
                                                value : 'true',
                                                label : ''
                                            }]
                                        }]
                                }],
                            propertyValues: prop,
                            cancelCallback: cancel,
                            showCancelButton: true,
                            saveCallback: savePropertiesGit,
                            saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><ui:msgEscJS key="${submitLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.submit"/></c:otherwise></c:choose>',
                            cancelButtonLabel: '<ui:msgEscJS key="general.method.label.cancel"/>',
                            closeAfterSaved: false,
                            validationFailedCallback: savePropertiesFailed,
                            adjustSize : adjustPropertySize
                        }
                        $('.menu-wizard-container-git').propertyEditor(options);
                    });
                </script>
            </div>
            </c:if>                
        </div>                    
    </div>

    <script>
        var tabView = new TabView('appTabView', 'top');
        tabView.init();
    </script>

<commons:popupFooter />


