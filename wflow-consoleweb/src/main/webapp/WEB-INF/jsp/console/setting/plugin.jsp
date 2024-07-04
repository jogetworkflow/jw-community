<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil,org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-cogs"></i> <fmt:message key='console.header.top.label.settings'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="reload()"><fmt:message key="console.setting.plugin.common.label.reloadPlugin"/></button></li>
            <li><button onclick="upload()"><fmt:message key="console.setting.plugin.upload.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="pluginstab">
            <ul>
                <li class="selected"><a href="#installed"><span><fmt:message key="console.setting.plugin.common.label.installed"/></span></a></li>
                <li><a href="#update"><span>Update</span> <span class="jgt-badge update_count">(0)</span></a></li>
            </ul>
            <div>
                <div id="installed">
                    <div id="main-body-content-filter">
                        <form>
                            <fmt:message key="console.plugin.label.typeFilter"/>
                            <select id="JsonDataTable1_filterbytype" onchange="filter(JsonDataTable1, '&className=', this.options[this.selectedIndex].value)">
                                <option></option>
                            <c:forEach items="${pluginType}" var="t">
                                <c:set var="selected"><c:if test="${t.key == param.className}"> selected</c:if></c:set>
                                <option value="${t.key}" ${selected}>${t.value}</option>
                            </c:forEach>
                            </select>
                        </form>
                    </div>
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listInstalledBundle?${pageContext.request.queryString}"
                        var="JsonDataTable1"
                        divToUpdate="pluginList"
                        jsonData="data"
                        rowsPerPage="15"
                        width="100%"
                        sort="name"
                        desc="false"
                        hrefParam="pluginClass"
                        hrefQuery="false"
                        hrefDialog="false"
                        hrefDialogWidth="600px"
                        hrefDialogHeight="400px"
                        hrefDialogTitle="Process Dialog"
                        checkbox="true"
                        checkboxId="pluginClass"
                        checkboxButton2="console.setting.plugin.unintall.label"
                        checkboxCallback2="uninstall"
                        searchItems="name|Name"
                        fields="['pluginClass','label','description','version','plugintype']"
                        column1="{key: 'label', label: 'console.plugin.label.name', sortable: false, width: 180}"
                        column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                        column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                        />
                </div>
                <div id="update">
                    <div id="main-body-content-filter">
                        <form>
                            <fmt:message key="console.plugin.label.typeFilter"/>
                            <select id="JsonDataTable2_filterbytype" onchange="filter(JsonDataTable2, '&className=', this.options[this.selectedIndex].value)">
                                <option></option>
                            <c:forEach items="${pluginType}" var="t">
                                <c:set var="selected"><c:if test="${t.key == param.className}"> selected</c:if></c:set>
                                <option value="${t.key}" ${selected}>${t.value}</option>
                            </c:forEach>
                            </select>
                        </form>
                    </div>
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listInstalledBundle?isUpdate=true"
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
    </div>
</div>

<script>
    $(document).ready(function(){
        var tabView = new TabView('pluginstab', 'top');
        tabView.init();
        
        $('#JsonDataTable_searchTerm').hide();
        $('#JsonDataTable1_searchTerm').hide();

        <c:if test="${isVirtualHostEnabled}">
            $('#JsonDataTable_pluginList-buttons button').hide();
            $('#JsonDataTable_pluginList-buttons button:eq(0)').show();
            $('#JsonDataTable1_pluginList-buttons button').hide();
            $('#JsonDataTable1_pluginList-buttons button:eq(0)').show();
        </c:if>
            
        //update the count when table done loading
        $("#pluginList2").on("success", function(){
            $(".update_count").text("(" + JsonDataTable.flexiGrid[0].p.total + ")");
        });
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/setting/plugin/upload"/>

    function upload(dummy){
        popupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
    }

    function reload(dummy){
        UI.blockUI();
        var callback = {
            success : function() {
                document.location = '${pageContext.request.contextPath}/web/console/setting/plugin';
            }
        }
        var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/plugin/refresh', callback, "");
    }

    function uninstall(selectedList){
         if (confirm('<ui:msgEscJS key="console.setting.plugin.unintall.label.confirmation"/>')) {
            UI.blockUI(); 
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/setting/plugin';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/plugin/uninstall', callback, 'selectedPlugins='+selectedList);
        }
    }
    
    /* Update all selected plugin from marketplace */
    function update(selectedList){
         if (confirm('<ui:msgEscJS key="appCenter.label.confirmPluginInstallation"/>')) {
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
                JsonDataTable.reload();
            });
        }
    }
    
    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        if(jsonTable == JsonDataTable){
            url = "&className=" + encodeURIComponent($('#JsonDataTable_filterbytype').val());
            url += "&name=" + encodeURIComponent($('#JsonDataTable_searchCondition').val());
        }else if(jsonTable == JsonDataTable1){
            url = "&className=" + encodeURIComponent($('#JsonDataTable1_filterbytype').val());
            url += "&name=" + encodeURIComponent($('#JsonDataTable1_searchCondition').val());
        }
        org_filter(jsonTable, url, '');
    };
</script>

<script>
    Template.init("", "#nav-setting-plugin");
</script>

<commons:footer />
