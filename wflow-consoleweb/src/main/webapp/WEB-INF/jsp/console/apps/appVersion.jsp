<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.app.version.label.title"/>
    </div>
    <div id="main-body-content">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/version/list?${pageContext.request.queryString}"
                   var="JsonDataTable"
                   divToUpdate="appVersionList"
                   jsonData="data"
                   rowsPerPage="10"
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
    </div>

<script>
    function newVersion(version){
        if (confirm('<fmt:message key="console.app.version.label.newVersion.confirm"/>')) {
            var callback = {
                success : function() {
                    parent.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>//forms';
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/version/new', callback, '');
        }
    }

    function deleteVersion(version){
        if (version != '' && confirm('<fmt:message key="console.app.delete.label.confirm"/>')) {
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
            var callback = {
                success : function() {
                    document.location.reload(true);
                    parent.location.reload(true);
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/publish', callback, '');
        }
    }

    function unpublishVersion(version){
        if (version != '' && confirm('<fmt:message key="console.app.unpublish.label.confirm"/>')) {
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
<commons:popupFooter />


