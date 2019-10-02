<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="runApps" />

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-th-large"></i> <fmt:message key="appCenter.label.publishedApps"/></p>
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
            </div>

            <div><ul id="apps" class="published-apps"></ul></div>
            <div class="clear"></div>
        
    </div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/appCenter7.css" />
<style>
.filterform {
    width: 173px;
    margin: 10px auto 30px;
}
@media (min-width: 1025px) {
    ul#apps li {
        margin: 0 15px 30px;
    }
}
</style>
<script src="${pageContext.request.contextPath}/js/appCenter7.js"></script>
<script>
UI.base = "${pageContext.request.contextPath}";    
AppCenter.searchFilter($("#title"), $("#apps")); 
AppCenter.loadPublishedApps("#apps");
</script>

<script>
    Template.init("#menu-run", "#nav-run-apps");
</script>

<commons:footer />
