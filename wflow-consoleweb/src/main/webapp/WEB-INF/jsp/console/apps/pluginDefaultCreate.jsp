<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

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
        <br/>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listDefault?${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/pluginDefault/config?action=create&"
                       hrefParam="id"
                       hrefQuery="true"
                       hrefDialog="false"
                       fields="['id','name','description','version']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false}"
                       />
    </div>
<commons:popupFooter />
