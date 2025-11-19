<%@ page import="org.joget.apps.workflow.security.EnhancedWorkflowUserManager"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%
    String theme = AppUtil.getSystemTheme();
    pageContext.setAttribute("theme", theme);

    boolean deviceTheme = AppUtil.isFollowDeviceTheme();
    pageContext.setAttribute("deviceTheme", deviceTheme);
%>
<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
<c:set var="isSystemAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(EnhancedWorkflowUserManager.ROLE_SYSADMIN) %>"/>
<c:set var="isSystemManager" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_SYSTEM_MANAGER) %>"/>
<c:set var="isAppCreator" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_APP_CREATOR) %>"/>
<c:set var="isCustomAppDesigner" value="<%= EnhancedWorkflowUserManager.isAppDesignerRole() %>"/>
<c:set var="hasAppComposerPerm" value="${!empty param.appId && (isAdmin || isCustomAppDesigner)}"/>
<c:set var="hasAllAppPerm" value="${isAdmin || isAppCreator}"/>
<c:set var="hasSysPerm" value="${isSystemAdmin || isSystemManager}"/>
    
<c:if test="${hasAppComposerPerm || hasAllAppPerm || hasSysPerm}">
    <c:set var="envName" value='<%= WorkflowUtil.getSystemSetupValue("environmentName") %>'/>
    
    <script>
        loadCSS("${pageContext.request.contextPath}/css/admin_bar_custom.css");
    </script>
    <div id="adminBar" class="adminBarInactive" <c:if test="${deviceTheme}">device-theme="true"</c:if> <c:if test="${!empty theme}">builder-theme="<c:out value="${theme}"/>"</c:if>>
        <a id="appCenter" <c:if test="${empty param.webConsole}"> target="_blank"</c:if> title="<ui:msgEscHTML key='adminBar.label.appCenter'/>" href="${pageContext.request.contextPath}/home"><i class="fab fa-joget"></i></a>  
        <div id="adminBarButtons">
            <div class="separator"></div>
        <c:set var="key" value="0" />
        <c:if test="${hasAppComposerPerm}">    
            <c:set var="key" value="1" />
            <div>
                <a class="adminBarButton" title="CTRL-1: <ui:msgEscHTML key='abuilder.title'/>" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/builders" onclick="return AdminBar.openAppComposer('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/builders');" target="_blank"><i class="far fa-edit"></i><span><fmt:message key='abuilder.title'/></span></a>
            </div>  
            <c:if test="${!empty param.appId && (hasAllAppPerm || hasSysPerm)}">
                <div class="separator"></div>
            </c:if>
        </c:if>   
        <c:if test="${hasAllAppPerm}">
            <div>
                <a class="adminBarButton" title="CTRL-<c:out value="${key + 1}"/>: <ui:msgEscHTML key='adminBar.label.manageApps'/>" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps')"><i class="fas fa-th"></i><span><fmt:message key='adminBar.label.allApps'/></span></a>
            </div>
        </c:if>
        <c:if test="${hasSysPerm}">   
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
        <c:if test="${hasAppComposerPerm}"> 
            <div id="quickEditModeOption">
                <div>
                    <a id="quickEditMode" title="CTRL-0: <ui:msgEscHTML key='adminBar.label.quickedit'/>"><i class="fas fa-paint-brush"></i><span><fmt:message key='adminBar.label.quickedit'/></span>
                        <input type="checkbox" name="admin-bar-toggle" id="admin-bar-toggle" class="admin-bar-toggle"/>
                        <label class="adminbar-control-label button" for="admin-bar-toggle">
                            <div class="dot"></div>
                        </label>
                    </a>
                </div>
            </div>
        </c:if>
        <c:if test="${!empty envName}">
            <span id="environmentName" title="<c:out value="${envName}"/>"><span><c:out value="${envName}"/></span></span>
        </c:if>
    </div>

    <div id="adminControl"  <c:if test="${!empty theme && (theme == 'light' || theme == 'dark')}">builder-theme="<c:out value="${theme}"/>"</c:if>>
        <i class="fas fa-cogs"></i>
    </div>    

    <script src="${pageContext.request.contextPath}/js/adminBar.js?build=<fmt:message key="build.number"/>"></script>
    <script>
        AdminBar.cookiePath = '${pageContext.request.contextPath}/';
        <c:if test="${param.webConsole == 'true'}">
        AdminBar.webConsole = true;
        </c:if>
        <c:if test="${param.builderMode == 'true'}">
        AdminBar.builderMode = true;
        </c:if>
        <c:if test="${hasAppComposerPerm}">
        AdminBar.hasAppComposerPerm = true;
        </c:if>
        <c:if test="${hasSysPerm}">
        AdminBar.hasSysPerm = true;
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
    <script>
        if (window.location === window.parent.location) {
            //Listen to theme changes
            function ajaxRequestChangeSystemTheme(deviceTheme) {
                var callback = {
                    success: function(response) {
                        UI.confirm('<ui:msgEscJS key="general.label.deviceThemeSwitching"/>' + deviceTheme + '<ui:msgEscJS key="general.label.deviceThemeSwitching2"/>',
                            () => {
                                location.reload();
                            }, {
                                confirmButtonLabel: '<ui:msgEscJS key="console.setting.plugin.common.label.reloadPlugin"/>',
                                confirmButtonClass: 'dialog-btn-primary'
                            }
                        );                     
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        console.error('Error updating theme:', jqXHR);
                    }
                }

                var params = "deviceTheme=" + deviceTheme;
                ConnectionManager.post(
                    UI.base +'/web/console/setting/general/changeSystemThemeAutomatically', 
                    callback,
                    params
                )
            }
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change',({ matches }) => {
                if (matches && $("div#adminBar").attr('device-theme') === "true" && $("div#adminBar").attr('builder-theme') !== "dark") {
                    ajaxRequestChangeSystemTheme('dark');
                } else if (!matches && $("div#adminBar").attr('device-theme') === "true" && $("div#adminBar").attr('builder-theme') !== "light"){
                    ajaxRequestChangeSystemTheme('light');
                }
            })   
        }
    </script>
    <script>
        $(document).ready(function(){
            <c:if test="${!hasAppComposerPerm}">
                AdminBar.disableQuickEditMode();
            </c:if>
        });
    </script>

    <jsp:include page="adminBarExt.jsp" flush="true"/>    
    <c:set var="requestUri" value="${requestScope['jakarta.servlet.forward.request_uri']}"/>
    <c:set var="matchingUri" value="${pageContext.request.contextPath}/web/console/app"/>
    <c:if test="${fn:startsWith(requestUri, matchingUri)}">
        <link href="${pageContext.request.contextPath}/presence/presence.css" rel="stylesheet" />
        <script src="${pageContext.request.contextPath}/presence/presence.js"></script>
    </c:if>
</c:if>
<jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=adminBar" flush="true" />             
<jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
<jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
