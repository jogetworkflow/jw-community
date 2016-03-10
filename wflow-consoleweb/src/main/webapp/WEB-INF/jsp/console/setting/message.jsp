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
            <li><button onclick="messageCreate()"><fmt:message key="console.setting.message.create.label"/></button></li>
            <li><button onclick="importPOFile()"><fmt:message key="console.setting.message.import.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.setting.message.filter.label.byLocale"/>
            <select id="JsonMessageDataTable_filterbyLocale" onchange="filter(JsonMessageDataTable, '&locale=', this.options[this.selectedIndex].value)">
                <option></option>
            <c:forEach items="${localeList}" var="o">
                <c:set var="selected"><c:if test="${o == param.locale}"> selected</c:if></c:set>
                <option value="${o}" ${selected}>${o}</option>
            </c:forEach>
            </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/setting/message/list?${pageContext.request.queryString}"
           var="JsonMessageDataTable"
           divToUpdate="messageList"
           jsonData="data"
           rowsPerPage="10"
           width="100%"
           sort="messageKey"
           desc="false"
           href="${pageContext.request.contextPath}/web/console/setting/message/edit"
           hrefParam="id"
           hrefQuery="false"
           hrefDialog="true"
           hrefDialogTitle=""
           checkbox="true"
           checkboxButton2="general.method.label.delete"
           checkboxCallback2="messageDelete"
           searchItems="filter|Filter"
           fields="['id','key','locale','message']"
           column1="{key: 'key', label: 'console.setting.message.common.label.key', sortable: true}"
           column2="{key: 'locale', label: 'console.setting.message.common.label.locale', sortable: true}"
           column3="{key: 'message', label: 'console.setting.message.common.label.message', sortable: false}"
           />
    </div>
</div>

<script>
    $(document).ready(function(){
        $('#JsonMessageDataTable_searchTerm').hide();
    });

    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/setting/message/create"/>
    <ui:popupdialog var="importPopupDialog" src="${pageContext.request.contextPath}/web/console/setting/message/import"/>

    function messageCreate(dummy){
        popupDialog.init();
    }

    function importPOFile(){
        importPopupDialog.init();
    }

    function closeDialog() {
        popupDialog.close();
        importPopupDialog.close();
    }

    function messageDelete(selectedList){
         if (confirm('<fmt:message key="console.setting.message.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    filter(JsonMessageDataTable, '', '');
                }
            }
            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/message/delete', callback, 'ids='+selectedList);
        }
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
</script>

<script>
    Template.init("", "#nav-setting-message");
</script>

<commons:footer />
