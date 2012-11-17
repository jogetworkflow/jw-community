<%@page import="org.joget.apps.userview.model.UserviewMenu"%>
<%@page import="org.springframework.util.StopWatch"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
    
    StopWatch sw = new StopWatch(request.getRequestURI());
    sw.start("userview");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:if test="${empty menuId && !empty userview.properties.homeMenuId}">
    <c:set var="homeRedirectUrl" scope="request" value="/web/"/>
    <c:if test="${embed}">
        <c:set var="homeRedirectUrl" scope="request" value="${homeRedirectUrl}embed/"/>
    </c:if>
    <c:set var="homeRedirectUrl" scope="request" value="${homeRedirectUrl}userview/${appId}/${userview.properties.id}/${key}/${userview.properties.homeMenuId}"/>
    <c:redirect url="${homeRedirectUrl}"/>
</c:if>

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${((!empty userview.setting.permission && !userview.setting.permission.authorize) || empty userview.current) && isAnonymous}">
    <c:set var="redirectUrl" scope="request" value="/web/"/>
    <c:choose>
        <c:when test="${embed}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}embed/ulogin/${appId}/${userview.properties.id}/"/>
        </c:when>
        <c:otherwise>
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}ulogin/${appId}/${userview.properties.id}/"/>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${!empty key}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}"/>
        </c:when>
        <c:otherwise>
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}______"/>
        </c:otherwise>
    </c:choose>
    <c:if test="${!empty menuId}">
        <c:set var="redirectUrl" scope="request" value="${redirectUrl}/${menuId}"/>
    </c:if>
    <c:redirect url="${redirectUrl}?${queryString}"/>
</c:if>

<c:set var="bodyId" scope="request" value=""/>
<c:choose>
    <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
        <c:set var="bodyId" scope="request" value="unauthorize"/>
    </c:when>
    <c:when test="${!empty userview.current}">
        <c:choose>
            <c:when test="${!empty userview.current.properties.customId}">
                <c:set var="bodyId" scope="request" value="${userview.current.properties.customId}"/>
            </c:when>
            <c:otherwise>
                <c:set var="bodyId" scope="request" value="${userview.current.properties.id}"/>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:set var="bodyId" scope="request" value="pageNotFound"/>
    </c:otherwise>
</c:choose>

<c:catch var="bodyError">
<c:set var="bodyContent">
    <c:if test="${!empty userview.setting.theme.beforeContent}">
        ${userview.setting.theme.beforeContent}
    </c:if>
    <c:choose>
        <c:when test="${!empty userview.current}">
            <c:set var="properties" scope="request" value="${userview.current.properties}"/>
            <c:set var="requestParameters" scope="request" value="${userview.current.requestParameters}"/>
            <c:set var="readyJspPage" value="${userview.current.readyJspPage}"/>
            <c:choose>
                <c:when test="${!empty readyJspPage}">
                    <jsp:include page="../${readyJspPage}" flush="true"/>
                </c:when>
                <c:otherwise>
                    ${userview.current.readyRenderPage}
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <h3><fmt:message key="ubuilder.pageNotFound"/></h3>

            <fmt:message key="ubuilder.pageNotFound.message"/>
            <br><br>
            <fmt:message key="ubuilder.pageNotFound.explanation"/>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>
                <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}/${userview.properties.homeMenuId}"><fmt:message key="ubuilder.pageNotFound.backToMain"/></a>
            </p>
        </c:otherwise>
    </c:choose>
    <c:if test="${!empty userview.setting.theme.pageBottom}">
        ${userview.setting.theme.pageBottom}
    </c:if>
</c:set>

<c:set var="alertMessageProperty" value="<%= UserviewMenu.ALERT_MESSAGE_PROPERTY %>"/>
<c:set var="alertMessageValue" value="${userview.current.properties[alertMessageProperty]}"/>
<c:set var="redirectUrlProperty" value="<%= UserviewMenu.REDIRECT_URL_PROPERTY %>"/>
<c:set var="redirectUrlValue" value="${userview.current.properties[redirectUrlProperty]}"/>
<c:set var="redirectParentProperty" value="<%= UserviewMenu.REDIRECT_PARENT_PROPERTY %>"/>
<c:set var="redirectParentValue" value="${userview.current.properties[redirectParentProperty]}"/>
<c:choose>
<c:when test="${!empty alertMessageValue}">
    <script>
        alert("${alertMessageValue}");
    <c:if test="${!empty redirectUrlValue}">
        <c:if test="${redirectParentValue}">parent.</c:if>location.href = "${redirectUrlValue}";
    </c:if>
    </script>
</c:when>
<c:when test="${!empty redirectUrlValue}">
    <c:choose>
        <c:when test="${redirectParentValue}">
            <script>
                parent.location.href = "${redirectUrlValue}";
            </script>
        </c:when>
        <c:otherwise>
            <c:if test="${!fn:containsIgnoreCase(redirectUrlValue, 'http') && !fn:startsWith(redirectUrlValue, '/')}">
                <c:set var="redirectBaseUrlValue" scope="request" value="/web/"/>
                <c:if test="${embed}">
                    <c:set var="redirectBaseUrlValue" scope="request" value="${redirectBaseUrlValue}embed/"/>
                </c:if>
                <c:set var="redirectBaseUrlValue" scope="request" value="${redirectBaseUrlValue}userview/${appId}/${userview.properties.id}/${key}/"/>
                <c:set var="redirectUrlValue" value="${redirectBaseUrlValue}${redirectUrlValue}"/>
            </c:if>
            <c:if test="${fn:startsWith(redirectUrlValue, pageContext.request.contextPath)}">
                <c:set var="redirectUrlValue" value="${fn:replace(redirectUrlValue, pageContext.request.contextPath, '')}"/>
            </c:if>
            <c:redirect url="${redirectUrlValue}"/>
        </c:otherwise>
    </c:choose>
</c:when>
</c:choose>
</c:catch>
    
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>
            ${userview.properties.name} &nbsp;&gt;&nbsp;
            <c:if test="${!empty userview.current}">
                ${userview.current.properties.label}
            </c:if>
        </title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

        <script type="text/javascript">
            function userviewPrint(){
                $('head').append('<link id="userview_print_css" rel="stylesheet" href="${pageContext.request.contextPath}/css/userview_print.css" type="text/css" media="print"/>');
                setTimeout("do_print()", 1000); 
            }

            function do_print(){
                window.print();
                $('#userview_print_css').remove();
            }
            
            ${userview.setting.theme.javascript}
        </script>

        <link href="${pageContext.request.contextPath}/css/userview.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />

        <style type="text/css">
            ${userview.setting.theme.css}
        </style>
    </head>

    <body id="${bodyId}" class="<c:if test="${embed}">embeded</c:if><c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}"> rtl</c:if>">
        <div id="page">
            <div id="header">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <div id="header-inner">${userview.setting.theme.header}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="header-info">
                            <div id="header-name">
                                <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}/${userview.properties.homeMenuId}" id="header-link"><span id="name">${userview.properties.name}</span></a>
                            </div>
                            <div id="header-description">
                                <span id="description">${userview.properties.description}</span>
                            </div>
                            <div class="clear"></div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div id="header-message">
                    <div id="header-welcome-message">
                        <span id="welcomeMessage">${userview.properties.welcomeMessage}</span>
                    </div>
                    <div id="header-logout-text">
                        <c:choose>
                            <c:when test="${isAnonymous}">
                                <a href="${pageContext.request.contextPath}/web/ulogin/${appId}/${userview.properties.id}/${key}"><span id="loginText"><fmt:message key="ubuilder.login"/></span></a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/j_spring_security_logout"><span id="logoutText">${userview.properties.logoutText}</span></a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
            <div id="main">
                <c:choose>
                    <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
                        <div id="content" class="unauthorize">
                            <h3><fmt:message key="ubuilder.noAuthorize"/></h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!empty userview.setting.theme.pageTop}">
                            ${userview.setting.theme.pageTop}
                        </c:if>
                        <c:if test="${!embed}">
                        <div id="navigation">
                            <div id="category-container">
                                <c:forEach items="${userview.categories}" var="category" varStatus="cStatus">
                                    <c:if test="${category.properties.hide ne 'yes'}">
                                        <c:set var="c_class" value=""/>

                                        <c:if test="${cStatus.first}">
                                            <c:set var="c_class" value="${c_class} first"/>
                                        </c:if>
                                        <c:if test="${cStatus.last}">
                                            <c:set var="c_class" value="${c_class} last"/>
                                        </c:if>
                                        <c:if test="${!empty userview.currentCategory && category.properties.id eq userview.currentCategory.properties.id}">
                                            <c:set var="c_class" value="${c_class} current-category"/>
                                        </c:if>

                                        <div id="${category.properties.id}" class="category ${c_class}">
                                            <div class="category-label">
                                                <c:set var="firstMenuItem" value="${category.menus[0]}"/>
                                                <c:choose>
                                                    <c:when test="${!empty firstMenuItem && firstMenuItem.homePageSupported}">
                                                        <c:set var="menuItemId" value="${firstMenuItem.properties.menuId}"/>
                                                        <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}/${menuItemId}"><span>${category.properties.label}</span></a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span>${category.properties.label}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="clear"></div>
                                            <div class="menu-container">
                                                <c:forEach items="${category.menus}" var="menu" varStatus="mStatus">
                                                    <c:set var="m_class" value=""/>

                                                    <c:if test="${mStatus.first}">
                                                        <c:set var="m_class" value="${m_class} first"/>
                                                    </c:if>
                                                    <c:if test="${mStatus.last}">
                                                        <c:set var="m_class" value="${m_class} last"/>
                                                    </c:if>
                                                    <c:if test="${!empty userview.current && menu.properties.id eq userview.current.properties.id}">
                                                        <c:set var="m_class" value="${m_class} current"/>
                                                    </c:if>

                                                    <div id="${menu.properties.id}" class="menu ${m_class}">
                                                        ${menu.menu}
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                        </c:if>
                        <div id="content">
                        ${bodyContent}
                        <c:if test="${!empty bodyError}"><c:out value="${bodyError}" escapeXml="true"/></c:if>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class="clear"></div>
            </div>
            <div id="footer">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.footer}">
                        <div id="footer-inner">${userview.setting.theme.footer}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="footer-message">
                            <span id="footerMessage">${userview.properties.footerMessage}</span>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#header";
            HelpGuide.key = "help.web.userview.${appId}.${userview.properties.id}.${bodyId}";
            HelpGuide.show();
        </script>

        <%
            sw.stop();
            long duration = sw.getTotalTimeMillis();
            pageContext.setAttribute("duration", duration);
        %>    
        <%--div class="small">[${duration}ms]</div--%>
    </body>
    
</html>
