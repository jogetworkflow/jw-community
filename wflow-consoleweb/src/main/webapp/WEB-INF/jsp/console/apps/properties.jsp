<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<div id="nav">
    <div id="nav-title">
        <jsp:include page="appTitle.jsp" flush="true" />
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="appSubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <c:if test="${protectedReadonly != 'true'}">
            <li><button onclick="environmentVariableCreate()"><fmt:message key="console.app.envVariable.create.label"/></button></li>
            <li><button onclick="messageCreate()"><fmt:message key="console.app.message.create.label"/></button></li>
            <li><button onclick="defaultPluginPropertiesCreate()"><fmt:message key="console.app.pluginDefault.create.label"/></button></li>
            </c:if>
            <li><button onclick="exportApp()"><fmt:message key="console.app.export.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="main-body-content">
            <div id="propertiesTab">
                <ul>
                    <li><a href="#appDesc"><span><fmt:message key="console.app.common.label.description"/></span></a></li>
                    <li><a href="#variable"><span><fmt:message key="console.app.envVariable.common.label"/></span></a></li>
                    <li><a href="#message"><span><fmt:message key="console.app.message.common.label"/></span></a></li>
                    <li><a href="#pluginDefault"><span><fmt:message key="console.app.pluginDefault.common.label"/></span></a></li>
                </ul>
                <div>
                    <div id="appDesc">
                        <form method="post" action="${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/note/submit">
                            <textarea id="description" name="description"><c:out value="${appDefinition.description}" escapeXml="true"/></textarea>
                            <br />
                            <input type="submit" value="<fmt:message key="general.method.label.submit"/>" class="form-button"/>
                        </form>
                    </div>    
                    <div id="variable">
                        <br/>
                        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/envVariable/list?${pageContext.request.queryString}"
                           var="JsonVariableDataTable"
                           divToUpdate="variableList"
                           jsonData="data"
                           rowsPerPage="10"
                           width="100%"
                           sort="id"
                           desc="false"
                           href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/envVariable/edit"
                           hrefParam="id"
                           hrefQuery="false"
                           hrefDialog="true"
                           hrefDialogTitle=""
                           checkbox="${protectedReadonly != 'true'}"
                           checkboxButton1="general.method.label.delete"
                           checkboxCallback1="envVariableDelete"
                           searchItems="filter|Filter"
                           fields="['id','value','remarks']"
                           column1="{key: 'id', label: 'console.app.envVariable.common.label.id', sortable: true}"
                           column2="{key: 'value', label: 'console.app.envVariable.common.label.value', sortable: true}"
                           column3="{key: 'remarks', label: 'console.app.envVariable.common.label.remarks', sortable: false}"
                           />
                    </div>
                    <div id="message">
                        <br/>
                        <div id="main-body-content-filter">
                            <form>
                            <fmt:message key="console.app.message.filter.label.byLocale"/>
                            <select id="JsonMessageDataTable_filterbyLocale" onchange="filter(JsonMessageDataTable, '&locale=', this.options[this.selectedIndex].value)">
                                <option></option>
                            <c:forEach items="${localeList}" var="o">
                                <c:set var="selected"><c:if test="${o == param.locale}"> selected</c:if></c:set>
                                <option value="${o}" ${selected}>${o}</option>
                            </c:forEach>
                            </select>
                            </form>
                        </div>
                        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/message/list?${pageContext.request.queryString}"
                           var="JsonMessageDataTable"
                           divToUpdate="messageList"
                           jsonData="data"
                           rowsPerPage="10"
                           width="100%"
                           sort="messageKey"
                           desc="false"
                           href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/message/edit"
                           hrefParam="id"
                           hrefQuery="true"
                           hrefDialog="true"
                           hrefDialogTitle=""
                           checkbox="${protectedReadonly != 'true'}"
                           checkboxButton1="general.method.label.delete"
                           checkboxCallback1="messageDelete"
                           checkboxButton2="console.app.message.generate.po.label"
                           checkboxCallback2="messageGenerate"
                           checkboxOptional2="true"
                           checkboxButton3="console.app.message.import.po.label"
                           checkboxCallback3="messageImport"
                           checkboxOptional3="true"
                           searchItems="filter|Filter"
                           fields="['id','messageKey','locale','message']"
                           column1="{key: 'messageKey', label: 'console.app.message.common.label.messageKey', sortable: true}"
                           column2="{key: 'locale', label: 'console.app.message.common.label.locale', sortable: true}"
                           column3="{key: 'message', label: 'console.app.message.common.label.message', sortable: false}"
                           />
                    </div>
                    <div id="pluginDefault">
                        <br/>
                        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/pluginDefault/list?${pageContext.request.queryString}"
                           var="JsonPluginDefaultDataTable"
                           divToUpdate="pluginDefaultList"
                           jsonData="data"
                           rowsPerPage="10"
                           width="100%"
                           sort="id"
                           desc="false"
                           href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/config"
                           hrefParam="id"
                           hrefQuery="true"
                           hrefDialog="true"
                           hrefDialogTitle=""
                           checkbox="${protectedReadonly != 'true'}"
                           checkboxButton1="general.method.label.delete"
                           checkboxCallback1="pluginDefaultDelete"
                           searchItems="filter|Filter"
                           fields="['id','pluginName','pluginDescription']"
                           column1="{key: 'pluginName', label: 'console.plugin.label.name', sortable: true}"
                           column2="{key: 'pluginDescription', label: 'console.plugin.label.description', sortable: true}"
                           />
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var tabView = new TabView('propertiesTab', 'top');
    tabView.init();
    <c:if test="${!empty param.tab}">
        tabView.select('#<c:out value="${param.tab}"/>');
    </c:if>

    $(document).ready(function(){
        $('#JsonMessageDataTable_searchTerm').hide();
        $('#JsonVariableDataTable_searchTerm').hide();
        $('#JsonPluginDefaultDataTable_searchTerm').hide();
        <c:if test="${protectedReadonly == 'true'}">
        $(".ui-tabs-panel button[onclick*='Delete']").hide();
        </c:if>
    });

    <ui:popupdialog var="messageCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/message/create"/>
    <ui:popupdialog var="messageGenerateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/message/generatepo"/>
    <ui:popupdialog var="messageImportDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/message/importpo"/>
    <ui:popupdialog var="variableCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/envVariable/create"/>
    <ui:popupdialog var="pluginDefaultCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/create"/>

    function messageCreate(){
        messageCreateDialog.init();
    }

    function environmentVariableCreate(){
        variableCreateDialog.init();
    }

    function defaultPluginPropertiesCreate(){
        pluginDefaultCreateDialog.init();
    }

    function closeDialog() {
        messageCreateDialog.close();
        messageGenerateDialog.close();
        messageImportDialog.close();
        variableCreateDialog.close();
        pluginDefaultCreateDialog.close();
    }

    function messageDelete(selectedList){
         if (confirm('<fmt:message key="console.app.message.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    filter(JsonMessageDataTable, '', '');
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/message/delete', callback, 'ids='+selectedList);
        }
    }
    
    function messageGenerate(selectedList){
        messageGenerateDialog.init();
    }
    
    function messageImport(selectedList){
        messageImportDialog.init();
    }

    function envVariableDelete(selectedList){
         if (confirm('<fmt:message key="console.app.envVariable.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    filter(JsonVariableDataTable, '&filter=', $('#JsonVariableDataTable_searchCondition').val());
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/envVariable/delete', callback, 'ids='+selectedList);
        }
    }

    function pluginDefaultDelete(selectedList){
         if (confirm('<fmt:message key="console.app.pluginDefault.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    filter(JsonPluginDefaultDataTable, '&filter=', $('#JsonPluginDefaultDataTable_searchCondition').val());
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/pluginDefault/delete', callback, 'ids='+selectedList);
        }
    }

    function exportApp(){
        document.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/export';
    }

    var org_filter = window.filter;
    var filter = function(jsonTable, url, value){
        var tempValue = "";
        if(jsonTable == JsonMessageDataTable){
            url = "&locale=" + $('#JsonMessageDataTable_filterbyLocale').val();
            url += "&filter=" + $('#JsonMessageDataTable_searchCondition').val();
        }else{
            tempValue = value
        }

        org_filter(jsonTable, url, tempValue);
    };
    
    Template.init("#menu-apps", "#nav-app-props");
</script>

<commons:footer />
