<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <c:choose>
        <c:when test="${!empty param.title}">
            <c:set var="title" value="${param.title}"></c:set>
        </c:when>
        <c:otherwise>
            <c:set var="title" value=" - ${param.activityName} (${activityDefId})"></c:set>
        </c:otherwise>    
    </c:choose>

    <div id="main-body-header">
        <fmt:message key="console.process.config.label.mapTools"/> <ui:stripTag html="${title}"/>
    </div>
    
    <c:set var="title"><ui:escape value="${title}" format="url;url;javascript" /></c:set>

    <div id="main-body-content" style="text-align: left">
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/list?className=org.joget.plugin.base.ApplicationPlugin&${pageContext.request.queryString}"
                       var="JsonDataTable"
                       divToUpdate="pluginList"
                       jsonData="data"
                       rowsPerPage="15"
                       width="100%"
                       sort="name"
                       desc="false"
                       href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/${activityDefId}/plugin/submit?title=${title}&tab=toolList&"
                       hrefParam="id"
                       hrefQuery="true"
                       hrefDialog="false"
                       hrefPost="true"
                       searchItems="name|Name"
                       fields="['id','name','description','version']"
                       column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
                       column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                       column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 80}"
                       />

    </div>
        
    <script>
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });
    </script>
<commons:popupFooter />
