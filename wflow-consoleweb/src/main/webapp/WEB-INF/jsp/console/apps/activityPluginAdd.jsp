<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.process.config.label.mapTools"/> - <c:out value=" ${param.activityName} (${activityDefId})" escapeXml="true" />
    </div>

    <div id="main-body-content" style="text-align: left">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?className=org.joget.plugin.base.ApplicationPlugin&${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/${activityDefId}/plugin/submit?title= - ${fn:escapeXml(param.activityName)} (${fn:escapeXml(activityDefId)})&"
                       hrefParam="id"
                       hrefQuery="true"
                       hrefDialog="false"
                       hrefPost="true"
                       fields="['id','name','description','version']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false}"
                       />

    </div>
<commons:popupFooter />
