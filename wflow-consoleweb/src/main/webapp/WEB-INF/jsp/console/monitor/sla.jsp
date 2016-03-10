<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">

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
    </div>
    <div id="main-body">
        <div id="main-body-content-filter">
            <form>
            <fmt:message key="console.monitoring.sla.label.filterByProcess"/>
            <select onchange="filter(slaTable, '&processDefId=', escape(this.options[this.selectedIndex].value))">
                <option></option>
            <c:forEach items="${processDefinitionList}" var="processDefinition">
                <c:set var="selected"><c:if test="${processDefinition.id == param.processDefId}"> selected</c:if></c:set>
                <option value="<c:out value="${processDefinition.id}"/>" ${selected}><c:out value="${processDefinition.name}"/> - Version <c:out value="${processDefinition.version}"/></option>
            </c:forEach>
            </select>
            </form>
        </div>

        <ui:jsontable url="${pageContext.request.contextPath}/web/json/workflow/sla/list?${pageContext.request.queryString}"
                       var="slaTable"
                       divToUpdate="slaList"
                       jsonData="data"
                       rowsPerPage="10"
                       width="100%"
                       sort="name"
                       desc="false"
                       fields="['activityName', 'minDelay', 'maxDelay','ratioWithDelay','ratioOnTime','serviceLevelMonitor']"
                       column1="{key: 'activityName', label: 'console.monitoring.sla.label.activity', sortable: false}"
                       column2="{key: 'serviceLevelMonitor', label: 'console.monitoring.sla.label.serviceLevelMonitor', sortable: false, relaxed: true}"
                       column3="{key: 'ratioWithDelay', label: 'console.monitoring.sla.label.ratioWithDelay', sortable: false}"
                       column4="{key: 'ratioOnTime', label: 'console.monitoring.sla.label.ratioOnTime', sortable: false}"
                       column5="{key: 'minDelay', label: 'console.monitoring.sla.label.minDelay', sortable: false}"
                       column6="{key: 'maxDelay', label: 'console.monitoring.sla.label.maxDelay', sortable: false}"
                       />
    </div>
</div>

<script>
    Template.init("#menu-monitor", "#nav-monitor-sla");
</script>

<commons:footer />
