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
        <ul id="main-action-buttons">
        </ul>
    </div>
    <div id="main-body">

        <div>
            <c:if test="${empty appDefinitionList[0]}">
                <fmt:message key="console.run.apps.none"/>
            </c:if>
        </div>
        <ul class="main-grid">
        <c:forEach items="${appDefinitionList}" var="appDefinition">
            <li class="main-grid-item">
                <div class="main-grid-title">${appDefinition.name}</div>
                <div class="main-grid-corner"><fmt:message key="console.app.common.label.version"/> ${appDefinition.version}</div>
                <div class="main-grid-description"></div>
                <ul class="main-subgrid">
                <c:set var="userviewDefinitionList" value="${appDefinition.userviewDefinitionList}"/>
                <c:if test="${empty userviewDefinitionList[0]}">
                    <li class="main-subgrid-item">
                        <div class="main-subgrid-description"><fmt:message key="console.run.notAvailable"/></div>
                    </li>
                </c:if>
                <c:forEach items="${userviewDefinitionList}" var="userviewDefinition">
                    <li class="main-subgrid-item">
                        <div class="main-subgrid-title">${userviewDefinition.name}</div>
                        <div class="main-subgrid-action">
                            <button onclick="window.open('${pageContext.request.contextPath}/web/userview/${appDefinition.id}/${userviewDefinition.id}')"><fmt:message key="console.run.launch"/></button>
                        </div>
                        <div class="main-subgrid-description">${userviewDefinition.description}</div>
                    </li>
                </c:forEach>
                </ul>
            </li>
        </c:forEach>
        </ul>
    </div>
</div>

<script>
    Template.init("#menu-run", "#nav-run-apps");
</script>

<commons:footer />
