<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled()%>"/>
<c:set var="isAdmin" scope="request" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN)%>"/>
<c:if test="${param.webConsole =='true' || (isQuickEditEnabled && isAdmin) }">

    <c:set var="username" scope="request" value="<%= WorkflowUtil.getCurrentUsername()%>"/>
    <c:set var="userIsReadonly" scope="request" value="<%= DirectoryUtil.userIsReadonly(WorkflowUtil.getCurrentUsername())%>"/>
    <c:set var="isAnonymous" scope="request" value="<%= WorkflowUtil.isCurrentUserAnonymous()%>"/>
    <c:set var="isCustomDirectoryManager" value="<%= DirectoryUtil.isCustomDirectoryManager()%>"/>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/nav_menu.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/nav_menu_custom.css"/>
    
    <script src="${pageContext.request.contextPath}/js/jquery/jquery.cookie.js"></script>
    <script src="${pageContext.request.contextPath}/js/navMenu.js"></script>
    
    <c:if test="${isAdmin}">
        <c:set var="useOverlay" value="${param.frontEndMode == 'true' || param.builderMode == 'true'}"/>
        <c:set var="quickEditMode" value="${cookie['quickEditMode'].value}"/>
        <c:set var="cookiePath" value="${pageContext.request.contextPath}/"/>
        <c:if test="${param.webConsole == 'true'}">
            <c:set var="quickEditMode" value="${true}"/>
            <style>
                #menu-popup #quickEditModeOption {
                    display: none;
                }
                #menu-popup .menu-link-admin {
                    display: block;
                }
            </style>
        </c:if>
        <c:if test="${param.frontEndMode == 'true'}">
            <c:set var="cookiePath" value="${pageContext.request.contextPath}/web/userview/"/>
        </c:if>
        <c:if test="${param.webConsole == 'true'}">
            <c:set var="quickEditMode" value="${param.webConsole}"/>
        </c:if>
        <c:if test="${quickEditMode == 'false'}">
            <style>
                .quickEdit, #form-canvas .quickEdit {
                    display: none;
                }
            </style>
        </c:if>
        <script>
        var path = "${cookiePath}";
        NavMenu.setCookiePath(path);
        <c:if test="${param.webConsole == 'true'}">
            NavMenu.enableQuickEditMode();
        </c:if>
        </script>
    </c:if>    

    <div id="main-menu">
        <a id="menu-button" href="#">
            <i class="icon-reorder"></i>                
        </a>
        <div id="menu-popup">
            <div id="menu-logo"></div>
            <ul>
                <li><a href="${pageContext.request.contextPath}/web/desktop"><i class="icon-desktop"></i> <fmt:message key="adminBar.label.appCenter"/></a></li>
                <c:if test="${!isAnonymous}">
                    <li><a href="${pageContext.request.contextPath}/web/console/run/inbox" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/console/run/inbox', '${useOverlay}')"><i class="icon-tasks"></i> <fmt:message key="console.header.submenu.label.inbox"/></a></li>
                </c:if>
                <c:if test="${isAdmin}">
                    <h3><fmt:message key="appCenter.label.administration"/>
                    <c:if test="${true || !empty param.appId}">
                        <div id="quickEditModeOption">
                            <input type="radio" id="quickEditModeOn" name="radio" /><label id="quickEditModeOnLabel" for="quickEditModeOn"><fmt:message key='adminBar.label.on'/></label>
                            <input type="radio" id="quickEditModeOff" name="radio" /><label id="quickEditModeOffLabel" for="quickEditModeOff"><fmt:message key='adminBar.label.off'/></label>
                        </div>
                    </c:if>
                    </h3>
                    <ul>
                        <c:if test="${!empty param.appId}">
                            <li><a class="menu-link-admin" href="${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/<c:out value="${param.appId}"/>/<c:out value="${param.appVersion}"/>/forms', '${useOverlay}')"><i class="icon-edit"></i> <fmt:message key="adminBar.label.designApp"/></a></li>
                        </c:if>
                        <c:if test="${empty param.appId}">
                            <li><a class="menu-link-admin" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps', '${useOverlay}')"><i class="icon-edit"></i> <fmt:message key="console.header.menu.label.apps"/></a></li>
                        </c:if>
                        <li><a class="menu-link-admin" href="${pageContext.request.contextPath}/web/console/monitor/running" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/console/monitor/running', '${useOverlay}')"><i class="icon-dashboard"></i> <fmt:message key='adminBar.label.monitor'/></a></li>
                        <li><a class="menu-link-admin" href="${pageContext.request.contextPath}/web/console/directory/orgs" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/console/directory/orgs', '${useOverlay}')"><i class="icon-group"></i> <fmt:message key='adminBar.label.users'/></a></li>
                        <li><a class="menu-link-admin" href="${pageContext.request.contextPath}/web/console/setting/general" onclick="return NavMenu.showQuickOverlay('${pageContext.request.contextPath}/web/console/setting/general', '${useOverlay}')"><i class="icon-cogs"></i> <fmt:message key='adminBar.label.settings'/></a></li>
                    </ul>
                </c:if>                    
                <c:if test="${!isAnonymous}">
                    <h3><fmt:message key="console.header.top.label.userProfile"/></h3>
                    <c:choose>
                        <c:when test="${isCustomDirectoryManager || userIsReadonly}">
                            <li><i class="icon-user"></i> ${username}</li>
                            </c:when>
                            <c:otherwise>
                            <li><a href="javascript:navMenuUserProfile()"><i class="icon-user"></i> <fmt:message key="console.header.top.label.userProfile"/> (<c:out value="${username}"/>)</a></li>
                            <script>
                                <ui:popupdialog var="userProfilePopupDialog" src="${pageContext.request.contextPath}/web/console/profile"/>
                                    function navMenuUserProfile() {
                                        userProfilePopupDialog.init();
                                    }

                                    function userProfileCloseDialog() {
                                        userProfilePopupDialog.close();
                                    }
                            </script>
                        </c:otherwise>
                    </c:choose>
                    <li><a href="${pageContext.request.contextPath}/j_spring_security_logout"><i class="icon-signout"></i> <fmt:message key="console.header.top.label.logout"/></a></li>
                </c:if>
                <c:if test="${isAnonymous}">
                <li><a href="${pageContext.request.contextPath}/web/login"><i class="icon-signin"></i> <fmt:message key="console.header.top.label.login"/></a></li>
                </c:if>
            </ul>
        </div>
    </div>
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
    <c:if test="${param.frontEndMode == 'true'}">
    <script>
        $("#main-menu").addClass("quickEditMode");
    </script>
    </c:if>
    <c:if test="${param.builderMode == 'true'}">
    <script>
        $("#main-menu").addClass("builderMode");
    </script>
    </c:if>
</c:if>