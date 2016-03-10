<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil,org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-cogs"></i> <fmt:message key='console.header.top.label.settings'/></p>
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
        <br/>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="10"
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
                       fields="['id','name','description','version']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false}"
                       />
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonDataTable_searchTerm').hide();

        <c:if test="${isVirtualHostEnabled}">
            $('#JsonDataTable_pluginList-buttons button').hide();
            $('#JsonDataTable_pluginList-buttons button:eq(0)').show();
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
        var callback = {
            success : function() {
                document.location = '${pageContext.request.contextPath}/web/console/setting/plugin';
            }
        }
        var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/plugin/refresh', callback, "");
    }

    function uninstall(selectedList){
         if (confirm('<fmt:message key="console.setting.plugin.unintall.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    document.location = '${pageContext.request.contextPath}/web/console/setting/plugin';
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/plugin/uninstall', callback, 'selectedPlugins='+selectedList);
        }
    }
</script>

<script>
    Template.init("", "#nav-setting-plugin");
</script>

<commons:footer />
