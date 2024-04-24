<%@page import="org.joget.apps.workflow.security.EnhancedWorkflowUserManager"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>
<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:set var="envName" value='<%= WorkflowUtil.getSystemSetupValue("environmentName") %>'/>
<c:if test="${isQuickEditEnabled || param.webConsole =='true'}">
    <c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
    <c:set var="isCustomAppAdmin" value="<%= EnhancedWorkflowUserManager.isAppAdminRole() %>"/>
    <c:if test="${isAdmin && (param.builderMode || !(isCustomAppAdmin && !empty param.webConsole))}">
        <script>
            loadCSS("${pageContext.request.contextPath}/css/admin_bar_custom.css");
        </script>
        <div id="adminBar" class="adminBarInactive"  <c:if test="${!empty theme && (theme == 'light' || theme == 'dark')}">builder-theme="<c:out value="${theme}"/>"</c:if>>
            <a id="appCenter" <c:if test="${empty param.webConsole}"> target="_blank"</c:if> title="<ui:msgEscHTML key='adminBar.label.appCenter'/>" href="${pageContext.request.contextPath}/home"><i class="fab fa-joget"></i></a>
            <div id="adminBarButtons">
            <c:set var="key" value="0" />    
            <c:if test="${!empty param.appId}">    
                <c:set var="key" value="1" />
                <div class="separator"></div>
                    <div>
                        <a class="adminBarButton" title="CTRL-1: <ui:msgEscHTML key='abuilder.title'/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/builders" onclick="return AdminBar.openAppComposer('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/builders');" target="_blank"><i class="far fa-edit"></i><span><fmt:message key='abuilder.title'/></span></a>
                    </div>
                
            </c:if>  
            <c:if test="${!empty param.appId && !isCustomAppAdmin}">
                <div class="separator"></div>
            </c:if>    
            <c:if test="${!isCustomAppAdmin}">    
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 1}"/>: <ui:msgEscHTML key='adminBar.label.manageApps'/>" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps')"><i class="fas fa-th"></i><span><fmt:message key='adminBar.label.allApps'/></span></a>
                </div>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 2}"/>: <ui:msgEscHTML key='adminBar.label.setupUsers'/>" href="${pageContext.request.contextPath}/web/console/directory/users" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/directory/users')"><i class="fas fa-users"></i><span><fmt:message key='adminBar.label.users'/></span></a>
                </div>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 3}"/>: <ui:msgEscHTML key='adminBar.label.monitorApps'/>" href="${pageContext.request.contextPath}/web/console/monitor/running" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/monitor/running')"><i class="fas fa-tachometer-alt"></i><span><fmt:message key='adminBar.label.monitor'/></span></a>
                </div>
                <div>
                    <a class="adminBarButton" title="CTRL-<c:out value="${key + 4}"/>: <ui:msgEscHTML key='adminBar.label.systemSettings'/>" href="${pageContext.request.contextPath}/web/console/setting/general" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/setting/general')"><i class="fas fa-cogs"></i><span><fmt:message key='adminBar.label.settings'/></span></a>
                </div>
            </c:if>    
            </div>
            <div id="quickEditModeOption">
                <div>
                    <a id="quickEditMode" title="CTRL-0: <ui:msgEscHTML key='adminBar.label.quickedit'/>"><i class="fas fa-paint-brush"></i><span><fmt:message key='adminBar.label.quickedit'/> : </span><span class="on"><fmt:message key='adminBar.label.on'/></span><span class="off"><fmt:message key='adminBar.label.off'/></span></a>
                </div>
            </div>
            <c:if test="${!empty envName}">
                <span id="environmentName"><span><c:out value="${envName}"/></span></span>
            </c:if>
        </div>
            
        <div id="adminControl"  <c:if test="${!empty theme && (theme == 'light' || theme == 'dark')}">builder-theme="<c:out value="${theme}"/>"</c:if>>
            <i class="fas fa-pencil-alt"></i>
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
        </script>
        
    <script>
        <ui:popupdialog var="appCreateDialog" src="${pageContext.request.contextPath}/web/console/app/create"/>
            function appCreate(templateId) {
                if (templateId !== undefined) {
                    appCreateDialog.src = "${pageContext.request.contextPath}/web/console/app/create?templateAppId=" + encodeURIComponent(templateId);
                } else {
                    appCreateDialog.src = "${pageContext.request.contextPath}/web/console/app/create";
                }
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
