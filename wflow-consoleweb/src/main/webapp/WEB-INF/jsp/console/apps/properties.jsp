<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="${theme}"/>
<div id="main-body-content">
    <div id="pluginstab">
        <ul>
            <li  class="selected"><a href="#pluginDefault"><span><fmt:message key="abuilder.pluginDefault"/></span></a></li>
            <li><a href="#installed"><span><fmt:message key="console.setting.plugin.common.label.installed"/></span></a></li>
            <li><a href="#update"><span>Update</span> <span class="jgt-badge update_count">(0)</span></a></li>
        </ul>
        <div>
            <div id="pluginDefault">
                <div id="pluginDefault">
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/pluginDefault/list?${pageContext.request.queryString}"
                       var="JsonPluginDefaultDataTable"
                       divToUpdate="pluginDefaultList"
                       jsonData="data"
                       rowsPerPage="15"
                       width="100%"
                       sort="id"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/config"
                       hrefParam="id"
                       hrefQuery="true"
                       hrefDialog="true"
                       hrefDialogTitle=""
                       checkbox="${protectedReadonly != 'true'}"
                       checkboxButton1="console.app.pluginDefault.create.label"
                       checkboxCallback1="defaultPluginPropertiesCreate"
                       checkboxOptional1="true"
                       checkboxButton2="general.method.label.delete"
                       checkboxCallback2="pluginDefaultDelete"
                       searchItems="filter|Filter"
                       fields="['id','pluginName','pluginDescription']"
                       column1="{key: 'pluginName', label: 'console.plugin.label.name', sortable: true}"
                       column2="{key: 'pluginDescription', label: 'console.plugin.label.description', sortable: true}"
                       />
                </div>
            </div>    
            <div id="installed">
                <ui:jsontable url="${pageContext.request.contextPath}/web/json/app/${appId}/${appVersion}/plugin/listInstalledBundle?${pageContext.request.queryString}"
                    var="JsonDataTable1"
                    divToUpdate="pluginList"
                    jsonData="data"
                    rowsPerPage="15"
                    width="100%"
                    sort="name"
                    desc="false"
                    searchItems="name|Name"
                    fields="['pluginClass','label','description','version','plugintype']"
                    column1="{key: 'label', label: 'console.plugin.label.name', sortable: false, width: 180}"
                    column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                    column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                    />
            </div>
            <div id="update">
                <ui:jsontable url="${pageContext.request.contextPath}/web/json/app/${appId}/${appVersion}/plugin/listInstalledBundle?isUpdate=true"
                    var="JsonDataTable"
                    divToUpdate="pluginList2"
                    jsonData="data"
                    rowsPerPage="15"
                    width="100%"
                    sort="name"
                    desc="false"
                    hrefParam="id"
                    hrefQuery="false"
                    hrefDialog="false"
                    hrefDialogWidth="600px"
                    hrefDialogHeight="400px"
                    hrefDialogTitle="Process Dialog"
                    checkbox="true"
                    checkboxButton2="appCenter.label.updateApp"
                    checkboxCallback2="update"
                    searchItems="name|Name"
                    fields="['id','label','description','version','plugintype']"
                    column1="{key: 'label', label: 'console.plugin.label.name', sortable: false, width: 180}"
                    column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                    column3="{key: 'latestVersion', label: 'console.plugin.label.latestVersion', sortable: false, width: 140}"
                    column4="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                    />
            </div>    
        </div>  
    </div>        
    <script>
        <ui:popupdialog var="pluginDefaultCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/create"/>
    
        $(document).ready(function(){
            var tabView = new TabView('pluginstab', 'top');
            tabView.init();
        
            <c:if test="${protectedReadonly == 'true'}">
                $(".ui-tabs-panel button").hide();
            </c:if>
                
            $('#JsonPluginDefaultDataTable_searchTerm').hide();
            $('#JsonDataTable_searchTerm').hide();
            $('#JsonDataTable1_searchTerm').hide();
        
            //reset to 0 first before refresh
            $("#pluginList2").on("refresh", function(){
                $(".update_count").text("(0)");
            });
            
            //update the count when table done loading
            $("#pluginList2").on("success", function(){
                $(".update_count").text("(" + JsonDataTable.flexiGrid[0].p.total + ")");
            });
        });
        
        /* Update all selected plugin from marketplace */
        function update(selectedList){
             if (confirm('<ui:msgEscJS key="cbuilder.seamless.marketplace.confirmPluginInstallation"/>')) {
                UI.blockUI(); 
                var installUrl = "${pageContext.request.contextPath}/web/json/apps/install";

                for (var i in selectedList) {
                    var deferreds = [];

                    var temp = $.Deferred();
                    deferreds.push(temp);
                    var installCallback = {
                        success: function (data) {
                            temp.resolve();
                        },
                        error: function (data) {
                            temp.resolve();
                        }
                    };

                    // invoke installation
                    var installParams = "url=" + encodeURIComponent("<ui:msgEscJS key="appCenter.link.marketplace.url"/>/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + selectedList[i]);
                    ConnectionManager.post(installUrl, installCallback, installParams);
                }

                //reload the table after all plugin updated
                $.when.apply($, deferreds).then(function(){
                    UI.unblockUI(); 
                    JsonDataTable.refresh();
                    JsonDataTable1.refresh();
                });
            }
        }
        
        function defaultPluginPropertiesCreate(){
            pluginDefaultCreateDialog.init();
        }
        
        function pluginDefaultDelete(selectedList){
            if (confirm('<ui:msgEscJS key="console.app.pluginDefault.delete.label.confirmation"/>')) {
               parent.UI.blockUI();
               var callback = {
                   success : function() {
                       reloadTable();
                       JsonPluginDefaultDataTable.clearSelectedRows();
                       parent.UI.unblockUI();
                   }
               }
               var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/pluginDefault/delete', callback, 'ids='+selectedList);
            }
        }
        function closeDialog() {
            pluginDefaultCreateDialog.close();
        }
        function reloadTable() {
            closeDialog();
            filter(JsonPluginDefaultDataTable, '&filter=', $('#JsonPluginDefaultDataTable_searchCondition').val());
        } 
    </script>
</div>  
<commons:popupFooter />
