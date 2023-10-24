<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ tag import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ tag import="org.joget.commons.util.LogUtil"%>
<%@ tag import="org.joget.commons.util.HostManager"%>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>

<%@attribute name="id" required="false"%>
<%@attribute name="title" required="false"%>
<c:set var="bodyId" value="<%= id != null ? id : \"\" %>"/>
<c:set var="isExtDirectoryManager" value="<%= DirectoryUtil.isExtDirectoryManager() %>"/>
<c:set var="isCustomDirectoryManager" value="<%= DirectoryUtil.isCustomDirectoryManager() %>"/>
<c:set var="username" scope="request" value="<%= WorkflowUtil.getCurrentUsername() %>"/>
<c:set var="userIsReadonly" scope="request" value="<%= DirectoryUtil.userIsReadonly(WorkflowUtil.getCurrentUsername()) %>"/>
<c:set var="isAnonymous" scope="request" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:set var="isAdmin" scope="request" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
<c:set var="title" value="<%= title != null ? title : \"\" %>"/>
<c:set var="lang" value="<%= AppUtil.getAppLocale() %>"/>
<c:if test="${empty title}">
    <c:set var="title"><fmt:message key="console.header.browser.title"/></c:set>
</c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="${lang}">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0">
        <title><c:out value="${title}"/></title>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/v7.css?build=<fmt:message key="build.number"/>">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css?build=<fmt:message key="build.number"/>">
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
    </head>
    <body id="${bodyId}">
        <div id="main-header">
            <a id="home-link" href="${pageContext.request.contextPath}/">
                <span id="logo"></span>
                <span id="logo-title"><jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=header" /></span>
            </a>
            <div id="header-links">
                <a href="${pageContext.request.contextPath}/" id="header-home"><i class="fas fa-home"></i> <span><fmt:message key="console.header.menu.label.home"/></span></a>
                <c:if test="${!isAnonymous}">
                    <a href="${pageContext.request.contextPath}/web/console/run/inbox" id="header-inbox"><i class="fas fa-inbox"></i> <span><fmt:message key="console.header.submenu.label.inbox"/></span></a>
                    <c:choose>
                        <c:when test="${isCustomDirectoryManager || userIsReadonly}">
                            <a><i class="fas fa-user"></i> ${username}</a>
                            </c:when>
                            <c:otherwise>
                            <a href="javascript:navMenuUserProfile()" id="header-profile"><i class="fas fa-user"></i> <span><fmt:message key="console.header.top.label.userProfile"/> (<c:out value="${username}"/>)</span></a>
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
                    <a href="${pageContext.request.contextPath}/j_spring_security_logout" id="header-logout"><i class="fas fa-sign-out-alt"></i> <span><fmt:message key="console.header.top.label.logout"/></span></a>
                </c:if>
                <c:if test="${isAnonymous}">
                <a href="${pageContext.request.contextPath}/web/login" id="header-login"><i class="fas fa-sign-in-alt"></i> <span><fmt:message key="console.header.top.label.login"/></span></a>
                </c:if>
            </div>
        </div>
        <div id="container">
            <div id="content-container">