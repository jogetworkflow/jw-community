<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup no-header" />
<div id="main-body-content">
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
                      checkboxButton2="console.app.version.label.view"
                      checkboxCallback2="viewVersion"
                      checkboxButton3="general.method.label.delete"
                      checkboxCallback3="deleteVersion"
                      fields="['version','published','description','dateCreated','dateModified']"
                      column1="{key: 'version', label: 'console.app.common.label.version', sortable: true}"
                      column2="{key: 'published', label: 'console.app.common.label.published', sortable: true, relaxed: true}"
                      column3="{key: 'description', label: 'console.app.common.label.description', sortable: false}"                   
                      column4="{key: 'dateCreated', label: 'console.app.common.label.dateCreated', sortable: true}"
                      column5="{key: 'dateModified', label: 'console.app.common.label.dateModified', sortable: true}"
                      />
        <script>
            function showLoading() {
                parent.$.blockUI({ css: { 
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
                            parent.$.unblockUI();
                            parent.CustomBuilder.ajaxRenderBuilder('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/_/builders');
                        }
                    };
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/version/new?version='+version, callback, '');
                }
            }

            function deleteVersion(version){
                if (version != '' && confirm('<ui:msgEscJS key="console.app.delete.label.confirm"/>')) {
                    showLoading();
                    var callback = {
                        success : function() {
                            parent.$.unblockUI();
                            document.location.reload(true);
                        }
                    }
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+ version +'/delete', callback, '');
                }
            }

            function viewVersion(version){
                if (version !== '') {
                    parent.CustomBuilder.ajaxRenderBuilder('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/'+version[0]+'/builders');
                }
            }
            function adjustPropertySize(height) {
                return height - 60;
            }
        </script>                
    </div>
</div>                
<commons:popupFooter />


