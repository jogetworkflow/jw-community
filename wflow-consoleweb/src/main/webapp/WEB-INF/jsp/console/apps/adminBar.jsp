<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:if test="${isQuickEditEnabled || param.desktop == 'true'}">
    <c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
    <c:if test="${isAdmin}">

    <script src="${pageContext.request.contextPath}/js/jquery/jquery.cookie.js"></script>

    <c:set var="quickEditMode" value="${cookie['quickEditMode'].value}"/>
    <c:set var="cookiePath" value="${pageContext.request.contextPath}/"/>
    <c:if test="${!empty param.webConsole || !empty param.desktop}">
        <c:set var="quickEditMode" value="${true}"/>
        <style>
            #quickEditModeOption {
                display: none;
            }
        </style>
        <c:if test="${!empty param.webConsole}">
            <c:set var="cookiePath" value="${pageContext.request.contextPath}/web/console/app/"/>
        </c:if>
        <c:if test="${!empty param.desktop}">
            <c:set var="cookiePath" value="${pageContext.request.contextPath}/web/desktop"/>
        </c:if>
    </c:if>
    <c:if test="${!empty param.quickEditMode}">
        <script>
            var path = "${cookiePath}";
            $.cookie("quickEditMode", "${param.quickEditMode}", {
                path: path
            });
        </script>

        <c:set var="quickEditMode" value="${param.quickEditMode}"/>
    </c:if>
    <c:choose>
    <c:when test="${quickEditMode == 'false'}">

        <style>
            .adminBar, .quickEdit, #form-canvas .quickEdit {
                display: none;
            }
        </style>

    </c:when>
    <c:otherwise>

        <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/admin_bar.css" />
        <div id="adminBar">
            <a id="appCenter" <c:if test="${empty param.desktop}"> target="_blank"</c:if> title="<fmt:message key='adminBar.label.appCenter'/>" href="${pageContext.request.contextPath}/home"></a>
            <div id="quickEditModeOption">
                <input type="radio" id="quickEditModeOn" name="radio" /><label for="quickEditModeOn">On</label>
                <input type="radio" id="quickEditModeOff" name="radio" /><label for="quickEditModeOff">Off</label>
            </div>
            <c:if test="${!empty param.appId || !empty param.webConsole || !empty param.desktop}">
                <c:if test="${!empty param.appId}">
                    <div>
                        <a class="adminBarButton" style="display:none" title="<fmt:message key='adminBar.label.designApp'/>" href="${pageContext.request.contextPath}/web/console/app/${param.appId}/${param.appVersion}/forms" onclick="return showQuickOverlay('${pageContext.request.contextPath}/web/console/app/${param.appId}/${param.appVersion}/forms')"><i class="icon-edit"></i><br>App</a>
                    </div>
                </c:if>
                <c:if test="${!empty param.appControls}">
                    <div>
                        <a class="adminBarButton" style="display:none" title="<fmt:message key='adminBar.label.manageApps'/>" href="${pageContext.request.contextPath}/web/desktop/apps" onclick="return showQuickOverlay('${pageContext.request.contextPath}/web/desktop/apps')"><i class="icon-wrench"></i><br>All Apps</a>
                    </div>
                </c:if>
                <div>
                    <a class="adminBarButton" style="display:none" title="<fmt:message key='adminBar.label.setupUsers'/>" href="${pageContext.request.contextPath}/web/console/directory/users" onclick="return showQuickOverlay('${pageContext.request.contextPath}/web/console/directory/users')"><i class="icon-user"></i><br>Users</a>
                </div>
                <div>
                    <a class="adminBarButton" style="display:none" title="<fmt:message key='adminBar.label.monitorApps'/>" href="${pageContext.request.contextPath}/web/console/monitor/running" onclick="return showQuickOverlay('${pageContext.request.contextPath}/web/console/monitor/running')"><i class="icon-dashboard"></i><br>Monitor</a>
                </div>
                <div>
                    <a class="adminBarButton" style="display:none" title="<fmt:message key='adminBar.label.systemSettings'/>" href="${pageContext.request.contextPath}/web/console/setting/general" onclick="return showQuickOverlay('${pageContext.request.contextPath}/web/console/setting/general')"><i class="icon-cogs"></i><br>Settings</a>
                </div>
            </c:if>
        </div>

        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <script>
            function showQuickOverlay(url) {
                if ($("#quickOverlayContainer").length == 0) {
                    var overlayContainer = 
                        '<div id="quickOverlayContainer"><div id="quickOverlay"></div>\
                        <div id="quickOverlayButton"><a href="#" onclick="hideQuickOverlay()"><i class="icon-remove"></i></a></div>\
                        <iframe id="quickOverlayFrame" name="quickOverlayFrame" src="about:blank"></iframe></div>';
                    $(document.body).append(overlayContainer);
                }
                $("#quickOverlayFrame").attr("src", "about:blank");
                $("#quickOverlayFrame").attr("src", url);
                $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeIn();
                return false;
            }
            function hideQuickOverlay() {
                $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeOut();
                $("#quickOverlayContainer").remove();
            }
            function enableQuickEditMode() {
                var path = "${cookiePath}";
                $.cookie("quickEditModeActive", "true", {
                    path: path
                });
                initQuickEditMode();
            }
            function disableQuickEditMode() {
                var path = "${cookiePath}";
                $.cookie("quickEditModeActive", "false", {
                    path: path
                });
                initQuickEditMode();
            }
            function initQuickEditMode() {
                var quickEditModeActive =  $.cookie("quickEditModeActive");
                if (quickEditModeActive == "true") {
                    $("#quickEditModeOn").attr("checked", "checked");
                    $("#quickEditModeOff").removeAttr("checked");
                    $(".quickEdit, .adminBarButton").fadeIn();
                    $("#page").addClass("quickEditModeActive");
                    $("#quickEditModeOption").button("refresh");
                } else {
                    $("#quickEditModeOff").attr("checked", "checked");
                    $("#quickEditModeOn").removeAttr("checked");
                    $(".quickEdit, .adminBarButton").css("display", "none");
                    $("#page").removeClass("quickEditModeActive");
                    $("#quickEditModeOption").button("refresh");
                }
            }
            $(window).load(function() {
                $("#quickEditModeOn").click(enableQuickEditMode);
                $("#quickEditModeOff").click(disableQuickEditMode);
                initQuickEditMode();
                $("#quickEditModeOption").buttonset();
                <c:if test="${!empty param.webConsole || !empty param.desktop}">
                    enableQuickEditMode();
                </c:if>
            });
        </script>

    </c:otherwise>
    </c:choose>
    </c:if>
    
</c:if>