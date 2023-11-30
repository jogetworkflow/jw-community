<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}"/>

    <div id="main-body-header">
        <fmt:message key="console.app.pluginDefault.create.label.title"/>
    </div>

    <div id="main-body-content">
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
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listDefault?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="15"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/config?action=create&"
                       hrefParam="id"
                       hrefQuery="true"
                       hrefDialog="false"
                       searchItems="name|Name"
                       fields="['id','name','description','version','plugintype']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
                       column4="{key: 'plugintype', label: 'console.plugin.label.plugintype', sortable: false, width: 300}"
                       />
    </div>
    <script>
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });
    </script>    
<commons:popupFooter />
