<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

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
    ${content}
</div>
<commons:footer />
