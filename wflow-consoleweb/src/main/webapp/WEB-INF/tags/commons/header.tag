<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ tag import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ tag import="org.joget.commons.util.LogUtil"%>
<%@ tag import="org.joget.commons.util.HostManager"%>

<%@attribute name="id" required="false"%>
<c:set var="isDesktop" value="<%= id == \"desktop\" %>"/>
<c:set var="bodyId" value="<%= id != null ? id : \"\" %>"/>
<c:set var="isExtDirectoryManager" value="<%= DirectoryUtil.isExtDirectoryManager() %>"/>
<c:set var="isCustomDirectoryManager" value="<%= DirectoryUtil.isCustomDirectoryManager() %>"/>
<c:set var="username" scope="request" value="<%= WorkflowUtil.getCurrentUsername() %>"/>
<c:set var="userIsReadonly" scope="request" value="<%= DirectoryUtil.userIsReadonly(WorkflowUtil.getCurrentUsername()) %>"/>
<c:set var="isAnonymous" scope="request" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:set var="isAdmin" scope="request" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
<c:set var="isProtected" scope="request" value="<%= WorkflowUtil.getSystemSetupValue(\"disableWebConsole\") %>"/>

<c:if test="${!isAnonymous && !isAdmin && !isDesktop && isProtected}">
    <c:redirect url="/home" />
</c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="console.header.browser.title"/></title>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <script>
            var Template = {
                debug: function() {
                    $("div").prepend(function(index, html) {
                        $(this).attr("style", "border: dotted 1px #dedede");
                        return "<span class='debug'>" + $(this).attr("id") + "</span>";
                    });
                },
                selectMenu: function(menu) {
                    if (menu) {
                        $("#menu-items").children("li").removeClass();
                        var first = $("#menu-items").children("li:first");
                        var last = $("#menu-items").children("li:last");
                        $(menu).addClass("active");
                        if ($(menu).prev().length == 0) {
                            first.addClass("first-active");
                        } else {
                            first.addClass("first-inactive");
                            $(menu).prev().addClass("next");
                        }
                        if ($(menu).next().length == 0) {
                            last.addClass("last-active");
                        } else {
                            last.addClass("last-inactive");
                        }
                    }
                },
                selectNav: function(nav) {
                    if (nav) {
                        $("#nav-list li").removeClass("nav-selected");
                        $(nav).addClass("nav-selected");
                    }
                },
                init: function(menu, nav) {
                    Template.selectMenu(menu);
                    Template.selectNav(nav);
                }
            }
        </script>
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
    </head>
    <body id="${bodyId}">

        <div id="header">
            <div id="header-bg"></div>
            <div id="header-label"><jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=header" /></div>
            <div id="topbar">
                <a href="${pageContext.request.contextPath}/"><div id="logo"></div><div id="logo-label"><jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=logo" /></div></a>
                <div id="account">
                    <jsp:include page="/WEB-INF/jsp/console/links.jsp" />
                    <c:if test="${isAdmin && !isDesktop}">
                        <a href="${pageContext.request.contextPath}/web/console/setting/general"><fmt:message key="console.header.top.label.settings"/></a> |
                    </c:if>
                    <c:if test="${!isAnonymous}">
                        <c:choose>
                            <c:when test="${isCustomDirectoryManager || userIsReadonly}">
                                ${username}
                            </c:when>
                            <c:otherwise>
                                <a href="javascript: editUserProfile()"><fmt:message key="console.header.top.label.userProfile"/> (<c:out value="${username}"/>)</a>

                                <script>
                                    <ui:popupdialog var="userProfilePopupDialog" src="${pageContext.request.contextPath}/web/console/profile"/>

                                    function editUserProfile(){
                                        userProfilePopupDialog.init();
                                    }

                                    function userProfileCloseDialog() {
                                        userProfilePopupDialog.close();
                                    }
                                </script>

                            </c:otherwise>
                        </c:choose>

                        |
                        <a href="${pageContext.request.contextPath}/j_spring_security_logout"><fmt:message key="console.header.top.label.logout"/></a>
                    </c:if>
                    <c:if test="${isAnonymous}">
                        <a href="${pageContext.request.contextPath}/web/login"><fmt:message key="console.header.top.label.login"/></a>
                    </c:if>
                </div>
            </div>
            <div id="menu">
                <ul id="menu-items">
                <c:if test="${isDesktop != 'true'}">
                    <c:if test="${isAnonymous}">
                        <li id="menu-login" class="first-active"><a href="${pageContext.request.contextPath}/web/login"><span class="menu-bg"><span class="title"><fmt:message key="console.header.menu.label.login"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.login"/></span></span></a></li>
                        <li id="menu-home" class="last-inactive"><a href="${pageContext.request.contextPath}/web/console/home"><span class="menu-bg"><span class="title"><fmt:message key="console.header.menu.label.home"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.home"/></span></span></a></li>
                    </c:if>
                    <c:if test="${!isAnonymous && !isAdmin}">
                        <li id="menu-home" class="first-inactive"><a href="${pageContext.request.contextPath}/web/console/home"><span class="menu-bg"><span class="title"><fmt:message key="console.header.menu.label.home"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.home"/></span></span></a></li>
                        <li id="menu-run">
                            <a href="${pageContext.request.contextPath}/web/console/run/apps"><span class="menu-bg"><span class="steps"></span><span class="title"><fmt:message key="console.header.menu.label.run"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.run"/></span></span></a>
                            <div class="dropdown">
                                <div class="top"></div>
                                <ul class="dropdown-list">
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/run/apps">
                                        <span class="substeps"></span>
                                        <span class="subtitle"><fmt:message key="console.header.submenu.label.publishedApps"/></span>
                                        <span class="subsubtitle"><fmt:message key="console.header.submenu.description.publishedApps"/></span></a>
                                    </li>
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/run/processes">
                                        <span class="substeps">&nbsp;</span>
                                        <span class="subtitle"><fmt:message key="console.header.submenu.label.publishedProcesses"/></span>
                                        <span class="subsubtitle"><fmt:message key="console.header.submenu.description.publishedProcesses"/></span></a>
                                    </li>
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/run/inbox" class="substeps">
                                        <span class="substeps"></span>
                                        <span class="subtitle"><fmt:message key="console.header.submenu.label.inbox"/></span>
                                        <span class="subsubtitle"><fmt:message key="console.header.submenu.description.inbox"/></span></a>
                                    </li>
                                </ul>
                                <div class="bottom"></div>
                            </div>
                        </li>
                    </c:if>
                    <c:if test="${!isAnonymous && isAdmin}">
                    <li id="menu-home" class="first-inactive"><a href="${pageContext.request.contextPath}/web/console/home"><span class="menu-bg"><span class="title"><fmt:message key="console.header.menu.label.home"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.home"/></span></span></a></li>
                    <li id="menu-users">
                        <c:choose>
                            <c:when test="${isExtDirectoryManager}">
                                <a href="${pageContext.request.contextPath}/web/console/directory/orgs"><span class="menu-bg"><span class="steps">1</span><span class="title"><fmt:message key="console.header.menu.label.users"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.users"/></span></span></a>
                                <div class="dropdown">
                                    <div class="top"></div>
                                    <ul class="dropdown-list">
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/directory/orgs">
                                            <span class="substeps">1</span>
                                            <span class="subtitle"><fmt:message key="console.header.submenu.label.organization"/></span>
                                            <span class="subsubtitle"><fmt:message key="console.header.submenu.description.organization"/></span></a>
                                        </li>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/directory/groups" class="substeps">
                                            <span class="substeps">2</span>
                                            <span class="subtitle"><fmt:message key="console.header.submenu.label.groups"/></span>
                                            <span class="subsubtitle"><fmt:message key="console.header.submenu.description.groups"/></span></a>
                                        </li>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/directory/users" class="substeps">
                                            <span class="substeps">3</span>
                                            <span class="subtitle"><fmt:message key="console.header.submenu.label.users"/></span>
                                            <span class="subsubtitle"><fmt:message key="console.header.submenu.description.users"/></span></a>
                                        </li>
                                    </ul>
                                    <div class="bottom"></div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <a><span class="menu-bg"><span class="steps">1</span><span class="title"><fmt:message key="console.header.menu.label.users"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.users.disabled"/></span></span></a>
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <li id="menu-apps"><a href="${pageContext.request.contextPath}/web/desktop/apps"><span class="menu-bg"><span class="steps">2</span><span class="title"><fmt:message key="console.header.menu.label.apps"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.apps"/></span></span></a>
                        <jsp:include page="/web/console/app/menu" />
                    </li>
                    <li id="menu-run">
                        <a href="${pageContext.request.contextPath}/web/console/run/apps"><span class="menu-bg"><span class="steps">3</span><span class="title"><fmt:message key="console.header.menu.label.run"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.run"/></span></span></a>
                        <div class="dropdown">
                            <div class="top"></div>
                            <ul class="dropdown-list">
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/run/apps">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.publishedApps"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.publishedApps"/></span></a>
                                </li>
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/run/processes">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.publishedProcesses"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.publishedProcesses"/></span></a>
                                </li>
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/run/inbox" class="substeps">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.inbox"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.inbox"/></span></a>
                                </li>
                            </ul>
                            <div class="bottom"></div>
                        </div>
                    </li>
                    <li id="menu-monitor" class="last-inactive">
                        <a href="${pageContext.request.contextPath}/web/console/monitor/running"><span class="menu-bg"><span class="steps">4</span><span class="title"><fmt:message key="console.header.menu.label.monitor"/></span><span class="subtitle"><fmt:message key="console.header.menu.description.monitor"/></span></span></a>
                        <div class="dropdown">
                            <div class="top"></div>
                            <ul class="dropdown-list">
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/monitor/running">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.runningProcesses"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.runningProcesses"/></span></a>
                                </li>
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/monitor/completed" class="substeps">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.completedProcesses"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.completedProcesses"/></span></a>
                                </li>
                                <li>
                                    <a href="${pageContext.request.contextPath}/web/console/monitor/audit" class="substeps">
                                    <span class="substeps">&nbsp;</span>
                                    <span class="subtitle"><fmt:message key="console.header.submenu.label.auditTrail"/></span>
                                    <span class="subsubtitle"><fmt:message key="console.header.submenu.description.auditTrail"/></span></a>
                                </li>
                                <c:set var="isTomcat" value="<%= LogUtil.isDeployInTomcat() %>"/>
                                <c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
                                <c:if test="${isTomcat && !isVirtualHostEnabled}">
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/monitor/logs" class="substeps">
                                        <span class="substeps">&nbsp;</span>
                                        <span class="subtitle"><fmt:message key="console.header.submenu.label.logs"/></span>
                                        <span class="subsubtitle"><fmt:message key="console.header.submenu.description.logs"/></span></a>
                                    </li>
                                </c:if>
                            </ul>
                            <div class="bottom"></div>
                        </div>
                    </li>
                    </c:if>
                </c:if>
                </ul>
            </div>
            <div class="clear"></div>   
        </div>
        <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=body" />
        <div id="container">
            <div class="content-container">
