<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />

<div id="main-body-header">
    <fmt:message key="console.app.dev.settings"/>
</div>
<div id="main-body-content" style="overflow-y: hidden; margin-top: 5px">

    <div id="appTabView" style="min-height: 200px">
        <ul>
            <li class="selected"><a href="#appVersionDiv"><span><fmt:message key="console.app.version.label.title"/></span></a></li>
            <li><a href="#appDevSettings"><span><fmt:message key="console.app.dev.admin.settings"/></span></a></li>
        </ul>
        <br><br>
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
                        }, message : "<i class='icon-spinner icon-spin icon-2x fa fa-spinner fa-spin fa-2x'></i>" });                    
                    }
                    
                    function newVersion(version){
                        if (confirm('<fmt:message key="console.app.version.label.newVersion.confirm"/>')) {
                            showLoading();
                            var callback = {
                                success : function() {
                                    parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>//forms';
                                }
                            }
                            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/version/new?version='+version, callback, '');
                        }
                    }

                    function deleteVersion(version){
                        if (version != '' && confirm('<fmt:message key="console.app.delete.label.confirm"/>')) {
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
                        if (version != '' && confirm('<fmt:message key="console.app.publish.label.confirm"/>')) {
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
                        if (version != '' && confirm('<fmt:message key="console.app.unpublish.label.confirm"/>')) {
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
                            parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/forms';
                        }
                    }
                </script>                
                
            </div>
            <div id="appDevSettings" style="position: relative; top:-5px; overflow-y:hidden">        

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
                <form id="propertiesForm" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/dev/submit" class="form" method="POST" style="display:none">
                    <input id="properties" name="properties" type="hidden" value=""/>
                    <input id="id" name="id" type="hidden" value="${appResource.id}"/>
                </form>
                <script>
                    function saveProperties(container, properties) {
                        $("#properties").val(JSON.encode(properties));
                        $("#propertiesForm").submit();
                    }

                    function savePropertiesFailed(container, returnedErrors) {
                        var errorMsg = '<fmt:message key="console.plugin.label.youHaveFollowingErrors"/>:\n';
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
                                    title: '<fmt:message key="console.app.dev.admin.settings"/>',
                                    properties: [{
                                            name: 'orgId',
                                            label: '<fmt:message key="userview.userpermission.selectOrg"/>',
                                            type: 'selectbox',
                                            options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getOrgs'
                                        },
                                        {
                                            name: 'ROLE_ADMIN',
                                            label: '<fmt:message key="userview.userpermission.selectUsers"/>',
                                            type: 'multiselect',
                                            size: '10',
                                            options_ajax_on_change: 'orgId',
                                            options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getUsers'
                                        },
                                        {
                                            name: 'ROLE_ADMIN_GROUP',
                                            label: '<fmt:message key="userview.grouppermission.selectGroups"/>',
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
                            saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><fmt:message key="${submitLabel}"/></c:when><c:otherwise><fmt:message key="general.method.label.submit"/></c:otherwise></c:choose>',
                            cancelButtonLabel: '<fmt:message key="general.method.label.cancel"/>',
                            closeAfterSaved: false,
                            validationFailedCallback: savePropertiesFailed
                        }
                        $('.menu-wizard-container').propertyEditor(options);
                    });
                </script>

            </div>
        </div>                    
    </div>

    <script>
        var tabView = new TabView('appTabView', 'top');
        tabView.init();
    </script>

<commons:popupFooter />


