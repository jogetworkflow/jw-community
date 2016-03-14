<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.process.config.label.mapTools"/> - <ui:stripTag html="${param.activityName}"/> <c:out value="(${activityDefId})" escapeXml="true" />
    </div>
    
    <c:set var="title"><ui:escape value=" - ${param.activityName} (${activityDefId})" format="url;url;javascript" /></c:set>

    <div id="main-body-content" style="text-align: left">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?className=org.joget.plugin.base.ApplicationPlugin&${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/${activityDefId}/plugin/submit?title=${title}&"
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
