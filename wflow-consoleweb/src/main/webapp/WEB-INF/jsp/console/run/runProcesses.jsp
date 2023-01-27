<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<script>
    <ui:popupdialog var="popupDialog" src="#"/>
        
    function runProcess(appId, appVersion, processDefId){
        popupDialog.src = "${pageContext.request.contextPath}/web/client/app/" + appId + "/" + appVersion + "/process/" + processDefId;
        popupDialog.init();
    }    
</script>

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
        <ul id="main-action-buttons">
        </ul>
    </div>
    <div id="main-body">

        <div>
            <c:if test="${empty appDefinitionList}">
                <fmt:message key="console.run.apps.none"/>
            </c:if>
        </div>
        <ul class="main-grid">
        <c:forEach items="${appProcessMap}" var="appEntry">
            <c:set var="appDefinition" value="${appEntry.key}"/>
            <c:set var="processList" value="${appEntry.value}"/>
            <li class="main-grid-item">
                <div class="main-grid-title"><c:out value="${appDefinition.name}"/></div>
                <div class="main-grid-corner"><fmt:message key="console.app.common.label.version"/> <c:out value="${appDefinition.version}"/></div>
                <div class="main-grid-description"></div>
                <ul class="main-subgrid">
                <c:if test="${empty processList[0]}">
                    <li class="main-subgrid-item">
                        <div class="main-subgrid-description"><fmt:message key="console.run.notAvailable"/></div>
                    </li>
                </c:if>
                <c:forEach items="${processList}" var="processDefinition">
                    <li class="main-subgrid-item">
                        <div class="main-subgrid-title"><c:out value="${processDefinition.name}"/></div>
                        <div class="main-subgrid-action">
                            <button onclick="runProcess('<c:out value="${appDefinition.id}"/>','<c:out value="${appDefinition.version}"/>','<c:out value="${processDefinition.idWithoutVersion}"/>')"><fmt:message key="console.run.launch"/></button>
                        </div>
                        <div class="main-subgrid-description"><!-- <fmt:message key="console.app.general.label.version"/> ${processDefinition.version} --></div>
                    </li>
                </c:forEach>
                </ul>
            </li>
        </c:forEach>
        </ul>
    </div>
</div>

<script>
    Template.init("#menu-run", "#nav-run-processes");
</script>

<commons:footer />
