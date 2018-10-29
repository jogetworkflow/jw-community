<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:if test="${isQuickEditEnabled || param.webConsole =='true'}">
    <c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
    <c:if test="${isAdmin}">
        <c:if test="${empty isDefaultUserview}"><c:set var="isDefaultUserview" value="<%= false %>"/></c:if>
        <script>
            loadCSS("${pageContext.request.contextPath}/js/font-awesome4/css/font-awesome.min.css");
            loadCSS("${pageContext.request.contextPath}/css/admin_bar_custom.css");
        </script>
        <div id="adminBar" class="adminBarInactive">
            <a id="appCenter" <c:if test="${empty param.webConsole}"> target="_blank"</c:if> title="<fmt:message key='adminBar.label.appCenter'/>" href="${pageContext.request.contextPath}/home"></a>
            <div id="adminBarButtons">
            <c:if test="${!empty param.appId || !empty param.webConsole}">
                <c:set var="key" value="1" />
                <c:if test="${!empty param.appId && !isDefaultUserview}">
                    <c:set var="key" value="3" />
                    <div class="separator">
                        <h5><fmt:message key='adminBar.label.app'/></h5>
                    </div>    
                    <div>
                        <a class="adminBarButton" title="CTRL-1: <fmt:message key="console.header.submenu.label.formsAndUi"/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms')"><i class="fa fa-edit"></i><span><fmt:message key="console.header.submenu.label.formsAndUi"/></span></a>
                    </div>
                    <div>
                        <a class="adminBarButton" title="CTRL-2: <fmt:message key="console.header.submenu.label.processes"/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/processes" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/processes')"><i class="fa fa-list"></i><span><fmt:message key="console.header.submenu.label.processes"/></span></a>
                    </div>
                    <div>
                        <a class="adminBarButton" title="CTRL-3: <fmt:message key='adminBar.label.app.properties'/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/properties" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/properties')"><i class="fa fa-cog"></i><span><fmt:message key='adminBar.label.app.properties'/></span></a>
                    </div>
                    <div class="separator">
                        <h5><fmt:message key='adminBar.label.system'/></h5>
                    </div> 
                </c:if>
                <c:if test="${!empty param.appControls || isDefaultUserview}">
                    <div>
                        <a class="adminBarButton" title="CTRL-1: <fmt:message key='adminBar.label.manageApps'/>" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps')"><i class="fa fa-wrench"></i><span><fmt:message key='adminBar.label.allApps'/></span></a>
                    </div>
                </c:if>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 1}"/>: <fmt:message key='adminBar.label.setupUsers'/>" href="${pageContext.request.contextPath}/web/console/directory/users" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/directory/users')"><i class="fa fa-user"></i><span><fmt:message key='adminBar.label.users'/></span></a>
                </div>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 2}"/>: <fmt:message key='adminBar.label.monitorApps'/>" href="${pageContext.request.contextPath}/web/console/monitor/running" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/monitor/running')"><i class="fa fa-dashboard"></i><span><fmt:message key='adminBar.label.monitor'/></span></a>
                </div>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 3}"/>: <fmt:message key='adminBar.label.systemSettings'/>" href="${pageContext.request.contextPath}/web/console/setting/general" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/setting/general')"><i class="fa fa-cogs"></i><span><fmt:message key='adminBar.label.settings'/></span></a>
                </div>
            </c:if>
            </div>
            <div id="quickEditModeOption">
                <div>
                    <a id="quickEditMode" title="CTRL-0: <fmt:message key='adminBar.label.quickedit'/>"><i class="fa fa-paint-brush"></i><span><fmt:message key='adminBar.label.quickedit'/> : </span><span class="on"><fmt:message key='adminBar.label.on'/></span><span class="off"><fmt:message key='adminBar.label.off'/></span></a>
                </div>
            </div>
            
        </div>
            
        <div id="adminControl">
            <i class="fa fa-pencil"></i>
        </div>    
            
        <script src="${pageContext.request.contextPath}/js/adminBar.js"></script>
        <script>
            AdminBar.cookiePath = '${pageContext.request.contextPath}/';
            <c:if test="${param.webConsole == 'true'}">
            AdminBar.webConsole = true;
            </c:if>
            <c:if test="${param.builderMode == 'true'}">
            AdminBar.builderMode = true;
            </c:if>
            <c:if test="${isDefaultUserview}">
            AdminBar.isDefaultUserview = true;
            </c:if>
        </script>
        
    <script>
        <ui:popupdialog var="appCreateDialog" src="${pageContext.request.contextPath}/web/console/app/create"/>
            function appCreate() {
                appCreateDialog.init();
            }
    </script>
    <script>
        <ui:popupdialog var="appCreateDialog2" src="${pageContext.request.contextPath}/web/console/app/import"/>
            function appImport() {
                appCreateDialog2.init();
            }
    </script>
        
    <jsp:include page="adminBarExt.jsp" flush="true"/>    
        <c:set var="requestUri" value="${requestScope['javax.servlet.forward.request_uri']}"/>
        <c:set var="matchingUri" value="${pageContext.request.contextPath}/web/console/app"/>
        <c:if test="${fn:startsWith(requestUri, matchingUri)}">
            <link href="${pageContext.request.contextPath}/presence/presence.css" rel="stylesheet" />
            <script src="${pageContext.request.contextPath}/presence/presence.js"></script>
        </c:if>
    </c:if>
    
</c:if>
<jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=adminBar" flush="true" />             
<jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
<jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
