<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:if test="${isQuickEditEnabled || param.webConsole =='true'}">
    <c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
    <c:if test="${isAdmin}">
        <c:if test="${empty isDefaultUserview}"><c:set var="isDefaultUserview" value="<%= false %>"/></c:if>
        <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/admin_bar.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/admin_bar_custom.css" />
        <div id="adminBar" class="adminBarInactive">
            <a id="appCenter" <c:if test="${empty param.webConsole}"> target="_blank"</c:if> title="<fmt:message key='adminBar.label.appCenter'/>" href="${pageContext.request.contextPath}/home"></a>
            <div id="quickEditModeOption">
                <input type="radio" id="quickEditModeOn" name="radio" /><label id="quickEditModeOnLabel" for="quickEditModeOn"><fmt:message key='adminBar.label.on'/></label>
                <input type="radio" id="quickEditModeOff" name="radio" /><label id="quickEditModeOffLabel" for="quickEditModeOff"><fmt:message key='adminBar.label.off'/></label>
            </div>
            <div id="adminBarButtons">
            <c:if test="${!empty param.appId || !empty param.webConsole}">
                <c:set var="key" value="1" />
                <c:if test="${!empty param.appId && !isDefaultUserview}">
                    <c:set var="key" value="3" />
                    <div class="separator">
                        <h5><fmt:message key='adminBar.label.app'/></h5>
                    </div>    
                    <div>
                        <a class="adminBarButton" style="display:none" title="CTRL-1: <fmt:message key="console.header.submenu.label.formsAndUi"/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms')"><i class="icon-edit"></i><br><fmt:message key="console.header.submenu.label.formsAndUi"/></a>
                    </div>
                    <div>
                        <a class="adminBarButton" style="display:none" title="CTRL-2: <fmt:message key="console.header.submenu.label.processes"/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/processes" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/processes')"><i class="icon-list"></i><br><fmt:message key="console.header.submenu.label.processes"/></a>
                    </div>
                    <div>
                        <a class="adminBarButton" style="display:none" title="CTRL-3: <fmt:message key='adminBar.label.app.properties'/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/properties" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/properties')"><i class="icon-cog"></i><br><fmt:message key='adminBar.label.app.properties'/></a>
                    </div>
                    <div class="separator">
                        <h5><fmt:message key='adminBar.label.system'/></h5>
                    </div> 
                </c:if>
                <c:if test="${!empty param.appControls || isDefaultUserview}">
                    <div>
                        <a class="adminBarButton" style="display:none" title="CTRL-1: <fmt:message key='adminBar.label.manageApps'/>" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps')"><i class="icon-wrench"></i><br><fmt:message key='adminBar.label.allApps'/></a>
                    </div>
                </c:if>
                <div>
                    <a class="adminBarButton" style="display:none" title="CTRL-<c:out value="${key + 1}"/>: <fmt:message key='adminBar.label.setupUsers'/>" href="${pageContext.request.contextPath}/web/console/directory/users" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/directory/users')"><i class="icon-user"></i><br><fmt:message key='adminBar.label.users'/></a>
                </div>
                <div>
                    <a class="adminBarButton" style="display:none" title="CTRL-<c:out value="${key + 2}"/>: <fmt:message key='adminBar.label.monitorApps'/>" href="${pageContext.request.contextPath}/web/console/monitor/running" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/monitor/running')"><i class="icon-dashboard"></i><br><fmt:message key='adminBar.label.monitor'/></a>
                </div>
                <div>
                    <a class="adminBarButton" style="display:none" title="CTRL-<c:out value="${key + 3}"/>: <fmt:message key='adminBar.label.systemSettings'/>" href="${pageContext.request.contextPath}/web/console/setting/general" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/setting/general')"><i class="icon-cogs"></i><br><fmt:message key='adminBar.label.settings'/></a>
                </div>
            </c:if>
            </div>
            <div id="adminBarWelcome">
            <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
            </div>
        </div>
            
        <div id="adminControl">
            <i class="icon-edit"></i>
        </div>    
            
        <script src="${pageContext.request.contextPath}/js/jquery/jquery.cookie.js"></script>
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
    </c:if>
    
</c:if>
<jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=adminBar" flush="true" />             
<jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
<jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
