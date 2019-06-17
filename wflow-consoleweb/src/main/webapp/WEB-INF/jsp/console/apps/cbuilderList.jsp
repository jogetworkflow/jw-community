<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<link href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" rel="stylesheet" type="text/css" />
<link href="${pageContext.request.contextPath}/js/at/usages.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/tooltipster.bundle.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/plugins/tooltipster/sideTip/themes/tooltipster-sideTip-shadow.min.css" />
<script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
<script type='text/javascript' src="${pageContext.request.contextPath}/js/at/usages.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>
<script type='text/javascript' src="${pageContext.request.contextPath}/js/nav.js"></script>

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
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
        </ul>
    </div>
    <div id="main-body">
        <div id="nv-toolbar">
            <div id="nv-search">
                
            </div>    
            <div id='nv-refresh'>
                <a id="toggleInfo" ><i class='fas fa-tags'></i> <span><fmt:message key="console.tag.show"/></span></a>&nbsp;&nbsp;
                <a id="refreshBtn" ><i class="fas fa-sync-alt"></i> <span><fmt:message key="general.method.label.refresh"/></span></a>
            </div>
        </div>
        <div id="nv-container">
        <jsp:include page="/web/console/app/${appId}/${appVersion}/customBuilders" flush="true"/>
        </div>
        <script>
            function refreshNavigator() {
                Nav.refresh();
                return false;
            }
            function closeDialog() {
                 Nav.refresh();
            }            
        </script>
    </div>
</div>

<script>
    Template.init("#menu-apps", "#nav-app-cbuilders");
</script>

<commons:footer />
