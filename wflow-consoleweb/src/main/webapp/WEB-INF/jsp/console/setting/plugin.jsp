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
                <li><a href="#allplugins"><span><fmt:message key="console.setting.plugin.common.label.allplugins"/></span></a></li>
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
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listOsgi?${pageContext.request.queryString}"
                        var="JsonDataTable1"
                        divToUpdate="pluginList"
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
                        checkboxButton2="console.setting.plugin.unintall.label"
                        checkboxCallback2="uninstall"
                        searchItems="name|Name"
                        fields="['id','name','description','version','plugintype', 'uninstallable']"
                        column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
                        column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                        column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                        column4="{key: 'plugintype', label: 'console.plugin.label.plugintype', sortable: false, width: 300}"
                        />
                </div>
                <div id="allplugins">
                    <div id="main-body-content-filter">
                        <form>
                            <fmt:message key="console.plugin.label.typeFilter"/>
                            <select id="JsonDataTable_filterbytype" onchange="filter(JsonDataTable, '&className=', this.options[this.selectedIndex].value)">
                                <option></option>
                            <c:forEach items="${pluginType}" var="t">
                                <c:set var="selected"><c:if test="${t.key == param.className}"> selected</c:if></c:set>
                                <option value="${t.key}" ${selected}>${t.value}</option>
                            </c:forEach>
                            </select>
                        </form>
                    </div>
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?${pageContext.request.queryString}"
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
                        checkboxButton2="console.setting.plugin.unintall.label"
                        checkboxCallback2="uninstall"
                        searchItems="name|Name"
                        fields="['id','name','description','version','plugintype', 'uninstallable']"
                        column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
                        column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                        column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                        column4="{key: 'plugintype', label: 'console.plugin.label.plugintype', sortable: false, width: 300}"
                        column5="{key: 'uninstallable', label: 'console.plugin.label.uninstallable', sortable: false, width: 110, relaxed: true}"
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
