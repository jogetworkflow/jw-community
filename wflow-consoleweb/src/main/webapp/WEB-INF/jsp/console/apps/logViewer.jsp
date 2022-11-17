<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>


<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
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

<jsp:include page="../log/log.jsp" flush="true" />
        
<script>
    Template.init("#menu-apps", "#nav-app-logs");
</script>    


<commons:footer />
