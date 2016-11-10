<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="runApps" />

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

            <div id="title">
                <fmt:message key="appCenter.label.publishedApps"/>
            </div>
        
            <div id="apps" class="published-apps"></div>
            <div class="clear"></div>
        
    </div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/appCenter.css" />
<script src="${pageContext.request.contextPath}/js/appCenter.js"></script>
<script>
AppCenter.searchFilter($("#title"), $("#apps")); 
AppCenter.loadPublishedApps("#apps");
</script>

<script>
    Template.init("#menu-run", "#nav-run-apps");
</script>

<commons:footer />
