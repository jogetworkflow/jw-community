<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<!DOCTYPE html>
<html class="ui-mobile" manifest="${pageContext.request.contextPath}/web/mobilecache/default">
    <head>
        <title><fmt:message key="mobile.apps.title"/></title>
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/mobile/jqm/jquery.mobile-1.2.0.css">
        <script src="${pageContext.request.contextPath}/mobile/jqm/jquery-1.7.2.min.js"></script>
        <script src="${pageContext.request.contextPath}/mobile/jqm/jquery.cookie.js"></script>
        <script src="${pageContext.request.contextPath}/mobile/jqm/jquery.mobile-1.2.0.min.js"></script>
        <script src="${pageContext.request.contextPath}/mobile/mobile.js"></script>
        <script>
            function desktopSite() {
                var path = "${pageContext.request.contextPath}/web/desktop";
                var href = "${pageContext.request.contextPath}/web/desktop";
                Mobile.viewFullSite(path, href);
                return false;
            }
            function showLoading(url) {
                $.mobile.showPageLoadingMsg();
                if (url) {
                    setTimeout(function() {
                        location.href = url;
                    }, 120);
                    setTimeout(function() {
                        $.mobile.hidePageLoadingMsg();
                    }, 2000);
                    return false;
                }
            }            
            Mobile.contextPath = "${pageContext.request.contextPath}";
            Mobile.updateCache();
            $("#mobileHome").live("pageshow", function() {
                Mobile.checkNetworkStatus();
            });
        </script>
    </head>

    <body>
        <div id="mobileHome" data-role="page" >
            <div data-role="header" data-position="fixed">
                <h1><fmt:message key="console.header.submenu.label.publishedApps"/></h1>
                <c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
                <c:choose>
                    <c:when test="${isAnonymous}">
                        <a href="${pageContext.request.contextPath}/web/mlogin" data-icon="gear" data-theme="b"><fmt:message key="console.login.label.login"/></a>
                        <a href="#" onclick="return desktopSite()" data-icon="home" rel="external"><fmt:message key="mobile.apps.desktop"/></a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/j_spring_security_logout" data-icon="back" data-theme="b" data-direction="reverse"><fmt:message key="console.header.top.label.logout"/></a>
                        <a href="#" onclick="return desktopSite()" data-icon="home" rel="external"><fmt:message key="mobile.apps.desktop"/></a>
                    </c:otherwise>
                </c:choose>                            
            </div>
            <div id="logo"></div>
            <div data-role="content">
                <ul id="appList" data-role="listview" data-filter="true" data-inset="true">
                    <c:forEach items="${appDefinitionList}" var="appDefinition">
                        <c:set var="userviewDefinitionList" value="${appDefinition.userviewDefinitionList}"/>
                        <c:forEach items="${userviewDefinitionList}" var="userviewDefinition">
                            <li>
                                <a onclick="showLoading('${pageContext.request.contextPath}/web/mobile/${appDefinition.id}/${userviewDefinition.id}')" href="${pageContext.request.contextPath}/web/mobile/${appDefinition.id}/${userviewDefinition.id}//landing" rel="external">
                                    <p><b>${appDefinition.name}</b></p>
                                    <h4>${userviewDefinition.name}</h4>
                                    <p>
                                        ${userviewDefinition.description}
                                        <br>
                                        <fmt:message key="console.app.common.label.version"/> ${appDefinition.version}
                                    </p>
                                </a>
                            </li>
                        </c:forEach>
                    </c:forEach>                    
                </ul>
            </div>		
        </div>
        <div id="online-status"></div>
        <style>
            #getting-started {
                height: 0px;
                width: 0px;
                overflow: hidden;
                z-index: -100;
                position: absolute;
                margin-top: 100%;
            }
        </style>
        <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />          
        <jsp:include page="mCss.jsp" flush="true"/>
    </body>

</html>